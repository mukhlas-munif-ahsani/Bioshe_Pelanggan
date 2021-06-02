package com.munifahsan.biosheapp.ui.promo

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.databinding.ActivityPromoBinding
import com.munifahsan.biosheapp.domain.Product
import com.munifahsan.biosheapp.domain.Promo
import com.munifahsan.biosheapp.ui.detailProduk.DetailProductActivity
import com.munifahsan.biosheapp.ui.keranjang.CartActivity
import com.munifahsan.biosheapp.utils.CheckConection
import com.munifahsan.biosheapp.utils.Constants
import com.munifahsan.biosheapp.utils.SpacesItemDecoration
import com.squareup.picasso.Picasso
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class PromoActivity : AppCompatActivity(), PromoContract.View {
    private lateinit var mPres: PromoContract.Presenter
    private lateinit var binding: ActivityPromoBinding
    private var adapterProduct: ProductPromoFirestoreRecyclerAdapter? = null
    private val dbPromo = FirebaseFirestore.getInstance()
        .collection("PROMO")
    var promoId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPromoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mPres = PromoPresenter(this)

        promoId = intent.getStringExtra("PROMO_ID").toString()

        getData(promoId)

        //Toast.makeText(this, promoId, Toast.LENGTH_LONG).show()

        binding.cartIcon.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        binding.backIcon.setOnClickListener {
            finish()
        }

        showProduct()
        showKeranjangSize()
    }

    private fun getData(id: String) {
        dbPromo.document(id).get()
            .addOnSuccessListener { documentSnapshot ->
                val promo = documentSnapshot.toObject(Promo::class.java)
                Picasso.get().load(promo!!.imageThumbnail).into(binding.promoImage)
            }.addOnFailureListener {

            }
    }

    override fun onStart() {
        super.onStart()
        adapterProduct?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterProduct?.stopListening()
    }

    private fun showProduct() {
        binding.rvPromo.layoutManager = GridLayoutManager(
            this,
            2,
            GridLayoutManager.VERTICAL,
            false
        )
        binding.rvPromo.addItemDecoration(
            SpacesItemDecoration(
                22
            )
        )
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef.collection("PRODUCT").whereEqualTo("show", true).whereEqualTo("selesai", true).whereEqualTo("promoId", promoId)
        val options = FirestoreRecyclerOptions
            .Builder<Product>()
            .setQuery(query, Product::class.java)
            .build()
        adapterProduct = ProductPromoFirestoreRecyclerAdapter(options)
        binding.rvPromo.adapter = adapterProduct
    }

    private inner class ProductViewHolder(private val view: View) :
        RecyclerView.ViewHolder(
            view
        ) {

        fun setThumbnailProduct(thumnailUrl: String) {
            val image = view.findViewById<ImageView>(R.id.thumbnailImage)
            Picasso.get()
                .load(thumnailUrl)
                .placeholder(R.drawable.black_transparent)
                .into(image)
        }

        fun setProductName(productName: String) {
            val textView = view.findViewById<TextView>(R.id.productName)
            textView.text = productName
        }

        fun setProductDiscon(discon: Int) {
            val textView = view.findViewById<TextView>(R.id.disconTxt)
            val linDiscon = view.findViewById<LinearLayout>(R.id.linDiscon)
            if (discon == 0) {
                linDiscon.visibility = View.GONE
            } else {
                linDiscon.visibility = View.VISIBLE
            }

            textView.text = discon.toString()
        }

        fun setProductDisconPrice(disconPrise: Int) {
            val textView = view.findViewById<TextView>(R.id.priceDisconTxt)

            textView.text = rupiahFormat(disconPrise)

            textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }

        fun setProductPrice(price: Int, discon: Int) {
            val textView = view.findViewById<TextView>(R.id.priceTxt)
            val dis: Int = price * discon / 100

            textView.text = rupiahFormat(price - dis)

            if (discon == 0) {
                (textView.layoutParams as RelativeLayout.LayoutParams).apply {
                    bottomMargin = 40
                }
            }
        }

        fun setClickableView(productId: String) {
            val addKeranjangBtn = view.findViewById<CardView>(R.id.keranjangBtn)
            val product = view.findViewById<CardView>(R.id.product)
            addKeranjangBtn.setOnClickListener {
//                viewModel.postKeranjang(productId).observe(viewLifecycleOwner, {
//                    if (it.status == Status.SUCCESS) {
//                        //showMessage("Product ")
//                        (activity as MainActivity?)?.showKeranjangSize()
//                    } else if (it.status == Status.ERROR) {
//                        showMessage("${it.message}")
//                    }
//                })
                if (CheckConection.isNetworkAvailable(this@PromoActivity)) {
                    addKeranjangBtn.isEnabled = false
                    mPres.postKeranjang(productId)
                    //enable button dalam 10 detik untuk menghindari double klik
                    Handler().postDelayed({
                        addKeranjangBtn.isEnabled = true
                    }, 10000)
                } else {
                    showMessage("Mohon periksa kembali konesi internet anda")
                }
            }

            product.setOnClickListener {
                startDetailProductActivity(productId)
            }
        }
    }

    private inner class ProductPromoFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<Product>) :
        FirestoreRecyclerAdapter<Product, ProductViewHolder>(
            options
        ) {
        override fun onBindViewHolder(
            productViewHolder: ProductViewHolder,
            position: Int,
            productModel: Product
        ) {
            productViewHolder.setThumbnailProduct(productModel.thumbnail)
            productViewHolder.setProductName(productModel.nama)
            productViewHolder.setProductDiscon(productModel.diskon)
            productViewHolder.setProductDisconPrice(productModel.harga)
            productViewHolder.setProductPrice(
                productModel.harga,
                productModel.diskon
            )
            productViewHolder.setClickableView(productModel.id)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_product_2,
                parent,
                false
            )
            return ProductViewHolder(view)
        }
    }

    private fun showKeranjangSize() {
//        viewModel.getKeranjangItemSize().observe(this, {
//            if (it.status == Status.SUCCESS) {
//                binding.keranjangSize.text = it.data.toString()
//            }
//        })
        Constants.KERANJANG_DB.addSnapshotListener { value, error ->
            if (value != null) {
                binding.keranjangSize.text = value.size().toString()
                for (field in value){
                    /*
                    update apakah produk tersedia
                    */
                    Constants.PRODUK_DB.document(field.getString("productId").toString())
                        .get()
                        .addOnSuccessListener {
                            if (it.exists()) {
                                if (!it.getBoolean("show")!!){
                                    Constants.KERANJANG_DB.document(field.id).delete()
                                }
                            } else {
                                Constants.KERANJANG_DB.document(field.id).delete()
                            }
                        }
                }
            }

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

    fun startDetailProductActivity(productId: String) {
        val intent = Intent(this, DetailProductActivity::class.java)
        intent.putExtra("PRODUCT_ID", productId)
        startActivity(intent)
    }

    override fun showMessage(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}