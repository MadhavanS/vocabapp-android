package com.mad.vocab.data

import com.mad.vocab.data.models.Lang
import retrofit2.http.GET

interface Api {
    @GET("/app?page=1&limit=100")
    suspend fun getLangList(): Lang
}