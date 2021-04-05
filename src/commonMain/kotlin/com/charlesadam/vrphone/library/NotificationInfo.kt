package com.charlesadam.vrphone.library

import kotlinx.serialization.Serializable

@Serializable
data class NotificationInfo(
    val title: String,
    val content: String,
    val app: String
)

@Serializable
data class NotificationData(
    val notificationList: List<NotificationInfo>
)