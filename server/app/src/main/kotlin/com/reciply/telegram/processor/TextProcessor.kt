package com.reciply.telegram.processor

import com.reciply.telegram.TelegramMessages
import com.reciply.telegram.model.MessageType
import com.reciply.telegram.model.TelegramRequestContext
import com.reciply.telegram.service.TelegramReplyService

class TextProcessor(
    private val replyService: TelegramReplyService,
) : TelegramUpdateProcessor {
    override fun canProcess(context: TelegramRequestContext): Boolean = context.messageType == MessageType.TEXT

    override suspend fun process(context: TelegramRequestContext) {
        replyService.sendTextWithButtons(
            chatId = context.chatId,
            text = TelegramMessages.greeting(context.firstName ?: "there"),
            buttons = TelegramMessages.menuButtons,
        )
    }
}
