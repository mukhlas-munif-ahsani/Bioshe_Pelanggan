package com.munifahsan.biosheapp.domain

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.munifahsan.biosheapp.vo.Resource
import java.util.*

class OrdersDao {
    val currentUser = Firebase.auth.currentUser
    val auth = FirebaseAuth.getInstance()
    private val dbOrders = FirebaseFirestore.getInstance()
        .collection("ORDERS")

    private val dbProduct = FirebaseFirestore.getInstance()
        .collection("ORDERS")
        .document(currentUser?.uid.toString())
        .collection("PRODUCT")

    fun getOrdersString(id: String, fbVar: String): LiveData<Resource<String>> {
        val data = MutableLiveData<Resource<String>>()

        //get data from firestore
        dbOrders.document(id)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    data.value = Resource.success(it.getString(fbVar))
                } else {
                    Log.d("document", "doesnt exist")
                    data.value = Resource.error("", "ID Sales tidak di temukan")
                }
            }
            .addOnFailureListener {
                data.value = Resource.error("Error", it.message!!)
            }
        return data
    }

    fun postOrders(
        ordersId: String,
        ordersStatus: String,
        kurir: String,
        alamatPengiriman: String,
        userId: String,
        salesId: String,
        metodePembayaran: String
    ): LiveData<Resource<Boolean>> {
        val dataStatus = MutableLiveData<Resource<Boolean>>()

        val ordersData = Orders(
            ordersId,
            Date(),
            ordersStatus,
            kurir,
            "-",
            alamatPengiriman,
            userId,
            salesId,
            metodePembayaran
        )

        dbOrders.document(ordersId)
            .set(ordersData)
            .addOnCompleteListener {
                dataStatus.value = Resource.success(true)
            }
            .addOnFailureListener {
                dataStatus.value = Resource.error(it.message!!, false)
            }
        return dataStatus
    }


}