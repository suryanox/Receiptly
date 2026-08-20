package com.reciply.telegram.processor

import com.reciply.report.InvoiceReportService
import com.reciply.telegram.TelegramMessages
import com.reciply.telegram.model.MessageType
import com.reciply.telegram.model.TelegramRequestContext
import com.reciply.telegram.service.TelegramReplyService
import org.slf4j.LoggerFactory

class CallbackProcessor(
    private val replyService: TelegramReplyService,
    private val reportService: InvoiceReportService,
) : TelegramUpdateProcessor {
    private val log = LoggerFactory.getLogger(CallbackProcessor::class.java)

    override fun canProcess(context: TelegramRequestContext): Boolean = context.messageType == MessageType.CALLBACK

    override suspend fun process(context: TelegramRequestContext) {
        context.callbackQueryId?.let { replyService.answerCallbackQuery(it) }

        when (val command = CallbackCommand.fromData(context.text)) {
            CallbackCommand.REPORT -> handleReport(context)
            null -> log.warn("Ignoring unknown callback: {}", context.text)
        }
    }

    private suspend fun handleReport(context: TelegramRequestContext) {
        when (val result = reportService.generate(context.userId)) {
            is InvoiceReportService.Result.Success ->
                replyService.sendDocument(
                    chatId = context.chatId,
                    document = result.bytes,
                    caption = TelegramMessages.REPORT_CAPTION,
                    fileName = result.fileName,
                )
            is InvoiceReportService.Result.Failure ->
                replyService.sendText(
                    chatId = context.chatId,
                    text = result.message,
                )
        }
    }
}
