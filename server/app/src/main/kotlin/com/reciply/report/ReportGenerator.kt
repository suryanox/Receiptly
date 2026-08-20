package com.reciply.report

import com.reciply.db.Invoice

interface ReportGenerator {
    val fileName: String

    fun generate(invoices: List<Invoice>): ByteArray
}
