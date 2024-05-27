package com.mad.vocab.data

import com.mad.vocab.data.models.Lang
import com.mad.vocab.data.models.LangObj
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface Api {
    @GET("/app?page=1&limit=1000")
    suspend fun getLangList(): Lang

    @POST("/app")
    suspend fun addLang(@Body langBody: LangObj): Response<LangObj>

//    @FormUrlEncoded
//    @Headers("Content-Type: application/x-www-form-urlencoded")
//    @POST("/app")
//    suspend fun addLang(@Field("dutch") dutch: String,
//                        @Field("engels") engels: String,
//                        @Field("notes") notes: String,
//                        @Field("sentences") sentences: String,): LangObj
}