package ke.ac.ku.ledgerly.data.repository

import ke.ac.ku.ledgerly.data.dao.CategoryDao
import ke.ac.ku.ledgerly.data.model.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val dao: CategoryDao
) {
    suspend fun createCategory(category: CategoryEntity) {
        dao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        dao.updateCategory(category)
    }

    suspend fun deleteCategory(categoryId: String) {
        dao.softDeleteCategory(categoryId)
    }

    suspend fun getCategoryById(categoryId: String): CategoryEntity? {
        return dao.getCategoryById(categoryId)
    }

    suspend fun getAllCategories(): List<CategoryEntity> {
        return dao.getAllCategories()
    }

    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>> {
        return dao.getAllCategoriesFlow()
    }

    suspend fun getCategoriesByType(type: String): List<CategoryEntity> {
        return dao.getCategoriesByType(type)
    }

    fun getCategoriesByTypeFlow(type: String): Flow<List<CategoryEntity>> {
        return dao.getCategoriesByTypeFlow(type)
    }

    suspend fun getDefaultCategories(): List<CategoryEntity> {
        return dao.getDefaultCategories()
    }

    suspend fun getCustomCategories(): List<CategoryEntity> {
        return dao.getCustomCategories()
    }

    fun getCustomCategoriesFlow(): Flow<List<CategoryEntity>> {
        return dao.getCustomCategoriesFlow()
    }

    suspend fun getCategoriesCount(): Int {
        return dao.getCategoriesCount()
    }

    // Sync methods for cloud synchronization
    suspend fun getAllCategoriesSync(): List<CategoryEntity> {
        return dao.getAllCategoriesSync()
    }

    suspend fun getAllCategoriesIncludingDeleted(): List<CategoryEntity> {
        return dao.getAllCategoriesIncludingDeleted()
    }
}
