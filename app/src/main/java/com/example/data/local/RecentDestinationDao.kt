package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentDestinationDao {
    @Query("SELECT * FROM recent_destinations ORDER BY timestamp DESC LIMIT 20")
    fun getAllRecent(): Flow<List<RecentDestinationEntity>>

    @Query("SELECT * FROM recent_destinations WHERE category IN ('HOME', 'WORK', 'FAVORITE') ORDER BY timestamp DESC")
    fun getSavedFavorites(): Flow<List<RecentDestinationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(destination: RecentDestinationEntity): Long

    @Delete
    suspend fun delete(destination: RecentDestinationEntity)

    @Query("DELETE FROM recent_destinations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recent_destinations WHERE category = 'RECENT'")
    suspend fun clearHistory()
}
