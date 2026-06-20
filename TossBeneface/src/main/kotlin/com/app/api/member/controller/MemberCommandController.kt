package com.app.api.member.controller

import com.app.api.member.dto.ChangeMemberAuthorityDto
import com.app.api.member.dto.UpdateMemberInitialStateDto
import com.app.api.member.dto.UpdateMemberProfileDto
import com.app.api.member.mapper.MemberCommandRequestMapper
import com.app.api.member.mapper.MemberCommandResponseMapper
import com.app.global.resolver.memberInfo.MemberInfo
import com.app.global.resolver.memberInfo.MemberInfoDto
import com.app.member.application.usecase.ChangeMemberAuthorityUseCase
import com.app.member.application.usecase.UpdateMemberInitialStateUseCase
import com.app.member.application.usecase.UpdateMemberProfileUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "member", description = "회원 수정 API")
@RestController
@RequestMapping("/api/member")
class MemberCommandController(
    private val updateMemberProfileUseCase: UpdateMemberProfileUseCase,
    private val updateMemberInitialStateUseCase: UpdateMemberInitialStateUseCase,
    private val changeMemberAuthorityUseCase: ChangeMemberAuthorityUseCase,
    private val memberCommandRequestMapper: MemberCommandRequestMapper,
    private val memberCommandResponseMapper: MemberCommandResponseMapper
) {

    @Operation(summary = "회원 프로필 수정 API", description = "본인 프로필 정보를 수정한다.")
    @PutMapping("/profile")
    fun updateProfile(
        @MemberInfo memberInfoDto: MemberInfoDto,
        @RequestBody request: UpdateMemberProfileDto.Request
    ): ResponseEntity<UpdateMemberProfileDto.Response> {
        val command = memberCommandRequestMapper.toCommand(memberInfoDto.memberId, request)
        val result = updateMemberProfileUseCase.update(command)
        return ResponseEntity.ok(memberCommandResponseMapper.toResponse(result))
    }

    @Operation(summary = "회원 초기 상태 수정 API", description = "본인 예산/활성 상태 등 초기 설정값을 수정한다.")
    @PutMapping("/initial-state")
    fun updateInitialState(
        @MemberInfo memberInfoDto: MemberInfoDto,
        @RequestBody request: UpdateMemberInitialStateDto.Request
    ): ResponseEntity<UpdateMemberInitialStateDto.Response> {
        val command = memberCommandRequestMapper.toCommand(memberInfoDto.memberId, request)
        val result = updateMemberInitialStateUseCase.update(command)
        return ResponseEntity.ok(memberCommandResponseMapper.toResponse(result))
    }

    @Operation(
        summary = "회원 권한 변경 API",
        description = "대상 회원의 권한을 변경한다. (ADMIN 전용 — RBAC)"
    )
    @PreAuthorize("@authz.can('member', 'manage')")
    @PutMapping("/{memberId}/authority")
    fun changeAuthority(
        @MemberInfo memberInfoDto: MemberInfoDto,
        @PathVariable("memberId") memberId: Long,
        @RequestBody request: ChangeMemberAuthorityDto.Request
    ): ResponseEntity<ChangeMemberAuthorityDto.Response> {
        val command = memberCommandRequestMapper.toCommand(memberId, request)
        val result = changeMemberAuthorityUseCase.change(command)
        return ResponseEntity.ok(memberCommandResponseMapper.toResponse(result))
    }
}
