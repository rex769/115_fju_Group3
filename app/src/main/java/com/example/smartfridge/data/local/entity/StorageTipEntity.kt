package com.example.smartfridge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.smartfridge.domain.model.StorageLocation

@Entity(tableName = "storage_tips")
data class StorageTipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val foodName: String,
    val wrongLocation: StorageLocation,
    val recommendedLocation: StorageLocation,
    val reason: String
)
