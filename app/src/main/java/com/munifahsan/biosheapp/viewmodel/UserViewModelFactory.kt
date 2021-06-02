package com.munifahsan.biosheapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.munifahsan.biosheapp.usecase.UserUseCase

class UserViewModelFactory(val userUseCase: UserUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(UserUseCase::class.java).newInstance(userUseCase)
    }
}
