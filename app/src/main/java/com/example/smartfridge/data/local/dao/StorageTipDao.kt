package com.example.smartfridge.data.local.dao

import androidx.room.*
import com.example.smartfridge.data.local.entity.StorageTipEntity

@Dao
interface StorageTipDao {
    @Query("SELECT * FROM storage_tips WHERE foodName LIKE '%' || :keyword || '%' LIMIT 1")
    suspend fun findTipByKeyword(keyword: String): StorageTipEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTips(tips: List<StorageTipEntity>)
}
