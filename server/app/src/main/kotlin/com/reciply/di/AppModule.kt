package com.reciply.di

import com.pengrad.telegrambot.TelegramBot
import com.reciply.report.InvoiceReportService
import com.reciply.report.ReportGenerator
import com.reciply.report.TxtReportGenerator
import com.reciply.telegram.processor.CallbackProcessor
import com.reciply.telegram.processor.ImageProcessor
import com.reciply.telegram.processor.TextProcessor
import com.reciply.telegram.service.TelegramReplyService
import com.reciply.telegram.service.TelegramWebhookService
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.koin.dsl.module

val appModule =
    module {
        single<Config> { ConfigFactory.load() }

        single { TelegramBot(get<Config>().getString("telegram.botToken")) }
        single { TelegramReplyService(get()) }

        single<ReportGenerator> { TxtReportGenerator() }
        single { InvoiceReportService(get(), get(), get()) }

        single { TextProcessor(get()) }
        single { CallbackProcessor(get(), get()) }
        single { ImageProcessor(get(), get()) }

        single {
            TelegramWebhookService(
                listOf(
                    get<TextProcessor>(),
                    get<CallbackProcessor>(),
                    get<ImageProcessor>(),
                ),
            )
        }
    }
