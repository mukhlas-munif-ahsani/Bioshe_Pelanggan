package com.munifahsan.biosheapp.ui.pageAkun

class Presenter (val mView: AkunContract.View): AkunContract.Presenter, AkunContract.Listener {

    private val mRepo : AkunContract.Repository

    init {
        this.mRepo = Repository(this)
    }

    override fun getData(){
        mRepo.getData()
    }

    override fun getDataListener(photoUrl:String, nama:String, loyalti: String, qrCodeId: String){
        mView.showPhoto(photoUrl)
        mView.showLoyalti(loyalti)
        mView.showNama(nama)
        mView.loadQrCode(qrCodeId)
    }

    override fun showError(error: String){

    }
}