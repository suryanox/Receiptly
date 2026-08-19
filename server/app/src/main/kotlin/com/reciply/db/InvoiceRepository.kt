package com.reciply.db

import org.jetbrains.exposed.sql.Database
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
                .map { row ->
                    Invoice(
                        id = row[InvoiceTable.id],
                        receiptId = row[InvoiceTable.receiptId],
                        invoiceNumber = row[InvoiceTable.invoiceNumber],
                        invoiceDate = row[InvoiceTable.invoiceDate],
                        supplierName = row[InvoiceTable.supplierName],
                        supplierTaxId = row[InvoiceTable.supplierTaxId],
                        currency = row[InvoiceTable.currency],
                        subtotal = row[InvoiceTable.subtotal],
                        discount = row[InvoiceTable.discount],
                        taxAmount = row[InvoiceTable.taxAmount],
                        taxRate = row[InvoiceTable.taxRate],
                        totalAmount = row[InvoiceTable.totalAmount],
                        category = row[InvoiceTable.category],
                    )
                }
        }
    }
}
