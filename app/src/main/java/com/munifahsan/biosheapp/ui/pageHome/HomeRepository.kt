package com.munifahsan.biosheapp.ui.pageHome

import com.google.firebase.Timestamp
import com.munifahsan.biosheapp.utils.Constants
import com.munifahsan.biosheapp.domain.Keranjang
import java.util.*

class HomeRepository(val mListener: HomeContract.Listener): HomeContract.Repository {

    override fun postKeranjang(productId: String){
        Constants.KERANJANG_DB.whereEqualTo("productId", productId)
            .get()
            .addOnCompleteListener {
                if (it.result!!.isEmpty) {

                    // post product to keranjang
                    val keranjang = Keranjang("", productId, Timestamp(Date()), 1, "", "", 0, 0, 0)
                    Constants.KERANJANG_DB.document()
                        .set(keranjang)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                mListener.postKeranjangListener("Produk ditambahkan")
                            } else {
                                mListener.postKeranjangListener(task.exception?.message!!)
                            }
                        }
                        .addOnFailureListener { exception ->
                            mListener.postKeranjangListener(exception.message!!)
                        }

                    //get product harga
                    Constants.PRODUK_DB.document(productId).get().addOnSuccessListener { productData->
                        val harga = productData.getLong("harga")
                        val discon = productData.getLong("diskon")

                        val dis: Long = harga!! * discon!! / 100

                        //update total harga and barang
                        Constants.USER_DB.document(Constants.CURRENT_USER_ID.toString()).get()
                            .addOnSuccessListener { keranjangData->
                                val keranjangTotalHarga = keranjangData.getLong("keranjangTotalHarga")
                                val keranjangTotalBarang = keranjangData.getLong("keranjangTotalBarang")

                                Constants.USER_DB.document(Constants.CURRENT_USER_ID.toString()).update("keranjangTotalHarga", keranjangTotalHarga?.plus(harga - dis))
                                Constants.USER_DB.document(Constants.CURRENT_USER_ID.toString()).update("keranjangTotalBarang", keranjangTotalBarang?.plus(1))
                            }

                    }

                }
                if (!it.result!!.isEmpty) {
                    mListener.postKeranjangListener("Produk sudah ada di keranjang")
                }
            }
    }
}