data class PaginatedResult<T>(
    val data: List<T>,
    val currentPage: Int,
    val pageSize: Int
) {
    val hasNext: Boolean
        get() = data.size == pageSize
}

data class PageRequest(
    val page: Int = 1,
    val pageSize: Int = 20
)
