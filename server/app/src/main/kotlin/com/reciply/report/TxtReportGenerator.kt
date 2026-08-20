package com.reciply.report

import com.reciply.db.Invoice
import com.reciply.telegram.TelegramMessages

class TxtReportGenerator : ReportGenerator {
    override val fileName: String = TelegramMessages.REPORT_FILENAME

    override fun generate(invoices: List<Invoice>): ByteArray {
        val builder = StringBuilder()

        builder.appendLine("Invoice Report")
        invoices.forEachIndexed { index, invoice ->
            if (index > 0) {
                builder.appendLine()
            }
            builder
                .appendLine("Invoice #${index + 1}")
                .appendLine("Invoice Number: ${asText(invoice.invoiceNumber)}")
                .appendLine("Invoice Date: ${asText(invoice.invoiceDate)}")
                .appendLine("Supplier Name: ${asText(invoice.supplierName)}")
                .appendLine("Supplier Tax ID: ${asText(invoice.supplierTaxId)}")
                .appendLine("Currency: ${invoice.currency}")
                .appendLine("Subtotal: ${asText(invoice.subtotal)}")
                .appendLine("Discount: ${asText(invoice.discount)}")
                .appendLine("Tax Amount: ${asText(invoice.taxAmount)}")
                .appendLine("Tax Rate: ${asText(invoice.taxRate)}")
                .appendLine("Total Amount: ${asText(invoice.totalAmount)}")
                .appendLine("Category: ${asText(invoice.category)}")
        }

        return builder.toString().toByteArray(Charsets.UTF_8)
    }

    private fun asText(value: Any?): String = value?.toString() ?: "—"
}
