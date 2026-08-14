package com.reciply.telegram.processor

import com.reciply.telegram.TelegramReplyService
import com.reciply.telegram.model.TelegramRequestContext

class NoopProcessor(private val replyService: TelegramReplyService) : TelegramUpdateProcessor {

    override fun canProcess(context: TelegramRequestContext): Boolean = true

    override suspend fun process(context: TelegramRequestContext) {
        // no-op
    }
}
