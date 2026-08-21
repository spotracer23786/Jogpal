package com.jogpal.app.domain.matching

data class Match(
    val id: String = "",
    val participantUids: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
