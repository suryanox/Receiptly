package com.reciply.telegram.processor

import com.reciply.db.Invoice
import com.reciply.db.InvoiceRepository
import com.reciply.db.ReceiptRepository
import com.reciply.telegram.model.MessageType
import com.reciply.telegram.model.TelegramRequestContext
import com.reciply.telegram.service.TelegramReplyService

class CallbackProcessor(
    private val replyService: TelegramReplyService,
    private val receiptRepository: ReceiptRepository,
    private val invoiceRepository: InvoiceRepository,
) : TelegramUpdateProcessor {
    override fun canProcess(context: TelegramRequestContext): Boolean = context.messageType == MessageType.CALLBACK

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
            buttons = listOf("Start" to "start", "Report" to "report"),
        )
    }

    private suspend fun handleReport(context: TelegramRequestContext) {
        val userId = context.userId

        val receiptIds = receiptRepository.findInvoiceCreatedReceiptIds(userId)
        if (receiptIds.isEmpty()) {
            replyService.sendText(
                chatId = context.chatId,
                text = "No receipt with status INVOICE_CREATED found.",
            )
            return
        }

        val invoices = invoiceRepository.findByReceiptIds(receiptIds)
        if (invoices.isEmpty()) {
            replyService.sendText(
                chatId = context.chatId,
                text = "No invoices found for these receipts.",
            )
            return
        }

        val textBytes = generateInvoiceText(invoices)
        replyService.sendDocument(
            chatId = context.chatId,
            document = textBytes,
            caption = "Invoice Report",
            fileName = "invoice_report.txt",
        )
    }

    private fun generateInvoiceText(invoices: List<Invoice>): ByteArray {
        val sb = StringBuilder()
        invoices.forEachIndexed { index, invoice ->
            if (index > 0) {
                sb.append("\n")
            }
            sb.appendLine("Invoice Report")
            sb.appendLine("Invoice #${index + 1}")
            sb.appendLine()
            sb.appendLine("Invoice Number: ${invoice.invoiceNumber ?: "—"}")
            sb.appendLine("Invoice Date: ${invoice.invoiceDate ?: "—"}")
            sb.appendLine("Supplier Name: ${invoice.supplierName ?: "—"}")
            sb.appendLine("Supplier Tax ID: ${invoice.supplierTaxId ?: "—"}")
            sb.appendLine("Currency: ${invoice.currency}")
            sb.appendLine("Subtotal: ${invoice.subtotal ?: "—"}")
            sb.appendLine("Discount: ${invoice.discount ?: "—"}")
            sb.appendLine("Tax Amount: ${invoice.taxAmount ?: "—"}")
            sb.appendLine("Tax Rate: ${invoice.taxRate ?: "—"}")
            sb.appendLine("Total Amount: ${invoice.totalAmount ?: "—"}")
            sb.appendLine("Category: ${invoice.category ?: "—"}")
            sb.appendLine()
        }
        return sb.toString().toByteArray(Charsets.UTF_8)
    }
}