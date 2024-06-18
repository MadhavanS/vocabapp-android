package com.mad.vocab.data.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mad.vocab.data.LangRepo
import com.mad.vocab.data.RetroInstance
import com.mad.vocab.data.models.LangObj
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LangViewModel(
    private val langRepo: LangRepo
): ViewModel() {
//    private val _lang = MutableLiveData("No data")
//    val lan: LiveData<String> get() = _lang

    private val _lang = MutableStateFlow<List<LangObj>>(emptyList())
    val lan = _lang

    private val _showErrorToast = Channel<Boolean>()
    val showErrorToast = _showErrorToast.receiveAsFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    private val _postResponse = MutableStateFlow<LangObj?>(null)
    val postResponse: StateFlow<LangObj?> = _postResponse

    private val _updateStatus = MutableStateFlow<String?>(null)
    val updateStatus: StateFlow<String?> = _updateStatus

    init {
        refreshAndLoad()
    }

    fun refreshAndLoad() {
        viewModelScope.launch {

            _isRefreshing.value = true
            delay(2000L)
            _isRefreshing.value = false

            getVocab()
        }
    }

    fun create(post: LangObj) {
        viewModelScope.launch {
            try {
                val response = RetroInstance.api.addLang(post).body()
                _postResponse.value = response
                refreshAndLoad()
            } catch (e: Exception) {}
        }
    }

    fun update(nlWord: String, post: LangObj) {
        viewModelScope.launch {
            try {
                val response = RetroInstance.api.updateLang(nlWord, post).body()
                _postResponse.value = response
                refreshAndLoad()
                _updateStatus.value = "Updated $nlWord"
            } catch (e: Exception) {
                _updateStatus.value = "Update failed: ${e.message}"
            }
        }
    }

    suspend fun getVocab() {
        langRepo.getLangList().collectLatest {
                result ->
            when(result) {
                is com.mad.vocab.data.Result.Error -> {
                    _showErrorToast.send(true)
                }
                is com.mad.vocab.data.Result.Success -> {
                    result.data?.let {langs ->
                        _lang.update { langs }
                    }
                }
            }
        }
    }

    fun search(searchTxt: String) {
        viewModelScope.launch {
            try {
                langRepo.getSearchList(searchTxt).collectLatest { result ->
                    when (result) {
                        is com.mad.vocab.data.Result.Error -> {
                            _showErrorToast.send(true)
                        }
                        is com.mad.vocab.data.Result.Success -> {
                            if(result.data?.isEmpty() == true) {
                                _updateStatus.value = "No results found for $searchTxt"
                                refreshAndLoad()
                                return@collectLatest
                            }
                            result.data?.let { langs ->
                                _lang.update { langs }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _updateStatus.value = "Update failed: ${e.message}"
            }
        }
    }

    fun clearUpdateStatus() {
        _updateStatus.value = null
    }
}