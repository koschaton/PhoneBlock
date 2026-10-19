package com.prefixblocker.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedPrefixDao {

    @Query("SELECT * FROM blocked_prefixes ORDER BY id DESC")
    fun getAllFlow(): Flow<List<BlockedPrefix>>

    @Query("SELECT * FROM blocked_prefixes WHERE isEnabled = 1")
    fun getActivePrefixesSync(): List<BlockedPrefix>

    @Query("SELECT COUNT(*) FROM blocked_prefixes")
    fun getCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prefix: BlockedPrefix): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(prefixes: List<BlockedPrefix>)

    @Update
    suspend fun update(prefix: BlockedPrefix)

    @Delete
    suspend fun delete(prefix: BlockedPrefix)

    @Query("DELETE FROM blocked_prefixes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
