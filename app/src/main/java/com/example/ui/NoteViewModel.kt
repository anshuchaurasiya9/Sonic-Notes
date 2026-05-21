package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Note
import com.example.data.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption {
    LAST_UPDATED,
    TITLE,
    CATEGORY
}

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _sortBy = MutableStateFlow(SortOption.LAST_UPDATED)
    val sortBy: StateFlow<SortOption> = _sortBy.asStateFlow()

    val filteredNotes: StateFlow<List<Note>> = combine(
        repository.allNotes,
        _searchQuery,
        _selectedCategory,
        _sortBy
    ) { notes, query, category, sort ->
        var result = notes

        // 1. Filter by category
        if (category != "All") {
            result = result.filter { it.category.equals(category, ignoreCase = true) }
        }

        // 2. Filter by search query
        if (query.isNotBlank()) {
            result = result.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true)
            }
        }

        // 3. Sort
        when (sort) {
            SortOption.LAST_UPDATED -> result.sortedWith(
                compareByDescending<Note> { it.isPinned }
                    .thenByDescending { it.lastUpdatedTimestamp }
            )
            SortOption.TITLE -> result.sortedWith(
                compareByDescending<Note> { it.isPinned }
                    .thenBy { it.title.lowercase() }
            )
            SortOption.CATEGORY -> result.sortedWith(
                compareByDescending<Note> { it.isPinned }
                    .thenBy { it.category.lowercase() }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectSortOption(sortOption: SortOption) {
        _sortBy.value = sortOption
    }

    fun addNote(title: String, content: String, category: String, colorIndex: Int, audioFilePath: String? = null, audioDurationMs: Long = 0L) {
        viewModelScope.launch {
            val note = Note(
                title = title.trim(),
                content = content.trim(),
                category = category,
                colorIndex = colorIndex,
                isPinned = false,
                createdTimestamp = System.currentTimeMillis(),
                lastUpdatedTimestamp = System.currentTimeMillis(),
                audioFilePath = audioFilePath,
                audioDurationMs = audioDurationMs
            )
            repository.insert(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.update(note.copy(lastUpdatedTimestamp = System.currentTimeMillis()))
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.update(
                note.copy(
                    isPinned = !note.isPinned,
                    lastUpdatedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }

    companion object {
        fun provideFactory(repository: NoteRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NoteViewModel(repository) as T
            }
        }
    }
}
