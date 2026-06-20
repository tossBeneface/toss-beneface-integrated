package com.app.domain.member.constant

enum class OnboardingStep {
    PROFILE,
    BUDGET,
    CARD,
    PREFERENCE,
    COMPLETED;

    fun next(): OnboardingStep {
        return when (this) {
            PROFILE -> BUDGET
            BUDGET -> CARD
            CARD -> PREFERENCE
            PREFERENCE -> COMPLETED
            COMPLETED -> COMPLETED
        }
    }
}
