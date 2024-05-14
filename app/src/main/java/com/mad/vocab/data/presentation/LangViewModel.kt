package com.mad.vocab.data.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mad.vocab.data.LangRepo
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
    val lan = _lang.asStateFlow()

    private val _showErrorToast = Channel<Boolean>()
    val showErrorToast = _showErrorToast.receiveAsFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    init {
        refreshAndLoad()
    }

    fun refreshAndLoad() {
        viewModelScope.launch {

            _isRefreshing.value = true
            delay(2000L)
            _isRefreshing.value = false

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
    }

//    private suspend fun getLang() {
//        _lang.value = RetroInstance.api.getLangList().toString();
//    }

}