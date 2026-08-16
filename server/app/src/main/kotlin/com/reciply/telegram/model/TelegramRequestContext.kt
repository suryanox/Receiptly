package com.reciply.telegram.model

data class TelegramRequestContext(
    val messageId: Long,
    val chatId: Long,
    val userId: Long,
    val isBot: Boolean,
    val username: String?,
    val firstName: String?,
    val lastName: String?,
    val languageCode: String?,
    val text: String?,
    val photoUrl: String?,
    val messageType: MessageType,
    val callbackQueryId: String? = null,
)
