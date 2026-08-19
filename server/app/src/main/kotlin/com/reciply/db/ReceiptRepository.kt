package com.reciply.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ReceiptRepository(
    private val database: Database,
) {
    suspend fun insert(
        imageFileId: String,
        chatId: Long,
        userId: Long,
    ): Long =
        newSuspendedTransaction(
            db = database,
        ) {
            ReceiptTable.insert {
                it[ReceiptTable.imageFileId] = imageFileId
                it[ReceiptTable.chatId] = chatId
                it[ReceiptTable.userId] = userId
                it[ReceiptTable.status] = Status.PENDING
            }[ReceiptTable.id]
        }

    suspend fun findInvoiceCreatedReceiptIds(userId: Long): List<Long> =
        newSuspendedTransaction(
            db = database,
        ) {
            ReceiptTable
                .selectAll()
                .where { (ReceiptTable.status eq Status.INVOICE_CREATED) and (ReceiptTable.userId eq userId) }
                .orderBy(ReceiptTable.createdAt to SortOrder.DESC)
                .map { it[ReceiptTable.id] }
        }
}
