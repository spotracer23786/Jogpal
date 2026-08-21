package com.jogpal.app.domain.matching

data class RunRequest(
    val id: String = "",
    val senderUid: String = "",
    val receiverUid: String = "",
    val status: RequestStatus = RequestStatus.PENDING,
    val compatibilityScore: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

enum class RequestStatus {
    PENDING, ACCEPTED, DECLINED
}
