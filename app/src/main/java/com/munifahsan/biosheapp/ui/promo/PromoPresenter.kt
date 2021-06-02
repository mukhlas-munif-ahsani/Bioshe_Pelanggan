package com.munifahsan.biosheapp.ui.promo

class PromoPresenter(val mView: PromoContract.View): PromoContract.Presenter, PromoContract.Listener {
    val mRepo: PromoContract.Repository
    init {
        mRepo = PromoRepository(this)
    }

    override fun postKeranjang(produkId: String){
        mRepo.postKeranjang(produkId)
    }

    override fun postKeranjangListener(msg: String){
        mView.showMessage(msg)
    }
}