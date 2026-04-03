package com.example.leitorclaro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipments")
data class Equipment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val barcode: String,
    val equipmentType: String,
    val equipmentName: String,
    val contract: String,
    val capturedAt: Long,
    val latitude: Double,
    val longitude: Double,
    val street: String,
    val neighborhood: String,
    val postalCode: String
)
