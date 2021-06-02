package com.munifahsan.biosheapp.ui.aturPesanan

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginStart
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.core.PaymentMethod
import com.midtrans.sdk.corekit.core.TransactionRequest
import com.midtrans.sdk.corekit.core.themes.CustomColorTheme
import com.midtrans.sdk.corekit.models.BillingAddress
import com.midtrans.sdk.corekit.models.CustomerDetails
import com.midtrans.sdk.corekit.models.ItemDetails
import com.midtrans.sdk.corekit.models.ShippingAddress
import com.midtrans.sdk.corekit.models.snap.TransactionResult
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import com.munifahsan.biosheapp.utils.Constants
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.databinding.ActivityAturPesananBinding
import com.munifahsan.biosheapp.domain.Keranjang
import com.munifahsan.biosheapp.ui.detailPesanan.DetailPesananActivity
import com.munifahsan.biosheapp.ui.tagihan.TagihanActivity
import com.squareup.picasso.Picasso
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.collections.ArrayList

class AturPesananActivity : AppCompatActivity(), TransactionFinishedCallback,
    AturPesananContract.View {
    private lateinit var mPres: AturPesananContract.Presenter
    private lateinit var binding: ActivityAturPesananBinding
    private var adapterCart: ProductFirestoreRecyclerAdapter? = null
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val dbKeranjang: CollectionReference =
        FirebaseFirestore.getInstance().collection("USERS")
            .document(auth.currentUser?.uid.toString())
            .collection("KERANJANG")  
    private var mBottomSheetBehavior: BottomSheetBehavior<*>? = null
    private var mBottomSheetBehaviorPembayaran: BottomSheetBehavior<*>? = null
    private var mBottomSheetBehaviorCicilan: BottomSheetBehavior<*>? = null

    var totalHarga = 0
    var biosheOrderId = ""
    var isDestroyEnable = false
    var db = FirebaseFirestore.getInstance()
    var batch = db.batch()
    var jenisPembayaran: String = ""
    var kurir: String = ""
    var alamatPengiriman = ""
    var salesId = ""
    var metodePembayaran: String = ""
    var nomorHp = ""
    var namaDepan = ""
    var namaBelakang = ""
    var email = ""
    var kota = ""
    var kodePos = ""
    var cicilan = ""
    var SILVER = 0
    var GOLD = 0
    var PLATINUM = 0
    var DIAMOND = 0
    var myLoyalti = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAturPesananBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mPres = AturPesananPresenter(this)

        binding.backIcon.setOnClickListener {
            finish()
        }

        binding.bayarBtn.setOnClickListener {
//            binding.toolbar.visibility = View.INVISIBLE
//            binding.scrollContent.visibility = View.INVISIBLE
//            binding.progress.visibility = View.VISIBLE
            if (binding.radioButtonCash.isChecked) {
                jenisPembayaran = "CASH"
                bayar()
            }

            if (binding.radioButtonTempo.isChecked) {
                jenisPembayaran = "TEMPO"
                checkLoyalti()
            }
        }

        binding.radioGroupPembayaran.setOnCheckedChangeListener { group, checkedId ->
            if (binding.radioButtonTempo.isChecked) {
                binding.relPilihCicilan.visibility = View.VISIBLE
                binding.relMetodePembayaran.visibility = View.GONE
                binding.pembayaranPilihanTxt.text = "Cicilan"
            }

            if (binding.radioButtonCash.isChecked) {
                binding.relPilihCicilan.visibility = View.GONE
                binding.relMetodePembayaran.visibility = View.VISIBLE
                binding.pembayaranPilihanTxt.text = "Metode"
            }
        }

//        binding.radioButtonTempo.setOnClickListener {
//            showMessage("Belum tersedia")
//        }

        mPres.createAndGet()

        initMid()
        openCloseKurirPembayaran(view)
        //pilihKurir(view)
        pilihPembayaran(view)
        pilihCicilan(view)
        showItemCart()
        showTotalBarangHarga(dbKeranjang)

    }

    private fun initMid() {
        SdkUIFlowBuilder.init()
            .setClientKey(Constants.CLIENT_KEY) // client_key is mandatory
            .setContext(this) // context is mandatory
            .setTransactionFinishedCallback(this) // set transaction finish callback (sdk callback)
            .setMerchantBaseUrl(Constants.BASE_URL) //set merchant url (required)
            .enableLog(true) // enable sdk log (optional)
            .setColorTheme(
                CustomColorTheme(
                    "#FFE51255",
                    "#B61548",
                    "#FFE51255"
                )
            ) // set theme. it will replace theme on snap theme on MAP ( optional)
            .setLanguage("id") //`en` for English and `id` for Bahasa
            .buildSDK()

    }

    private fun customerDetails(): CustomerDetails {
        val customerDetails = CustomerDetails()
        customerDetails.customerIdentifier = "$namaDepan-${auth.currentUser!!.uid}"
        customerDetails.phone = nomorHp
        customerDetails.firstName = namaDepan
        customerDetails.lastName = namaBelakang
        customerDetails.email = email

        val shippingAddress = ShippingAddress()
        shippingAddress.address = alamatPengiriman
        shippingAddress.city = kota
        shippingAddress.postalCode = kodePos
        customerDetails.shippingAddress = shippingAddress

        val billingAddress = BillingAddress()
        billingAddress.address = alamatPengiriman
        billingAddress.city = kota
        billingAddress.postalCode = kodePos
        customerDetails.billingAddress = billingAddress

        return customerDetails
    }

    private fun showTotalBarangHarga(ref: CollectionReference) {

        //ambil data product dari keranjang secara realtime
        ref.addSnapshotListener { value, error ->
            val itemDetailsList: ArrayList<ItemDetails> = ArrayList()
            val jumlah = ArrayList<Int>()
            var jumlahItem = 0
            var berat = 0
            for (field in value!!) {
                val a = field.getLong("harga")
                val b = field.getLong("jumlahItem")
                val c = field.getLong("diskon")
                val d = field.getLong("berat")
                val e = field.getString("nama").toString()
                jumlahItem += b!!.toInt()
                berat += d!!.toInt() * b.toInt()
                val disconNum: Int = c!!.toInt()
                val disconHarga: Int = a!!.toInt() * disconNum / 100
                val harga = a - disconHarga
                jumlah.add(harga.toInt() * b.toInt())

                //tambah item detail product ke midtrans payment list
                itemDetailsList.add(ItemDetails(field.id, harga.toDouble(), b.toInt(), e))
            }

            //tampilkan total harga dan total ongkos kirim
            "Total Harga (${jumlahItem} Barang)".also { binding.totalJumlahBarang.text = it }
            binding.totalHargaBarang.text = rupiahFormat(jumlah.sum())
            totalHarga = jumlah.sum()
            "Total Ongkos Kirim (${berat} gr)".also { binding.totalBeratBarang.text = it }

            pilihKurir(itemDetailsList)
        }
    }

    private fun pilihKurir(itemDetailsList: ArrayList<ItemDetails>) {
        val relBiosheExpress = findViewById<RelativeLayout>(R.id.relBiosheExpress)
        var totalHargaPlusOngkir = 0
        relBiosheExpress.setOnClickListener {
            binding.pilihKurirTxt.text = "Bioshe Express"
            kurir = "BIOSHE EXPRESS"

            val harga = 0
            (rupiahFormat(totalHarga.plus(harga))).also { binding.totalHarga.text = it }
            binding.hargaOngkir.text = rupiahFormat(harga)

            totalHargaPlusOngkir = totalHarga + harga

            itemDetailsList.add(
                ItemDetails(
                    "BIOSHE_EXPRESS",
                    harga.toDouble(),
                    1,
                    "BIOSHE EXPRESS"
                )
            )
            val transactionRequest = TransactionRequest(
                System.currentTimeMillis().toString() + "",
                totalHarga.plus(harga).toDouble()
            )

            transactionRequest.itemDetails = itemDetailsList
            transactionRequest.customerDetails = customerDetails()
            MidtransSDK.getInstance().transactionRequest = transactionRequest

            closePilihKurir(binding.root)

            setHargaCicilan(totalHargaPlusOngkir)
        }

    }

    override fun setAlamatOutlet(alamat: String) {
        binding.alamatTujuan.text = alamat
        alamatPengiriman = alamat
    }

    override fun setName(name: String) {
        binding.nama.text = name
        namaDepan = name
    }

    override fun setNoHp(nohp: String) {
        binding.nomor.text = nohp
        nomorHp = nohp
    }

    override fun setSales(id: String) {
        salesId = id
    }

    override fun setEmailBioshe(email: String){
        this.email = email
    }

    override fun showContent() {
        binding.progress.visibility = View.GONE
        binding.scrollContent.visibility = View.VISIBLE
        binding.backIcon.isClickable = true
        isDestroyEnable = true
    }

    override fun hideContent() {
        binding.progress.visibility = View.VISIBLE
        binding.scrollContent.visibility = View.INVISIBLE
        binding.backIcon.isClickable = false
    }

    private fun openCloseKurirPembayaran(view: ViewGroup) {
        //--bottom sheet pilih kurir & pembayaran
        val bottomSheet = findViewById<FrameLayout>(R.id.bottom_sheet_pilih_kurir)
        val bottomSheetPembayaran = findViewById<FrameLayout>(R.id.bottom_sheet_pilih_pembayaran)
        val bottomSheetCicilan = findViewById<FrameLayout>(R.id.bottom_sheet_pilih_cicilan)

        val closePilih = findViewById<CardView>(R.id.pilihKurirClose)
        val closePilihPembayaran = findViewById<CardView>(R.id.pilihPembayaranClose)
        val closePilihCicilan = findViewById<CardView>(R.id.pilihCicilanClose)

        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        (mBottomSheetBehavior as BottomSheetBehavior<*>).isDraggable = false
        mBottomSheetBehaviorPembayaran = BottomSheetBehavior.from(bottomSheetPembayaran)
        (mBottomSheetBehaviorPembayaran as BottomSheetBehavior<*>).isDraggable = false
        mBottomSheetBehaviorCicilan = BottomSheetBehavior.from(bottomSheetCicilan)
        (mBottomSheetBehaviorCicilan as BottomSheetBehavior<*>).isDraggable = false

        bottomSheet.setOnClickListener {

        }

        bottomSheetPembayaran.setOnClickListener {

        }

        bottomSheetCicilan.setOnClickListener {

        }

        closePilih.setOnClickListener {
            closePilihKurir(view)
        }

        closePilihPembayaran.setOnClickListener {
            closePilihPembayaran(view)
        }

        closePilihCicilan.setOnClickListener {
            closePilihCicilan(view)
        }

        binding.linPilihBg.setOnClickListener {
            closePilihKurir(view)
            closePilihPembayaran(view)
            closePilihCicilan(view)
        }

        binding.pilihKurir.setOnClickListener {
            openPilihKurir(view)
        }

        binding.pilihPembayaran.setOnClickListener {
            openPilihPembayaran(view)
        }

        binding.pilihCicilan.setOnClickListener {
            openPilihCicilan(view)
        }
        //--bottom sheet pilih kurir & pembayaran
    }

    private fun pilihPembayaran(view: ViewGroup) {
        //pilih transfer bank sebagai metode pembayaran
        findViewById<RelativeLayout>(R.id.relTransferBank).setOnClickListener {
            binding.namaMetode.text = getString(R.string.nama_transfer_bank)
            binding.namaMetode.marginStart
            binding.descMetode.text = getString(R.string.desc_transfer_bank)
            binding.descMetode.visibility = View.VISIBLE
            binding.icMetode.visibility = View.VISIBLE
            binding.icMetode.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_atm,
                    null
                )
            )
            metodePembayaran = "TRANSFER_BANK"
            closePilihPembayaran(view)
        }

        //pilih Gopay sebagai metode pembayaran
        findViewById<RelativeLayout>(R.id.relGoPay).setOnClickListener {
            binding.namaMetode.text = getString(R.string.nama_gopay)
            binding.descMetode.text = getString(R.string.desc_gopay)
            binding.descMetode.visibility = View.VISIBLE
            binding.icMetode.visibility = View.VISIBLE
            binding.icMetode.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_gopay,
                    null
                )
            )
            metodePembayaran = "GOPAY"
            closePilihPembayaran(view)
        }

        //pilih Shopee Pay sebagai metode pembayaran
        findViewById<RelativeLayout>(R.id.relShoopePay).setOnClickListener {
            binding.namaMetode.text = getString(R.string.nama_shopee)
            binding.descMetode.text = getString(R.string.desc_shopee)
            binding.descMetode.visibility = View.VISIBLE
            binding.icMetode.visibility = View.VISIBLE
            binding.icMetode.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.uikit_ic_shopeepay,
                    null
                )
            )
            metodePembayaran = "SHOPEE_PAY"
            closePilihPembayaran(view)
        }

        //pilih BCA Klik sebagai metode pembayaran
        findViewById<RelativeLayout>(R.id.relBcaClick).setOnClickListener {
            binding.namaMetode.text = getString(R.string.nama_bca)
            binding.descMetode.text = getString(R.string.desc_bca)
            binding.descMetode.visibility = View.VISIBLE
            binding.icMetode.visibility = View.VISIBLE
            binding.icMetode.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_bca,
                    null
                )
            )
            metodePembayaran = "BCA_KLIK"
            closePilihPembayaran(view)
        }

        //pilih CIMBS sebagai metode pembayaran
        findViewById<RelativeLayout>(R.id.relCimb).setOnClickListener {
            binding.namaMetode.text = getString(R.string.nama_cimb)
            binding.descMetode.text = getString(R.string.desc_cimb)
            binding.descMetode.visibility = View.VISIBLE
            binding.icMetode.visibility = View.VISIBLE
            binding.icMetode.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_cimb,
                    null
                )
            )
            metodePembayaran = "CIMBS_KLIK"
            closePilihPembayaran(view)
        }

        //pilih DANAMON sebagai metode pembayaran
        findViewById<RelativeLayout>(R.id.relDanamon).setOnClickListener {
            binding.namaMetode.text = getString(R.string.nama_danamon)
            binding.descMetode.text = getString(R.string.desc_danamon)
            binding.descMetode.visibility = View.VISIBLE
            binding.icMetode.visibility = View.VISIBLE
            binding.icMetode.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_danamon_online,
                    null
                )
            )
            metodePembayaran = "DANAMON"
            closePilihPembayaran(view)
        }

        //pilih Indomart sebagai metode pembayaran
        findViewById<RelativeLayout>(R.id.relIndomart).setOnClickListener {
            binding.namaMetode.text = getString(R.string.nama_indomart)
            binding.descMetode.text = getString(R.string.desc_indomart)
            binding.descMetode.visibility = View.VISIBLE
            binding.icMetode.visibility = View.VISIBLE
            binding.icMetode.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_indomaret,
                    null
                )
            )
            metodePembayaran = "INDOMART"
            closePilihPembayaran(view)
        }

        //pilih Alfamart sebagai metode pembayaran
        findViewById<RelativeLayout>(R.id.relAlfamart).setOnClickListener {
            binding.namaMetode.text = getString(R.string.nama_alfamart)
            binding.descMetode.text = getString(R.string.desc_alfamart)
            binding.descMetode.visibility = View.VISIBLE
            binding.icMetode.visibility = View.VISIBLE
            binding.icMetode.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_alfamart,
                    null
                )
            )
            metodePembayaran = "ALFAMART"
            closePilihPembayaran(view)
        }

        //pilih Akulaku sebagai metode pembayaran
        findViewById<RelativeLayout>(R.id.relAkulaku).setOnClickListener {
            binding.namaMetode.text = getString(R.string.nama_akulaku)
            binding.descMetode.text = getString(R.string.desc_akulaku)
            binding.descMetode.visibility = View.VISIBLE
            binding.icMetode.visibility = View.VISIBLE
            binding.icMetode.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_akulaku,
                    null
                )
            )
            metodePembayaran = "AKULAKU"
            closePilihPembayaran(view)
        }
    }

    private fun pilihCicilan(view: ViewGroup){
        findViewById<RelativeLayout>(R.id.relCicilan1Bulan).setOnClickListener {

            binding.pilihCicilanTxt.text = ""
            cicilan = "1_BULAN"
            closePilihCicilan(view)
        }

        findViewById<RelativeLayout>(R.id.relCicilan2Bulan).setOnClickListener {
            cicilan = "2_BULAN"
            closePilihCicilan(view)
        }

        findViewById<RelativeLayout>(R.id.relCicilan3Bulan).setOnClickListener {
            cicilan = "3_BULAN"
            closePilihCicilan(view)
        }
    }

    private fun openPilihCicilan(view: ViewGroup) {
        if (kurir != ""){
            val transition: Transition = Fade()
            transition.duration = 200
            transition.addTarget(R.id.linPilihBg)

            TransitionManager.beginDelayedTransition(view, transition)
            binding.linPilihBg.visibility = View.VISIBLE

            (mBottomSheetBehaviorCicilan as BottomSheetBehavior<*>).state =
                BottomSheetBehavior.STATE_EXPANDED
        } else {
            showMessage("Kurir harus dipilih terlebih dahulu")
        }
    }

    private fun openPilihPembayaran(view: ViewGroup) {
        val transition: Transition = Fade()
        transition.duration = 200
        transition.addTarget(R.id.linPilihBg)

        TransitionManager.beginDelayedTransition(view, transition)
        binding.linPilihBg.visibility = View.VISIBLE

        (mBottomSheetBehaviorPembayaran as BottomSheetBehavior<*>).state =
            BottomSheetBehavior.STATE_EXPANDED
    }

    private fun openPilihKurir(view: ViewGroup) {
        val transition: Transition = Fade()
        transition.duration = 200
        transition.addTarget(R.id.linPilihBg)

        TransitionManager.beginDelayedTransition(view, transition)
        binding.linPilihBg.visibility = View.VISIBLE

        (mBottomSheetBehavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun closePilihPembayaran(view: ViewGroup) {
        val transition: Transition = Fade()
        transition.duration = 200
        transition.addTarget(R.id.linPilihBg)

        TransitionManager.beginDelayedTransition(view, transition)
        binding.linPilihBg.visibility = View.GONE
        (mBottomSheetBehaviorPembayaran as BottomSheetBehavior<*>).state =
            BottomSheetBehavior.STATE_COLLAPSED

    }

    private fun closePilihCicilan(view: ViewGroup) {
        val transition: Transition = Fade()
        transition.duration = 200
        transition.addTarget(R.id.linPilihBg)

        TransitionManager.beginDelayedTransition(view, transition)
        binding.linPilihBg.visibility = View.GONE
        (mBottomSheetBehaviorCicilan as BottomSheetBehavior<*>).state =
            BottomSheetBehavior.STATE_COLLAPSED

    }

    private fun closePilihKurir(view: ViewGroup) {
        val transition: Transition = Fade()
        transition.duration = 200
        transition.addTarget(R.id.linPilihBg)

        TransitionManager.beginDelayedTransition(view, transition)
        binding.linPilihBg.visibility = View.GONE
        (mBottomSheetBehavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun setHargaCicilan(totalHargaPlusOngkir: Int){
        findViewById<TextView>(R.id.jumlahCicilan1Bln).text = rupiahFormat(totalHargaPlusOngkir + (totalHargaPlusOngkir * 5 / 100))
        findViewById<TextView>(R.id.jumlahCicilan2Bln).text = rupiahFormat((totalHargaPlusOngkir + (totalHargaPlusOngkir * 10 / 100)) / 2 )
        findViewById<TextView>(R.id.jumlahCicilan3Bln).text = rupiahFormat((totalHargaPlusOngkir + (totalHargaPlusOngkir * 15 / 100)) / 3)

        findViewById<RelativeLayout>(R.id.relCicilan1Bulan).setOnClickListener {
            binding.pilihCicilanTxt.text = "Cicilan 1X (+5%) ${rupiahFormat(totalHargaPlusOngkir + (totalHargaPlusOngkir * 5 / 100))}"
            cicilan = "1_BULAN"
            closePilihCicilan(binding.root)
        }

        findViewById<RelativeLayout>(R.id.relCicilan2Bulan).setOnClickListener {
            binding.pilihCicilanTxt.text = "Cicilan 2X (+10%) ${rupiahFormat((totalHargaPlusOngkir + (totalHargaPlusOngkir * 10 / 100)) / 2)}"
            cicilan = "2_BULAN"
            closePilihCicilan(binding.root)
        }

        findViewById<RelativeLayout>(R.id.relCicilan3Bulan).setOnClickListener {
            binding.pilihCicilanTxt.text = "Cicilan 3X (+15%) ${rupiahFormat((totalHargaPlusOngkir + (totalHargaPlusOngkir * 15 / 100)) / 3)}"
            cicilan = "3_BULAN"
            closePilihCicilan(binding.root)
        }
    }

    override fun onStart() {
        super.onStart()
        adapterCart?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterCart?.stopListening()
    }

    override fun onDestroy() {
        if (isDestroyEnable) {
            mPres.deleteOrder()
            super.onDestroy()
        }
//        super.onDestroy()
    }

    private fun bayar() {
        if (kurir != "") {
            if (metodePembayaran != "") {

                val builder = AlertDialog.Builder(this)
                builder.setTitle("Peringatan!!!")
                builder.setMessage("Harap segera kembali ke halaman ini setelah melakukan pembayaran untuk memastikan pembayaran anda berhasil dan dapat tersimpan di Database kami.")
                builder.setPositiveButton("Oke") { _, _ ->
                    when (metodePembayaran) {
                        "TRANSFER_BANK" -> {
                            MidtransSDK.getInstance()
                                .startPaymentUiFlow(this, PaymentMethod.BANK_TRANSFER)
                        }
                        "GOPAY" -> {
                            MidtransSDK.getInstance().startPaymentUiFlow(
                                this,
                                PaymentMethod.GO_PAY
                            )
                        }
                        "SHOPEE_PAY" -> {
                            MidtransSDK.getInstance().startPaymentUiFlow(
                                this,
                                PaymentMethod.SHOPEEPAY
                            )
                        }
                        "BCA_KLIK" -> {
                            MidtransSDK.getInstance()
                                .startPaymentUiFlow(this, PaymentMethod.BCA_KLIKPAY)
                        }
                        "CIMBS_KLIK" -> {
                            MidtransSDK.getInstance()
                                .startPaymentUiFlow(this, PaymentMethod.CIMB_CLICKS)
                        }
                        "DANAMON" -> {
                            MidtransSDK.getInstance()
                                .startPaymentUiFlow(this, PaymentMethod.DANAMON_ONLINE)
                        }
                        "INDOMART" -> {
                            MidtransSDK.getInstance().startPaymentUiFlow(
                                this,
                                PaymentMethod.INDOMARET
                            )
                        }
                        "ALFAMART" -> {
                            MidtransSDK.getInstance().startPaymentUiFlow(
                                this,
                                PaymentMethod.ALFAMART
                            )
                        }
                        "AKULAKU" -> {
                            MidtransSDK.getInstance().startPaymentUiFlow(
                                this,
                                PaymentMethod.AKULAKU
                            )
                        }
                    }

                    mPres.updateOrder(
                        "",
                        "",
                        membayar = true,
                        dibayar = false,
                        show = false,
                        metodePembayaran,
                        ""
                    , kurir, totalHarga)
                }
                builder.show()

            } else {
                showMessage("Metode pembayaran harus dipilih")
            }
        } else {
            showMessage("Kurir harus dipilih")
        }
    }

    private fun checkLoyalti() {

        when (myLoyalti){
            "SILVER"->{
                if (totalHarga <= SILVER){
                    cicil()
                } else {
                    showMessage("Total belanja anda melawati limit loyati SILVER(${rupiahFormat(SILVER)})")
                }
            }
            "GOLD"->{
                if (totalHarga <= GOLD){
                    cicil()
                } else {
                    showMessage("Total belanja anda melawati limit loyati GOLD(${rupiahFormat(GOLD)})")
                }
            }
            "PLATINUM"->{
                if (totalHarga <= PLATINUM){
                    cicil()
                } else {
                    showMessage("Total belanja anda melawati limit loyati PLATINUM(${rupiahFormat(PLATINUM)})")
                }
            }
            "DIAMOND"->{
                if (totalHarga <= DIAMOND){
                    cicil()
                } else {
                    showMessage("Total belanja anda melawati limit loyati DIAMOND(${rupiahFormat(DIAMOND)})")
                }
            }
        }

    }

    private fun cicil(){
        if (kurir != ""){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Konfirmasi")
            builder.setMessage("Anda akan melakukan Cicilan untuk pesanan ini ?")
            builder.setPositiveButton("Ya"){_,_->
                when(cicilan){
                    "1_BULAN"->{
                        mPres.createCicilan(1, totalHarga, biosheOrderId, kurir)
                    }
                    "2_BULAN"->{
                        mPres.createCicilan(2, totalHarga, biosheOrderId, kurir)
                    }
                    "3_BULAN"->{
                        mPres.createCicilan(3, totalHarga, biosheOrderId, kurir)
                    }
                }
            }
            builder.setNegativeButton("Tidak"){dialog,_->
                dialog.cancel()
            }
            builder.show()
        } else {
            showMessage("Kurir harus dipilih")
        }
    }

    private fun showItemCart() {
        binding.rvItem.layoutManager = LinearLayoutManager(
            this
        )
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef.collection("USERS").document(auth.currentUser?.uid.toString())
            .collection("KERANJANG")
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

            "${jumlahItem} Barang (${berat * jumlahItem} gr)".also { jumlahItemTxt.text = it }

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

    override fun onTransactionFinished(result: TransactionResult?) {
        if (result!!.response != null) {
            when (result.status) {
                TransactionResult.STATUS_SUCCESS -> {
                    showMessage("Transaksi Berhasil ID : " + result.response.transactionId)
                    showMessage("Transaksi Berhasil")
                    mPres.updateOrder(
                        result.response.orderId, result.response.transactionId,
                        membayar = true,
                        dibayar = true,
                        show = true,
                        metodePembayaran,
                        "DIPROSES", kurir, totalHarga
                    )
                    startDetailOrderActivity()
                }
                TransactionResult.STATUS_PENDING -> {
                    //showMessage("Transaksi Pending ID : " + result.response.transactionId + ". Pesan : " + result.response.statusMessage)
                    mPres.updateOrder(
                        result.response.orderId, result.response.transactionId,
                        membayar = false,
                        dibayar = false,
                        show = false, metodePembayaran, "", kurir, 0
                    )
                    showMessage("Transaksi dibatalkan")
//                    mPres.deleteOrder()
//                    startDetailOrderActivity()
                }
                TransactionResult.STATUS_FAILED -> {
                    showMessage("Transaksi Gagal ID : " + result.response.transactionId)
                    mPres.updateOrder(
                        result.response.orderId, result.response.transactionId,
                        membayar = false,
                        dibayar = false,
                        show = false, metodePembayaran, "", kurir, 0
                    )
//                    mPres.deleteOrder()
//                    startDetailOrderActivity()
                }
            }
            result.response.validationMessages
        } else if (result.isTransactionCanceled) {
            showMessage("Transaksi dibatalkan")
            mPres.updateOrder(
                "", "",
                membayar = false,
                dibayar = false,
                show = false, metodePembayaran, "",kurir, 0
            )
        } else {
            if (result.status.equals(TransactionResult.STATUS_INVALID, ignoreCase = true)) {
                showMessage("Transaksi tidak Valid")
                mPres.updateOrder(
                    "", "",
                    membayar = false,
                    dibayar = false,
                    show = false, metodePembayaran, "",kurir
                , 0)
            } else {
                showMessage("Transaksi Berhasil dengan failure")
            }
        }
    }

    override fun setLoyalti(silver: Int, gold: Int, platinum: Int, diamond: Int, myLoyalti: String){
        SILVER = silver
        GOLD = gold
        PLATINUM = platinum
        DIAMOND = diamond

        this.myLoyalti = myLoyalti
    }

    override fun setOrderId(orderId: String) {
        this.biosheOrderId = orderId
    }

    private fun startDetailOrderActivity() {
        val intent = Intent(this, DetailPesananActivity::class.java)
        intent.putExtra("ORDER_ID", biosheOrderId)
        startActivity(intent)
        finish()
        //showMessage(biosheOrderId)
    }

    override fun startTagihanActivity(){
        val intent = Intent(this, TagihanActivity::class.java)
        startActivity(intent)
        finish()
    }
}