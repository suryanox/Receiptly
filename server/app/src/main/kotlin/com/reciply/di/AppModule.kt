package com.reciply.di

import com.pengrad.telegrambot.TelegramBot
import com.reciply.telegram.service.TelegramReplyService
import com.reciply.telegram.service.TelegramWebhookService
import com.reciply.telegram.processor.CallbackProcessor
import com.reciply.telegram.processor.ImageProcessor
import com.reciply.telegram.processor.TextProcessor
import com.typesafe.config.ConfigFactory
import org.koin.dsl.module

val appModule = module {
    single {
        TelegramBot(ConfigFactory.load().getString("telegram.botToken"))
    }
    single { TelegramReplyService(get()) }

    single { TextProcessor(get()) }
    single { CallbackProcessor(get()) }
    single { ImageProcessor(get(), get()) }

    single {
        val textProcessor = get<TextProcessor>()
        val callbackProcessor = get<CallbackProcessor>()
        val imageProcessor = get<ImageProcessor>()
        TelegramWebhookService(listOf(textProcessor, callbackProcessor, imageProcessor))
    }
}
