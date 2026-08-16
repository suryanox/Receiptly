package com.reciply.db

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ReceiptRepository {
    suspend fun insert(imageFileId: String, chatId: Long): Long = newSuspendedTransaction {
        ReceiptTable.insert {
            it[ReceiptTable.imageFileId] = imageFileId
            it[ReceiptTable.chatId] = chatId
            it[ReceiptTable.ocrStatus] = OcrStatus.PENDING
        }[ReceiptTable.id]
    }
}
