package com.mad.vocab.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetroInstance {
//    const val BASE_URL = "http://192.168.178.11:3000/"
//    const val BASE_URL = "http://localhost:3120/"
    const val BASE_URL = "https://vocab-api-render.onrender.com/"

    private val inceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client: OkHttpClient = OkHttpClient().newBuilder()
        .addInterceptor(inceptor)
        .build()

    val api: Api by lazy {
        Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(client)
            .build()
            .create(Api::class.java)
    }
}