package com.example.aialarm.ui.adapters

import com.example.aialarm.data.db.entities.AIAlarmEntity
import com.example.aialarm.data.db.entities.BasicAlarmEntity

interface BasicAlarmRVListeners {
    fun onBasicAlarmClick(basicAlarmEntity:BasicAlarmEntity)
    fun onDeleteButtonClick(basicAlarmEntity:BasicAlarmEntity,isChecked: Boolean)
    fun onSwitchClick(position: Int, isChecked: Boolean, basicAlarmEntity: BasicAlarmEntity)
}

interface AIAlarmRVListeners {
    fun onAIAlarmClick(aiAlarmEntity:AIAlarmEntity)
    fun onDeleteButtonClick(aiAlarmEntity:AIAlarmEntity,isChecked: Boolean)
    fun onSwitchClick(position: Int, isChecked: Boolean, aiAlarmEntity: AIAlarmEntity)
    fun onImgClick(aiAlarmEntity: AIAlarmEntity)
}