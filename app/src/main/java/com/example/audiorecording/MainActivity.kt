package com.example.audiorecording

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.audiorecording.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity(), Timer.OnTickListener {

    // 🔹 Enum - ilovaning hozirgi holatini belgilaydi:
    // RELEASE = hech narsa qilinmayapti
    // RECORDING = yozuv ketayapti
    // PLAYING = yozilgan audio ijro etilayapti (hali ishlatilmagan)
    private enum class State {
        RELEASE, RECORDING, PLAYING
    }

    private lateinit var binding: ActivityMainBinding
    private var recorder: MediaRecorder? = null      // 🔹 MediaRecorder obyekti

    private var player: MediaPlayer?= null
    private var fileName: String = ""                // 🔹 Audio saqlanadigan fayl nomi
    private var state: State = State.RELEASE         // 🔹 Dastlabki holat

    private lateinit var  time: Timer



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Yozuv saqlanadigan fayl manzili (3gp formatda)
        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"


        time = Timer(this)
        // 🔹 "Record" tugmasi bosilganda bajariladigan ishlar
        binding.btnRecord.setOnClickListener {
            when (state) {
                State.RELEASE -> {  // Hozir yozuv yo‘q → yozishni boshlaymiz
                    record()
                }
                State.RECORDING -> {  // Yozuv ketayapti → to‘xtatamiz
                    onRecord(false)
                }
                State.PLAYING -> {  // (Hozircha ishlatilmagan)
                }
            }
        }

        binding.btnPlay.setOnClickListener {
            when (state) {
                State.RELEASE -> {  //
                    onPlay(true)
                }
               else -> {
                   // do nothing

               }
            }
        }

        binding.btnStop.setOnClickListener {
            when (state) {

                State.PLAYING -> {

                    onPlay(false)
                }
                else -> {
                    // do nothing

                }
            }
        }
    }


    private fun onPlay(start: Boolean){
        if(start) startPlaying() else stopPlaying()
    }

    private fun startPlaying() {
        state = State.PLAYING

        player = MediaPlayer().apply {
            setDataSource(fileName)

            try{
                prepare()
            }catch (e : IOException){
                Log.e("App", "Media player prepare fail $e")

            }
            start()
        }


        player?.setOnCompletionListener {
            stopPlaying()
        }
        binding.btnRecord.isEnabled = false
        binding.btnRecord.alpha = 0.3f




    }

    private fun stopPlaying() {
        state =  State.PLAYING

        player?.release()
        player =null
        binding.btnRecord.isEnabled = true
        binding.btnRecord.alpha = 1.0f
    }


    // 🔹 Bu funksiya permissionni tekshiradi va kerak bo‘lsa so‘raydi
    private fun record() {
        when {
            // ✅ Agar permission allaqachon berilgan bo‘lsa — yozishni boshlaymiz
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                onRecord(true)
            }

            // ⚠️ Agar foydalanuvchi ilgari ruxsat bermagan bo‘lsa, dialog ko‘rsatamiz
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) -> {
                showPermissionRationalDialog()
            }

            // ❗ Birinchi marta so‘rayotgan bo‘lsak
            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_CODE
                )
            }
        }
    }

    // 🔹 Yozishni boshlash yoki to‘xtatish (true = start, false = stop)
    private fun onRecord(start: Boolean) = if (start) startRecording() else stopRecording()

    // 🎙️ Yozishni boshlaydi
    private fun startRecording() {
        state = State.RECORDING
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)                 // Mikrofon manbasi
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)         // .3gp format
            setOutputFile(fileName)                                       // Saqlanish joyi
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)            // Kodek turi

            try {
                prepare() // Recorder’ni tayyorlash
            } catch (e: IOException) {
                Log.e("App", "prepare() failed $e")
            }

            start() // 🎙️ Yozishni boshlaydi
        }

        recorder?.maxAmplitude?.toFloat()
        time.start()

        // 🔹 UI yangilanishi
        binding.btnRecord.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.baseline_stop_24)
        )
        binding.btnRecord.imageTintList = ColorStateList.valueOf(Color.BLACK)

        // Play tugmasini vaqtincha o‘chirib qo‘yamiz
        binding.btnPlay.isEnabled = false
        binding.btnPlay.alpha = 0.3f


    }

    // ⏹️ Yozishni to‘xtatadi va faylni saqlaydi
    private fun stopRecording() {
        recorder?.apply {
            stop()     // Yozishni to‘xtatadi
            release()  // Resursni bo‘shatadi
        }

        recorder = null
        state = State.RELEASE

        // UI’ni qayta holatga keltirish
        binding.btnRecord.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.baseline_fiber_manual_record_24)
        )
        binding.btnRecord.imageTintList = ColorStateList.valueOf(Color.RED)

        // Play tugmasini qayta faollashtiramiz
        binding.btnPlay.isEnabled = true
        binding.btnPlay.alpha = 1.0f

        time.stop()

    }

    // 🗣️ Agar foydalanuvchi ilgari ruxsat bermagan bo‘lsa — tushuntiruvchi dialog
    private fun showPermissionRationalDialog() {
        AlertDialog.Builder(this)
            .setMessage("You need permission to record audio.")
            .setPositiveButton("Okay") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_CODE
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // 🔹 Permission natijasi qaytganda ishlaydigan funksiya
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

        val audioRecordPermissionGranted = requestCode == REQUEST_RECORD_AUDIO_CODE &&
                grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if (audioRecordPermissionGranted) {
            // ✅ Ruxsat berilgan — yozishni boshlaymiz
            onRecord(true)
        } else {
            // ❌ Ruxsat berilmagan
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.RECORD_AUDIO
                )
            ) {
                showPermissionRationalDialog()
            } else {
                showPermissionSettingDialog()
            }
        }
    }

    // ⚙️ Agar foydalanuvchi “Don’t ask again” bosgan bo‘lsa — Setting’ga olib borish
    private fun showPermissionSettingDialog() {
        AlertDialog.Builder(this)
            .setMessage("To use audio recording, please go to Settings and enable the permission.")
            .setPositiveButton("OK") { _, _ ->
                navigateToAppSetting()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ⚙️ App’ning Settings sahifasiga olib boradi
    private fun navigateToAppSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    override fun onTick(duration: Long) {
        binding.view.addAmplitude(recorder?.maxAmplitude?.toFloat() ?: 0f)
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_CODE = 200 // Permission so‘rovi kodi
    }
}
