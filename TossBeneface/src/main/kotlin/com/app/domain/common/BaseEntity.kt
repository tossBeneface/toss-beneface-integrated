package com.app.domain.common

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@EntityListeners(value = [AuditingEntityListener::class])
@MappedSuperclass
abstract class BaseEntity : BaseTimeEntity() {

    @CreatedBy
    @Column(updatable = false)
    var createdBy: String? = null
        protected set

    @LastModifiedBy
    var modifiedBy: String? = null
        protected set
}
