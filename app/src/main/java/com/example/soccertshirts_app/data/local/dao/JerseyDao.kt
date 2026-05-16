package com.example.soccertshirts_app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.soccertshirts_app.data.local.entity.JerseyEntity

@Dao
interface JerseyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jerseys: List<JerseyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jersey: JerseyEntity)

    @Query("SELECT * FROM jerseys ORDER BY createdAt DESC")
    fun getAllJerseys(): LiveData<List<JerseyEntity>>

    @Query("DELETE FROM jerseys WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM jerseys")
    suspend fun deleteAll()
}