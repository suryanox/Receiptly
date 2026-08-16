package com.reciply.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ReceiptRepository(
    private val database: Database
) {

    suspend fun insert(
        imageFileId: String,
        chatId: Long,
        userId: Long
    ): Long = newSuspendedTransaction(
        db = database
    ) {
        ReceiptTable.insert {
            it[ReceiptTable.imageFileId] = imageFileId
            it[ReceiptTable.chatId] = chatId
            it[ReceiptTable.userId] = userId
            it[ReceiptTable.ocrStatus] = OcrStatus.PENDING
        }[ReceiptTable.id]
    }
}