package com.munifahsan.biosheapp.domain

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Orders(
    @DocumentId
    var id: String = "",
    @ServerTimestamp
    var orderDate: Date? = null,
    var orderStatus:String = "",
    var kurir: String = "",
    var resi: String = "",
    var alamatPengiriman: String = "",
    var userId: String = "",
    var salesId: String = "",
    var metodePembayaran: String = "",
    var ongkir: Int = 0
)
