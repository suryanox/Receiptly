package com.reciply.telegram

object TelegramMessages {
    private const val WELCOME_TITLE = "Welcome to Receiptly. Choose an option:"

    val menuButtons = listOf("Report" to "report")

    fun greeting(name: String) = "Hello $name! $WELCOME_TITLE"

    const val RECEIPT_ACK = "Image received. Processing..."
    const val REPORT_CAPTION = "Invoice Report"
    const val REPORT_FILENAME = "invoice_report.txt"
}
