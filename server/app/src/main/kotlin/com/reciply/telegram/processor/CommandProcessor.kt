package com.reciply.telegram.processor

import com.pengrad.telegrambot.model.Update

class CommandProcessor : TelegramUpdateProcessor {
    override fun canProcess(update: Update): Boolean {
        return update.message()?.text()?.startsWith("/") == true
    }

    override fun process(update: Update) {
        // TODO: implement
    }
}
