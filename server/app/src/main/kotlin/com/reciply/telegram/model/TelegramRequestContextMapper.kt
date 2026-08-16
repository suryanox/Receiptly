package com.reciply.telegram.model

import com.pengrad.telegrambot.model.Update

fun Update.toRequestContext(): TelegramRequestContext? {
    val callbackQuery = callbackQuery()

    if (callbackQuery != null) {
        val from = callbackQuery.from()
        val message = callbackQuery.message()

        return TelegramRequestContext(
            messageId = message.messageId().toLong(),
            chatId = message.chat().id().toLong(),
            userId = from.id().toLong(),
            isBot = from.isBot,
            username = from.username(),
            firstName = from.firstName(),
            lastName = from.lastName(),
            languageCode = from.languageCode(),
            text = callbackQuery.data(),
            photoUrl = null,
            messageType = MessageType.CALLBACK,
            callbackQueryId = callbackQuery.id(),
        )
    }

    val message = message() ?: return null
    val from = message.from()

    val messageType =
        when {
            message.photo() != null && message.photo().isNotEmpty() -> MessageType.IMAGE
            message.text() != null -> MessageType.TEXT
            else -> MessageType.UNSUPPORTED
        }

    return TelegramRequestContext(
        messageId = message.messageId().toLong(),
        chatId = message.chat().id().toLong(),
        userId = from.id().toLong(),
        isBot = from.isBot,
        username = from.username(),
        firstName = from.firstName(),
        lastName = from.lastName(),
        languageCode = from.languageCode(),
        text = message.text(),
        photoUrl =
            message
                .photo()
                ?.maxByOrNull { it.width() * it.height() }
                ?.fileId(),
        messageType = messageType,
    )
}
