package com.munifahsan.biosheapp.ui.detailPesanan

import java.util.*

class Presenter(val mView: DetailPesananContract.View) : DetailPesananContract.Pres,
    DetailPesananContract.Listener {
    private val mRepo: DetailPesananContract.Repo

    init {
        this.mRepo = Repository(this)
    }

    override fun getData(orderId: String) {
        mRepo.getData(orderId)

        mView.hideStatus()
        mView.hideTanggalPesanan()
        mView.hideIdPemesanan()
        mView.hideKurirPengirim()
        mView.hideNoResi()
        mView.hideAlamatPengiriman()
        mView.hideMetodePembayaran()
        mView.hideTotalHarga()
        mView.hideTotalOngkir()
    }

    override fun getDataListener(
        status: String,
        tanggalPesanan: Date?,
        idPemesanan: String,
        kurirPengiriman: String,
        noResi: String,
        alamatPengiriman: String,
        nama: String,
        nomor: String,
        metodePembayaran: String,
        ongkir: Int
    ) {
        mView.showStatus(status)
        mView.showTanggalPesanan(tanggalPesanan)
        mView.showIdPemesanan(idPemesanan)
        mView.showKurirPengirim(kurirPengiriman)
        mView.showNoResi(noResi)
        mView.showAlamatPengiriman(nama, nomor, alamatPengiriman)
        mView.showMetodePembayaran(metodePembayaran)
        mView.showTotalOngkir(ongkir)
    }

    override fun totalHargaBarangListener(totalHarga: Int, totalBarang:Int, ongkir: Int, berat: Int){
        mView.showTotalHargaBarang(totalHarga, totalBarang, ongkir, berat)
    }
}