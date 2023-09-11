package com.example.aialarm.ui.basicalarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aialarm.data.repository.BasicAlarmRepository

class BasicAlarmViewModelFactory(
    private val repository: BasicAlarmRepository
) :ViewModelProvider.NewInstanceFactory(){

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BasicAlarmViewModel(repository) as T
    }


}