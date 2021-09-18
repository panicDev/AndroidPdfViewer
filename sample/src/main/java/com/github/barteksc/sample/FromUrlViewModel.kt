package com.github.barteksc.sample

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL

class FromUrlViewModel(application: Application) : AndroidViewModel(application) {

    private val dataFlow: MutableStateFlow<DataState<InputStream>> =
        MutableStateFlow(DataState.idle())
    val dataStateFlow = dataFlow.asStateFlow()

    fun loadPdf(url: String = "http://www.africau.edu/images/default/sample.pdf") {
        viewModelScope.launch {
            dataFlow.emit(DataState.loading())
            runCatching {
                withContext(Dispatchers.IO) {
                    URL(url).openStream()
                }
            }.onFailure {
                dataFlow.emit(DataState.error(it.message))
            }.onSuccess {
                dataFlow.emit(DataState.success(it))
            }
        }
    }

    sealed class DataState<out T> {
        data class Success<out T>(val data: T) : DataState<T>()
        data class Error(val exception: String?) : DataState<Nothing>()
        object Loading : DataState<Nothing>()
        object Idle : DataState<Nothing>()

        companion object {

            /**
             * Returns [DataState.Idle] instance.
             */
            fun idle() = Idle

            /**
             * Returns [DataState.Loading] instance.
             */
            fun loading() = Loading

            /**
             * Returns [DataState.Success] instance.
             * @param data Data to emit with status.
             */
            fun <T> success(data: T) =
                Success(data)

            /**
             * Returns [DataState.Error] instance.
             * @param message Description of failure.
             */
            fun error(message: String?) = Error(message)
        }
    }
}