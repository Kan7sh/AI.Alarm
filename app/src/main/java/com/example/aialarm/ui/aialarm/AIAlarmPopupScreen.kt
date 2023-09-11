package com.example.aialarm.ui.aialarm

import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.media.ThumbnailUtils
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import com.example.aialarm.R
import com.example.aialarm.data.db.AppDatabase
import com.example.aialarm.data.repository.AIAlarmRepository
import com.example.aialarm.data.repository.BasicAlarmRepository
import com.example.aialarm.databinding.AiAlarmPopupBinding
import com.example.aialarm.databinding.BasicAlarmPopupBinding
import com.example.aialarm.ml.Model
import com.example.aialarm.services.BasicAlarmSoundService
import com.example.aialarm.utlis.Coroutines
import com.example.aialarm.utlis.getCurrentDayOfWeek
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AIAlarmPopupScreen:AppCompatActivity (){

    private var alarmHour: String? = null
    private var alarmMinute: String? = null
    private var ringtoneIndex = 0
    private var isRepeating = false
    private lateinit var repository: AIAlarmRepository
    private lateinit var savedImageByteArray: ByteArray
    private lateinit var takenImageByteArray: ByteArray
    private lateinit var binding:AiAlarmPopupBinding
    private val CAMERA_REQUEST_CODE  = 69
    private val imageSize = 224
    private val dayAbbreviations = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }


        showWhenLockedAndTurnScreenOn()
        turnScreenOnAndKeyguardOff()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
        }

        binding = DataBindingUtil.inflate<AiAlarmPopupBinding>(
            LayoutInflater.from(this),
            R.layout.ai_alarm_popup,null,false)

        repository = AIAlarmRepository(AppDatabase.getInstance(this))

        ringtoneIndex = intent.getIntExtra("ringtone",0)
        alarmHour = intent.getStringExtra("hour")
        alarmMinute = intent.getStringExtra("minute")
        isRepeating = intent.getBooleanExtra("repeating",false)
        savedImageByteArray = intent.getByteArrayExtra("savedImage")!!
        binding.dayAIText.text=dayAbbreviations[getCurrentDayOfWeek()-1]

        var savedImageBitmap = BitmapFactory.decodeByteArray(savedImageByteArray, 0, savedImageByteArray.size)
        binding.referenceIMG.setImageBitmap(savedImageBitmap)

        val params = WindowManager.LayoutParams(

            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,

            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,

            )



        windowManager.addView(binding.root, params)

        binding.popupAIText.text = "${String.format("%02d", alarmHour!!.toInt())}:${String.format("%02d", alarmMinute!!.toInt())}"

        binding.takeImageCompare.setOnClickListener {
            binding.root.visibility = View.GONE
            openCameraImagePicker()
        }


    }
    private fun openCameraImagePicker() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)

        val cameraPackage = takePictureIntent.resolveActivity(packageManager)?.packageName

        if (takePictureIntent.resolveActivity(packageManager) != null) {
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            var cameraBitmap = data?.extras?.get("data") as? Bitmap
            if (cameraBitmap != null) {

                val savedImageUri = savedImageByteArray
                var savedImageBitmap = BitmapFactory.decodeByteArray(savedImageUri, 0, savedImageUri.size)


                val dimension1 = Math.min(savedImageBitmap.width,savedImageBitmap.height)
                savedImageBitmap = ThumbnailUtils.extractThumbnail(savedImageBitmap,dimension1,dimension1)

                val dimension2 = Math.min(cameraBitmap.width,cameraBitmap.height)
                cameraBitmap = ThumbnailUtils.extractThumbnail(cameraBitmap,dimension2,dimension2)

                savedImageBitmap = Bitmap.createScaledBitmap(savedImageBitmap,imageSize,imageSize,false)
                cameraBitmap = Bitmap.createScaledBitmap(cameraBitmap,imageSize,imageSize,false)
                predictScore(savedImageBitmap,cameraBitmap)


            }
            binding.root.visibility = View.VISIBLE
        }
    }

    private fun predictScore(img1: Bitmap?, img2: Bitmap?) {
        val pre1 = processImage(img1)
        val pre2 = processImage(img2)

        val result = calculateSimilarity(pre1,pre2)
        Log.d("TESTING", "Score: $result")

        if(result>0.48){
            stopAlarm()
        }else{
            binding.imageNotMatchTv.text = "Image did not match"
        }

    }

    private fun showWhenLockedAndTurnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    fun Activity.turnScreenOnAndKeyguardOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }

        with(getSystemService(KEYGUARD_SERVICE) as KeyguardManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestDismissKeyguard(this@turnScreenOnAndKeyguardOff, null)
            }
        }
    }

    private fun stopAlarm() {
        updateAlarm()

        val intent = Intent(this, BasicAlarmSoundService::class.java)
        stopService(intent)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.cancel(alarmHour!!.toInt() * 60 + alarmMinute!!.toInt())

        windowManager.removeView(binding.root)
        finish()
    }

    private fun updateAlarm() {
        if(!isRepeating){
            Coroutines.io {
                repository.updateTurnoffAIAlarm(false,alarmHour!!.toInt(),alarmMinute!!.toInt())
            }
        }
    }

    fun calculateSimilarity(prediction1: FloatArray, prediction2: FloatArray): Float {
        var dotProduct = 0.0f
        for (i in prediction1.indices) {
            dotProduct += prediction1[i] * prediction2[i]
        }

        var norm1 = 0.0f
        for (value in prediction1) {
            norm1 += value * value
        }
        norm1 = Math.sqrt(norm1.toDouble()).toFloat()

        var norm2 = 0.0f
        for (value in prediction2) {
            norm2 += value * value
        }
        norm2 = Math.sqrt(norm2.toDouble()).toFloat()

        val similarity = dotProduct / (norm1 * norm2)

        return similarity
    }


    private fun processImage(img1: Bitmap?):FloatArray {
        val model = Model.newInstance(applicationContext)

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)

        val byteBuffer = ByteBuffer.allocateDirect(4*imageSize*imageSize*3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(imageSize * imageSize)

        img1!!.getPixels(intValues,0,img1.width,0,0,img1.width,img1.height)
        var pixel1 = 0
        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val value = intValues[pixel1++]
                byteBuffer.putFloat(((value shr 16) and 0xFF) *(1.0f/255))
                byteBuffer.putFloat(((value shr 8) and 0xFF) *(1.0f/255))
                byteBuffer.putFloat((value and 0xFF) *(1.0f/255))
            }
        }

        inputFeature0.loadBuffer(byteBuffer)

        val outputs = model.process(inputFeature0)

        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        var confidence = outputFeature0.floatArray

        model.close()

        return  confidence

    }




    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event?.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }


}