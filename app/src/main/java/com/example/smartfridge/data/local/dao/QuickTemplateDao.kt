package com.example.smartfridge.data.local.dao

import androidx.room.*
import com.example.smartfridge.data.local.entity.QuickTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuickTemplateDao {
    @Query("SELECT * FROM quick_templates")
    fun getAllTemplates(): Flow<List<QuickTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<QuickTemplateEntity>)
}
