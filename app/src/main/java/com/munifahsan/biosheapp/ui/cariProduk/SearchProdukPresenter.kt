package com.munifahsan.biosheapp.ui.cariProduk

class SearchProdukPresenter(val mView: SearchProdukContract.View): SearchProdukContract.Presenter, SearchProdukContract.Listener {
    val mRepo: SearchProdukContract.Repository
    init {
        mRepo = SearchProdukRepository(this)
    }

    override fun postKeranjang(produkId: String){
        mRepo.postKeranjang(produkId)
    }

    override fun postKeranjangListener(msg: String){
        mView.showMessage(msg)
    }
}