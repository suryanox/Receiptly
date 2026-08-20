package com.reciply.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.math.BigDecimal
import java.time.LocalDate

data class Invoice(
    val id: Long,
    val receiptId: Long,
    val invoiceNumber: String?,
    val invoiceDate: LocalDate?,
    val supplierName: String?,
    val supplierTaxId: String?,
    val currency: String,
    val subtotal: BigDecimal?,
    val discount: BigDecimal?,
    val taxAmount: BigDecimal?,
    val taxRate: BigDecimal?,
    val totalAmount: BigDecimal?,
    val category: String?,
)

private fun ResultRow.toInvoice(): Invoice =
    Invoice(
        id = this[InvoiceTable.id],
        receiptId = this[InvoiceTable.receiptId],
        invoiceNumber = this[InvoiceTable.invoiceNumber],
        invoiceDate = this[InvoiceTable.invoiceDate],
        supplierName = this[InvoiceTable.supplierName],
        supplierTaxId = this[InvoiceTable.supplierTaxId],
        currency = this[InvoiceTable.currency],
        subtotal = this[InvoiceTable.subtotal],
        discount = this[InvoiceTable.discount],
        taxAmount = this[InvoiceTable.taxAmount],
        taxRate = this[InvoiceTable.taxRate],
        totalAmount = this[InvoiceTable.totalAmount],
        category = this[InvoiceTable.category],
    )

class InvoiceRepository(
    private val database: Database,
) {
    suspend fun findByReceiptIds(receiptIds: List<Long>): List<Invoice> {
        if (receiptIds.isEmpty()) return emptyList()

        return newSuspendedTransaction(
            db = database,
        ) {
            InvoiceTable
                .selectAll()
                .where { InvoiceTable.receiptId inList receiptIds }
                .map { it.toInvoice() }
        }
    }
}
