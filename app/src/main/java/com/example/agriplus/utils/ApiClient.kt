package com.example.agriplus.utils

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object ApiClient {

    private val client = OkHttpClient()

    fun uploadImage(imageBytes: ByteArray, callback: (String) -> Unit) {

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "image.jpg",
                imageBytes.toRequestBody("image/*".toMediaType())
            )
            .build()

        val url =
            "https://serverless.roboflow.com/leaf-disease-s6rb1/1?api_key=vQ2rpHdjy6e4XHjDMyVY"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                callback("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string() ?: "No response"
                callback(result)
            }
        })
    }
}