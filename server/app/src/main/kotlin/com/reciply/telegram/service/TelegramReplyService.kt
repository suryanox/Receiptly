package com.reciply.telegram.service

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.request.AnswerCallbackQuery
import com.pengrad.telegrambot.request.SendDocument
import com.pengrad.telegrambot.request.SendMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TelegramReplyService(
    private val bot: TelegramBot,
) {
    suspend fun sendText(
        chatId: Long,
        text: String,
    ) = withContext(Dispatchers.IO) {
        val request = SendMessage(chatId, text)
        bot.execute(request)
    }

    suspend fun sendTextWithButtons(
        chatId: Long,
        text: String,
        buttons: List<Pair<String, String>>,
    ) = withContext(Dispatchers.IO) {
        val keyboard =
            buttons
                .chunked(8)
                .map { row ->
                    row.map { (text, data) -> InlineKeyboardButton(text).callbackData(data) }.toTypedArray()
                }.toTypedArray()

        val replyMarkup = InlineKeyboardMarkup(*keyboard)
        val request = SendMessage(chatId, text).replyMarkup(replyMarkup)
        bot.execute(request)
    }

    suspend fun answerCallbackQuery(callbackQueryId: String) =
        withContext(Dispatchers.IO) {
            val request = AnswerCallbackQuery(callbackQueryId)
            bot.execute(request)
        }

    suspend fun sendDocument(
        chatId: Long,
        document: ByteArray,
        caption: String? = null,
    ) = withContext(Dispatchers.IO) {
        val request = SendDocument(chatId, document)
        caption?.let { request.caption(it) }
        bot.execute(request)
    }
}
