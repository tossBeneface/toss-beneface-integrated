package com.app.api.member.controller

import com.app.api.member.dto.MemberOnboardingResponseDto
import com.app.api.member.mapper.MemberCommandResponseMapper
import com.app.global.resolver.memberInfo.MemberInfo
import com.app.global.resolver.memberInfo.MemberInfoDto
import com.app.member.application.dto.CompleteMemberOnboardingCommand
import com.app.member.application.dto.CompleteMemberOnboardingStepCommand
import com.app.member.application.dto.StartMemberOnboardingCommand
import com.app.member.application.usecase.CompleteBudgetStepUseCase
import com.app.member.application.usecase.CompleteCardStepUseCase
import com.app.member.application.usecase.CompleteMemberOnboardingUseCase
import com.app.member.application.usecase.CompleteMemberProfileStepUseCase
import com.app.member.application.usecase.CompletePreferenceStepUseCase
import com.app.member.application.usecase.StartMemberOnboardingUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "member-onboarding", description = "회원 온보딩 API")
@RestController
@RequestMapping("/api/member/onboarding")
class MemberOnboardingController(
    private val startMemberOnboardingUseCase: StartMemberOnboardingUseCase,
    private val completeMemberProfileStepUseCase: CompleteMemberProfileStepUseCase,
    private val completeBudgetStepUseCase: CompleteBudgetStepUseCase,
    private val completeCardStepUseCase: CompleteCardStepUseCase,
    private val completePreferenceStepUseCase: CompletePreferenceStepUseCase,
    private val completeMemberOnboardingUseCase: CompleteMemberOnboardingUseCase,
    private val memberCommandResponseMapper: MemberCommandResponseMapper
) {

    @Operation(summary = "온보딩 시작 API", description = "회원 온보딩 플로우를 시작한다.")
    @PostMapping("/start")
    fun start(@MemberInfo memberInfoDto: MemberInfoDto): ResponseEntity<MemberOnboardingResponseDto> {
        val result = startMemberOnboardingUseCase.start(StartMemberOnboardingCommand(memberInfoDto.memberId))
        return ResponseEntity.ok(memberCommandResponseMapper.toResponse(result))
    }

    @Operation(summary = "프로필 단계 완료 API", description = "온보딩 프로필 단계를 완료한다.")
    @PostMapping("/steps/profile")
    fun completeProfileStep(@MemberInfo memberInfoDto: MemberInfoDto): ResponseEntity<MemberOnboardingResponseDto> {
        val result = completeMemberProfileStepUseCase.complete(CompleteMemberOnboardingStepCommand(memberInfoDto.memberId))
        return ResponseEntity.ok(memberCommandResponseMapper.toResponse(result))
    }

    @Operation(summary = "예산 단계 완료 API", description = "온보딩 예산 단계를 완료한다.")
    @PostMapping("/steps/budget")
    fun completeBudgetStep(@MemberInfo memberInfoDto: MemberInfoDto): ResponseEntity<MemberOnboardingResponseDto> {
        val result = completeBudgetStepUseCase.complete(CompleteMemberOnboardingStepCommand(memberInfoDto.memberId))
        return ResponseEntity.ok(memberCommandResponseMapper.toResponse(result))
    }

    @Operation(summary = "카드 단계 완료 API", description = "온보딩 카드 단계를 완료한다.")
    @PostMapping("/steps/card")
    fun completeCardStep(@MemberInfo memberInfoDto: MemberInfoDto): ResponseEntity<MemberOnboardingResponseDto> {
        val result = completeCardStepUseCase.complete(CompleteMemberOnboardingStepCommand(memberInfoDto.memberId))
        return ResponseEntity.ok(memberCommandResponseMapper.toResponse(result))
    }

    @Operation(summary = "추천 취향 단계 완료 API", description = "온보딩 추천 취향 단계를 완료한다.")
    @PostMapping("/steps/preference")
    fun completePreferenceStep(@MemberInfo memberInfoDto: MemberInfoDto): ResponseEntity<MemberOnboardingResponseDto> {
        val result = completePreferenceStepUseCase.complete(CompleteMemberOnboardingStepCommand(memberInfoDto.memberId))
        return ResponseEntity.ok(memberCommandResponseMapper.toResponse(result))
    }

    @Operation(summary = "온보딩 완료 API", description = "회원 온보딩 플로우를 완료한다.")
    @PostMapping("/complete")
    fun complete(@MemberInfo memberInfoDto: MemberInfoDto): ResponseEntity<MemberOnboardingResponseDto> {
        val result = completeMemberOnboardingUseCase.complete(CompleteMemberOnboardingCommand(memberInfoDto.memberId))
        return ResponseEntity.ok(memberCommandResponseMapper.toResponse(result))
    }
}
