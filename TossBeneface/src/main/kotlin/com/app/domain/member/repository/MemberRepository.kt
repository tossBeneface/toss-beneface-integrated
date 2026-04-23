package com.app.domain.member.repository

import com.app.domain.member.entity.Member
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface MemberRepository : JpaRepository<Member, Long> {

    fun findByEmail(email: String): Optional<Member>
    fun findBySocialTypeAndSocialId(socialType: String, socialId: String): Optional<Member>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.memberId = :memberId")
    fun findByIdWithPessimisticLock(@Param("memberId") memberId: Long): Optional<Member>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Member m SET m.budget = :budget WHERE m.memberId = :memberId")
    fun updateBudget(@Param("memberId") memberId: Long, @Param("budget") budget: Int): Int
}
