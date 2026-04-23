package com.app.api.UserDataTest.controller

import com.app.api.UserDataTest.dto.UserCardListDto
import com.app.api.UserDataTest.dto.UserCardRegisterDto
import com.app.api.UserDataTest.service.UserCardService
import com.app.global.resolver.memberInfo.MemberInfo
import com.app.global.resolver.memberInfo.MemberInfoDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user-cards")
class UserCardController(
    private val userCardService: UserCardService
) {

    @GetMapping
    fun getUserCards(@MemberInfo memberInfoDto: MemberInfoDto): ResponseEntity<List<UserCardListDto>> {
        val userCards = userCardService.getUserCards(memberInfoDto.memberId)
        return ResponseEntity.ok(userCards)
    }

    @PostMapping("/register")
    fun registerCard(
        @RequestBody dto: UserCardRegisterDto,
        @MemberInfo memberInfoDto: MemberInfoDto
    ): ResponseEntity<String> {
        return ResponseEntity.ok(userCardService.registerCard(dto, memberInfoDto.memberId))
    }

    @DeleteMapping("/{cardId}")
    fun deleteUserCard(
        @PathVariable cardId: Long,
        @MemberInfo memberInfoDto: MemberInfoDto
    ): ResponseEntity<String> {
        userCardService.deleteUserCard(cardId, memberInfoDto.memberId)
        return ResponseEntity.ok("카드 삭제 완료")
    }
}
