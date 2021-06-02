package com.munifahsan.biosheapp.ui.detailPesanan

import java.util.*

interface DetailPesananContract {
    interface View {

        fun showContent()
        fun showStatus(txt: String)
        fun hideStatus()
        fun showTanggalPesanan(date: Date?)
        fun hideTanggalPesanan()
        fun showIdPemesanan(txt: String)
        fun hideIdPemesanan()
        fun showKurirPengirim(txt: String)
        fun hideKurirPengirim()
        fun showNoResi(txt: String)
        fun hideNoResi()
        fun hideAlamatPengiriman()
        fun showAlamatPengiriman(nama: String, nomor: String, txt: String)
        fun hideMetodePembayaran()
        fun showMetodePembayaran(txt: String)
        fun hideTotalHarga()
        fun showTotalHargaBarang(totalHarga: Int, totalBarang: Int, ongkir: Int, berat: Int)
        fun hideTotalOngkir()
        fun showTotalOngkir(txt: Int)
        fun showMessage(msg: String)
    }

    interface Pres {

        fun getData(orderId: String)

    }

    interface Repo {

        fun getData(orderId: String)
    }

    interface Listener {
        fun getDataListener(
            status: String,
            tanggalPesanan: Date?,
            idPemesanan: String,
            kurirPengiriman: String,
            noResi: String,
            alamatPengiriman: String, nama: String, nomor: String,
            metodePembayaran: String,
            ongkir: Int
        )

        fun totalHargaBarangListener(totalHarga: Int, totalBarang: Int, ongkir: Int, berat: Int)
    }
}