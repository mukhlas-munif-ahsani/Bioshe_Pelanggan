package com.munifahsan.biosheapp.ui.detailPesanan

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.munifahsan.biosheapp.domain.Orders
import com.munifahsan.biosheapp.domain.User

class Repository(private val mListener: DetailPesananContract.Listener) : DetailPesananContract.Repo {

    val currentUser = Firebase.auth.currentUser!!.uid
    val auth = FirebaseAuth.getInstance()
    private val dbOrders = FirebaseFirestore.getInstance()
        .collection("ORDERS")
    private val dbUsers = FirebaseFirestore.getInstance()
        .collection("USERS")

    override fun getData(orderId:String){
        dbOrders.document(orderId).addSnapshotListener { value, error ->
            val order = value?.toObject(Orders::class.java)

            dbUsers.document(currentUser).addSnapshotListener { valueUsr, error ->
                val user = valueUsr?.toObject(User::class.java)
                mListener.getDataListener(
                    order!!.orderStatus,
                    order.orderDate,
                    order.id,
                    order.kurir,
                    order.resi,
                    order.alamatPengiriman,
                    user!!.nama,
                    user.noHp,
                    order.metodePembayaran,
                    order.ongkir
                )
            }

        }
        showTotalBarangHarga(orderId)
    }

    private fun showTotalBarangHarga(orderId: String) {
        val rev = dbOrders.document(orderId).collection("PRODUCT")
        rev.addSnapshotListener { value, error ->
            val jumlah = ArrayList<Int>()
            var jumlahItem = 0
            var berat = 0
            for (field in value!!) {
                val a = field.getLong("harga")
                val b = field.getLong("jumlahItem")
                val c = field.getLong("diskon")
                val d = field.getLong("berat")
                jumlahItem += b!!.toInt()
                berat += d!!.toInt()*b.toInt()
                val disconNum: Int = c!!.toInt()
                val disconHarga: Int = a!!.toInt() * disconNum / 100
                val harga = a - disconHarga
                jumlah.add(harga.toInt() * b.toInt())
            }

            dbOrders.document(orderId).addSnapshotListener { valueOrdr, error ->
                mListener.totalHargaBarangListener(jumlah.sum(), jumlahItem, valueOrdr!!.getLong("ongkir")!!.toInt(), berat)
            }
        }
    }
}