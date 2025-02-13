package com.example.examplerecorder

import android.app.Activity
import android.net.Uri
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

fun Activity.sendRecordingToServer(
    outputUri: Uri?,
    fileName: String,
    serverUrl: String = "http://10.0.2.2:5000/upload"
) {
    if (outputUri == null) {
        Toast.makeText(this, "No recording available", Toast.LENGTH_SHORT).show()
        return
    }

    // Show the loading spinner.
    val progressBar = findViewById<ProgressBar>(R.id.progressBar)
    runOnUiThread { progressBar.visibility = View.VISIBLE }

    // Read file content from the content URI.
    val inputStream = contentResolver.openInputStream(outputUri)
    val fileBytes = inputStream?.readBytes()
    inputStream?.close()

    if (fileBytes == null) {
        runOnUiThread { progressBar.visibility = View.GONE }
        Toast.makeText(this, "Error reading the recording file", Toast.LENGTH_SHORT).show()
        return
    }

    // Create a multipart request body with the audio file.
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "file", fileName,
            fileBytes.toRequestBody("audio/3gpp".toMediaTypeOrNull())
        )
        .build()

    // Build the request.
    val request = Request.Builder()
        .url(serverUrl)
        .post(requestBody)
        .build()

    // Add a logging interceptor to inspect HTTP details.
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            runOnUiThread {
                progressBar.visibility = View.GONE
                Toast.makeText(this@sendRecordingToServer, "File upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            runOnUiThread {
                progressBar.visibility = View.GONE
                if (response.isSuccessful && responseBody != null) {
                    try {
                        // Parse the JSON response from the server.
                        val jsonObject = JSONObject(responseBody)
                        val message = jsonObject.getString("message")
                        val transcription = jsonObject.getString("transcription")
                        Toast.makeText(this@sendRecordingToServer, message, Toast.LENGTH_SHORT).show()

                        // Update the server response TextView.
                        val tvServerResponse = findViewById<TextView>(R.id.tvServerResponse)
                        tvServerResponse.text = transcription
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@sendRecordingToServer, "Failed to parse server response", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@sendRecordingToServer, "Upload error: ${response.code}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    })
}
