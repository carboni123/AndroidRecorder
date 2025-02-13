package com.example.examplerecorder

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var mediaRecorder: MediaRecorder? = null
    var outputUri: Uri? = null
    private var fileDescriptor: android.os.ParcelFileDescriptor? = null

    // Save the file name so it can be reused when uploading.
    private var outputFileName: String? = null

    private lateinit var tvRecordingStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvRecordingStatus = findViewById(R.id.tvRecordingStatus)
        val recordButton = findViewById<Button>(R.id.btnRecord)
        val stopButton = findViewById<Button>(R.id.btnStop)
        val playButton = findViewById<Button>(R.id.btnPlay)
        val sendButton = findViewById<Button>(R.id.btnSend)

        recordButton.setOnClickListener {
            if (checkPermission()) {
                startRecording()
            } else {
                requestPermission()
            }
        }

        stopButton.setOnClickListener {
            stopRecording()
        }

        playButton.setOnClickListener {
            playRecording()
        }

        // Call the send function and pass both the outputUri and the recorded file's name.
        sendButton.setOnClickListener {
            sendRecordingToServer(outputUri, outputFileName ?: "default.3gp")
        }
    }

    private fun startRecording() {
        // Generate a unique file name with .m4a extension.
        outputFileName = "audiorecord_${System.currentTimeMillis()}.m4a"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, outputFileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
        }

        outputUri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (outputUri == null) {
            Toast.makeText(this, "Error creating file for recording", Toast.LENGTH_SHORT).show()
            return
        }

        fileDescriptor = contentResolver.openFileDescriptor(outputUri!!, "w")
        if (fileDescriptor == null) {
            Toast.makeText(this, "Error opening file for recording", Toast.LENGTH_SHORT).show()
            return
        }

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()  // Fallback for older devices
        }

        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // MP4/M4A container.
            setOutputFile(fileDescriptor!!.fileDescriptor)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)      // Use AAC encoder.

            try {
                prepare()
                start()
                Toast.makeText(this@MainActivity, "Recording started", Toast.LENGTH_SHORT).show()
                tvRecordingStatus.text = "Recording in progress..."
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Recording failed to start", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                Toast.makeText(this@MainActivity, "Recording stopped", Toast.LENGTH_SHORT).show()
                tvRecordingStatus.text = "Recording saved:\n$outputUri"
            } catch (e: RuntimeException) {
                Toast.makeText(this@MainActivity, "Error stopping recording", Toast.LENGTH_SHORT).show()
            } finally {
                release()
            }
        }
        mediaRecorder = null

        fileDescriptor?.close()
        fileDescriptor = null
    }

    private fun playRecording() {
        if (outputUri == null) {
            Toast.makeText(this, "No recording available", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(this@MainActivity, outputUri!!)
                prepare()
                start()
            }
            Toast.makeText(this, "Playing recording", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to play recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            101
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
