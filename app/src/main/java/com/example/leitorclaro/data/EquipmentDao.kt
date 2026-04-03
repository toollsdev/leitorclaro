package com.example.leitorclaro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(equipment: Equipment)

    @Query("SELECT * FROM equipments ORDER BY capturedAt DESC")
    fun observeAll(): Flow<List<Equipment>>
}
