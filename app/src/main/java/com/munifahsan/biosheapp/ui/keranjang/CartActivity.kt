package com.munifahsan.biosheapp.ui.keranjang

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.databinding.ActivityCartBinding
import com.munifahsan.biosheapp.domain.Keranjang
import com.munifahsan.biosheapp.ui.aturPesanan.AturPesananActivity
import com.munifahsan.biosheapp.usecase.UserUseCase
import com.munifahsan.biosheapp.utils.CheckConection
import com.munifahsan.biosheapp.utils.Constants
import com.munifahsan.biosheapp.viewmodel.UserViewModelFactory
import com.munifahsan.biosheapp.vo.Resource
import com.munifahsan.biosheapp.vo.Status
import com.squareup.picasso.Picasso
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private var adapterCart: ProductFirestoreRecyclerAdapter? = null
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val dbKeranjang: CollectionReference =
        FirebaseFirestore.getInstance().collection("USERS")
            .document(auth.currentUser?.uid.toString())
            .collection("KERANJANG")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.backIcon.setOnClickListener {
            finish()
        }

        binding.cardChecoutBtn.setOnClickListener {
            if (CheckConection.isNetworkAvailable(this)){
                val intent = Intent(this, AturPesananActivity::class.java)
                startActivity(intent)
            }else{
                showMessage("Mohon periksa kembali konesi internet anda")
            }
        }

//        getCount(dbKeranjang, auth.currentUser?.uid.toString()).addOnSuccessListener {
//            binding.totalBarang.text = it.toString()
//        }

        showTotalBarangHarga(dbKeranjang)

        showItemCart()
    }

    override fun onStart() {
        super.onStart()
        adapterCart?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterCart?.stopListening()
    }

    private fun showItemCart() {
        binding.rvCart.layoutManager = LinearLayoutManager(
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
        binding.rvCart.adapter = adapterCart
    }

    private inner class ProductViewHolder(private val view: View) :
        RecyclerView.ViewHolder(
            view
        ) {

        fun setProduct(
            itemKeranjangId: String,
            productId: String, jumlahItem: Int,
            namaProduct: String, thumbnailProduct: String,
            hargaProduct: Int, disconProduct: Int
        ) {
            val productName = view.findViewById<TextView>(R.id.productName)
            val priceDiscon = view.findViewById<TextView>(R.id.priceDisconTxt)
            val disconTxt = view.findViewById<TextView>(R.id.disconTxt)
            val priceProduct = view.findViewById<TextView>(R.id.priceProduct)
            val linDiscon = view.findViewById<LinearLayout>(R.id.linDiscon)
            val image = view.findViewById<ImageView>(R.id.thumbnailImage)
            val jumlahItemTxt = view.findViewById<TextView>(R.id.jumlahItem)

            jumlahItemTxt.text = jumlahItem.toString()

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

            /*
            update thumbnail keranjang produk
             */
            Constants.PRODUK_DB.document(productId)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        Constants.KERANJANG_DB.document(itemKeranjangId)
                            .update("thumbnail", it.getString("thumbnail"))
                            .addOnSuccessListener {
                                //error
                            }
                    }
                }

            /*
            update nama keranjang produk
             */
            Constants.PRODUK_DB.document(productId)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        Constants.KERANJANG_DB.document(itemKeranjangId)
                            .update("nama", it.getString("nama"))
                            .addOnSuccessListener {
                                //error
                            }
                    }
                }

            /*
           update harga keranjang produk
            */
            Constants.PRODUK_DB.document(productId)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        Constants.KERANJANG_DB.document(itemKeranjangId)
                            .update("harga", it.getLong("harga")!!.toInt())
                            .addOnSuccessListener {
                                //error
                            }
                    }
                }

            /*
           update diskon keranjang produk
            */
            Constants.PRODUK_DB.document(productId)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        Constants.KERANJANG_DB.document(itemKeranjangId)
                            .update("diskon", it.getLong("diskon")!!.toInt())
                            .addOnSuccessListener {
                                //error
                            }
                        setJumlahClickableItem(itemKeranjangId, jumlahItem, harga)
                    }
                }

            /*
            update berat keranjang produk
             */
            Constants.PRODUK_DB.document(productId)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        Constants.KERANJANG_DB.document(itemKeranjangId)
                            .update("berat", it.getLong("berat"))
                            .addOnSuccessListener {
                                //error
                            }
                    }
                }

        }

        fun setJumlahClickableItem(itemKeranjangId: String, jumlahItem: Int, harga: Int) {
            val jumlahItemTxt = view.findViewById<TextView>(R.id.jumlahItem)

            val increaseItem = view.findViewById<CardView>(R.id.increaseItem)
            val decreaseItem = view.findViewById<CardView>(R.id.decreaseItem)
            val removeItem = view.findViewById<CardView>(R.id.removeItem)

            if (jumlahItem <= 1) {
                decreaseItem.isEnabled = false
            }

            if (jumlahItem > 1) {
                decreaseItem.isEnabled = true
            }

            if (jumlahItem <= 0) {
                //viewModel.editKeranjangItemInt(itemKeranjangId, "jumlahItem", 1)
                Constants.KERANJANG_DB.document(itemKeranjangId)
                    .update("jumlahItem", 1)
            }

            // when increase button clicked
            increaseItem.setOnClickListener {

                dbKeranjang.document(itemKeranjangId)
                    .update("jumlahItem", FieldValue.increment(+1))
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener {

                    }

                decreaseItem.isEnabled = true

//                val jumlah = jumlahItem + 1
//                //jumlahItemTxt.text = jumlah.toString()
//                viewModel.editKeranjangItem(itemKeranjangId, "jumlahItem", jumlah).observe(this@CartActivity, { a ->
//                        if (a.status == Status.SUCCESS) {
//
//                        } else if (a.status == Status.ERROR) {
//                            showMessage("${a.data}")
//                        }
//                    })

                //tambahHarga(harga.toLong())
                //tambahBarang()

//                decreaseItem.isEnabled = true
            }

            // when decrease button clicked
            decreaseItem.setOnClickListener {
                if (jumlahItem != 1) {

                    dbKeranjang.document(itemKeranjangId)
                        .update("jumlahItem", FieldValue.increment(-1))
                        .addOnSuccessListener {

                        }
                        .addOnFailureListener {

                        }


//                    val jumlah = jumlahItem - 1
//                    //jumlahItemTxt.text = jumlah.toString()
//                    viewModel.editKeranjangItem(itemKeranjangId, "jumlahItem", jumlah)
//                        .observe(this@CartActivity, {a->
//                            if (a.status == Status.SUCCESS) {
//
//                            } else if (a.status == Status.ERROR) {
//                                showMessage("${a.data}")
//                            }
//                        })

                    //kurangHarga(harga.toLong())
                    //kurangBarang()
                }

//                if (jumlahItem == 1){
//                    decreaseItem.isEnabled = false
//                }
            }

            // when remove button clicked
            removeItem.setOnClickListener {
//                viewModel.deleteKeranjangItem(itemKeranjangId).observe(this@CartActivity, {
//                    if (it.status == Status.SUCCESS) {
//                        //showMessage("deleted")
//                    } else if (it.status == Status.ERROR) {
//                        showMessage("${it.data}")
//                    }
//                })
                Constants.KERANJANG_DB.document(itemKeranjangId).delete()
            }

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
                keranjangModel.diskon
            )
            //productViewHolder.setJumlahClickableItem(keranjangModel.id, keranjangModel.jumlahItem)

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_keranjang,
                parent,
                false
            )
            return ProductViewHolder(view)
        }
    }

    private fun showTotalBarangHarga(ref: CollectionReference) {
        ref.addSnapshotListener { value, error ->
            val jumlah = ArrayList<Int>()
            var jumlahItem = 0
            if (value != null) {
                for (field in value) {
                    val a = field.getLong("harga")
                    val b = field.getLong("jumlahItem")
                    val c = field.getLong("diskon")
                    jumlahItem += b!!.toInt()
                    val disconNum: Int = c!!.toInt()
                    val disconHarga: Int = a!!.toInt() * disconNum / 100
                    val harga = a - disconHarga
                    jumlah.add(harga.toInt() * b.toInt())
//                field.getLong("hargaProduct")?.let { hargaProduct->
//                    field.getLong("jumlahItem")?.let {
//                        jumlah.add(hargaProduct.toInt()*it.toInt())
//                    }
//                }
                }
            }
            binding.totalBarang.text = jumlahItem.toString()
            binding.totalHarga.text = rupiahFormat(jumlah.sum())
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

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}