package com.nbks.mi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbks.mi.data.repository.MemoRepository
import com.nbks.mi.domain.model.Memo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MemoViewModel @Inject constructor(
    private val memoRepository: MemoRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val memos: StateFlow<List<Memo>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) memoRepository.getAllMemos()
        else memoRepository.searchMemos(query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editingMemo = MutableStateFlow<Memo?>(null)
    val editingMemo: StateFlow<Memo?> = _editingMemo.asStateFlow()

    private val _showEditor = MutableStateFlow(false)
    val showEditor: StateFlow<Boolean> = _showEditor.asStateFlow()

    fun setSearchQuery(q: String) { _searchQuery.value = q }

    fun newMemo() {
        _editingMemo.value = Memo(title = "", content = "", colorIndex = 0)
        _showEditor.value = true
    }

    fun editMemo(memo: Memo) {
        _editingMemo.value = memo
        _showEditor.value = true
    }

    fun saveMemo(title: String, content: String, colorIndex: Int, isPinned: Boolean = false) {
        val editing = _editingMemo.value ?: return
        viewModelScope.launch {
            memoRepository.saveMemo(
                editing.copy(
                    title = title.trim().ifBlank { "Untitled" },
                    content = content,
                    colorIndex = colorIndex,
                    updatedAt = LocalDateTime.now(),
                    isPinned = isPinned,
                )
            )
            _showEditor.value = false
            _editingMemo.value = null
        }
    }

    fun deleteMemo(id: Long) = viewModelScope.launch {
        memoRepository.deleteMemo(id)
    }

    fun closeEditor() {
        _showEditor.value = false
        _editingMemo.value = null
    }
}
