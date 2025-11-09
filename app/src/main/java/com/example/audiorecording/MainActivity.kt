package com.example.audiorecording

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.example.audiorecording.databinding.ActivityMainBinding
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRecord.setOnClickListener {

            when{

                ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.RECORD_AUDIO
                )== PackageManager.PERMISSION_GRANTED ->{


                    //TODO  zapis
                }

                ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.RECORD_AUDIO) ->{


                    showPermissionRationalDialog()
                }
                else ->{
                    requestPermissions(this,
                        arrayOf(android.Manifest.permission.RECORD_AUDIO),
                        REQUEST_RECORD_AUDIO_CODE)
                }
            }
        }
    }

    private fun showPermissionRationalDialog() {

        AlertDialog.Builder(this)
            .setMessage("You need permission to recording audio. ")
            .setPositiveButton("Okay"){_,_ ->
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_CODE)
            }
            .setNegativeButton("cancel", null)
            .show()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

        val audioRecordPermissionGranted = requestCode == REQUEST_RECORD_AUDIO_CODE
                && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if(audioRecordPermissionGranted){

        }else{

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.RECORD_AUDIO)){
                showPermissionRationalDialog()
            }else{
                showPermissionSettingDialog()
            }
        }


    }

    private fun showPermissionSettingDialog() {
        AlertDialog.Builder(this)
            .setMessage("To use audio recording, please go to Settings and enable the permission")
            .setPositiveButton("Ok"){_,_ ->
                navigateToAppSetting()
            }
            .setNegativeButton("cancel",null)
            .show()
    }


    private fun navigateToAppSetting(){
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            data = Uri.fromParts("package",packageName, null)
        }
        startActivity(intent)
    }

    companion object{
        private const val REQUEST_RECORD_AUDIO_CODE = 200
    }
}


