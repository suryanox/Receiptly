package com.reciply.telegram.model

import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.User

fun Update.toRequestContext(): TelegramRequestContext? {
    callbackQuery()?.let { callback ->
        val message = callback.message() ?: return null
        return TelegramRequestContext(
            messageId = message.messageId().toLong(),
            chatId = message.chat().id().toLong(),
            userId = callback.from().id().toLong(),
            sender = callback.from(),
            text = callback.data(),
            messageType = MessageType.CALLBACK,
            callbackQueryId = callback.id(),
        )
    }

    val message = message() ?: return null
    val messageType =
        when {
            !message.photo().isNullOrEmpty() -> MessageType.IMAGE
            !message.text().isNullOrEmpty() -> MessageType.TEXT
            else -> MessageType.UNSUPPORTED
        }
    if (messageType == MessageType.UNSUPPORTED) return null

    return TelegramRequestContext(
        messageId = message.messageId().toLong(),
        chatId = message.chat().id().toLong(),
        userId = message.from().id().toLong(),
        sender = message.from(),
        text = message.text(),
        photoUrl = message.photo()?.maxByOrNull { it.width() * it.height() }?.fileId(),
        messageType = messageType,
    )
}

private fun TelegramRequestContext(
    messageId: Long,
    chatId: Long,
    userId: Long,
    sender: User,
    text: String?,
    messageType: MessageType,
    callbackQueryId: String? = null,
    photoUrl: String? = null,
): TelegramRequestContext =
    TelegramRequestContext(
        messageId = messageId,
        chatId = chatId,
        userId = userId,
        isBot = sender.isBot,
        username = sender.username(),
        firstName = sender.firstName(),
        lastName = sender.lastName(),
        languageCode = sender.languageCode(),
        text = text,
        photoUrl = photoUrl,
        messageType = messageType,
        callbackQueryId = callbackQueryId,
    )
