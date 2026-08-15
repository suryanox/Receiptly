package com.reciply.telegram.processor

import com.reciply.telegram.service.TelegramReplyService
import com.reciply.telegram.model.MessageType
import com.reciply.telegram.model.TelegramRequestContext

class CallbackProcessor(private val replyService: TelegramReplyService) : TelegramUpdateProcessor {

    override fun canProcess(context: TelegramRequestContext): Boolean {
        return context.messageType == MessageType.CALLBACK
    }

    override suspend fun process(context: TelegramRequestContext) {
        context.callbackQueryId?.let { replyService.answerCallbackQuery(it) }

        when (context.text) {
            "start" -> handleStart(context)
            "report" -> handleReport(context)
        }
    }

    private suspend fun handleStart(context: TelegramRequestContext) {
        val name = context.firstName ?: "there"
        replyService.sendTextWithButtons(
            chatId = context.chatId,
            text = "Hello $name! Welcome to Receiptly.\nChoose an option:",
            buttons = listOf("Start" to "start", "Report" to "report")
        )
    }

    private suspend fun handleReport(context: TelegramRequestContext) {
        replyService.sendText(
            chatId = context.chatId,
            text = "Report feature coming soon."
        )
    }
}
