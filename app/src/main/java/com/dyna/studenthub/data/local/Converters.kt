package com.dyna.studenthub.data.local

import androidx.room.TypeConverter

class RoomConverters {
    @TypeConverter
    fun transactionTypeToString(value: TransactionType): String = value.name

    @TypeConverter
    fun stringToTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun taskTypeToString(value: TaskType): String = value.name

    @TypeConverter
    fun stringToTaskType(value: String): TaskType = TaskType.valueOf(value)
}
