package com.munifahsan.biosheapp.ui.transferBayar

import android.net.Uri

interface Contract {
    interface View {

        fun showTotalBayar(bayar: String)
        fun hideTotalBayar()
        fun hideNoRek()
        fun showNoRek(no: String)
        fun showProgress(message: String, progress: Double)
        fun hideProgress()
    }

    interface Presenter {
        fun uploadImage(imageUri: Uri?, orderId: String)
        fun getData(id: String)
    }
    interface Repository {
        fun getData(id: String)
        fun uploadImage(imageUri: Uri?, orderId: String)
    }
    interface Listener {
        fun getDataListener(totalBayar: String)
        fun uploadImageListener(message: String, progress: Double)
        fun uploadImageSuccessListener()
    }
}