package com.example.smartfridge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.smartfridge.domain.model.StorageLocation

@Entity(tableName = "quick_templates")
data class QuickTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val defaultShelfLifeDays: Int,
    val defaultUnit: String = "份",
    val category: String = "一般",
    val recommendedLocation: StorageLocation = StorageLocation.COLD
)
