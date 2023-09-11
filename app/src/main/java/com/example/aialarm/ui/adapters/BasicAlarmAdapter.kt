package com.example.aialarm.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.aialarm.R
import com.example.aialarm.data.db.entities.BasicAlarmEntity
import com.example.aialarm.databinding.RowBasicAlarmBinding
import com.example.aialarm.utlis.RepeatConverter

class BasicAlarmAdapter(
    private val basicAlarms:List<BasicAlarmEntity>,
    private val rvListeners: BasicAlarmRVListeners
) :Adapter<BasicAlarmAdapter.BasicAlarmHolder>(){


    private val dayAbbreviations = arrayOf("SU", "MO", "TU", "WE", "TH", "FR", "SA")


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasicAlarmHolder =
        BasicAlarmHolder(
            DataBindingUtil.inflate<RowBasicAlarmBinding>(
                LayoutInflater.from(parent.context),
                R.layout.row_basic_alarm,
                parent,
                false
            )
        )

    override fun getItemCount(): Int {
        return basicAlarms.size
    }

    override fun onBindViewHolder(holder: BasicAlarmHolder, position: Int) {

       val alarmTime = "${String.format("%02d", basicAlarms[position].hour)}:${String.format("%02d", basicAlarms[position].minute)}"
       holder.recyclerViewBasicAlarmBinding.basicAlarmTime.text = alarmTime

        holder.recyclerViewBasicAlarmBinding.basicAlarmSwitch.isChecked = basicAlarms[position].turnedOn


        if(basicAlarms[position].repeatDays==""){
            holder.recyclerViewBasicAlarmBinding.repeatBasicAlarm.text = "Once"
        }else if(RepeatConverter.toList(basicAlarms[position].repeatDays)== listOf<Int>(1,2,3,4,5,6,7)){
            holder.recyclerViewBasicAlarmBinding.repeatBasicAlarm.text = "Everyday"
        }else{
            val days = RepeatConverter.toList(basicAlarms[position].repeatDays)
            val dayAbbreviationList = days.map { dayAbbreviations[it - 1] }
            val daysText = dayAbbreviationList.joinToString(" • ")
            holder.recyclerViewBasicAlarmBinding.repeatBasicAlarm.text = daysText
        }

        holder.recyclerViewBasicAlarmBinding.basicAlarmSwitch.setOnCheckedChangeListener { _, isChecked ->

            rvListeners.onSwitchClick(position,isChecked,basicAlarms[position])

        }

        holder.recyclerViewBasicAlarmBinding.deleteAlarm.setOnClickListener {
            rvListeners.onDeleteButtonClick(basicAlarms[position],holder.recyclerViewBasicAlarmBinding.basicAlarmSwitch.isChecked)
        }

        holder.recyclerViewBasicAlarmBinding.basicAlarmCard.setOnClickListener{
            rvListeners.onBasicAlarmClick(basicAlarms[position]);
        }

    }




    inner class BasicAlarmHolder(
        val recyclerViewBasicAlarmBinding:RowBasicAlarmBinding
    ):ViewHolder(recyclerViewBasicAlarmBinding.root){

    }

}