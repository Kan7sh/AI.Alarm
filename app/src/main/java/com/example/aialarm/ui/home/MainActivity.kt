package com.example.aialarm.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.aialarm.R
import com.example.aialarm.databinding.ActivityMainBinding
import com.example.aialarm.ui.aialarm.AIAlarmHomeFragment
import com.example.aialarm.ui.aialarm.SetAIAlarmActivity
import com.example.aialarm.ui.basicalarm.BasicAlarmHomeFragment
import com.example.aialarm.ui.basicalarm.SetBasicAlarmActivity
import com.example.aialarm.utlis.toast

class MainActivity : AppCompatActivity() {

    private val OVERLAY_PERMISSION_REQ_CODE = 564
    private val CAMERA_PERMISSION_REQ_CODE = 565
    private var currentFragment: String = "basicFragment"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        binding.viewModel = viewModel

        showFragment(BasicAlarmHomeFragment())

        binding.bottomNavView.itemActiveIndicatorColor = getColorStateList(R.color.activeIndicatorColor)

        binding.bottomNavView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.basicAlarmItem -> {
                    currentFragment = "basicFragment"
                    showFragment(BasicAlarmHomeFragment())
                }
                R.id.aiAlarmItem -> {
                    currentFragment = "aiFragment"
                    showFragment(AIAlarmHomeFragment())
                }
            }
            return@setOnItemSelectedListener true
        }

        binding.addButton.setOnClickListener {
            requestPermissions()
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.homeFrameLayout, fragment)
            .commit()
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            checkOverlayPermission()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQ_CODE)
        }
    }

    private fun checkOverlayPermission() {
        if (Settings.canDrawOverlays(this)) {
            proceedWithAction()
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
        }
    }

    private fun proceedWithAction() {
        if (currentFragment == "basicFragment") {
            val intent = Intent(this, SetBasicAlarmActivity::class.java)
            startActivityForResult(intent,99)
        } else if (currentFragment == "aiFragment") {
            val intent = Intent(this, SetAIAlarmActivity::class.java)
            startActivityForResult(intent,89)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQ_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkOverlayPermission()
            } else {
                toast("Camera permission required")
            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Settings.canDrawOverlays(this)) {
                proceedWithAction()
            } else {
                toast("Overlay permission required")
            }
        }

        if(requestCode==89){
            currentFragment = "aiFragment"
            showFragment(AIAlarmHomeFragment())
        }
        if(requestCode==99){
            currentFragment = "basicFragment"
            showFragment(BasicAlarmHomeFragment())
        }

    }
}
