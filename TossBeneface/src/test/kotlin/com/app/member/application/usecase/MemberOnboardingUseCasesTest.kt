package com.app.member.application.usecase

import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.OnboardingStatus
import com.app.domain.member.constant.OnboardingStep
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.service.MemberService
import com.app.global.error.exception.BusinessException
import com.app.member.application.dto.CompleteMemberOnboardingCommand
import com.app.member.application.dto.CompleteMemberOnboardingStepCommand
import com.app.member.application.dto.StartMemberOnboardingCommand
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class MemberOnboardingUseCasesTest {

    private val memberService = mockk<MemberService>()

    @Test
    fun `starts member onboarding`() {
        val member = createMember()
        every { memberService.findMemberById(42L) } returns member
        every { memberService.updateMember(any()) } answers { firstArg() }

        val result = StartMemberOnboardingUseCase(memberService)
            .start(StartMemberOnboardingCommand(memberId = 42L))

        assertEquals(OnboardingStatus.IN_PROGRESS, result.onboardingStatus)
        assertEquals(OnboardingStep.PROFILE, result.onboardingStep)
        assertNull(result.onboardingCompletedAt)
        verify(exactly = 1) { memberService.updateMember(member) }
    }

    @Test
    fun `completes member onboarding steps in order`() {
        val member = createMember(
            onboardingStatus = OnboardingStatus.IN_PROGRESS,
            onboardingStep = OnboardingStep.PROFILE
        )
        every { memberService.findMemberById(42L) } returns member
        every { memberService.updateMember(any()) } answers { firstArg() }
        val command = CompleteMemberOnboardingStepCommand(memberId = 42L)

        val profileResult = CompleteMemberProfileStepUseCase(memberService).complete(command)
        assertEquals(OnboardingStatus.IN_PROGRESS, profileResult.onboardingStatus)
        assertEquals(OnboardingStep.BUDGET, profileResult.onboardingStep)

        val budgetResult = CompleteBudgetStepUseCase(memberService).complete(command)
        assertEquals(OnboardingStep.CARD, budgetResult.onboardingStep)

        val cardResult = CompleteCardStepUseCase(memberService).complete(command)
        assertEquals(OnboardingStep.PREFERENCE, cardResult.onboardingStep)

        val preferenceResult = CompletePreferenceStepUseCase(memberService).complete(command)
        assertEquals(OnboardingStatus.IN_PROGRESS, preferenceResult.onboardingStatus)
        assertEquals(OnboardingStep.COMPLETED, preferenceResult.onboardingStep)
        assertNull(preferenceResult.onboardingCompletedAt)
    }

    @Test
    fun `completes member onboarding after all steps`() {
        val member = createMember(
            onboardingStatus = OnboardingStatus.IN_PROGRESS,
            onboardingStep = OnboardingStep.COMPLETED
        )
        every { memberService.findMemberById(42L) } returns member
        every { memberService.updateMember(any()) } answers { firstArg() }

        val result = CompleteMemberOnboardingUseCase(memberService)
            .complete(CompleteMemberOnboardingCommand(memberId = 42L))

        assertEquals(OnboardingStatus.COMPLETED, result.onboardingStatus)
        assertEquals(OnboardingStep.COMPLETED, result.onboardingStep)
        assertNotNull(result.onboardingCompletedAt)
        assertEquals(OnboardingStatus.COMPLETED, member.onboardingStatus)
        assertEquals(OnboardingStep.COMPLETED, member.onboardingStep)
        assertNotNull(member.onboardingCompletedAt)
        verify(exactly = 1) { memberService.updateMember(member) }
    }

    @Test
    fun `rejects completing member onboarding before all steps`() {
        val member = createMember(
            onboardingStatus = OnboardingStatus.IN_PROGRESS,
            onboardingStep = OnboardingStep.PREFERENCE
        )
        every { memberService.findMemberById(42L) } returns member

        assertThrows(BusinessException::class.java) {
            CompleteMemberOnboardingUseCase(memberService)
                .complete(CompleteMemberOnboardingCommand(memberId = 42L))
        }
    }

    private fun createMember(
        onboardingStatus: OnboardingStatus = OnboardingStatus.NOT_STARTED,
        onboardingStep: OnboardingStep = OnboardingStep.PROFILE
    ) = Member(
        memberId = 42L,
        email = "member@example.com",
        password = "password",
        memberName = "Member",
        phoneNumber = "010-1234-5678",
        gender = Gender.MALE,
        profileImg = "profile.png",
        budget = 10_000,
        role = Role.USER,
        memberStatus = MemberStatus.ACTIVATE,
        onboardingStatus = onboardingStatus,
        onboardingStep = onboardingStep
    )
}
