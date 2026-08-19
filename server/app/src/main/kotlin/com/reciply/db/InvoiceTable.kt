package com.reciply.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

object InvoiceTable : Table("invoices") {
    val id = long("id").autoIncrement()
    val receiptId = long("receipt_id")
    val invoiceNumber = varchar("invoice_number", 255)
    val invoiceDate = date("invoice_date")
    val supplierName = varchar("supplier_name", 255)
    val supplierTaxId = varchar("supplier_tax_id", 255)
    val currency = varchar("currency", 3)
    val subtotal = decimal("subtotal", 12, 2)
    val discount = decimal("discount", 12, 2)
    val taxAmount = decimal("tax_amount", 12, 2)
    val taxRate = decimal("tax_rate", 5, 2)
    val totalAmount = decimal("total_amount", 12, 2)
    val category = varchar("category", 50)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
