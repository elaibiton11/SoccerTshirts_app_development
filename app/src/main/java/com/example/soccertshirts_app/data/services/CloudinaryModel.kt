package com.example.soccertshirts_app.data.services

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

object CloudinaryModel {

    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            val config = mapOf(
                "cloud_name" to "dvi4biwcx",
                "api_key" to "625489964139741",
                "api_secret" to "1cqnkTbL6vMcHHm1GsPhYkwbiXc"
            )
            MediaManager.init(context, config)
            isInitialized = true
        }
    }

    fun uploadImage(imageUri: Uri, publicId: String, callback: (String?) -> Unit) {
        MediaManager.get().upload(imageUri)
            .option("public_id", publicId)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    callback(resultData["secure_url"] as? String)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    callback(null)
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }
}