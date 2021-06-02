package com.munifahsan.biosheapp.ui.pageHome

class HomePresenter(val mView: HomeContract.View): HomeContract.Presenter, HomeContract.Listener {
private val mRepo: HomeContract.Repository
    init {
        mRepo = HomeRepository(this)
    }

    override fun postKeranjang(produkId: String){
        mRepo.postKeranjang(produkId)
    }

    override fun postKeranjangListener(msg: String){
        mView.showMessage(msg)
    }
}