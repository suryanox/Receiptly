package com.reciply.telegram.model

import com.pengrad.telegrambot.model.Update

fun Update.toRequestContext(): TelegramRequestContext {
    val message = message() ?: throw IllegalStateException("Message is null")
    val from = message.from()

    val messageType = when {
        message.photo() != null && message.photo().isNotEmpty() -> MessageType.IMAGE
        message.text()?.startsWith("/") == true -> MessageType.COMMAND
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
        photoUrls = message.photo()?.map { it.fileId() } ?: emptyList(),
        messageType = messageType
    )
}
