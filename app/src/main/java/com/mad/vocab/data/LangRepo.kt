package com.mad.vocab.data

import com.mad.vocab.data.models.Lang
import com.mad.vocab.data.models.LangObj
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface LangRepo {
    suspend fun getLangList(): Flow<Result<List<LangObj>>>

    suspend fun addLang(lang: LangObj): Flow<Response<LangObj>>
}