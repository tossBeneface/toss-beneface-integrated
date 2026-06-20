package com.app.domain.member.model

import com.app.domain.member.constant.MemberStatus

data class MemberInitialState(
    val initialBudget: Int = DEFAULT_INITIAL_BUDGET,
    val memberStatus: MemberStatus = MemberStatus.ACTIVATE
) {
    companion object {
        const val DEFAULT_INITIAL_BUDGET = 10_000_000
    }
}
