package com.reciply.telegram.processor

import com.reciply.telegram.TelegramReplyService
import com.reciply.telegram.model.MessageType
import com.reciply.telegram.model.TelegramRequestContext

class CommandProcessor(private val replyService: TelegramReplyService) : TelegramUpdateProcessor {
    override val order: Int = 1

    override fun canProcess(context: TelegramRequestContext): Boolean {
        return context.messageType == MessageType.COMMAND
    }

    override suspend fun process(context: TelegramRequestContext) {
        // TODO: implement
    }
}
