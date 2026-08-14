package com.reciply.telegram.processor

import com.reciply.telegram.model.MessageType
import com.reciply.telegram.model.TelegramRequestContext

class NoopProcessor() : TelegramUpdateProcessor {

    override fun canProcess(context: TelegramRequestContext): Boolean {
        return context.messageType == MessageType.UNSUPPORTED
    }

    override suspend fun process(context: TelegramRequestContext) {
        // For now not returning anything. As replying to every unsupported event could spam
    }
}
