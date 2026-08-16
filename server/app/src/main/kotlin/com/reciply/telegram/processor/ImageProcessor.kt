package com.reciply.telegram.processor

import com.reciply.db.ReceiptRepository
import com.reciply.telegram.service.TelegramReplyService
import com.reciply.telegram.model.MessageType
import com.reciply.telegram.model.TelegramRequestContext

class ImageProcessor(
    private val replyService: TelegramReplyService,
    private val receiptRepository: ReceiptRepository
) : TelegramUpdateProcessor {

    override fun canProcess(context: TelegramRequestContext): Boolean {
        return context.messageType == MessageType.IMAGE
    }

    override suspend fun process(context: TelegramRequestContext) {
        val fileId = context.photoUrls.firstOrNull() ?: return

        receiptRepository.insert(
            imageFileId = fileId,
            chatId = context.chatId
        )

        replyService.sendText(
            chatId = context.chatId,
            text = "Image received. Processing..."
        )
    }
}
