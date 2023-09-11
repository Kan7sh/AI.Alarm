package com.example.aialarm.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel:ViewModel() {

    private val _selectedNavItem = MutableLiveData<Int>()
    val selectedNavItem: LiveData<Int> = _selectedNavItem

}