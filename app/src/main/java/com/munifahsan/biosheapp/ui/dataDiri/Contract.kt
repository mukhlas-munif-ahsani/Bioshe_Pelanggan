package com.munifahsan.biosheapp.ui.dataDiri

import android.net.Uri

interface Contract {
    interface View{

        fun showPhoto(url:String)
        fun showEmail(email: String)
        fun hideEmail()
        fun showNama(nama: String)
        fun hideNama()
        fun showNik(nik: String)
        fun hideNik()
        fun showGender(gender: String)
        fun hideGender()
        fun showAlamatRumah(alamat: String)
        fun hideAlamatRumah()
        fun showNoHp(no: String)
        fun hideNoHp()
        fun showNoWa(no: String)
        fun hideNoWa()
        fun showAhliWaris(ahli: String)
        fun hideAhliWaris()
        fun showNamaOutlet(outlet: String)
        fun hideNamaOutlet()
        fun showAlamatOutlet(alamat: String)
        fun hideAlamatOutlet()
        fun showMessage(msg: String)
    }

    interface Presenter{

        fun getData()
        fun updateData(
            email: String,
            imageUri: Uri?,
            nik: String,
            nama: String,
            gender: String,
            alamatRumah: String,
            noHp: String,
            noWa: String,
            ahliWaris: String,
            namaOutlet: String,
            alamatOutlet: String
        )
    }

    interface Repository{

        fun getData()
        fun updateData(
            email: String,
            imageUri: Uri?, nik: String,
            nama: String, gender: String,
            alamatRumah: String, noHp: String,
            noWa: String, ahliWaris: String,
            namaOutlet: String, alamatOutlet: String
        )
    }

    interface Listener{

        fun getDataListener(
            url:String,
            email: String,
            nama: String,
            nik: String,
            gender: String,
            alamatRumah: String,
            noHp: String,
            noWa: String,
            ahli: String,
            namaOutlet: String,
            alamatOutlet: String
        )

        fun showError(error: String)
    }
}