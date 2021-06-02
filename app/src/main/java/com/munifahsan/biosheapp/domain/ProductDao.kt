package com.munifahsan.biosheapp.domain

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.munifahsan.biosheapp.vo.Resource

class ProductDao {
    val currentUser = Firebase.auth.currentUser
    val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
        .collection("PRODUCT")

    fun getProductData(productIdId: String, fbVar: String): LiveData<Resource<String>> {
        val data = MutableLiveData<Resource<String>>()

        //get data from firestore
        db.document(productIdId)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    data.value = Resource.success(it.getString(fbVar))
                } else {
                    Log.d("document", "dosnt exist")
                    data.value = Resource.error("", "ID Sales tidak di temukan fsdfasgaef")
                }
            }
            .addOnFailureListener {
                data.value = Resource.error("Error", it.message!!)
            }
        return data
    }

    fun getProductDataInt(productIdId: String, fbVar: String): LiveData<Resource<Long>> {
        val data = MutableLiveData<Resource<Long>>()

        //get data from firestore
        db.document(productIdId)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    val money: Long? = it.getLong(fbVar)
                    data.value = Resource.success(it.getLong(fbVar))
                } else {
                    Log.d("document", "dosnt exist")
                    data.value = Resource.error("", 0)
                }
            }
            .addOnFailureListener {
                data.value = Resource.error(it.message!!, 0)
            }
        return data
    }


}