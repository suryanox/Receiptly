package com.reciply.report

import com.reciply.db.InvoiceRepository
import com.reciply.db.ReceiptRepository

class InvoiceReportService(
    private val receiptRepository: ReceiptRepository,
    private val invoiceRepository: InvoiceRepository,
    private val reportGenerator: ReportGenerator,
) {
    sealed interface Result {
        data class Success(
            val bytes: ByteArray,
            val fileName: String,
        ) : Result

        data class Failure(
            val message: String,
        ) : Result
    }

    suspend fun generate(userId: Long): Result {
        val receiptIds = receiptRepository.findInvoiceCreatedReceiptIds(userId)
        if (receiptIds.isEmpty()) {
            return Result.Failure("No receipt with status INVOICE_CREATED found.")
        }

        val invoices = invoiceRepository.findByReceiptIds(receiptIds)
        if (invoices.isEmpty()) {
            return Result.Failure("No invoices found for these receipts.")
        }

        return Result.Success(
            bytes = reportGenerator.generate(invoices),
            fileName = reportGenerator.fileName,
        )
    }
}
