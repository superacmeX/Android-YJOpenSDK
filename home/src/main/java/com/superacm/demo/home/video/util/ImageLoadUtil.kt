package com.superacm.demo.home.video.util

import android.content.Context
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import java.io.PrintWriter
import java.io.StringWriter


object ImageLoadUtil {
   val TAG = "ImageLoadUtil"
   lateinit var imageLoader:ImageLoader

    fun init(context:Context) {
        imageLoader = ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .respectCacheHeaders(false)
            .logger(object : coil.util.Logger {
                override var level: Int =  android.util.Log.DEBUG
                    get() =  android.util.Log.DEBUG

                override fun log(
                    tag: String,
                    priority: Int,
                    message: String?,
                    throwable: Throwable?
                ) {
                    if (message != null) {
                    }

                    if (throwable != null) {
                        val writer = StringWriter()
                        throwable.printStackTrace(PrintWriter(writer))
                    }
                }

            })
            .allowHardware(false)
            .build()
        Coil.setImageLoader(imageLoader)

    }

}
