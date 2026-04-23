package com.app.domain.qnaboard.constant

enum class ContentStatus {
    ACTIVATE, DEACTIVATE;

    companion object {
        fun from(contentStatus: String): ContentStatus = valueOf(contentStatus)
    }
}

enum class CommentStatus {
    ACTIVATE, DEACTIVATE;

    companion object {
        fun from(commentStatus: String): CommentStatus = valueOf(commentStatus)
    }
}

enum class FileStatus {
    ACTIVATE, DEACTIVATE;

    companion object {
        fun from(fileStatus: String): FileStatus = valueOf(fileStatus)
    }
}
