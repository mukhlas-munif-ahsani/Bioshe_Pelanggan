package com.munifahsan.biosheapp.ui.detailPesanan

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.databinding.ActivityDetailPesananBinding
import com.munifahsan.biosheapp.domain.Keranjang
import com.munifahsan.biosheapp.ui.tagihan.BayarTagihanActivity
import com.munifahsan.biosheapp.ui.tagihan.TagihanActivity
import com.munifahsan.biosheapp.ui.transferBayar.TransferBayarActivity
import com.squareup.picasso.Picasso
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class DetailPesananActivity : AppCompatActivity(), DetailPesananContract.View {
    private lateinit var binding: ActivityDetailPesananBinding

    private lateinit var mPres: DetailPesananContract.Pres

    private var adapterCart: ProductFirestoreRecyclerAdapter? = null
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val dbOrders = FirebaseFirestore.getInstance()
        .collection("ORDERS")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPesananBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mPres = Presenter(this)

        val intent = intent
        val orderId = intent.getStringExtra("ORDER_ID")

        mPres.getData(orderId.toString())

        binding.backIcon.setOnClickListener {
            finish()
        }

        binding.bayarBtn.setOnClickListener {
            val intent = Intent(this, TagihanActivity::class.java)
            //intent.putExtra("ORDER_ID", orderId)
            startActivity(intent)
        }

        showItemCart(orderId.toString())
    }


    override fun onStart() {
        super.onStart()
        adapterCart?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterCart?.stopListening()
    }

    private fun showItemCart(orderId: String) {
        binding.rvItem.layoutManager = LinearLayoutManager(
            this
        )
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef.collection("ORDERS").document(orderId)
            .collection("PRODUCT")
        val options = FirestoreRecyclerOptions
            .Builder<Keranjang>()
            .setQuery(query, Keranjang::class.java)
            .build()
        adapterCart = ProductFirestoreRecyclerAdapter(options)
        binding.rvItem.adapter = adapterCart
        binding.rvItem.isNestedScrollingEnabled = false
    }

    private inner class ProductViewHolder(private val view: View) :
        RecyclerView.ViewHolder(
            view
        ) {

        fun setProduct(
            itemKeranjangId: String,
            productId: String, jumlahItem: Int,
            namaProduct: String, thumbnailProduct: String,
            hargaProduct: Int, disconProduct: Int, berat: Int
        ) {
            val productName = view.findViewById<TextView>(R.id.productName)
            val priceDiscon = view.findViewById<TextView>(R.id.priceDisconTxt)
            val disconTxt = view.findViewById<TextView>(R.id.disconTxt)
            val priceProduct = view.findViewById<TextView>(R.id.priceProduct)
            val linDiscon = view.findViewById<LinearLayout>(R.id.linDiscon)
            val image = view.findViewById<ImageView>(R.id.thumbnailImage)
            val jumlahItemTxt = view.findViewById<TextView>(R.id.jumlahItem)

            "${jumlahItem} Barang (${berat*jumlahItem} gr)".also { jumlahItemTxt.text = it }

            if (thumbnailProduct != "") {
                Picasso.get()
                    .load(thumbnailProduct)
                    .placeholder(R.drawable.black_transparent)
                    .into(image)
            }

            productName.text = namaProduct
            disconTxt.text = disconProduct.toString()
            priceDiscon.text = rupiahFormat(hargaProduct)
            priceDiscon.paintFlags = priceDiscon.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            if (disconProduct == 0) {
                linDiscon.visibility = View.INVISIBLE
            } else {
                linDiscon.visibility = View.VISIBLE
            }

            val disconNum: Int = disconProduct
            val disconHarga: Int = hargaProduct * disconNum / 100
            val harga = hargaProduct - disconHarga
            priceProduct.text = rupiahFormat(harga)

        }
    }

    private inner class ProductFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<Keranjang>) :
        FirestoreRecyclerAdapter<Keranjang, ProductViewHolder>(
            options
        ) {
        override fun onBindViewHolder(
            productViewHolder: ProductViewHolder,
            position: Int,
            keranjangModel: Keranjang
        ) {
            productViewHolder.setProduct(
                keranjangModel.id,
                keranjangModel.productId,
                keranjangModel.jumlahItem,
                keranjangModel.nama,
                keranjangModel.thumbnail,
                keranjangModel.harga,
                keranjangModel.diskon,
                keranjangModel.berat
            )
            //productViewHolder.setJumlahClickableItem(keranjangModel.id, keranjangModel.jumlahItem)

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_atur_pesanan,
                parent,
                false
            )
            return ProductViewHolder(view)
        }
    }


    override fun showContent() {
        binding.status.text = ""
        binding.tanggalPemesanan.text = ""
        binding.idPemesanan.text = ""
        binding.kurirPengirim.text = ""
        binding.noResi.text = ""
        binding.alamatPengiriman.text = ""
        binding.metodePembayaran.text = ""
        binding.totalHarga.text = ""
        binding.totalOngkir.text = ""
    }

    override fun showStatus(txt: String) {
        binding.status.visibility = View.VISIBLE
        binding.statusPesananShimmer.visibility = View.INVISIBLE
        binding.status.text = txt

//        if (txt == "DIPROSES" || txt == "DIKIRIM" || txt == "SELESAI"){
//            binding.bayarBtn.visibility = View.INVISIBLE
//        }

        if (txt == "DICICIL"){
            binding.bayarBtn.visibility = View.VISIBLE
        } else {
            binding.bayarBtn.visibility = View.GONE
        }
    }

    override fun hideStatus() {
        binding.status.visibility = View.INVISIBLE
        binding.statusPesananShimmer.visibility = View.VISIBLE
    }

    override fun showTanggalPesanan(date: Date?) {
        binding.tanggalPemesanan.text = getTimeDate(date)
        binding.tanggalPemesanan.visibility = View.VISIBLE
        binding.tanggalPemesananShimmer.visibility = View.INVISIBLE
    }

    override fun hideTanggalPesanan() {
        binding.tanggalPemesanan.visibility = View.INVISIBLE
        binding.tanggalPemesananShimmer.visibility = View.VISIBLE
    }

    override fun showIdPemesanan(txt: String) {
        "...${txt.substring(15, txt.length)}".also { binding.idPemesanan.text = it }
        binding.idPemesanan.visibility = View.VISIBLE
        binding.idPesananShimmer.visibility = View.INVISIBLE

        binding.idPemesanan.setOnClickListener {
            val clipboardManager = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

            val clipData = ClipData.newPlainText("text", txt)
            clipboardManager.setPrimaryClip(clipData)

            showMessage("ID Pesanan disalin ke clipboard")
        }

        binding.copyIcon.setOnClickListener {
            val clipboardManager = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

            val clipData = ClipData.newPlainText("text", txt)
            clipboardManager.setPrimaryClip(clipData)

            showMessage("ID Pesanan disalin ke clipboard")
        }

    }

    override fun hideIdPemesanan() {
        binding.idPemesanan.visibility = View.INVISIBLE
        binding.idPesananShimmer.visibility = View.VISIBLE
    }

    override fun showKurirPengirim(txt: String) {
        binding.kurirPengirim.text = txt
        binding.kurirPengirim.visibility = View.VISIBLE
        binding.kurirShimmer.visibility = View.INVISIBLE
    }

    override fun hideKurirPengirim() {
        binding.kurirPengirim.visibility = View.INVISIBLE
        binding.kurirShimmer.visibility = View.VISIBLE
    }

    override fun showNoResi(txt: String) {
        binding.noResi.text = txt
        binding.noResi.visibility = View.VISIBLE
        binding.noResiShimmer.visibility = View.INVISIBLE
    }

    override fun hideNoResi() {
        binding.noResi.visibility = View.INVISIBLE
        binding.noResiShimmer.visibility = View.VISIBLE
    }

    override fun showAlamatPengiriman(nama: String, nomor: String, alamat: String) {
        binding.nama.text = nama
        binding.nomor.text = nomor
        binding.alamatPengiriman.text = alamat
        binding.nama.visibility = View.VISIBLE
        binding.nomor.visibility = View.VISIBLE
        binding.alamatPengiriman.visibility = View.VISIBLE
        binding.alamatPengirimanShimmer.visibility = View.INVISIBLE
    }

    override fun hideAlamatPengiriman() {
        binding.nama.visibility = View.GONE
        binding.nomor.visibility = View.GONE
        binding.alamatPengiriman.visibility = View.INVISIBLE
        binding.alamatPengirimanShimmer.visibility = View.VISIBLE
    }

    override fun showMetodePembayaran(txt: String) {
        binding.metodePembayaran.text = txt
        binding.metodePembayaran.visibility = View.VISIBLE
        binding.metodePembayaranShimmer.visibility = View.INVISIBLE
    }

    override fun hideMetodePembayaran() {
        binding.metodePembayaran.visibility = View.INVISIBLE
        binding.metodePembayaranShimmer.visibility = View.VISIBLE
    }

    override fun showTotalHargaBarang(totalHarga: Int, totalBarang: Int, ongkir: Int, berat: Int) {
        binding.totalHarga.text = rupiahFormat(totalHarga)
        "Total Harga (${totalBarang} Barang)".also { binding.totalBarang.text = it }
        binding.totalHarga.visibility = View.VISIBLE
        binding.totalHargaShimmer.visibility = View.INVISIBLE

        binding.totalBayar.text = rupiahFormat(totalHarga.plus(ongkir))

        "Total Ongkos Kirim (${berat} gr)".also { binding.totalBerat.text = it }
    }

    override fun hideTotalHarga() {
        binding.totalHarga.visibility = View.INVISIBLE
        binding.totalHargaShimmer.visibility = View.VISIBLE
    }

    override fun showTotalOngkir(txt: Int) {
        if (txt == 0) {
            binding.totalOngkir.text = "Gratis"
        } else {
            binding.totalOngkir.text = rupiahFormat(txt)
        }
        binding.totalOngkir.visibility = View.VISIBLE
        binding.totalOngkirShimmer.visibility = View.INVISIBLE
    }

    override fun hideTotalOngkir() {
        binding.totalOngkir.visibility = View.INVISIBLE
        binding.totalOngkirShimmer.visibility = View.VISIBLE
    }

    private fun getTimeDate(timestamp: Date?): String? {
        return try {
            //Date netDate = (timestamp);
            val sfd = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val tz = TimeZone.getTimeZone("Asia/Jakarta")
            //sfd.timeZone = tz

            //showMessage(tz.getDisplayName(false, TimeZone.SHORT, Locale.ENGLISH))

            sfd.format(timestamp)
        } catch (e: Exception) {
            "date"
        }
    }

    private fun rupiahFormat(number: Int): String {
        val kursIndonesia = DecimalFormat.getCurrencyInstance() as DecimalFormat
        val formatRp = DecimalFormatSymbols()
        formatRp.currencySymbol = "Rp "
        formatRp.monetaryDecimalSeparator = ','
        formatRp.groupingSeparator = '.'
        kursIndonesia.decimalFormatSymbols = formatRp
        val harga = kursIndonesia.format(number).toString()
        return harga.replace(",00", " ")
    }

    override fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
