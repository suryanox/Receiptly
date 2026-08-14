package com.reciply.telegram.processor

import com.reciply.telegram.TelegramReplyService
import com.reciply.telegram.model.MessageType
import com.reciply.telegram.model.TelegramRequestContext

class ImageProcessor(private val replyService: TelegramReplyService) : TelegramUpdateProcessor {
    override val order: Int = 3

    override fun canProcess(context: TelegramRequestContext): Boolean {
        return context.messageType == MessageType.IMAGE
    }

    override suspend fun process(context: TelegramRequestContext) {
        // TODO: implement
    }
}
