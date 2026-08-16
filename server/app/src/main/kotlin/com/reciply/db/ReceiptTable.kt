package com.reciply.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object ReceiptTable : Table("receipts") {
    val id = long("id").autoIncrement()
    val imageFileId = varchar("image_file_id", 255).uniqueIndex()
    val chatId = long("chat_id")
    val userId = long("user_id").index()
    val ocrStatus = enumerationByName("ocr_status", 20, OcrStatus::class).default(OcrStatus.PENDING)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
