package ke.ac.ku.ledgerly.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ke.ac.ku.ledgerly.data.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: String)

    @Query("SELECT * FROM categories WHERE id = :categoryId AND isDeleted = 0")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE isDeleted = 0 ORDER BY name ASC")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE categoryType = :type AND isDeleted = 0 ORDER BY name ASC")
    suspend fun getCategoriesByType(type: String): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE categoryType = :type AND isDeleted = 0 ORDER BY name ASC")
    fun getCategoriesByTypeFlow(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isDefault = 1 AND isDeleted = 0 ORDER BY name ASC")
    suspend fun getDefaultCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE isDefault = 0 AND isDeleted = 0 ORDER BY name ASC")
    suspend fun getCustomCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE isDefault = 0 AND isDeleted = 0 ORDER BY name ASC")
    fun getCustomCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("UPDATE categories SET isDeleted = 1, lastModified = :timestamp WHERE id = :categoryId")
    suspend fun softDeleteCategory(categoryId: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM categories WHERE isDeleted = 0 ORDER BY lastModified DESC")
    suspend fun getAllCategoriesSync(): List<CategoryEntity>

    @Query("SELECT * FROM categories ORDER BY lastModified DESC")
    suspend fun getAllCategoriesIncludingDeleted(): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM categories WHERE isDeleted = 0")
    suspend fun getCategoriesCount(): Int
}
