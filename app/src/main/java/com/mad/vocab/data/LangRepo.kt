package com.mad.vocab.data

import com.mad.vocab.data.models.Lang
import com.mad.vocab.data.models.LangObj
import kotlinx.coroutines.flow.Flow

interface LangRepo {
    suspend fun getLangList(): Flow<Result<List<LangObj>>>
}