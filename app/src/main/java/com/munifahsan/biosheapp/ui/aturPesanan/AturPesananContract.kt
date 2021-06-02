package com.munifahsan.biosheapp.ui.aturPesanan

interface AturPesananContract {
    interface View{

        fun setAlamatOutlet(alamat: String)
        fun setName(name: String)
        fun setNoHp(nohp: String)
        fun setSales(id: String)
        fun showMessage(msg: String)
        fun showContent()
        fun hideContent()
        fun setOrderId(orderId: String)
        fun startTagihanActivity()
        fun setLoyalti(silver: Int, gold: Int, platinum: Int, diamond: Int, myLoyalti: String)
        fun setEmailBioshe(email: String)
    }
    interface Presenter{

        fun createAndGet()
        fun deleteOrder()
        fun updateOrder(
            midtransOrderId: String,
            midtransTransactionId: String,
            membayar: Boolean,
            dibayar: Boolean,
            show: Boolean,
            metodePembayaran: String,
            statusPembayaran: String,
            kurirPengirim: String,
            jumlahPembayaran: Int
        )
        fun createCicilan(jumlahCicilan: Int, jumlahPembayaran: Int, biosheOrderId: String, kurirPengirim: String)
    }
    interface Repository{

        fun createAndGetData()
        fun deleteOrder()
        fun updateOrder(
            midtransOrderId: String,
            midtransTransactionId: String,
            membayar: Boolean,
            dibayar: Boolean,
            show: Boolean,
            metodePembayaran: String,
            statusPembayaran: String,
            kurirPengirim: String,
            jumlahPembayaran: Int
        )

        fun createCicilan(jumlahCicilan: Int, jumlahPembayaran: Int, biosheOrderId: String, kurirPengirim: String)
    }
    interface Listener{

        fun showError(error: String)
        fun getDataListener(alamatOutlet: String, nama: String, noHp: String, salesId: String, email: String)
        fun orderCreatedListener(orderId: String)
        fun createCicilanListener()
        fun getLoyaltiListener(silver: Int, gold: Int, platinum: Int, diamond: Int, myLoyalti: String)
    }
}