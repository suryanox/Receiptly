package com.reciply.telegram.processor

import com.reciply.db.ReceiptRepository
import com.reciply.telegram.service.TelegramReplyService
import com.reciply.telegram.model.MessageType
import com.reciply.telegram.model.TelegramRequestContext
import org.slf4j.LoggerFactory

class ImageProcessor(
    private val replyService: TelegramReplyService,
    private val receiptRepository: ReceiptRepository
) : TelegramUpdateProcessor {

    private val log = LoggerFactory.getLogger(ImageProcessor::class.java)

    override fun canProcess(context: TelegramRequestContext): Boolean {
        return context.messageType == MessageType.IMAGE
    }

    override suspend fun process(context: TelegramRequestContext) {
        val fileId = context.photoUrls.firstOrNull() ?: return
        log.info("Processing image: fileId=$fileId, chatId=${context.chatId}")

        val id = receiptRepository.insert(
            imageFileId = fileId,
            chatId = context.chatId
        )

        log.info("Receipt created: id=$id")
        replyService.sendText(
            chatId = context.chatId,
            text = "Image received. Processing..."
        )
    }
}
