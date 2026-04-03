package com.example.leitorclaro.data

import kotlinx.coroutines.flow.Flow

class EquipmentRepository(private val dao: EquipmentDao) {
    fun observeAll(): Flow<List<Equipment>> = dao.observeAll()

    suspend fun insert(equipment: Equipment) = dao.insert(equipment)
}
