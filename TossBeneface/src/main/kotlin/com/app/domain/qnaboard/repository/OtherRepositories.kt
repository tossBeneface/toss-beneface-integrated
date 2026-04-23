package com.app.domain.qnaboard.repository

import com.app.domain.qnaboard.entity.Attachment
import com.app.domain.qnaboard.entity.Comment
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<Comment, Long>

interface AttachmentRepository : JpaRepository<Attachment, Long>
