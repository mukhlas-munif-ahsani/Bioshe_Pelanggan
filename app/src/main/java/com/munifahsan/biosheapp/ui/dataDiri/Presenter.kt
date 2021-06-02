package com.munifahsan.biosheapp.ui.dataDiri

import android.net.Uri

class Presenter(private val mView: Contract.View) : Contract.Presenter, Contract.Listener {
    private val mRepo: Contract.Repository

    init {
        this.mRepo = Repository(this)
    }

    override fun getData() {
        mRepo.getData()
    }

    override fun updateData(
        email: String,
        imageUri: Uri?, nik: String,
        nama: String, gender: String,
        alamatRumah: String, noHp: String,
        noWa: String, ahliWaris: String,
        namaOutlet: String, alamatOutlet: String
    ) {
        mRepo.updateData(
            email,
            imageUri,
            nik,
            nama,
            gender,
            alamatRumah,
            noHp,
            noWa,
            ahliWaris,
            namaOutlet,
            alamatOutlet
        )
    }

    override fun getDataListener(
        url: String,
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
    ) {
        mView.showPhoto(url)
        mView.showEmail(email)
        mView.showNama(nama)
        mView.showNik(nik)
        mView.showGender(gender)
        mView.showAlamatRumah(alamatRumah)
        mView.showNoHp(noHp)
        mView.showNoWa(noWa)
        mView.showAhliWaris(ahli)
        mView.showNamaOutlet(namaOutlet)
        mView.showAlamatOutlet(alamatOutlet)
    }

    override fun showError(error: String) {
        mView.showMessage(error)
    }
}