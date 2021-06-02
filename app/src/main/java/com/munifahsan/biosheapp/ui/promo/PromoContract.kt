package com.munifahsan.biosheapp.ui.promo

interface PromoContract {
    interface View{

        fun showMessage(msg: String)
    }
    interface Presenter{

        fun postKeranjang(produkId: String)
    }
    interface Repository{

        fun postKeranjang(productId: String)
    }
    interface Listener{

        fun postKeranjangListener(msg: String)
    }
}