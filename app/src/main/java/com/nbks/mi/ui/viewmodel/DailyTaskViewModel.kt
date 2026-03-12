package com.nbks.mi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbks.mi.data.repository.DailyTaskRepository
import com.nbks.mi.domain.model.DailyTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyTaskViewModel @Inject constructor(
    private val repo: DailyTaskRepository,
) : ViewModel() {

    val tasks: StateFlow<List<DailyTask>> = repo.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // 起動時に古い完了フラグをリセット
        viewModelScope.launch { repo.resetYesterdayTasks() }
    }

    fun addTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch { repo.addTask(title.trim()) }
    }

    fun toggleTask(id: Long) {
        viewModelScope.launch {
            val task = tasks.value.find { it.id == id } ?: return@launch
            repo.setCompleted(id, !task.isCompletedToday)
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch { repo.deleteTask(id) }
    }
}
