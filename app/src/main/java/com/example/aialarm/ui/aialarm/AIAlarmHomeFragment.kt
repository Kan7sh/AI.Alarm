package com.example.aialarm.ui.aialarm

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.aialarm.R
import com.example.aialarm.data.db.AppDatabase
import com.example.aialarm.data.db.entities.AIAlarmEntity
import com.example.aialarm.data.repository.AIAlarmRepository
import com.example.aialarm.data.repository.BasicAlarmRepository
import com.example.aialarm.databinding.BottomSheetSetAlarmBinding
import com.example.aialarm.ui.adapters.AIAlarmAdapter
import com.example.aialarm.ui.adapters.AIAlarmRVListeners
import com.example.aialarm.ui.basicalarm.BasicAlarmViewModel
import com.example.aialarm.ui.basicalarm.BasicAlarmViewModelFactory
import com.example.aialarm.ui.basicalarm.DataHolder
import com.example.aialarm.ui.basicalarm.SetBasicAlarmActivity
import com.example.aialarm.utlis.RepeatConverter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AIAlarmHomeFragment : Fragment() {

    private lateinit var mContext: Context
    private lateinit var factory: AIAlarmViewModelFactory
    private lateinit var aiAlarmRV: RecyclerView

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_a_i_alarm_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = AIAlarmRepository(AppDatabase.getInstance(mContext))
        factory = AIAlarmViewModelFactory(repository)
        val viewModel = ViewModelProvider(this,factory)[AIAlarmViewModel::class.java]

        aiAlarmRV = view.findViewById(R.id.aiAlarmRv)

        viewModel.aiAlarms.observe(viewLifecycleOwner, Observer {
            aiAlarmRV.adapter = AIAlarmAdapter(
                it,
                object :AIAlarmRVListeners{
                    override fun onAIAlarmClick(aiAlarmEntity: AIAlarmEntity) {
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


                        updateAlarmBinding.hourPickerBS.value = aiAlarmEntity.hour
                        updateAlarmBinding.minutePickerBS.value = aiAlarmEntity.minute
                        updateAlarmBinding.setAlarm.setOnClickListener {

                            if(aiAlarmEntity.isRepeating){
                                viewModel.cancelRepeatAlarm(aiAlarmEntity,mContext)
                            }else{
                                viewModel.cancelAlarm(aiAlarmEntity,mContext)
                            }

                            aiAlarmEntity.hour = updateAlarmBinding.hourPickerBS.value
                            aiAlarmEntity.minute = updateAlarmBinding.minutePickerBS.value
                            aiAlarmEntity.turnedOn = true
                            viewModel.updateAlarm(aiAlarmEntity)
                            if(aiAlarmEntity.isRepeating){
                                viewModel.setRepeatingAIAlarm(
                                    aiAlarmEntity.hour,
                                    aiAlarmEntity.minute,
                                    aiAlarmEntity.ringtone,
                                    RepeatConverter.toList(aiAlarmEntity.repeatDays),
                                    mContext,
                                    true,
                                    aiAlarmEntity.imageURI
                                )
                            }else{
                                viewModel.setOnceAIAlarm(
                                    aiAlarmEntity.hour,
                                    aiAlarmEntity.minute,
                                    aiAlarmEntity.ringtone,
                                    mContext,
                                    true,
                                    aiAlarmEntity.imageURI
                                )

                            }
                            updateAlarmBS.dismiss()

                        }

                        updateAlarmBinding.moreSettings.setOnClickListener {
                            AIDataHolder.aiAlarmEntity = aiAlarmEntity
                            val intent = Intent(mContext, SetAIAlarmActivity::class.java)
                            startActivity(intent)
                            updateAlarmBS.dismiss()
                        }

                        updateAlarmBS.setContentView(updateAlarmBinding.root)
                        updateAlarmBS.show()
                    }

                    override fun onDeleteButtonClick(
                        aiAlarmEntity: AIAlarmEntity,
                        isChecked: Boolean
                    ) {

                        if(isChecked){
                            if(aiAlarmEntity.isRepeating){
                                viewModel.cancelRepeatAlarm(aiAlarmEntity,mContext)
                            }else{
                                viewModel.cancelAlarm(aiAlarmEntity,mContext)
                            }
                        }
                        viewModel.deleteAlarm(aiAlarmEntity)


                    }

                    override fun onSwitchClick(
                        position: Int,
                        isChecked: Boolean,
                        aiAlarmEntity: AIAlarmEntity
                    ) {


                        if(isChecked){
                            if(aiAlarmEntity.isRepeating){
                                viewModel.setRepeatingAIAlarm(
                                    aiAlarmEntity.hour,
                                    aiAlarmEntity.minute,
                                    aiAlarmEntity.ringtone,
                                    RepeatConverter.toList(aiAlarmEntity.repeatDays),
                                    mContext,
                                    true,
                                    aiAlarmEntity.imageURI
                                )

                                aiAlarmEntity.turnedOn=true
                                viewModel.updateAlarm(aiAlarmEntity)
                            }else{
                                viewModel.setOnceAIAlarm(
                                    aiAlarmEntity.hour,
                                    aiAlarmEntity.minute,
                                    aiAlarmEntity.ringtone,
                                    mContext,
                                    true,
                                    aiAlarmEntity.imageURI
                                )

                                aiAlarmEntity.turnedOn=true
                                viewModel.updateAlarm(aiAlarmEntity)


                            }
                        }else{
                            if(aiAlarmEntity.isRepeating){
                                viewModel.cancelRepeatAlarm(aiAlarmEntity,mContext)
                            }else{
                                viewModel.cancelAlarm(aiAlarmEntity,mContext)
                            }
                            aiAlarmEntity.turnedOn=false
                            viewModel.updateAlarm(aiAlarmEntity)
                        }

                    }

                    override fun onImgClick(aiAlarmEntity: AIAlarmEntity) {
                        val inflater = LayoutInflater.from(mContext)
                        val dialogView = inflater.inflate(R.layout.image_view_dialog, null)
                        val builder = MaterialAlertDialogBuilder(mContext)
                        builder.setView(dialogView)
                        var savedImageBitmap = BitmapFactory.decodeByteArray(aiAlarmEntity.imageURI, 0, aiAlarmEntity.imageURI.size)

                        dialogView.findViewById<ImageView>(R.id.dialogImageView).setImageBitmap(savedImageBitmap)


                        val dialog = builder.create()
                        dialog.show()
                    }

                }
            )
        })
    }




}