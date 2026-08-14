package com.reciply.telegram.processor

import com.pengrad.telegrambot.model.Update
import com.reciply.telegram.TelegramReplyService

interface TelegramUpdateProcessor {
    val order: Int
    fun canProcess(update: Update): Boolean
    suspend fun process(update: Update)
}
