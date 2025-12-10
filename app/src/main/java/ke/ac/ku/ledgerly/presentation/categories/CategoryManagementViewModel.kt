package ke.ac.ku.ledgerly.presentation.categories

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.base.BaseViewModel
import ke.ac.ku.ledgerly.base.UiEvent
import ke.ac.ku.ledgerly.data.model.CategoryEntity
import ke.ac.ku.ledgerly.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : BaseViewModel() {

    private val _allCategories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val allCategories: StateFlow<List<CategoryEntity>> = _allCategories.asStateFlow()

    private val _customCategories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val customCategories: StateFlow<List<CategoryEntity>> = _customCategories.asStateFlow()

    private val _defaultCategories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val defaultCategories: StateFlow<List<CategoryEntity>> = _defaultCategories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

    private val _editingCategory = MutableStateFlow<CategoryEntity?>(null)
    val editingCategory: StateFlow<CategoryEntity?> = _editingCategory.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            categoryRepository.getAllCategoriesFlow()
                .catch { e ->
                    _errorMessage.value = "Failed to load categories: ${e.message}"
                    _isLoading.value = false
                }
                .collectLatest { categories ->
                    _allCategories.value = categories
                    _defaultCategories.value = categories.filter { it.isDefault }
                    _customCategories.value = categories.filter { !it.isDefault }
                    _isLoading.value = false
                }
        }
    }

    fun createCategory(
        name: String,
        icon: Int,
        color: Long,
        categoryType: String = "Expense"
    ) {
        if (name.isBlank()) {
            _errorMessage.value = "Category name cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                val categoryId =
                    name.lowercase().replace(" ", "_") + "_" + UUID.randomUUID().toString().take(8)
                val newCategory = CategoryEntity(
                    id = categoryId,
                    name = name,
                    icon = icon,
                    color = color,
                    isDefault = false,
                    categoryType = categoryType
                )
                categoryRepository.createCategory(newCategory)
                _showCreateDialog.value = false
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create category: ${e.message}"
            }
        }
    }

    fun updateCategory(
        categoryId: String,
        name: String,
        icon: Int,
        color: Long
    ) {
        if (name.isBlank()) {
            _errorMessage.value = "Category name cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                val category = categoryRepository.getCategoryById(categoryId)
                if (category == null) {
                    _errorMessage.value = "Category not found"
                    return@launch
                }
                val updatedCategory = category.copy(
                    name = name,
                    icon = icon,
                    color = color,
                    lastModified = System.currentTimeMillis()
                )
                categoryRepository.updateCategory(updatedCategory)
                _editingCategory.value = null
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update category: ${e.message}"
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                categoryRepository.deleteCategory(categoryId)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete category: ${e.message}"
            }
        }
    }

    fun updateDefaultCategoryColor(categoryId: String, newColor: Long) {
        viewModelScope.launch {
            try {
                val category = categoryRepository.getCategoryById(categoryId) ?: return@launch
                val updatedCategory = category.copy(
                    color = newColor,
                    lastModified = System.currentTimeMillis()
                )
                categoryRepository.updateCategory(updatedCategory)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update category color: ${e.message}"
            }
        }
    }

    fun openCreateDialog() {
        _showCreateDialog.value = true
    }

    fun closeCreateDialog() {
        _showCreateDialog.value = false
    }

    fun setEditingCategory(category: CategoryEntity?) {
        _editingCategory.value = category
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    override fun onEvent(event: UiEvent) {
        // Handle UI events as needed
    }
}

sealed class CategoryUiEvent : UiEvent() {
    data class CreateCategory(val name: String, val icon: Int, val color: Long) : CategoryUiEvent()
    data class UpdateCategory(val id: String, val name: String, val icon: Int, val color: Long) :
        CategoryUiEvent()

    data class DeleteCategory(val id: String) : CategoryUiEvent()
    data class EditCategory(val category: CategoryEntity) : CategoryUiEvent()
}
