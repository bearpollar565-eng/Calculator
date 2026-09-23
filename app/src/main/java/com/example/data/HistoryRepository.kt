package com.example.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {

  val allHistory: Flow<List<CalculationHistory>> = historyDao.getAllHistory()

  suspend fun insert(expression: String, result: String): Long {
    return historyDao.insert(
      CalculationHistory(
        expression = expression,
        result = result,
        timestamp = System.currentTimeMillis()
      )
    )
  }

  suspend fun deleteById(id: Long) {
    historyDao.deleteById(id)
  }

  suspend fun clearAll() {
    historyDao.clearAll()
  }
}
