package com.example.aialarm.utlis

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Coroutines {

    fun io(work:suspend (()->Unit))=
        CoroutineScope(Dispatchers.IO).launch {
            work()
        }



}