package com.example.aialarm.ui.aialarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aialarm.data.repository.AIAlarmRepository

class AIAlarmViewModelFactory(
    private val repository: AIAlarmRepository
):ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AIAlarmViewModel(repository) as T
    }

}