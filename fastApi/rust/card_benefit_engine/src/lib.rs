use std::collections::HashMap;

use pyo3::exceptions::PyRuntimeError;
use pyo3::prelude::*;
use serde::{Deserialize, Serialize};

#[derive(Deserialize)]
struct BenefitCandidateInput {
    card_name: String,
    card_company: String,
    shop: String,
    summary: String,
    benefit: i32,
    limit_once: i32,
    limit_month: i32,
    min_pay: i32,
    min_per: i32,
    monthly: i32,
    last_per: i32,
    now_per: i32,
    monthly_split: i32,
    accrue_benefit: i32,
    visit_count: i32, // New field for progressive benefits logic
}

#[derive(Deserialize)]
struct StoreBatchInput {
    store_name: String,
    amount: i32,
    candidates: Vec<BenefitCandidateInput>,
}

#[derive(Clone, Serialize)]
struct BenefitOption {
    card_name: String,
    discount_amount: i32,
    benefit_type: String,
    description: String,
}

#[derive(Serialize)]
struct BenefitAnalysis {
    best_card_name: String,
    total_potential_benefit: i32,
    all_options: Vec<BenefitOption>,
}

#[derive(Serialize)]
struct StoreBenefitResult {
    store_name: String,
    best_card_name: String,
    total_potential_benefit: i32,
}

#[derive(Serialize)]
struct BatchBenefitAnalysis {
    results: Vec<StoreBenefitResult>,
}

#[derive(Clone)]
struct ScoredOption {
    card_name: String,
    card_company: String,
    option: BenefitOption,
    rank: (i32, i32, i32),
}

#[pyfunction]
fn analyze_batch_benefits(batch_json: &str) -> PyResult<String> {
    let batch: Vec<StoreBatchInput> = serde_json::from_str(batch_json)
        .map_err(|err| PyRuntimeError::new_err(err.to_string()))?;

    let mut results = Vec::with_capacity(batch.len());

    for item in batch {
        let best = find_best_benefit(&item.candidates, item.amount);
        results.push(StoreBenefitResult {
            store_name: item.store_name,
            best_card_name: best.best_card_name,
            total_potential_benefit: best.total_potential_benefit,
        });
    }

    let response = BatchBenefitAnalysis { results };
    serde_json::to_string(&response).map_err(|err| PyRuntimeError::new_err(err.to_string()))
}

#[pyfunction]
fn analyze_best_benefit(candidates_json: &str, amount: i32) -> PyResult<String> {
    let candidates: Vec<BenefitCandidateInput> = serde_json::from_str(candidates_json)
        .map_err(|err| PyRuntimeError::new_err(err.to_string()))?;

    let result = find_best_benefit(&candidates, amount);
    serde_json::to_string(&result).map_err(|err| PyRuntimeError::new_err(err.to_string()))
}

fn find_best_benefit(candidates: &[BenefitCandidateInput], amount: i32) -> BenefitAnalysis {
    if candidates.is_empty() {
        return BenefitAnalysis {
            best_card_name: "No Card".to_string(),
            total_potential_benefit: 0,
            all_options: vec![],
        };
    }

    let safe_amount = amount.max(0);
    let mut best_by_card: HashMap<(String, String), ScoredOption> = HashMap::new();

    for candidate in candidates {
        let scored = score_candidate(candidate, safe_amount);
        let key = (scored.card_name.clone(), scored.card_company.clone());

        match best_by_card.get(&key) {
            Some(current) if current.rank >= scored.rank => {}
            _ => {
                best_by_card.insert(key, scored);
            }
        }
    }

    let mut scored_options: Vec<ScoredOption> = best_by_card.into_values().collect();
    scored_options.sort_by(|left, right| {
        right
            .rank
            .cmp(&left.rank)
            .then_with(|| left.option.card_name.cmp(&right.option.card_name))
    });

    let all_options: Vec<BenefitOption> = scored_options.into_iter().map(|item| item.option).collect();
    let best_option = all_options.first().cloned().unwrap_or(BenefitOption {
        card_name: "No Card".to_string(),
        discount_amount: 0,
        benefit_type: "NONE".to_string(),
        description: "No benefit found".to_string(),
    });

    BenefitAnalysis {
        best_card_name: best_option.card_name.clone(),
        total_potential_benefit: best_option.discount_amount,
        all_options,
    }
}

fn score_candidate(candidate: &BenefitCandidateInput, amount: i32) -> ScoredOption {
    let (discount_amount, description) = evaluate_discount(candidate, amount);
    ScoredOption {
        card_name: candidate.card_name.clone(),
        card_company: candidate.card_company.clone(),
        rank: (discount_amount, candidate.benefit, candidate.limit_once),
        option: BenefitOption {
            card_name: candidate.card_name.clone(),
            discount_amount,
            benefit_type: "DISCOUNT".to_string(),
            description,
        },
    }
}

fn evaluate_discount(candidate: &BenefitCandidateInput, amount: i32) -> (i32, String) {
    if amount < candidate.min_pay {
        return (0, format!("{:}원 이상 결제 시 적용", candidate.min_pay));
    }

    if candidate.last_per < candidate.min_per {
        return (0, format!("전월 실적 {:}원 필요", candidate.min_per));
    }

    if candidate.monthly > 0 && candidate.monthly_split >= candidate.monthly {
        return (0, format!("월 {}회 혜택 한도 소진", candidate.monthly));
    }

    // --- Complex Business Logic: Progressive Bonus ---
    // If visit_count >= 5, add 10% bonus. If >= 2, add 5% bonus.
    let mut effective_benefit = candidate.benefit;
    let mut bonus_msg = String::new();
    
    if candidate.visit_count >= 5 {
        effective_benefit += 10;
        bonus_msg = format!(" (단골 보너스 +10% 적용, 총 {}회 방문)", candidate.visit_count + 1);
    } else if candidate.visit_count >= 2 {
        effective_benefit += 5;
        bonus_msg = format!(" (재방문 보너스 +5% 적용, 총 {}회 방문)", candidate.visit_count + 1);
    }
    // --------------------------------------------------

    let mut discount_amount = amount.saturating_mul(effective_benefit).div_euclid(100);
    if candidate.limit_once > 0 {
        discount_amount = discount_amount.min(candidate.limit_once);
    }

    if candidate.limit_month > 0 {
        let remaining_monthly_benefit = (candidate.limit_month - candidate.accrue_benefit).max(0);
        if remaining_monthly_benefit <= 0 {
            return (0, format!("월 혜택 한도 {:}원 소진", candidate.limit_month));
        }
        discount_amount = discount_amount.min(remaining_monthly_benefit);
    }

    if discount_amount <= 0 {
        return (0, "적용 가능한 혜택 없음".to_string());
    }

    let base_desc = if !candidate.summary.trim().is_empty() {
        candidate.summary.clone()
    } else {
        format!("{}% 할인 적용", effective_benefit)
    };

    (discount_amount, format!("{}{}", base_desc, bonus_msg))
}

#[pymodule]
fn card_benefit_rust(m: &Bound<'_, PyModule>) -> PyResult<()> {
    m.add_function(wrap_pyfunction!(analyze_best_benefit, m)?)?;
    m.add_function(wrap_pyfunction!(analyze_batch_benefits, m)?)?;
    Ok(())
}
