package com.reciply.telegram

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.KeyboardButton
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.request.SendDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TelegramReplyService(private val bot: TelegramBot) {

    suspend fun sendText(chatId: Long, text: String) = withContext(Dispatchers.IO) {
        val request = SendMessage(chatId, text)
        bot.execute(request)
    }

    suspend fun sendTextWithButtons(chatId: Long, text: String, buttons: List<String>) = withContext(Dispatchers.IO) {
        val keyboard = buttons.chunked(2).map { row ->
            row.map { KeyboardButton(it) }.toTypedArray()
        }.toTypedArray()

        val replyMarkup = ReplyKeyboardMarkup(*keyboard)
            .resizeKeyboard(true)

        val request = SendMessage(chatId, text)
            .replyMarkup(replyMarkup)

        bot.execute(request)
    }

    suspend fun sendDocument(chatId: Long, document: ByteArray, caption: String? = null) = withContext(Dispatchers.IO) {
        val request = SendDocument(chatId, document)
        caption?.let { request.caption(it) }
        bot.execute(request)
    }
}
