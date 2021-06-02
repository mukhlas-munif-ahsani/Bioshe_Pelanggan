package com.munifahsan.biosheapp.usecase

import android.net.Uri
import androidx.lifecycle.LiveData
import com.munifahsan.biosheapp.domain.KeranjangDao
import com.munifahsan.biosheapp.domain.OrdersDao
import com.munifahsan.biosheapp.domain.ProductDao
import com.munifahsan.biosheapp.domain.UserDao
import com.munifahsan.biosheapp.vo.Resource

class UserUseCase {
    private val userDao = UserDao()
    private val keranjangDao = KeranjangDao()
    private val productDao = ProductDao()
    private val ordersDao = OrdersDao()

    //USERS
    fun getUserData(userId: String, fbVar: String): LiveData<Resource<String>> {
        return userDao.getUserData(userId, fbVar)
    }

    fun getUserDataInt(userId: String, fbVar: String): LiveData<Resource<Long>> {
        return userDao.getUserDataInt(userId, fbVar)
    }

    fun editKeranjangData(fbVar: String, fbValue: Int): LiveData<Resource<String>> {
        return userDao.editKeranjangData(fbVar, fbValue)
    }

    fun registerAndPostUser(
        email: String,
        pass: String,
        level: String,
        imageUri: Uri,
        nik: String,
        nama: String,
        gender: String,
        alamatRumah: String,
        noHp: String,
        noWa: String,
        ahliWaris: String,
        namaOutlet: String,
        alamatOutlet: String,
        idSales: String
    ): LiveData<Resource<Boolean>> {
        return userDao.registerAndPostUser(
            email,
            pass,
            level,
            imageUri,
            nik,
            nama,
            gender,
            alamatRumah,
            noHp,
            noWa,
            ahliWaris,
            namaOutlet,
            alamatOutlet,
            idSales
        )
    }

    fun login(email: String, pass: String): LiveData<Resource<Boolean>> {
        return userDao.login(email, pass)
    }

    //KERANJANG
    fun getKeranjangData(productId: String, fbVar: String): LiveData<Resource<String>> {
        return keranjangDao.getKeranjangData(productId, fbVar)
    }

    fun editKeranjangItemInt(
        itemKeranjangId: String,
        fbVar: String,
        fbValue: Int
    ): LiveData<Resource<String>> {
        return keranjangDao.editKeranjangItemInt(itemKeranjangId, fbVar, fbValue)
    }

    fun editKeranjangItemString(
        itemKeranjangId: String,
        fbVar: String,
        fbValue: String
    ): LiveData<Resource<String>> {
        return keranjangDao.editKeranjangItemString(itemKeranjangId, fbVar, fbValue)
    }

    fun deleteKeranjangItem(itemKeranjangId: String): LiveData<Resource<String>> {
        return keranjangDao.deleteKeranjangItem(itemKeranjangId)
    }

    fun postKeranjang(productId: String): LiveData<Resource<Boolean>> {
        return keranjangDao.postKeranjang(productId)
    }

    fun getKeranjangItemSize(): LiveData<Resource<Int>> {
        return keranjangDao.getKeranjangItemSize()
    }

    //PRODUCT
    fun getProductData(productId: String, fbVar: String): LiveData<Resource<String>> {
        return productDao.getProductData(productId, fbVar)
    }

    fun getProductDataInt(productId: String, fbVar: String): LiveData<Resource<Long>> {
        return productDao.getProductDataInt(productId, fbVar)
    }

    //ORDERS
    fun postOrders(
        ordersId: String,
        ordersStatus: String,
        kurir: String,
        alamatPengiriman: String,
        userId: String,
        salesId: String, metodePembayaran: String
    ): LiveData<Resource<Boolean>> {
        return ordersDao.postOrders(
            ordersId,
            ordersStatus,
            kurir,
            alamatPengiriman,
            userId,
            salesId,
            metodePembayaran
        )
    }
}