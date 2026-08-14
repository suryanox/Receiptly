package com.reciply.telegram.processor

import com.pengrad.telegrambot.model.Update

interface TelegramUpdateProcessor {
    val order: Int
    fun canProcess(update: Update): Boolean
    fun process(update: Update)
}
