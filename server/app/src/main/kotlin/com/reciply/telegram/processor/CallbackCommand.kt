package com.reciply.telegram.processor

enum class CallbackCommand(
    val data: String,
) {
    REPORT("report"),
    ;

    companion object {
        fun fromData(data: String?): CallbackCommand? = entries.firstOrNull { it.data == data }
    }
}
