package com.munifahsan.biosheapp.ui.tagihan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
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
import com.munifahsan.biosheapp.databinding.ActivityBayarTagihanBinding
import com.munifahsan.biosheapp.domain.PaymentHistory
import java.util.*
import kotlin.collections.ArrayList

class BayarTagihanActivity : AppCompatActivity(), TransactionFinishedCallback {
    private lateinit var binding: ActivityBayarTagihanBinding
    private val dbUsers = FirebaseFirestore.getInstance()
        .collection("USERS")
    private val currentUserId = Firebase.auth.currentUser!!.uid
    var metodePembayaran: String = ""

    var alamatPengiriman = ""
    var nomorHp = ""
    var namaDepan = ""
    var namaBelakang = ""
    var email = ""
    var kota = ""
    var kodePos = ""
    val itemDetailsList: ArrayList<ItemDetails> = ArrayList()
    var tagihanId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBayarTagihanBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        tagihanId = intent.getStringExtra("TAGIHAN_ID").toString()

        getTagihanData(tagihanId)
        binding.backIcon.setOnClickListener {
            finish()
        }

        initMid()
        pilihPembayaran(view)
        getUserData()
    }

    private fun getUserData() {
        dbUsers.document(currentUserId).get().addOnSuccessListener {
            namaDepan = it.getString("nama").toString()
            namaBelakang = it.getString("namaBelakang").toString()
            email = it.getString("email").toString()
            alamatPengiriman = it.getString("alamatOutlet").toString()
            nomorHp = it.getString("noHp").toString()
            kota = it.getString("kota").toString()
            kodePos = it.getString("kodePos").toString()
        }
    }

    private fun getTagihanData(tagihanId: String) {
        var transactionRequest: TransactionRequest?
        binding.content.visibility = View.GONE
        binding.progress.visibility = View.VISIBLE

        dbUsers.document(currentUserId).collection("TAGIHAN").document(tagihanId).get()
            .addOnSuccessListener {
                binding.content.visibility = View.VISIBLE
                binding.progress.visibility = View.GONE

                if (Date().after(it.getDate("deadLinePembayaran"))) {
                    itemDetailsList.add(
                        ItemDetails(
                            it.getString("biosheOrderId") + "_$tagihanId",
                            it.getLong("jumlahPembayaranTelat")!!.toDouble(),
                            1,
                            it.id
                        )
                    )
                    transactionRequest = TransactionRequest(
                        System.currentTimeMillis().toString() + "",
                        it.getLong("jumlahPembayaranTelat")!!.toDouble()
                    )
                } else {
                    itemDetailsList.add(
                        ItemDetails(
                            it.getString("biosheOrderId") + "_$tagihanId",
                            it.getLong("jumlahPembayaran")!!.toDouble(),
                            1,
                            it.id
                        )
                    )
                    transactionRequest = TransactionRequest(
                        System.currentTimeMillis().toString() + "",
                        it.getLong("jumlahPembayaran")!!.toDouble()
                    )
                }

                transactionRequest!!.itemDetails = itemDetailsList
                transactionRequest!!.customerDetails = customerDetails()
                MidtransSDK.getInstance().transactionRequest = transactionRequest
            }
    }

    private fun customerDetails(): CustomerDetails {
        val customerDetails = CustomerDetails()
        customerDetails.customerIdentifier = "${namaDepan}-${currentUserId}"
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

    private fun pilihPembayaran(view: ViewGroup) {


        //pilih transfer bank sebagai metode pembayaran
        binding.relTransferBank.setOnClickListener {
            metodePembayaran = "BANK_TRANSFER"
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Peringatan!!!")
            builder.setMessage("Harap segera kembali ke halaman ini setelah melakukan pembayaran untuk memastikan pembayaran anda berhasil dan dapat tersimpan di Database kami.")
            builder.setPositiveButton("Oke") { _, _ ->

                MidtransSDK.getInstance()
                    .startPaymentUiFlow(this, PaymentMethod.BANK_TRANSFER)
                updateTagihan(
                    "",
                    "",
                    false,
                    "",
                    metodePembayaran,
                    true
                )
            }
            builder.setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            builder.show()

        }

        //pilih Gopay sebagai metode pembayaran
        binding.relGoPay.setOnClickListener {
            metodePembayaran = "GO_PAY"
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Peringatan!!!")
            builder.setMessage("Harap segera kembali ke halaman ini setelah melakukan pembayaran untuk memastikan pembayaran anda berhasil dan dapat tersimpan di Database kami.")
            builder.setPositiveButton("Oke") { _, _ ->
                MidtransSDK.getInstance().startPaymentUiFlow(
                    this,
                    PaymentMethod.GO_PAY
                )
                updateTagihan(
                    "",
                    "",
                    false,
                    "",
                    metodePembayaran,
                    true
                )
            }
            builder.setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
        }

        //pilih Shopee Pay sebagai metode pembayaran
        binding.relShoopePay.setOnClickListener {
            metodePembayaran = "SHOPEEPAY"
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Peringatan!!!")
            builder.setMessage("Harap segera kembali ke halaman ini setelah melakukan pembayaran untuk memastikan pembayaran anda berhasil dan dapat tersimpan di Database kami.")
            builder.setPositiveButton("Oke") { _, _ ->

                MidtransSDK.getInstance().startPaymentUiFlow(
                    this,
                    PaymentMethod.SHOPEEPAY
                )
                updateTagihan(
                    "",
                    "",
                    false,
                    "",
                    metodePembayaran,
                    true
                )
            }
            builder.setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            builder.show()

        }

        //pilih BCA Klik sebagai metode pembayaran
        binding.relBcaClick.setOnClickListener {
            metodePembayaran = "BCA_KLIKPAY"
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Peringatan!!!")
            builder.setMessage("Harap segera kembali ke halaman ini setelah melakukan pembayaran untuk memastikan pembayaran anda berhasil dan dapat tersimpan di Database kami.")
            builder.setPositiveButton("Oke") { _, _ ->
                MidtransSDK.getInstance()
                    .startPaymentUiFlow(this, PaymentMethod.BCA_KLIKPAY)
                updateTagihan(
                    "",
                    "",
                    false,
                    "",
                    metodePembayaran,
                    true
                )
            }
            builder.setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            builder.show()

        }

        //pilih CIMBS sebagai metode pembayaran
        binding.relCimb.setOnClickListener {
            metodePembayaran = "CIMB_CLICKS"
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Peringatan!!!")
            builder.setMessage("Harap segera kembali ke halaman ini setelah melakukan pembayaran untuk memastikan pembayaran anda berhasil dan dapat tersimpan di Database kami.")
            builder.setPositiveButton("Oke") { _, _ ->

                MidtransSDK.getInstance()
                    .startPaymentUiFlow(this, PaymentMethod.CIMB_CLICKS)
                updateTagihan(
                    "",
                    "",
                    false,
                    "",
                    metodePembayaran,
                    true
                )
            }
            builder.setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            builder.show()

        }

        //pilih DANAMON sebagai metode pembayaran
        binding.relDanamon.setOnClickListener {
            metodePembayaran = "DANAMON_ONLINE"
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Peringatan!!!")
            builder.setMessage("Harap segera kembali ke halaman ini setelah melakukan pembayaran untuk memastikan pembayaran anda berhasil dan dapat tersimpan di Database kami.")
            builder.setPositiveButton("Oke") { _, _ ->

                MidtransSDK.getInstance()
                    .startPaymentUiFlow(this, PaymentMethod.DANAMON_ONLINE)
                updateTagihan(
                    "",
                    "",
                    false,
                    "",
                    metodePembayaran,
                    true
                )
            }
            builder.setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            builder.show()

        }

        //pilih Indomart sebagai metode pembayaran
        binding.relIndomart.setOnClickListener {
            metodePembayaran = "INDOMARET"
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Peringatan!!!")
            builder.setMessage("Harap segera kembali ke halaman ini setelah melakukan pembayaran untuk memastikan pembayaran anda berhasil dan dapat tersimpan di Database kami.")
            builder.setPositiveButton("Oke") { _, _ ->
                MidtransSDK.getInstance().startPaymentUiFlow(
                    this,
                    PaymentMethod.INDOMARET
                )
                updateTagihan(
                    "",
                    "",
                    false,
                    "",
                    metodePembayaran,
                    true
                )
            }
            builder.setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
        }

        //pilih Alfamart sebagai metode pembayaran
        binding.relAlfamart.setOnClickListener {
            metodePembayaran = "ALFAMART"
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Peringatan!!!")
            builder.setMessage("Harap segera kembali ke halaman ini setelah melakukan pembayaran untuk memastikan pembayaran anda berhasil dan dapat tersimpan di Database kami.")
            builder.setPositiveButton("Oke") { _, _ ->

                MidtransSDK.getInstance().startPaymentUiFlow(
                    this,
                    PaymentMethod.ALFAMART
                )
                updateTagihan(
                    "",
                    "",
                    false,
                    "",
                    metodePembayaran,
                    true
                )
            }
            builder.setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            builder.show()

        }

        //pilih Akulaku sebagai metode pembayaran
        binding.relAkulaku.setOnClickListener {
            metodePembayaran = "AKULAKU"
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Peringatan!!!")
            builder.setMessage("Harap segera kembali ke halaman ini setelah melakukan pembayaran untuk memastikan pembayaran anda berhasil dan dapat tersimpan di Database kami.")
            builder.setPositiveButton("Oke") { _, _ ->
                MidtransSDK.getInstance().startPaymentUiFlow(
                    this,
                    PaymentMethod.AKULAKU
                )
                updateTagihan(
                    "",
                    "",
                    false,
                    "",
                    metodePembayaran,
                    true
                )
            }
            builder.setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
        }
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

    override fun onTransactionFinished(result: TransactionResult?) {
        if (result!!.response != null) {
            when (result.status) {
                TransactionResult.STATUS_SUCCESS -> {
                    showMessage("Transaksi Berhasil ID : " + result.response.transactionId)
                    showMessage("Transaksi Berhasil")
                    updateTagihan(
                        result.response.orderId,
                        result.response.transactionId,
                        true,
                        "SELESAI",
                        metodePembayaran,
                        true
                    )
                }
                TransactionResult.STATUS_PENDING -> {
                    //showMessage("Transaksi Pending ID : " + result.response.transactionId + ". Pesan : " + result.response.statusMessage)
                    showMessage("Transaksi dibatalkan")
                    updateTagihan(
                        result.response.orderId,
                        result.response.transactionId,
                        false,
                        "",
                        metodePembayaran,
                        false
                    )
//                    mPres.deleteOrder()
//                    startDetailOrderActivity()
                }
                TransactionResult.STATUS_FAILED -> {
                    showMessage("Transaksi Gagal ID : " + result.response.transactionId)
                    updateTagihan(
                        result.response.orderId,
                        result.response.transactionId,
                        false,
                        "",
                        metodePembayaran,
                        false
                    )
//                    mPres.deleteOrder()
//                    startDetailOrderActivity()
                }
            }
            result.response.validationMessages
        } else if (result.isTransactionCanceled) {
            showMessage("Transaksi dibatalkan")
            updateTagihan(
                "",
                "",
                false,
                "",
                metodePembayaran,
                false
            )
        } else {
            if (result.status.equals(TransactionResult.STATUS_INVALID, ignoreCase = true)) {
                showMessage("Transaksi tidak Valid")
                updateTagihan(
                    "",
                    "",
                    false,
                    "",
                    metodePembayaran,
                    false
                )
            } else {
                showMessage("Transaksi Berhasil dengan failure")
                updateTagihan(
                    "",
                    "",
                    false,
                    "",
                    metodePembayaran,
                    false
                )
            }
        }
    }

    private fun updateTagihan(
        orderId: String,
        transactionId: String,
        dibayar: Boolean,
        statusPembayaran: String,
        metodePembayaran: String,
        membayar: Boolean
    ) {
        Constants.TAGIHAN_DB.document(tagihanId)
            .update(
                "midtransOrderId", orderId,
                "midtransTransactionId", transactionId,
                "dibayar", dibayar,
                "statusPembayaran", statusPembayaran,
                "metodePembayaran", metodePembayaran,
                "membayar", membayar
            )
    }

    private fun createPaymentHistory(
        biosheOrderId: String,
        midtransOrderId: String,
        midtransTransactionId: String,
        userId: String,
        keterangan: String,
        jumlahPembayaran: Int
    ) {
        val data = PaymentHistory(biosheOrderId, midtransOrderId, midtransTransactionId, userId, keterangan, jumlahPembayaran)
        Constants.PAYMENT_HISTORY_DB.document().set(data)
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}