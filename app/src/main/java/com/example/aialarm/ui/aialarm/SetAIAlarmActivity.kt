package com.example.aialarm.ui.aialarm

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.aialarm.R
import com.example.aialarm.data.db.AppDatabase
import com.example.aialarm.data.repository.AIAlarmRepository
import com.example.aialarm.databinding.ActivitySetAialarmBinding
import com.example.aialarm.databinding.BottomSheetDaysBinding
import com.example.aialarm.databinding.BottomSheetRepeatBinding
import com.example.aialarm.utlis.RepeatConverter
import com.example.aialarm.utlis.getCurrentHours
import com.example.aialarm.utlis.getCurrentMinutes
import com.example.aialarm.utlis.toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.ByteArrayOutputStream

class SetAIAlarmActivity : AppCompatActivity() {


    private var mediaPlayer: MediaPlayer? = null
    private lateinit var factory: AIAlarmViewModelFactory
    private lateinit var soundPool: SoundPool
    private lateinit var binding: ActivitySetAialarmBinding
    private var tickSoundId: Int = 0
   private lateinit var viewModel: AIAlarmViewModel
    private var isRepeating:Boolean = false
    private var selectedRingtone:Int=0
    private var isFromUpdate=false
    private var repeat:List<Int> = emptyList()
    private val ringtones = arrayOf(R.raw.basic,R.raw.classic_alarm,R.raw.oversimplified,R.raw.evacuation,R.raw.emergency)
    private var isScrolling = false
    private  var cameraBitmapByteArray:ByteArray= byteArrayOf()
    private val CAMERA_REQUEST_CODE  = 39

    companion object {
        private const val TAG = "TESTING"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_aialarm)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_set_aialarm)
        val repository = AIAlarmRepository(AppDatabase.getInstance(this))
        factory = AIAlarmViewModelFactory(repository)
        val viewModel = ViewModelProvider(this,factory)[AIAlarmViewModel::class.java]

        soundPool = SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0)
        tickSoundId = soundPool.load(this, R.raw.tick_sound, 1)

        binding.hourPickerAI.minValue = 0
        binding.hourPickerAI.maxValue = 23

        binding.minutePickerAI.minValue = 0
        binding.minutePickerAI.maxValue = 59

        binding.hourPickerAI.displayedValues = (0..23).map { "%02d".format(it) }.toTypedArray()
        binding.minutePickerAI.displayedValues = (0..59).map { "%02d".format(it) }.toTypedArray()

        binding.hourPickerAI.setOnValueChangedListener{ _, _, _ ->
            playTickSound()
        }
        binding.minutePickerAI.setOnValueChangedListener{ _, _, _ ->
            playTickSound()
        }

        binding.hourPickerAI.setOnScrollListener { _, _ ->
            playTickSound()
        }

        binding.repeatAIButton.setOnClickListener {
            showRepeatDialog()
        }

        binding.backAIButton.setOnClickListener {
            setResult(89)
            finish()
        }

        binding.addImageAIButton.setOnClickListener {
            openCameraImagePicker()
        }

        val mList = arrayOf<String?>("Basic Alarm", "Classic Alarm", "Oversimplified", "Evacuation", "Emergency")

        val mArrayAdapter = ArrayAdapter<Any?>(this, R.layout.spinner_list, mList)
        mArrayAdapter.setDropDownViewResource(R.layout.spinner_list)

        binding.selectRingtone.adapter = mArrayAdapter


        if(AIDataHolder.aiAlarmEntity!=null){
            isFromUpdate = true
            cameraBitmapByteArray = AIDataHolder.aiAlarmEntity!!.imageURI
            binding.addImageTv.text = "Image added ✓"
            binding.hourPickerAI.value = AIDataHolder.aiAlarmEntity!!.hour
            binding.minutePickerAI.value = AIDataHolder.aiAlarmEntity!!.minute
            binding.selectRingtone.isSelected = false
            binding.selectRingtone.setSelection(AIDataHolder.aiAlarmEntity!!.ringtone,true)

            if(AIDataHolder.aiAlarmEntity!!.isRepeating){
                repeat = RepeatConverter.toList(AIDataHolder.aiAlarmEntity!!.repeatDays)
            }

            isRepeating = AIDataHolder.aiAlarmEntity!!.isRepeating

            if(repeat.size==7){
                binding.repeatAIButton.text="Everyday"
            }else if(repeat.contains(2)&&repeat.contains(3)&&repeat.contains(4)&&repeat.contains(5)&&repeat.contains(6)){
                binding.repeatAIButton.text="Mon to Fri"
            }else if(repeat.isNotEmpty()){
                binding.repeatAIButton.text="Custom"
            }


            binding.saveAIAlarm.setOnClickListener {
                if(!isRepeating){

                    viewModel.cancelAlarm(AIDataHolder.aiAlarmEntity!!,this)
                    AIDataHolder.aiAlarmEntity!!.hour = binding.hourPickerAI.value
                    AIDataHolder.aiAlarmEntity!!.minute = binding.minutePickerAI.value
                    AIDataHolder.aiAlarmEntity!!.ringtone = selectedRingtone
                    AIDataHolder.aiAlarmEntity!!.turnedOn = true
                    AIDataHolder.aiAlarmEntity!!.isRepeating = false
                    viewModel.updateAlarm(AIDataHolder.aiAlarmEntity!!)

                    viewModel.setOnceAIAlarm(
                        binding.hourPickerAI.value,
                        binding.minutePickerAI.value,
                        selectedRingtone,
                        this,
                        true,
                        cameraBitmapByteArray
                    )
                }else{
                    viewModel.cancelRepeatAlarm(AIDataHolder.aiAlarmEntity!!,this)
                    AIDataHolder.aiAlarmEntity!!.hour = binding.hourPickerAI.value
                    AIDataHolder.aiAlarmEntity!!.minute = binding.minutePickerAI.value
                    AIDataHolder.aiAlarmEntity!!.ringtone = selectedRingtone
                    AIDataHolder.aiAlarmEntity!!.turnedOn = true
                    AIDataHolder.aiAlarmEntity!!.repeatDays = RepeatConverter.fromList(repeat)
                    AIDataHolder.aiAlarmEntity!!.isRepeating = true
                    viewModel.updateAlarm(AIDataHolder.aiAlarmEntity!!)
                    viewModel.setRepeatingAIAlarm(
                        binding.hourPickerAI.value,
                        binding.minutePickerAI.value,
                        selectedRingtone,
                        repeat,
                        this,
                        true,
                        cameraBitmapByteArray
                    )
                }
                setResult(89)
                finish()

            }

        }else {


            binding.hourPickerAI.value = getCurrentHours()
            binding.minutePickerAI.value = getCurrentMinutes()
            binding.selectRingtone.isSelected = false
            binding.selectRingtone.setSelection(0, true)

            binding.saveAIAlarm.setOnClickListener {

                if (cameraBitmapByteArray.isEmpty()) {
                    toast("Please add a image to create alarm")
                    return@setOnClickListener
                }

                if (!isRepeating) {
                    viewModel.setOnceAIAlarm(
                        binding.hourPickerAI.value,
                        binding.minutePickerAI.value,
                        selectedRingtone,
                        this,
                        false,
                        cameraBitmapByteArray
                    )
                } else {
                    viewModel.setRepeatingAIAlarm(
                        binding.hourPickerAI.value,
                        binding.minutePickerAI.value,
                        selectedRingtone,
                        repeat,
                        this,
                        false,
                        cameraBitmapByteArray
                    )
                }
                setResult(89)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            var imageBitmap = data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                binding.addImageTv.text = "Image added✓"
                val byteArrayOutputStream = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val imageByteArray = byteArrayOutputStream.toByteArray()
                cameraBitmapByteArray = imageByteArray

            }
        }
    }

    private fun openCameraImagePicker() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)

        val cameraPackage = takePictureIntent.resolveActivity(packageManager)?.packageName

        if (takePictureIntent.resolveActivity(packageManager) != null) {
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
            binding.repeatAIButton.text="Once"
            repeatBS.dismiss()

        }

        bindingRepeatBS.mTofAlarm.setOnClickListener {
            isRepeating = true
            repeat = listOf(2,3,4,5,6)
            binding.repeatAIButton.text="Mon to Fri"
            repeatBS.dismiss()

        }

        bindingRepeatBS.everydayAlarm.setOnClickListener {
            isRepeating = true
            repeat = listOf(1,2,3,4,5,6,7)
            binding.repeatAIButton.text="Everyday"
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
            binding.repeatAIButton.text="Custom"

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
        AIDataHolder.aiAlarmEntity = null
        stopMusic()
    }
}