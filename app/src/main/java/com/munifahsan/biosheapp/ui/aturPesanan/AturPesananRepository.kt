package com.munifahsan.biosheapp.ui.aturPesanan

import android.content.Intent
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.ktx.Firebase
import com.google.firestore.v1.DocumentTransform
import com.munifahsan.biosheapp.domain.Cicilan
import com.munifahsan.biosheapp.domain.PaymentHistory
import com.munifahsan.biosheapp.domain.Product
import com.munifahsan.biosheapp.ui.detailPesanan.DetailPesananActivity
import com.munifahsan.biosheapp.utils.Constants
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AturPesananRepository(val mListener: AturPesananContract.Listener) :
    AturPesananContract.Repository {
    private val dbUsers = FirebaseFirestore.getInstance()
        .collection("USERS")
    private val dbOrders = FirebaseFirestore.getInstance()
        .collection("ORDERS")
    private val dbBioshe = FirebaseFirestore.getInstance()
        .collection("BIOSHE")
    private val currentUserId = Firebase.auth.currentUser!!.uid

    val productIdList = ArrayList<String>()

    override fun createAndGetData() {

        //ambil data user
        dbUsers.document(currentUserId).get().addOnSuccessListener {
            if (it.exists()) {
                val orderNumber = it.getLong("jumlahPesanan")?.toInt()
                val alamatPengiriman = it.getString("alamatOutlet").toString()
                val salesId = it.getString("salesId").toString()
                val userName = it.getString("nama").toString()
                val noHp = it.getString("noHp").toString()
                val email = it.getString("email").toString()

                mListener.getDataListener(alamatPengiriman, userName, noHp, salesId, email)

                create(orderNumber, alamatPengiriman, salesId)
            }
        }

        //get loyalti
        getLoyalti()
    }

    private fun getProductIdList(orderNumber: Int?) {

        dbOrders.document(currentUserId + orderNumber).collection("PRODUCT").get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    for (field in result) {
                        val productId = field.id
                        Log.d("PRODUCT", "id : ${field.id}")
                        productIdList.add(productId)
                        Log.d("PRODUCT", "lst : $productIdList")
                    }
                } else {
                    Log.d("PRODUCT", "kosong")
                }

            }
    }

    private fun getLoyalti() {
        dbBioshe.document("LOYALTI").get().addOnCompleteListener { loyalti ->
            dbUsers.document(currentUserId).get().addOnCompleteListener { user ->
                mListener.getLoyaltiListener(
                    loyalti.result!!.getLong("silver")!!.toInt(),
                    loyalti.result!!.getLong("gold")!!.toInt(),
                    loyalti.result!!.getLong("platinum")!!.toInt(),
                    loyalti.result!!.getLong("diamond")!!.toInt(),
                    user.result!!.getString("loyalti")!!
                )
            }
        }
    }

    private fun create(orderNumber: Int?, alamatPengiriman: String, salesId: String) {

        val orderData = hashMapOf(
            "orderDate" to Date(),
            "orderStatus" to "",
            "kurir" to "",
            "resi" to "",
            "alamatPengiriman" to alamatPengiriman,
            "userId" to currentUserId,
            "salesId" to salesId,
            "metodePembayaran" to "",
            "ongkir" to 0,
            "membayar" to false
        )

        dbOrders.document(currentUserId + orderNumber).set(orderData)
            .addOnCompleteListener {

                getKeranjangData(orderNumber)

                //tell view, order has been created
                mListener.orderCreatedListener(currentUserId + orderNumber)


            }.addOnFailureListener {
                mListener.showError(it.message.toString())
            }

    }

    private fun getKeranjangData(orderNumber: Int?) {

        //get keranjang data
        val query = FirebaseFirestore.getInstance().collection("USERS")
            .document(currentUserId)
            .collection("KERANJANG")
        query.get().addOnCompleteListener { Task ->
            for (documentSnapshot: QueryDocumentSnapshot in Task.result!!) {
                val productId =
                    documentSnapshot.getString("productId")

                //post keranjang data to orders
                val data = mapOf(
                    "productId" to productId.toString(),
                    "thumbnail" to documentSnapshot.getString("thumbnail"),
                    "nama" to documentSnapshot.getString("nama"),
                    "harga" to documentSnapshot.getLong("harga")!!
                        .toInt(),
                    "diskon" to documentSnapshot.getLong("diskon")!!
                        .toInt(),
                    "jumlahItem" to documentSnapshot.getLong("jumlahItem")!!
                        .toInt(),
                    "berat" to documentSnapshot.getLong("berat")!!
                        .toInt()
                )
                dbOrders
                    .document(currentUserId + orderNumber)
                    .collection("PRODUCT").document().set(
                        data
                    ).addOnCompleteListener {
                        getProductIdList(orderNumber)
                    }
            }

//            Product(
//                productId.toString(),
//                documentSnapshot.getString("nama")
//                    .toString(),
//                documentSnapshot.getString("thumbnail")
//                    .toString(),
//                "",
//                documentSnapshot.getLong("harga")!!
//                    .toInt(),
//                documentSnapshot.getLong("diskon")!!
//                    .toInt(),
//                documentSnapshot.getLong("jumlahItem")!!
//                    .toInt(),
//                documentSnapshot.getLong("berat")!!
//                    .toInt()
//            )

            //increase USERS jumlah pesanan
            dbUsers
                .document(currentUserId)
                .update(
                    "jumlahPesanan",
                    FieldValue.increment(1)
                ).addOnCompleteListener {

                }

        }
    }

    override fun deleteOrder() {

        //ambil data user
        dbUsers.document(currentUserId).get().addOnSuccessListener { user ->
            val orderNumber = user.getLong("jumlahPesanan")!!.toInt().minus(1)


            dbOrders.document(currentUserId + orderNumber).get().addOnSuccessListener { order ->
                val membayar = order.getBoolean("membayar")

                if (membayar == false) {
                    for (productId in productIdList) {
                        dbOrders.document(currentUserId + orderNumber).collection("PRODUCT")
                            .document(productId).delete()
                    }

                    dbOrders.document(currentUserId + orderNumber).delete()
                    //increase USERS jumlah pesanan
                    dbUsers
                        .document(currentUserId)
                        .update(
                            "jumlahPesanan",
                            FieldValue.increment(-1)
                        )
                }
            }
        }
    }

    override fun updateOrder(
        midtransOrderId: String,
        midtransTransactionId: String,
        membayar: Boolean,
        dibayar: Boolean,
        show: Boolean,
        metodePembayaran: String,
        statusPembayaran: String,
        kurirPengirim: String,
        jumlahPembayaran: Int
    ) {
        dbUsers.document(currentUserId).get().addOnSuccessListener {
            val orderNumber = it.getLong("jumlahPesanan")!!.toInt().minus(1)

            Log.d("order number : ", orderNumber.toString())

            dbOrders.document(currentUserId + orderNumber).update(
                "midtransOrderId", midtransOrderId,
                "midtransTransactionId", midtransTransactionId,
                "membayar", membayar,
                "dibayar", dibayar,
                "diproses", false,
                "dikirim", false,
                "selesaiDikirim", false,
                "selesaiDiterima", false,
                "show", show,
                "metodePembayaran", metodePembayaran,
                "orderStatus", statusPembayaran,
                "kurir", kurirPengirim
            ).addOnCompleteListener {
                if (dibayar) {
                    createPaymentHistory(
                        currentUserId + orderNumber,
                        midtransOrderId,
                        midtransTransactionId,
                        currentUserId,
                        "User $currentUserId telah sukses melakukan pembayaran untuk pesanan $currentUserId$orderNumber",
                        jumlahPembayaran
                    )
                    val point:Int = jumlahPembayaran / 100000
                    Constants.USER_DB.document(currentUserId).update("bioshePoints", FieldValue.increment(point.toDouble()))
                }
            }

        }
    }

    private fun createPaymentHistory(
        biosheOrderId: String,
        midtransOrderId: String,
        midtransTransactionId: String,
        userId: String,
        keterangan: String,
        jumlahPembayaran: Int
    ) {
        val data = PaymentHistory(biosheOrderId, midtransOrderId, midtransTransactionId, userId, keterangan, jumlahPembayaran)
        Constants.PAYMENT_HISTORY_DB.document().set(data)
    }

    override fun createCicilan(
        jumlahCicilan: Int,
        jumlahPembayaran: Int,
        biosheOrderId: String,
        kurirPengirim: String
    ) {

        when (jumlahCicilan) {
            1 -> {
                val hargaCicilan = jumlahPembayaran + (jumlahPembayaran * 5 / 100)
                val cicilan = Cicilan(
                    "",
                    "PERTAMA",
                    Date(),
                    currentUserId,
                    5,
                    hargaCicilan,
                    0,
                    "",
                    "",
                    "",
                    "",
                    true,
                    dibayar = false,
                    biohseOrderId = biosheOrderId,
                    mulaiPembayaran = addOneMonthCalendar(Date(), 1),
                    deadLinePembayaran = addOneMonthCalendar(Date(), 2)
                )
                dbUsers.document(currentUserId).collection("TAGIHAN").document().set(cicilan)
                    .addOnSuccessListener {
                        mListener.createCicilanListener()
                    }
            }
            2 -> {
                val hargaCicilan = (jumlahPembayaran + (jumlahPembayaran * 10 / 100)) / 2
                var urutan = ""
                for (i in 1..2) {
                    if (i == 1) {
                        urutan = "PERTAMA"
                    } else if (i == 2) {
                        urutan = "KEDUA"
                    }
                    val cicilan = Cicilan(
                        "",
                        urutan,
                        Date(),
                        currentUserId,
                        5,
                        hargaCicilan,
                        0,
                        "",
                        "",
                        "",
                        "",
                        true,
                        dibayar = false,
                        biohseOrderId = biosheOrderId,
                        mulaiPembayaran = addOneMonthCalendar(Date(), i),
                        deadLinePembayaran = addOneMonthCalendar(Date(), i + 1)
                    )
                    dbUsers.document(currentUserId).collection("TAGIHAN").document().set(cicilan)
                }
                mListener.createCicilanListener()
            }
            3 -> {
                val hargaCicilan = (jumlahPembayaran + (jumlahPembayaran * 15 / 100)) / 3
                var urutan = ""
                for (i in 1..3) {
                    when (i) {
                        1 -> {
                            urutan = "PERTAMA"
                        }
                        2 -> {
                            urutan = "KEDUA"
                        }
                        3 -> {
                            urutan = "KETIGA"
                        }
                    }
                    val cicilan = Cicilan(
                        "",
                        urutan,
                        Date(),
                        currentUserId,
                        5,
                        hargaCicilan,
                        0,
                        "",
                        "",
                        "",
                        "",
                        true,
                        dibayar = false,
                        biohseOrderId = biosheOrderId,
                        mulaiPembayaran = addOneMonthCalendar(Date(), i),
                        deadLinePembayaran = addOneMonthCalendar(Date(), i + 1)
                    )
                    dbUsers.document(currentUserId).collection("TAGIHAN").document().set(cicilan)
                }
                mListener.createCicilanListener()
            }
        }

        updateOrder(
            "",
            "",
            true,
            dibayar = true,
            show = true,
            metodePembayaran = "TEMPO",
            statusPembayaran = "DICICIL",
            kurirPengirim = kurirPengirim,
            jumlahPembayaran
        )
    }

    @Throws(ParseException::class)
    fun addOneDayCalendar(date: Date?): Date? {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val c = Calendar.getInstance()
        c.time = date
        c.add(Calendar.MONTH, 1)
        return c.time
    }

    @Throws(ParseException::class)
    fun addOneMonthCalendar(date: Date?, m: Int): Date? {
        val c = Calendar.getInstance()
        c.time = date
        c.add(Calendar.MONTH, m)
        return c.time
    }

}