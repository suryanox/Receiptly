package com.reciply.telegram.processor

import com.pengrad.telegrambot.model.Update

class ImageProcessor : TelegramUpdateProcessor {
    override fun canProcess(update: Update): Boolean {
        return update.message()?.photo()?.isNotEmpty() == true
    }

    override fun process(update: Update) {
        // TODO: implement
    }
}
