package com.example.a3dmodelsample

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.a3dmodelsample.retrofit.GracenoteRepository
import com.example.a3dmodelsample.retrofit.data.Program
import com.example.a3dmodelsample.retrofit.data.ProgramBundle

class ProgramListViewModel(private val repository: GracenoteRepository) : ViewModel() {

    private val _bundles = MutableLiveData<List<ProgramBundle>>(emptyList())
    val bundles: LiveData<List<ProgramBundle>> get() = _bundles

    private val _selectedPrograms = MutableLiveData<List<Program>>(emptyList())
    val selectedPrograms: LiveData<List<Program>> get() = _selectedPrograms

    private val _videoLink = MutableLiveData<List<String>>(emptyList())
    val videoLink: LiveData<List<String>> get() = _videoLink

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> get() = _error

    fun loadBundles() {
        Log.d("mjpark", "loadBundles 실행됨")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val data = repository.fetchProgramBundles()
                _bundles.value = data
                _error.value = null
                Log.d("mjpark","data : $data")
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Unknown error"
                Log.d("mjpark","e : $e")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadProgram(programId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val data = repository.fetchProgramById(programId)
                _selectedPrograms.value = data
                _error.value = null
                Log.d("mjpark","data : $data")
            } catch (e: Exception) {
                _error.value = e.localizedMessage
                Log.d("mjpark","e : $e")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadProgramUrls() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val urls = _selectedPrograms.value
                    ?.flatMap { program ->
                        program.availableOn.orEmpty().flatMap { avail -> avail.URLs.orEmpty() }
                            .map { urlObj -> urlObj.URL }.filter { it.startsWith("https://") }
                    } ?: emptyList()

                _videoLink.value = urls
                Log.d("mjpark", "urls : $urls")
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Unknown error"
                Log.d("mjpark", "e : $e")
            } finally {
                _isLoading.value = false
            }
        }

    }

    fun clearSelectedPrograms() {
        _selectedPrograms.value = emptyList()
    }
}

class ProgramListViewModelFactory(private val repository: GracenoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProgramListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProgramListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}