package com.example.aialarm.ui.basicalarm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.aialarm.R
import com.example.aialarm.data.db.AppDatabase
import com.example.aialarm.data.db.entities.BasicAlarmEntity
import com.example.aialarm.data.repository.BasicAlarmRepository
import com.example.aialarm.databinding.BottomSheetSetAlarmBinding
import com.example.aialarm.ui.adapters.BasicAlarmAdapter
import com.example.aialarm.ui.adapters.BasicAlarmRVListeners
import com.example.aialarm.utlis.RepeatConverter
import com.google.android.material.bottomsheet.BottomSheetDialog


class BasicAlarmHomeFragment : Fragment() {


    private lateinit var mContext: Context
    private lateinit var factory: BasicAlarmViewModelFactory
    private lateinit var basicAlarmRv: RecyclerView

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_basic_alarm_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = BasicAlarmRepository(AppDatabase.getInstance(mContext))
        factory = BasicAlarmViewModelFactory(repository)
        val viewModel = ViewModelProvider(this,factory)[BasicAlarmViewModel::class.java]

        basicAlarmRv = view.findViewById(R.id.basicAlarmRv)

        viewModel.basicAlarms.observe(viewLifecycleOwner, Observer {
            basicAlarmRv.adapter = BasicAlarmAdapter(
                it,
            object :BasicAlarmRVListeners{
                override fun onBasicAlarmClick(basicAlarmEntity: BasicAlarmEntity) {
                    val updateAlarmBS = BottomSheetDialog(mContext)
                    val updateAlarmBinding = DataBindingUtil.inflate<BottomSheetSetAlarmBinding>(
                        LayoutInflater.from(mContext),
                        R.layout.bottom_sheet_set_alarm,
                        null,
                        false
                    )

                    updateAlarmBinding.hourPickerBS.minValue = 0
                    updateAlarmBinding.hourPickerBS.maxValue = 23

                    updateAlarmBinding.minutePickerBS.minValue = 0
                    updateAlarmBinding.minutePickerBS.maxValue = 59

                    updateAlarmBinding.hourPickerBS.displayedValues = (0..23).map { "%02d".format(it) }.toTypedArray()
                    updateAlarmBinding.minutePickerBS.displayedValues = (0..59).map { "%02d".format(it) }.toTypedArray()


                    updateAlarmBinding.hourPickerBS.value = basicAlarmEntity.hour
                    updateAlarmBinding.minutePickerBS.value = basicAlarmEntity.minute
                    updateAlarmBinding.setAlarm.setOnClickListener {

                        if(basicAlarmEntity.isRepeating){
                            viewModel.cancelRepeatAlarm(basicAlarmEntity,mContext)
                        }else{
                            viewModel.cancelAlarm(basicAlarmEntity,mContext)
                        }

                        basicAlarmEntity.hour = updateAlarmBinding.hourPickerBS.value
                        basicAlarmEntity.minute = updateAlarmBinding.minutePickerBS.value
                        basicAlarmEntity.turnedOn = true
                        viewModel.updateAlarm(basicAlarmEntity)
                        if(basicAlarmEntity.isRepeating){
                            viewModel.setRepeatingBasicAlarm(
                                basicAlarmEntity.hour,
                                basicAlarmEntity.minute,
                                basicAlarmEntity.ringtone,
                                RepeatConverter.toList(basicAlarmEntity.repeatDays),
                                mContext,
                                true
                            )
                        }else{
                            viewModel.setOnceBasicAlarm(
                                basicAlarmEntity.hour,
                                basicAlarmEntity.minute,
                                basicAlarmEntity.ringtone,
                                mContext,
                                true
                            )

                        }
                        updateAlarmBS.dismiss()

                    }

                    updateAlarmBinding.moreSettings.setOnClickListener {
                        DataHolder.basicAlarmEntity = basicAlarmEntity
                        val intent = Intent(mContext, SetBasicAlarmActivity::class.java)
                        startActivity(intent)
                        updateAlarmBS.dismiss()
                    }

                    updateAlarmBS.setContentView(updateAlarmBinding.root)
                    updateAlarmBS.show()
                }

                override fun onDeleteButtonClick(
                    basicAlarmEntity: BasicAlarmEntity,
                    isChecked: Boolean
                ) {
                    if(isChecked){
                        if(basicAlarmEntity.isRepeating){
                            viewModel.cancelRepeatAlarm(basicAlarmEntity,mContext)
                        }else{
                            viewModel.cancelAlarm(basicAlarmEntity,mContext)
                        }
                    }
                    viewModel.deleteAlarm(basicAlarmEntity)
                }

                override fun onSwitchClick(
                    position: Int,
                    isChecked: Boolean,
                    basicAlarmEntity: BasicAlarmEntity
                ) {

                    if(isChecked){
                        if(basicAlarmEntity.isRepeating){
                            viewModel.setRepeatingBasicAlarm(
                                basicAlarmEntity.hour,
                                basicAlarmEntity.minute,
                                basicAlarmEntity.ringtone,
                                RepeatConverter.toList(basicAlarmEntity.repeatDays),
                                mContext,
                                true
                            )

                            basicAlarmEntity.turnedOn=true
                            viewModel.updateAlarm(basicAlarmEntity)
                        }else{
                            viewModel.setOnceBasicAlarm(
                                basicAlarmEntity.hour,
                                basicAlarmEntity.minute,
                                basicAlarmEntity.ringtone,
                                mContext,
                                true
                            )

                            basicAlarmEntity.turnedOn=true
                            viewModel.updateAlarm(basicAlarmEntity)


                        }
                    }else{
                        if(basicAlarmEntity.isRepeating){
                            viewModel.cancelRepeatAlarm(basicAlarmEntity,mContext)
                        }else{
                            viewModel.cancelAlarm(basicAlarmEntity,mContext)
                        }
                        basicAlarmEntity.turnedOn=false
                        viewModel.updateAlarm(basicAlarmEntity)
                    }


                }

            })
        })
    }

}