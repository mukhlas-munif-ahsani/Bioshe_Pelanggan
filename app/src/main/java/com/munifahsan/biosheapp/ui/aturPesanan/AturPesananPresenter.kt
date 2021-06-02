package com.munifahsan.biosheapp.ui.aturPesanan

class AturPesananPresenter (val mView: AturPesananContract.View):AturPesananContract.Presenter, AturPesananContract.Listener {
    private val mRepo: AturPesananContract.Repository

    init {
        mRepo = AturPesananRepository(this)
    }

    override fun createAndGet(){
        mView.hideContent()
        mRepo.createAndGetData()
    }

    override fun deleteOrder(){
        mRepo.deleteOrder()
    }

    override fun updateOrder(midtransOrderId: String,
                             midtransTransactionId: String,
                             membayar: Boolean,
                             dibayar: Boolean,
                             show: Boolean,
                             metodePembayaran: String,
                             statusPembayaran: String,
                             kurirPengirim: String,
                             jumlahPembayaran: Int){
       mRepo.updateOrder(midtransOrderId, midtransTransactionId, membayar, dibayar, show, metodePembayaran, statusPembayaran, kurirPengirim, jumlahPembayaran)
    }

    override fun orderCreatedListener(orderId: String){
        mView.showContent()
        mView.setOrderId(orderId)
    }

    override fun createCicilan(jumlahCicilan: Int, jumlahPembayaran: Int, biosheOrderId: String,kurirPengirim: String){
        mRepo.createCicilan(jumlahCicilan, jumlahPembayaran, biosheOrderId, kurirPengirim)
        mView.hideContent()
    }

    override fun showError(error: String){
        mView.showMessage(error)
    }

    override fun getDataListener(alamatOutlet: String, nama:String, noHp:String, salesId: String, email: String){
        mView.setAlamatOutlet(alamatOutlet)
        mView.setName(nama)
        mView.setNoHp(noHp)
        mView.setSales(salesId)
        mView.setEmailBioshe(email)
    }

    override fun getLoyaltiListener(silver: Int, gold: Int, platinum: Int, diamond: Int, myLoyalti: String){
        mView.setLoyalti(silver, gold, platinum, diamond, myLoyalti)
    }

    override fun createCicilanListener(){
        mView.startTagihanActivity()
    }
}