package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_destinations")
data class RecentDestinationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val category: String, // "HOME", "WORK", "RECENT", "FAVORITE"
    val timestamp: Long = System.currentTimeMillis()
)
