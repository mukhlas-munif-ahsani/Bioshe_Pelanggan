package com.munifahsan.biosheapp.ui.transferBayar

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class Repository(private val mListener: Contract.Listener) : Contract.Repository {

    val currentUser = Firebase.auth.currentUser
    val auth = FirebaseAuth.getInstance()
    private val dbOrders = FirebaseFirestore.getInstance()
        .collection("ORDERS")
    private val dbUsers = FirebaseFirestore.getInstance()
        .collection("USERS")

    override fun getData(id: String) {
        showTotalBarangHarga(id)
    }

    override fun uploadImage(imageUri: Uri?, orderId: String) {
        val fileReference: StorageReference =
            FirebaseStorage.getInstance().reference.child("orderImages/" + orderId + "/" + imageUri!!.lastPathSegment)
        fileReference
            .putFile(imageUri)
            .addOnSuccessListener {
                // download uploaded image url
                fileReference
                    .downloadUrl.addOnSuccessListener {
                        val data = hashMapOf(
                            "imageUrl" to it.toString()
                        )
                        dbOrders.document(orderId)
                            .collection("BUKTI_PEMBAYARAN").document()
                            .set(data).addOnCompleteListener {s->
                                if (s.isSuccessful){
                                    mListener.uploadImageSuccessListener()

                                    dbOrders.document(orderId).update("orderStatus", "MENUNGGU KONFIRMASI")
                                }
                        }
                    }
            }
            .addOnProgressListener {
                val progress = (100.0 * it.bytesTransferred / it.totalByteCount)
                mListener.uploadImageListener("uploading..", progress)
            }
    }

    private fun showTotalBarangHarga(orderId: String) {
        val rev = dbOrders.document(orderId).collection("PRODUCT")
        rev.addSnapshotListener { value, error ->
            val jumlah = ArrayList<Int>()
            var jumlahItem = 0
            var berat = 0
            for (field in value!!) {
                val a = field.getLong("hargaProduct")
                val b = field.getLong("jumlahItem")
                val c = field.getLong("disconProduct")
                val d = field.getLong("berat")
                jumlahItem += b!!.toInt()
                berat += d!!.toInt() * b.toInt()
                val disconNum: Int = c!!.toInt()
                val disconHarga: Int = a!!.toInt() * disconNum / 100
                val harga = a - disconHarga
                jumlah.add(harga.toInt() * b.toInt())
            }

            dbOrders.document(orderId).addSnapshotListener { valueOrdr, error ->
                mListener.getDataListener(
                    jumlah.sum().plus(valueOrdr!!.getLong("ongkir")!!.toInt()).toString()
                )
                //mListener.totalHargaBarangListener(jumlah.sum(), jumlahItem, valueOrdr!!.getLong("ongkir")!!.toInt(), berat)
            }
        }
    }
}