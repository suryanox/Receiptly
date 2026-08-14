package com.reciply.di

import com.pengrad.telegrambot.TelegramBot
import com.reciply.telegram.TelegramReplyService
import com.reciply.telegram.TelegramWebhookService
import com.reciply.telegram.processor.CommandProcessor
import com.reciply.telegram.processor.ImageProcessor
import com.reciply.telegram.processor.MessageProcessor
import com.reciply.telegram.processor.NoopProcessor
import com.reciply.telegram.processor.TelegramUpdateProcessor
import com.typesafe.config.ConfigFactory
import org.koin.dsl.module

val appModule = module {
    single {
        TelegramBot(ConfigFactory.load().getString("telegram.botToken"))
    }
    single { TelegramReplyService(get()) }
    single<TelegramUpdateProcessor> { CommandProcessor() }
    single<TelegramUpdateProcessor> { MessageProcessor() }
    single<TelegramUpdateProcessor> { ImageProcessor() }
    single<TelegramUpdateProcessor> { NoopProcessor() }
    single { TelegramWebhookService(getAll()) }
}
