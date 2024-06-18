package com.mad.vocab.data

import com.mad.vocab.data.models.LangObj
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import java.io.IOException

class LangRepoImpl(
    private val api: Api
): LangRepo {
    override suspend fun getLangList(): Flow<Result<List<LangObj>>> {
        return flow {
            val lang = try {
                api.getLangList()
            } catch (ioe: IOException) {
                println("==========================")
                ioe.printStackTrace()
                emit(com.mad.vocab.data.Result.Error(message = "Error loading LangResponse"))
                return@flow
            } catch (httpe: retrofit2.HttpException) {
                println("-----------------------")
                httpe.printStackTrace()
                emit(com.mad.vocab.data.Result.Error(message = "Error loading LangResponse"))
                return@flow
            } catch (e: Exception) {
                println("*************************")
                e.printStackTrace()
                emit(com.mad.vocab.data.Result.Error(message = "Error loading LangResponse"))
                return@flow
            }

            emit(Result.Success(lang.result))
        }
    }

    override suspend fun getSearchList(search: String): Flow<Result<List<LangObj>>> {
        return flow {
            val lang = try {
                api.getLangList(search)
            } catch (ioe: IOException) {
                println("==========================")
                ioe.printStackTrace()
                emit(com.mad.vocab.data.Result.Error(message = "Error loading LangResponse"))
                return@flow
            } catch (httpe: retrofit2.HttpException) {
                println("-----------------------")
                httpe.printStackTrace()
                emit(com.mad.vocab.data.Result.Error(message = "Error loading LangResponse"))
                return@flow
            } catch (e: Exception) {
                println("*************************")
                e.printStackTrace()
                emit(com.mad.vocab.data.Result.Error(message = "Error loading LangResponse"))
                return@flow
            }
            emit(Result.Success(lang.result))
        }
    }

    override suspend fun addLang(lang: LangObj): Flow<Response<LangObj>> {
        return flow {
            val lang = try {
                api.addLang(lang)
            } catch (ioe: IOException) {
                println("==========================")
                ioe.printStackTrace()
//                emit(com.mad.vocab.data.Result.Error(message = "Error loading LangResponse"))
                return@flow
            } catch (httpe: retrofit2.HttpException) {
                println("-----------------------")
                httpe.printStackTrace()
//                emit(com.mad.vocab.data.Result.Error(message = "Error loading LangResponse"))
                return@flow
            } catch (e: Exception) {
                println("*************************")
                e.printStackTrace()
//                emit(com.mad.vocab.data.Result.Error(message = "Error loading LangResponse"))
                return@flow
            }
            emit(lang)
        }
    }

    override suspend fun updateLang(nlWord: String, lang: LangObj): Flow<Response<LangObj>> {
        return flow {
            val lang = try {
                api.updateLang(nlWord, lang)
            } catch (ioe: IOException) {
                println("==========================")
                ioe.printStackTrace()
//                emit(com.mad.vocab.data.Result.Error(message = "Error loading LangResponse"))
                return@flow
            } catch (httpe: retrofit2.HttpException) {
                println("-----------------------")
                httpe.printStackTrace()
//                emit(com.mad.vocab.data.Result.Error(message = "Error loading LangResponse"))
                return@flow
            } catch (e: Exception) {
                println("*************************")
                e.printStackTrace()
//                emit(com.mad.vocab.data.Result.Error(message = "Error loading LangResponse"))
                return@flow
            }
            emit(lang)
        }
    }
}