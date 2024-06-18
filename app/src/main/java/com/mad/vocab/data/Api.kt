package com.mad.vocab.data

import com.mad.vocab.data.models.Lang
import com.mad.vocab.data.models.LangObj
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface Api {
    @GET("/app?page=1&limit=1000")
    suspend fun getLangList(): Lang

    @GET("/app/search")
    suspend fun getLangList(@Query("txt") search: String): Lang

    @POST("/app")
    suspend fun addLang(@Body langBody: LangObj): Response<LangObj>

    @PUT("/app")
    suspend fun updateLang(@Query("nl") nlWord: String, @Body langBody: LangObj): Response<LangObj>

//    @FormUrlEncoded
//    @Headers("Content-Type: application/x-www-form-urlencoded")
//    @POST("/app")
//    suspend fun addLang(@Field("dutch") dutch: String,
//                        @Field("engels") engels: String,
//                        @Field("notes") notes: String,
//                        @Field("sentences") sentences: String,): LangObj
}