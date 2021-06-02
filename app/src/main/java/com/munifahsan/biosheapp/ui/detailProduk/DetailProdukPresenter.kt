package com.munifahsan.biosheapp.ui.detailProduk

class DetailProdukPresenter(val mView: DetailProdukContract.View): DetailProdukContract.Presenter, DetailProdukContract.Listener {
    val mRepo: DetailProdukContract.Repository

    init {
        mRepo = DetailProdukRepository(this)
    }

    override fun postKeranjang(produkId: String){
        mRepo.postKeranjang(produkId)
    }

    override fun postKeranjangListener(msg: String){
        mView.showMessage(msg)
    }
}