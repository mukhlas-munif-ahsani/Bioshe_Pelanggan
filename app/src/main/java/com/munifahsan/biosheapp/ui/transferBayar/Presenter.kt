package com.munifahsan.biosheapp.ui.transferBayar

import android.net.Uri

class Presenter (private val mView: Contract.View): Contract.Presenter, Contract.Listener {
    private val mRepo: Contract.Repository

    init {
        mRepo = Repository(this)
    }

    override fun getData(id:String){
        mRepo.getData(id)
    }

    override fun uploadImage(imageUri: Uri?, orderId: String){
        mRepo.uploadImage(imageUri, orderId)
    }

    override fun getDataListener(totalBayar: String){
        mView.showTotalBayar(totalBayar)
        mView.showNoRek("3r22582038232")
    }

    override fun uploadImageListener(message:String, progress:Double){
        mView.showProgress(message, progress)
    }

    override fun uploadImageSuccessListener(){
        mView.hideProgress()
    }
}





