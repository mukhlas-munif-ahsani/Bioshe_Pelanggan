package com.munifahsan.biosheapp.ui.detailProduk

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.munifahsan.biosheapp.usecase.UserUseCase
import com.munifahsan.biosheapp.vo.Resource

class DetailProductViewModel(private val userUseCase: UserUseCase) : ViewModel() {
    fun getProductData(userId:String, fbVar:String): LiveData<Resource<String>> {
        val dataStatus = MutableLiveData<Resource<String>>()
        userUseCase.getProductData(userId, fbVar).observeForever{
            dataStatus.postValue(it)
        }
        return dataStatus
    }

    fun getProductDataInt(userId:String, fbVar:String): LiveData<Resource<Long>> {
        val dataStatus = MutableLiveData<Resource<Long>>()
        userUseCase.getProductDataInt(userId, fbVar).observeForever{
            dataStatus.postValue(it)
        }
        return dataStatus
    }

    fun getKeranjangItemSize(): LiveData<Resource<Int>> {
        val dataStatus = MutableLiveData<Resource<Int>>()
        userUseCase.getKeranjangItemSize().observeForever{
            dataStatus.postValue(it)
        }
        return dataStatus
    }

    fun postKeranjang(productId:String):LiveData<Resource<Boolean>>{
        val dataStatus = MutableLiveData<Resource<Boolean>>()
        userUseCase.postKeranjang(productId).observeForever{
            dataStatus.postValue(it)
        }
        return dataStatus
    }
}