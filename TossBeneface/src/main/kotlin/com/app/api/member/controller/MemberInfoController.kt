package com.app.api.member.controller

import com.app.api.member.dto.MemberInfoResponseDto
import com.app.api.member.mapper.MemberInfoResponseMapper
import com.app.global.resolver.memberInfo.MemberInfo
import com.app.global.resolver.memberInfo.MemberInfoDto
import com.app.member.application.usecase.GetMemberInfoUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "member", description = "회원 API")
@RestController
@RequestMapping("/api/member")
class MemberInfoController(
    private val getMemberInfoUseCase: GetMemberInfoUseCase,
    private val memberInfoResponseMapper: MemberInfoResponseMapper
) {

    @Tag(name = "member")
    @Operation(summary = "회원 정보 조회 API", description = "회원 정보 조회 API")
    @ApiResponses(
        ApiResponse(responseCode = "500", description = "서버 오류 발생"),
        ApiResponse(responseCode = "M-003", description = "해당 회원은 존재하지 않는 회원입니다.")
    )
    @GetMapping("/name")
    fun getMemberName(@RequestParam("memberId") memberId: Long): ResponseEntity<String> {
        val memberName = getMemberInfoUseCase.getMemberName(memberId)
        return ResponseEntity.ok(memberName)
    }

    @GetMapping("/info")
    fun getMemberInfo(@MemberInfo memberInfoDto: MemberInfoDto): ResponseEntity<MemberInfoResponseDto> {
        val memberId = memberInfoDto.memberId
        val memberInfoResponseDto = memberInfoResponseMapper.toResponse(getMemberInfoUseCase.getMemberInfo(memberId))
        return ResponseEntity.ok(memberInfoResponseDto)
    }
}
