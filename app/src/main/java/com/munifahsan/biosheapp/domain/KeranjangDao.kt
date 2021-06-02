package com.munifahsan.biosheapp.domain

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.munifahsan.biosheapp.vo.Resource
import java.util.*

class KeranjangDao {
    val currentUser = Firebase.auth.currentUser
    val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
        .collection("USERS")
        .document(currentUser?.uid.toString())
        .collection("KERANJANG")

    private val dbUser = FirebaseFirestore.getInstance()
        .collection("USERS")
        .document(currentUser?.uid.toString())

    private val dbProduct = FirebaseFirestore.getInstance()
        .collection("PRODUCT")

    fun getKeranjangData(productIdId: String, fbVar: String): LiveData<Resource<String>> {
        val data = MutableLiveData<Resource<String>>()

        //get data from firestore
        db.document(productIdId)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    data.value = Resource.success(it.getString(fbVar))
                } else {
                    Log.d("document", "dosnt exist")
                    data.value = Resource.error("", "ID Sales tidak di temukan gghgjjhftftfhfhtfh")
                }
            }
            .addOnFailureListener {
                data.value = Resource.error("Error", it.message!!)
            }
        return data
    }

    fun editKeranjangItemInt(
        itemKeranjangId: String,
        fbVar: String,
        fbValue: Int
    ): LiveData<Resource<String>> {
        val dataStatus = MutableLiveData<Resource<String>>()

        db.document(itemKeranjangId)
            .update(fbVar, fbValue)
            .addOnSuccessListener {
                dataStatus.value = Resource.success("updated")
            }
            .addOnFailureListener {
                dataStatus.value = Resource.error("Error", it.message)
            }

        return dataStatus
    }

    fun editKeranjangItemString(
        itemKeranjangId: String,
        fbVar: String,
        fbValue: String
    ): LiveData<Resource<String>> {
        val dataStatus = MutableLiveData<Resource<String>>()

        db.document(itemKeranjangId)
            .update(fbVar, fbValue)
            .addOnSuccessListener {
                dataStatus.value = Resource.success("updated")
            }
            .addOnFailureListener {
                dataStatus.value = Resource.error("Error", it.message)
            }

        return dataStatus
    }

    fun deleteKeranjangItem(
        itemKeranjangId: String
    ): LiveData<Resource<String>> {
        val dataStatus = MutableLiveData<Resource<String>>()

        db.document(itemKeranjangId)
            .delete()
            .addOnSuccessListener {
                dataStatus.value = Resource.success("deleted")
            }
            .addOnFailureListener {
                dataStatus.value = Resource.error("Error", it.message)
            }

        return dataStatus
    }

    fun postKeranjang(productId: String): LiveData<Resource<Boolean>> {
        val dataStatus = MutableLiveData<Resource<Boolean>>()

        db.whereEqualTo("productId", productId)
            .get()
            .addOnCompleteListener {
            if (it.result!!.isEmpty) {

                // post product to keranjang
                val keranjang = Keranjang("", productId, Timestamp(Date()), 1, "", "", 0, 0, 0)
                db.document()
                    .set(keranjang)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            dataStatus.value = Resource.success(true)
                        } else {
                            dataStatus.value =
                                Resource.error(task.exception?.message!!, false)
                        }
                    }
                    .addOnFailureListener { exception ->
                        dataStatus.value =
                            Resource.error(exception.message!!, false)
                    }

                //get product harga
                dbProduct.document(productId).get().addOnSuccessListener {productData->
                    val harga = productData.getLong("hargaProduct")
                    val discon = productData.getLong("disconProduct")

                    val dis: Long = harga!! * discon!! / 100

                    //update total harga and barang
                    dbUser.get()
                        .addOnSuccessListener { keranjangData->
                            val keranjangTotalHarga = keranjangData.getLong("keranjangTotalHarga")
                            val keranjangTotalBarang = keranjangData.getLong("keranjangTotalBarang")

                            dbUser.update("keranjangTotalHarga", keranjangTotalHarga?.plus(harga - dis))
                            dbUser.update("keranjangTotalBarang", keranjangTotalBarang?.plus(1))
                        }

                }

            }
            if (!it.result!!.isEmpty) {
                dataStatus.value = Resource.error("Product sudah ada di keranjang", false)
            }
        }
        return dataStatus
    }

    fun getKeranjangItemSize(): LiveData<Resource<Int>> {
        val dataStatus = MutableLiveData<Resource<Int>>()

        db.addSnapshotListener { value, error ->
            if (value != null){
                dataStatus.value = Resource.success(value.size())
            }
        }
//        db.get().addOnSuccessListener {
//            dataStatus.value = Resource.success(it.size())
//        }

        return dataStatus
    }
}