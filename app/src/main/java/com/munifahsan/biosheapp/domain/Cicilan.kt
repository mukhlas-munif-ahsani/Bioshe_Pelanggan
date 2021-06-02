package com.munifahsan.biosheapp.domain

import com.google.firebase.firestore.DocumentId
import java.util.*

data class Cicilan(
    @DocumentId
    var id: String = "",
    var urutan: String = "",
    var createdDate:Date? = null,
    var userId: String = "",
    var bunga: Int = 0,
    var jumlahPembayaran: Int = 0,
    val jumlahPembayaranTelat: Int = 0,
    var statusPembayaran: String = "",
    var metodePembayaran: String = "",
    var midtransOrderId: String = "",
    var midtransTransactionId: String = "",
    var show: Boolean = false,
    var dibayar: Boolean = false,
    var biohseOrderId: String = "",
    var mulaiPembayaran: Date? = null,
    var deadLinePembayaran: Date? = null
)