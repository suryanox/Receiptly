package com.reciply.telegram.processor

import com.reciply.telegram.service.TelegramReplyService
import com.reciply.telegram.model.MessageType
import com.reciply.telegram.model.TelegramRequestContext

class TextProcessor(private val replyService: TelegramReplyService) : TelegramUpdateProcessor {

    override fun canProcess(context: TelegramRequestContext): Boolean {
        return context.messageType == MessageType.TEXT
    }

    override suspend fun process(context: TelegramRequestContext) {
        val name = context.firstName ?: "there"
        replyService.sendTextWithButtons(
            chatId = context.chatId,
            text = "Hello $name! Welcome to Receiptly.\nChoose an option:",
            buttons = listOf("Start" to "start", "Report" to "report")
        )
    }
}
