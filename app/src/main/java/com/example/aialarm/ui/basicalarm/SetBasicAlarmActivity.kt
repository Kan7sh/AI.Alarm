package com.example.aialarm.ui.basicalarm

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.aialarm.R
import com.example.aialarm.data.db.AppDatabase
import com.example.aialarm.data.repository.BasicAlarmRepository
import com.example.aialarm.databinding.ActivitySetBasicAlarmBinding
import com.example.aialarm.databinding.BottomSheetDaysBinding
import com.example.aialarm.databinding.BottomSheetRepeatBinding
import com.example.aialarm.utlis.getCurrentHours
import com.example.aialarm.utlis.getCurrentMinutes
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.media.MediaPlayer
import android.util.AttributeSet
import android.util.Log
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import com.example.aialarm.utlis.RepeatConverter

class SetBasicAlarmActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var factory: BasicAlarmViewModelFactory
    private lateinit var soundPool: SoundPool
    private lateinit var binding:ActivitySetBasicAlarmBinding
    private var tickSoundId: Int = 0
    private lateinit var viewModel:BasicAlarmViewModel
    private var isRepeating:Boolean = false
    private var selectedRingtone:Int=0
    private var isFromUpdate=false
    private var repeat:List<Int> = emptyList()
    private val ringtones = arrayOf(R.raw.basic,R.raw.classic_alarm,R.raw.oversimplified,R.raw.evacuation,R.raw.emergency)
    private var isScrolling = false


    companion object {
        private const val TAG = "TESTING"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_set_basic_alarm)

        val repository = BasicAlarmRepository(AppDatabase.getInstance(this))
        factory = BasicAlarmViewModelFactory(repository)
        viewModel = ViewModelProvider(this,factory)[BasicAlarmViewModel::class.java]

        soundPool = SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0)
        tickSoundId = soundPool.load(this, R.raw.tick_sound, 1)

        binding.hourPicker.minValue = 0
        binding.hourPicker.maxValue = 23

        binding.minutePicker.minValue = 0
        binding.minutePicker.maxValue = 59

        binding.hourPicker.displayedValues = (0..23).map { "%02d".format(it) }.toTypedArray()
        binding.minutePicker.displayedValues = (0..59).map { "%02d".format(it) }.toTypedArray()

        binding.hourPicker.setOnValueChangedListener{ _, _, _ ->
            playTickSound()
        }
        binding.minutePicker.setOnValueChangedListener{ _, _, _ ->
            playTickSound()
        }

        binding.hourPicker.setOnScrollListener { _, _ ->
            playTickSound()
        }

        binding.repeatButton.setOnClickListener {
            showRepeatDialog()
        }

        binding.backButton.setOnClickListener {
            setResult(99)
            finish()
        }

        val mList = arrayOf<String?>("Basic Alarm", "Classic Alarm", "Oversimplified", "Evacuation", "Emergency")

        val mArrayAdapter = ArrayAdapter<Any?>(this, R.layout.spinner_list, mList)
        mArrayAdapter.setDropDownViewResource(R.layout.spinner_list)

        binding.selectRingtone.adapter = mArrayAdapter


        if(DataHolder.basicAlarmEntity!=null){
            isFromUpdate = true

            binding.hourPicker.value = DataHolder.basicAlarmEntity!!.hour
            binding.minutePicker.value = DataHolder.basicAlarmEntity!!.minute
            binding.selectRingtone.isSelected = false
            binding.selectRingtone.setSelection(DataHolder.basicAlarmEntity!!.ringtone,true)

            if(DataHolder.basicAlarmEntity!!.isRepeating){
                repeat = RepeatConverter.toList(DataHolder.basicAlarmEntity!!.repeatDays)
            }

            isRepeating = DataHolder.basicAlarmEntity!!.isRepeating

            if(repeat.size==7){
                binding.repeatButton.text="Everyday"
            }else if(repeat.contains(2)&&repeat.contains(3)&&repeat.contains(4)&&repeat.contains(5)&&repeat.contains(6)){
                binding.repeatButton.text="Mon to Fri"
            }else if(repeat.isNotEmpty()){
                binding.repeatButton.text="Custom"
            }


            binding.saveAlarm.setOnClickListener {
                if(!isRepeating){

                    viewModel.cancelAlarm(DataHolder.basicAlarmEntity!!,this)
                    DataHolder.basicAlarmEntity!!.hour = binding.hourPicker.value
                    DataHolder.basicAlarmEntity!!.minute = binding.minutePicker.value
                    DataHolder.basicAlarmEntity!!.ringtone = selectedRingtone
                    DataHolder.basicAlarmEntity!!.turnedOn = true
                    DataHolder.basicAlarmEntity!!.isRepeating = false
                    viewModel.updateAlarm(DataHolder.basicAlarmEntity!!)

                    viewModel.setOnceBasicAlarm(
                        binding.hourPicker.value,
                        binding.minutePicker.value,
                        selectedRingtone,
                        this,
                        true
                    )
                }else{
                    viewModel.cancelRepeatAlarm(DataHolder.basicAlarmEntity!!,this)
                    DataHolder.basicAlarmEntity!!.hour = binding.hourPicker.value
                    DataHolder.basicAlarmEntity!!.minute = binding.minutePicker.value
                    DataHolder.basicAlarmEntity!!.ringtone = selectedRingtone
                    DataHolder.basicAlarmEntity!!.turnedOn = true
                    DataHolder.basicAlarmEntity!!.repeatDays = RepeatConverter.fromList(repeat)
                    DataHolder.basicAlarmEntity!!.isRepeating = true
                    viewModel.updateAlarm(DataHolder.basicAlarmEntity!!)
                    viewModel.setRepeatingBasicAlarm(
                        binding.hourPicker.value,
                        binding.minutePicker.value,
                        selectedRingtone,
                        repeat,
                        this,
                        true
                    )
                }
                setResult(99)
                finish()

            }


        }else{

            binding.hourPicker.value = getCurrentHours()
            binding.minutePicker.value = getCurrentMinutes()
            binding.selectRingtone.isSelected = false
            binding.selectRingtone.setSelection(0,true)

            binding.saveAlarm.setOnClickListener {
                if(!isRepeating){
                    viewModel.setOnceBasicAlarm(
                        binding.hourPicker.value,
                        binding.minutePicker.value,
                        selectedRingtone,
                        this,
                        false
                    )
                }else{
                    viewModel.setRepeatingBasicAlarm(
                        binding.hourPicker.value,
                        binding.minutePicker.value,
                        selectedRingtone,
                        repeat,
                        this,
                        false
                    )
                }

                setResult(99)
                finish()

            }

        }

        binding.selectRingtone.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                selectedRingtone = position
                stopMusic()
                playMusic(selectedRingtone)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

    }


    private fun showRepeatDialog() {
        val repeatBS = BottomSheetDialog(this)

        val bindingRepeatBS = DataBindingUtil.inflate<BottomSheetRepeatBinding>(
            LayoutInflater.from(this),
            R.layout.bottom_sheet_repeat,
            null,
            false
        )

        repeatBS.setContentView(bindingRepeatBS.root)
        repeatBS.show()

        bindingRepeatBS.onceAlarm.setOnClickListener {
            isRepeating = false
            binding.repeatButton.text="Once"
            repeatBS.dismiss()

        }

        bindingRepeatBS.mTofAlarm.setOnClickListener {
            isRepeating = true
            repeat = listOf(2,3,4,5,6)
            binding.repeatButton.text="Mon to Fri"
            repeatBS.dismiss()

        }

        bindingRepeatBS.everydayAlarm.setOnClickListener {
            isRepeating = true
            repeat = listOf(1,2,3,4,5,6,7)
            binding.repeatButton.text="Everyday"
            repeatBS.dismiss()

        }

        bindingRepeatBS.customAlarm.setOnClickListener {
            showDaysDialog(repeatBS)
        }


    }

    private fun showDaysDialog(repeatBS: BottomSheetDialog) {
        val daysBS = BottomSheetDialog(this)
        val bindingDaysBS = DataBindingUtil.inflate<BottomSheetDaysBinding>(
            LayoutInflater.from(this),
            R.layout.bottom_sheet_days,
            null,
            false
        )

        daysBS.setContentView(bindingDaysBS.root)
        daysBS.show()

        bindingDaysBS.cancelDays.setOnClickListener {
            daysBS.dismiss()
        }

        if(repeat.isEmpty()){
            bindingDaysBS.mondayRB.isChecked=true

        }

        if(repeat.contains(1)){
            bindingDaysBS.sundayRB.isChecked=true
        }
        if(repeat.contains(2)){
            bindingDaysBS.mondayRB.isChecked=true
        }
        if(repeat.contains(3)){
            bindingDaysBS.tuesdayRB.isChecked=true
        }
        if(repeat.contains(4)){
            bindingDaysBS.wednesdayRB.isChecked=true
        }
        if(repeat.contains(5)){
            bindingDaysBS.thursdayRB.isChecked=true
        }
        if(repeat.contains(6)){
            bindingDaysBS.fridayRB.isChecked=true
        }
        if(repeat.contains(7)){
            bindingDaysBS.saturdayRB.isChecked = true
        }

        val selectedDays = mutableListOf<Int>()

        bindingDaysBS.setDays.setOnClickListener {

            isRepeating = true

            if(bindingDaysBS.sundayRB.isChecked){
                selectedDays.add(1)
            }

            if(bindingDaysBS.mondayRB.isChecked){
                selectedDays.add(2)
            }

            if(bindingDaysBS.tuesdayRB.isChecked){
                selectedDays.add(3)
            }

            if(bindingDaysBS.wednesdayRB.isChecked){
                selectedDays.add(4)
            }

            if(bindingDaysBS.thursdayRB.isChecked){
                selectedDays.add(5)
            }

            if(bindingDaysBS.fridayRB.isChecked){
                selectedDays.add(6)
            }

            if(bindingDaysBS.saturdayRB.isChecked){
                selectedDays.add(7)
            }

            repeat = selectedDays.toList()
            binding.repeatButton.text="Custom"

            daysBS.dismiss()
            repeatBS.dismiss()

        }

    }

    private fun playTickSound() {
        soundPool.play(tickSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    private fun playMusic(index: Int) {

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        } else {
            mediaPlayer?.reset()
        }
        try {
            val descriptor = resources.openRawResourceFd(ringtones[index])
            mediaPlayer?.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
            descriptor.close()
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        DataHolder.basicAlarmEntity = null
        stopMusic()
    }

}