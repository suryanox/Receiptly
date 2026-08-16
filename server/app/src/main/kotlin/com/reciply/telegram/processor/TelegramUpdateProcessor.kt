package com.reciply.telegram.processor

import com.reciply.telegram.model.TelegramRequestContext

interface TelegramUpdateProcessor {
    fun canProcess(context: TelegramRequestContext): Boolean

    suspend fun process(context: TelegramRequestContext)
}
