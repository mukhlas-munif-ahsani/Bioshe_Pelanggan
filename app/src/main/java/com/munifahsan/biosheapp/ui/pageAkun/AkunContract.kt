package com.munifahsan.biosheapp.ui.pageAkun

interface AkunContract {
    interface View{

        fun showPhoto(url: String)
        fun hidePhoto()
        fun showNama(nama: String)
        fun showLoyalti(loyalti: String)
        fun hideNama()
        fun hideLoyalti()
        fun loadQrCode(data: String)
    }

    interface Presenter{

        fun getData()
    }

    interface Repository{

        fun getData()
    }

    interface Listener{

        fun getDataListener(photoUrl:String, nama:String, loyalti: String, qrCodeId: String)
        fun showError(error: String)
    }
}