package com.reciply.telegram.processor

import com.reciply.db.ReceiptRepository
import com.reciply.telegram.TelegramMessages
import com.reciply.telegram.model.MessageType
import com.reciply.telegram.model.TelegramRequestContext
import com.reciply.telegram.service.TelegramReplyService
import org.slf4j.LoggerFactory

class ImageProcessor(
    private val replyService: TelegramReplyService,
    private val receiptRepository: ReceiptRepository,
) : TelegramUpdateProcessor {
    private val log = LoggerFactory.getLogger(ImageProcessor::class.java)

    override fun canProcess(context: TelegramRequestContext): Boolean = context.messageType == MessageType.IMAGE

    override suspend fun process(context: TelegramRequestContext) {
        val fileId =
            context.photoUrl ?: return run {
                log.warn("Image update without a photo fileId: chatId={}", context.chatId)
            }
        log.info("Ingesting image: fileId={}, chatId={}", fileId, context.chatId)

        val receiptId =
            receiptRepository.insert(
                imageFileId = fileId,
                chatId = context.chatId,
                userId = context.userId,
            )
        log.info("Receipt persisted: id={}", receiptId)

        replyService.sendText(
            chatId = context.chatId,
            text = TelegramMessages.RECEIPT_ACK,
        )
    }
}
