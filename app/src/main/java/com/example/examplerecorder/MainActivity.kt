package com.example.examplerecorder

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
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
    private var outputUri: Uri? = null
    private var fileDescriptor: android.os.ParcelFileDescriptor? = null

    private lateinit var tvRecordingStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvRecordingStatus = findViewById(R.id.tvRecordingStatus)
        val recordButton = findViewById<Button>(R.id.btnRecord)
        val stopButton = findViewById<Button>(R.id.btnStop)
        val playButton = findViewById<Button>(R.id.btnPlay)

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
    }

    private fun startRecording() {
        // Create a new MediaStore entry for the recording.
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "audiorecord_${System.currentTimeMillis()}.3gp")
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/3gpp")
            // Save in the Music directory
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
        }

        // Insert the entry into MediaStore and obtain its Uri.
        outputUri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (outputUri == null) {
            Toast.makeText(this, "Error creating file for recording", Toast.LENGTH_SHORT).show()
            return
        }

        // Open a ParcelFileDescriptor for writing.
        fileDescriptor = contentResolver.openFileDescriptor(outputUri!!, "w")
        if (fileDescriptor == null) {
            Toast.makeText(this, "Error opening file for recording", Toast.LENGTH_SHORT).show()
            return
        }

        // Set up MediaRecorder with the file descriptor.
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileDescriptor!!.fileDescriptor)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

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

        // Close the file descriptor if it's open.
        fileDescriptor?.close()
        fileDescriptor = null
    }

    private fun playRecording() {
        if (outputUri == null) {
            Toast.makeText(this, "No recording available", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            // Initialize MediaPlayer to play the content URI.
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
