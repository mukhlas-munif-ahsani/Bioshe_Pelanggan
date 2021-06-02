package com.munifahsan.biosheapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.munifahsan.biosheapp.usecase.UserUseCase
import com.munifahsan.biosheapp.vo.Resource

class MainViewModel(private val userUseCase: UserUseCase) : ViewModel()  {
    fun getKeranjangItemSize(): LiveData<Resource<Int>> {
        val dataStatus = MutableLiveData<Resource<Int>>()
        userUseCase.getKeranjangItemSize().observeForever{
            dataStatus.postValue(it)
        }
        return dataStatus
    }
}