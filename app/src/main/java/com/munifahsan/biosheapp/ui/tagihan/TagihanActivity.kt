package com.munifahsan.biosheapp.ui.tagihan

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.databinding.ActivityTagihanBinding
import com.munifahsan.biosheapp.domain.Cicilan
import com.munifahsan.biosheapp.ui.detailPesanan.DetailPesananActivity
import com.munifahsan.biosheapp.utils.Constants
import org.joda.time.DateTime
import org.joda.time.Months
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.*

class TagihanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTagihanBinding
    private var adapterTagihan: TagihanActivity.TagihanFirestoreRecyclerAdapter? = null
    private var adapterTagihanPaid: TagihanActivity.TagihanPaidFirestoreRecyclerAdapter? = null
    private val currentUserId = Firebase.auth.currentUser!!.uid
    private val dbOrders = FirebaseFirestore.getInstance()
        .collection("ORDERS")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagihanBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        showTagihan()
        showTagihanPaid()

        binding.backIcon.setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        adapterTagihan!!.startListening()
        adapterTagihanPaid!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterTagihan!!.stopListening()
        adapterTagihanPaid!!.stopListening()
    }

    private fun showTagihan() {
        binding.rvTagihan.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef
            .collection("USERS")
            .document(currentUserId)
            .collection("TAGIHAN")
//            .orderBy("biohseOrderId", Query.Direction.ASCENDING)
            .whereEqualTo("dibayar", false)
            .orderBy("mulaiPembayaran", Query.Direction.ASCENDING)
        val options = FirestoreRecyclerOptions
            .Builder<Cicilan>()
            .setQuery(query, Cicilan::class.java)
            .build()
        adapterTagihan = TagihanFirestoreRecyclerAdapter(options)
        binding.rvTagihan.adapter = adapterTagihan
    }

    private fun showTagihanPaid() {
        binding.rvTagihanPaid.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef
            .collection("USERS")
            .document(currentUserId)
            .collection("TAGIHAN")
            .whereEqualTo("dibayar", true)
            .orderBy("mulaiPembayaran", Query.Direction.ASCENDING)
        val options = FirestoreRecyclerOptions
            .Builder<Cicilan>()
            .setQuery(query, Cicilan::class.java)
            .build()
        adapterTagihanPaid = TagihanPaidFirestoreRecyclerAdapter(options)
        binding.rvTagihanPaid.adapter = adapterTagihanPaid
    }

    private inner class ProductViewHolder(private val view: View) :
        RecyclerView.ViewHolder(
            view
        ) {

        fun setAll(
            idTagihan: String,
            idOrder: String,
            mulaiBayar: Date?,
            deadline: Date?,
            status: String,
            tagihan: Int,
            tagihanTelat: Int,
            bungaTagihan: Int,
            urutan: String,
            dibayar: Boolean
        ) {
            val idPesanan = view.findViewById<TextView>(R.id.idPesanan)
            val carditem = view.findViewById<CardView>(R.id.cardItem)
            val bayarBtn = view.findViewById<Button>(R.id.bayarTagihan)
            val tanggalDeadline = view.findViewById<TextView>(R.id.deadLinePembayaran)
            val statusTxt = view.findViewById<TextView>(R.id.status)
            val statusCard = view.findViewById<CardView>(R.id.statusCard)
            val totalTagihanTxt = view.findViewById<TextView>(R.id.totalTagihan)
            val totalBayar = view.findViewById<TextView>(R.id.totalHarga)
            val pinaltiTxt = view.findViewById<TextView>(R.id.pinaltiPembayaran)

            idPesanan.text = "..." + idOrder.substring(8, idOrder.length)

            //jika tanggal sekarang lebih dari $deadline maka bunga
            // akan mulai dihitung dan text tanggal berubah warna orange
            if (Date().after(deadline!!)){
                val bunga = (tagihan * bungaTagihan / 100) * getBulanTelat(deadline)
                Constants.TAGIHAN_DB.document(idTagihan).update("jumlahPembayaranTelat", tagihan + bunga)
                totalBayar.text = rupiahFormat(tagihanTelat)
                tanggalDeadline.setTextColor(ContextCompat.getColor(this@TagihanActivity, R.color.orange))
            } else {
                totalBayar.text = rupiahFormat(tagihan)
            }

            //jika tanggal sekarang sama atau lebih dari $mulaiBayar maka button enable
            //bayarBtn.isEnabled = Date() == mulaiBayar || Date().after(mulaiBayar)

            if (Date() == mulaiBayar || Date().after(mulaiBayar)){
                bayarBtn.text = "BAYAR"
//                bayarBtn.isEnabled = true
                bayarBtn.setTextColor(ContextCompat.getColor(this@TagihanActivity, R.color.white))
                bayarBtn.setBackgroundColor(ContextCompat.getColor(this@TagihanActivity, R.color.biru_dasar))
            }

            if (Date().before(mulaiBayar)){
                bayarBtn.text = "BAYAR SEKARANG"
//                bayarBtn.isEnabled = true
                bayarBtn.setTextColor(ContextCompat.getColor(this@TagihanActivity, R.color.biru_dasar))
                bayarBtn.setBackgroundColor(ContextCompat.getColor(this@TagihanActivity, R.color.biru_muda))
            }

//            carditem.setOnClickListener {
//                //start detailPesanan activity
//                val intent = Intent(this@TagihanActivity, DetailPesananActivity::class.java)
//                intent.putExtra("ORDER_ID", idOrder)
//                startActivity(intent)
//            }

            bayarBtn.setOnClickListener {
                val intent = Intent(this@TagihanActivity, BayarTagihanActivity::class.java)
                intent.putExtra("TAGIHAN_ID", idTagihan)
                startActivity(intent)
            }

            ("Mulai Pembayaran " + getTimeDate(mulaiBayar)).also { view.findViewById<TextView>(R.id.mulaiPembayaran).text = it }
            ("Deadline Pembayaran " + getTimeDate(deadline)).also { tanggalDeadline.text = it }

            if (status == "MENUNGGU PEMBAYARAN") {
                statusCard.setCardBackgroundColor(Color.parseColor("#F8CAB6"))
                statusTxt.setTextColor(Color.parseColor("#EA5411"))
            } else {
                statusCard.setCardBackgroundColor(Color.parseColor("#B7DDF9"))
                statusTxt.setTextColor(Color.parseColor("#118EEA"))
            }

            if (Date().before(mulaiBayar)){
                statusTxt.text = "MENUGGU"
                statusCard.setCardBackgroundColor(ContextCompat.getColor(this@TagihanActivity, R.color.putih_abu_abu))
                statusTxt.setTextColor(ContextCompat.getColor(this@TagihanActivity, R.color.black))
            } else if (Date() == mulaiBayar || Date().after(mulaiBayar) && Date().before(deadline)){
                statusTxt.text = "MENUGGU PEMBAYARAN"
                statusCard.setCardBackgroundColor(ContextCompat.getColor(this@TagihanActivity, R.color.biru_muda))
                statusTxt.setTextColor(ContextCompat.getColor(this@TagihanActivity, R.color.biru_dasar))
            } else if (Date().after(deadline)){
                statusTxt.text = "MENUGGU PEMBAYARAN"
                statusCard.setCardBackgroundColor(ContextCompat.getColor(this@TagihanActivity, R.color.orangeMuda))
                statusTxt.setTextColor(ContextCompat.getColor(this@TagihanActivity, R.color.red))

                pinaltiTxt.visibility = View.VISIBLE
                pinaltiTxt.setTextColor(ContextCompat.getColor(this@TagihanActivity, R.color.red))
                tanggalDeadline.setTextColor(ContextCompat.getColor(this@TagihanActivity, R.color.red))
                bayarBtn.setTextColor(ContextCompat.getColor(this@TagihanActivity, R.color.white))
                bayarBtn.setBackgroundColor(ContextCompat.getColor(this@TagihanActivity, R.color.biru_dasar))
                //carditem.setCardBackgroundColor(ContextCompat.getColor(this@TagihanActivity, R.color.merah_muda))
                "Telat pembayaran ${getBulanTelat(deadline)} bulan : + bunga ${bungaTagihan*getBulanTelat(deadline)}%".also { pinaltiTxt.text = it }
            }

            if (dibayar){
                statusTxt.text = "SELESAI"
                idPesanan.alpha = 0.4F
                tanggalDeadline.alpha = 0.4F
                totalBayar.alpha = 0.4F
                statusCard.alpha = 0.4F
                view.findViewById<TextView>(R.id.mulaiPembayaran).alpha = 0.4F
                carditem.cardElevation = 0F
                carditem.setCardBackgroundColor(ContextCompat.getColor(this@TagihanActivity, R.color.putih_abu_abu))
                bayarBtn.visibility = View.INVISIBLE
            }

            if (urutan != ""){
                when(urutan){
                    "PERTAMA"->{

                    }
                    "KEDUA"->{

                    }
                    "KETIGA"->{

                    }
                }
            }

        }

    }

    private fun showMessage(idTagihan: String) {
        Toast.makeText(this, idTagihan, Toast.LENGTH_LONG).show()
    }

    private inner class TagihanFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<Cicilan>) :
        FirestoreRecyclerAdapter<Cicilan, ProductViewHolder>(
            options
        ) {
        override fun onBindViewHolder(
            productViewHolder: ProductViewHolder,
            position: Int,
            model: Cicilan
        ) {
            productViewHolder.setAll(
                model.id,
                model.biohseOrderId,
                model.mulaiPembayaran,
                model.deadLinePembayaran,
                model.statusPembayaran,
                model.jumlahPembayaran,
                model.jumlahPembayaranTelat,
                model.bunga,
                model.urutan,
                model.dibayar
            )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_tagihan,
                parent,
                false
            )
            return ProductViewHolder(view)
        }
    }

    private inner class TagihanPaidFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<Cicilan>) :
        FirestoreRecyclerAdapter<Cicilan, ProductViewHolder>(
            options
        ) {
        override fun onBindViewHolder(
            productViewHolder: ProductViewHolder,
            position: Int,
            model: Cicilan
        ) {
            productViewHolder.setAll(
                model.id,
                model.biohseOrderId,
                model.mulaiPembayaran,
                model.deadLinePembayaran,
                model.statusPembayaran,
                model.jumlahPembayaran,
                model.jumlahPembayaranTelat,
                model.bunga,
                model.urutan,
                model.dibayar
            )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_tagihan,
                parent,
                false
            )
            return ProductViewHolder(view)
        }
    }

    private fun getBulanTelat(deadline: Date?): Int{
        val sfd = SimpleDateFormat("M", Locale.getDefault())
        val calendar1 = Calendar.getInstance()
        val calendar2 = Calendar.getInstance()
        calendar1.time = deadline!!
        calendar2.time = Date()
        val date1 = DateTime().withDate(calendar1.get(Calendar.YEAR), sfd.format(deadline).toInt(), calendar1.get(Calendar.DAY_OF_MONTH))
        val date2 = DateTime().withDate(calendar2.get(Calendar.YEAR), sfd.format(Date()).toInt(), calendar2.get(Calendar.DAY_OF_MONTH))

        return Months.monthsBetween(date1, date2).months
    }

    private fun getTimeDate(timestamp: Date?): String? {
        return try {
            //Date netDate = (timestamp);
            val sfd = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
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

}