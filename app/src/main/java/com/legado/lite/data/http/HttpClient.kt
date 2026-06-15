package com.legado.lite.data.http

import android.content.Context
import com.legado.lite.LegadoApp
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object HttpClient {

    @Volatile private var instance: OkHttpClient? = null

    fun get(@Suppress("UNUSED_PARAMETER") context: Context = LegadoApp.get()): OkHttpClient =
        instance ?: synchronized(this) {
            instance ?: build().also { instance = it }
        }

    private fun build(): OkHttpClient {
        val log = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor(BookSourceHeaderInterceptor())
            .addInterceptor(log)
            .build()
    }
}
