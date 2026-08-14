package com.reciply.telegram.processor

import com.reciply.telegram.service.TelegramReplyService
import com.reciply.telegram.model.MessageType
import com.reciply.telegram.model.TelegramRequestContext

class TextMessageProcessor(private val replyService: TelegramReplyService) : TelegramUpdateProcessor {
    override fun canProcess(context: TelegramRequestContext): Boolean {
        return context.messageType == MessageType.TEXT
    }

    override suspend fun process(context: TelegramRequestContext) {
        // TODO: implement
    }
}
