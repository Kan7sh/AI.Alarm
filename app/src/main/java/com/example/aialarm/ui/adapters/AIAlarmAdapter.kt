package com.example.aialarm.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.aialarm.R
import com.example.aialarm.data.db.entities.AIAlarmEntity
import com.example.aialarm.databinding.RowAiAlarmBinding
import com.example.aialarm.utlis.RepeatConverter

class AIAlarmAdapter(
    private val aiAlarms:List<AIAlarmEntity>,
    private val rvListeners: AIAlarmRVListeners
) : RecyclerView.Adapter<AIAlarmAdapter.AIAlarmHolder>(){


    private val dayAbbreviations = arrayOf("SU", "MO", "TU", "WE", "TH", "FR", "SA")


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AIAlarmAdapter.AIAlarmHolder =
        AIAlarmHolder(
            DataBindingUtil.inflate<RowAiAlarmBinding>(
                LayoutInflater.from(parent.context),
                R.layout.row_ai_alarm,
                parent,
                false
            )
        )

    override fun getItemCount(): Int {
        return aiAlarms.size
    }

    override fun onBindViewHolder(holder: AIAlarmHolder, position: Int) {

        val alarmTime = "${String.format("%02d", aiAlarms[position].hour)}:${String.format("%02d", aiAlarms[position].minute)}"
        holder.recyclerViewAIAlarmBinding.aiAlarmTime.text = alarmTime

        holder.recyclerViewAIAlarmBinding.aiAlarmSwitch.isChecked = aiAlarms[position].turnedOn


        if(aiAlarms[position].repeatDays==""){
            holder.recyclerViewAIAlarmBinding.repeatAIAlarm.text = "Once"
        }else{
            val days = RepeatConverter.toList(aiAlarms[position].repeatDays)
            val dayAbbreviationList = days.map { dayAbbreviations[it - 1] }
            val daysText = dayAbbreviationList.joinToString("-")
            holder.recyclerViewAIAlarmBinding.repeatAIAlarm.text = daysText
        }

        holder.recyclerViewAIAlarmBinding.aiAlarmSwitch.setOnCheckedChangeListener { _, isChecked ->

            rvListeners.onSwitchClick(position,isChecked,aiAlarms[position])

        }

        holder.recyclerViewAIAlarmBinding.deleteAIAlarm.setOnClickListener {
            rvListeners.onDeleteButtonClick(aiAlarms[position],holder.recyclerViewAIAlarmBinding.aiAlarmSwitch.isChecked)
        }

        holder.recyclerViewAIAlarmBinding.aiAlarmCard.setOnClickListener{
            rvListeners.onAIAlarmClick(aiAlarms[position])
        }

        holder.recyclerViewAIAlarmBinding.aiPhotoAdded.setOnClickListener {
            rvListeners.onImgClick(aiAlarms[position])
        }

    }



    inner class AIAlarmHolder(
        val recyclerViewAIAlarmBinding: RowAiAlarmBinding
    ): RecyclerView.ViewHolder(recyclerViewAIAlarmBinding.root){

    }

}