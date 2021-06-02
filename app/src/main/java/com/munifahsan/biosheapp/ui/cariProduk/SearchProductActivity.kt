 package com.munifahsan.biosheapp.ui.cariProduk

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.munifahsan.biosheapp.MainActivity
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.adapters.SearchAdapter
import com.munifahsan.biosheapp.databinding.ActivitySearchProductBinding
import com.munifahsan.biosheapp.domain.Product
import com.munifahsan.biosheapp.utils.SpacesItemDecoration
import com.munifahsan.biosheapp.ui.detailProduk.DetailProductActivity
import com.munifahsan.biosheapp.ui.keranjang.CartActivity
import com.munifahsan.biosheapp.usecase.UserUseCase
import com.munifahsan.biosheapp.utils.CheckConection
import com.munifahsan.biosheapp.utils.Constants
import com.munifahsan.biosheapp.viewmodel.UserViewModelFactory
import com.munifahsan.biosheapp.vo.Status
import com.squareup.picasso.Picasso
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


class SearchProductActivity : AppCompatActivity(), SearchProdukContract.View {
    private lateinit var mPres: SearchProdukContract.Presenter
    private lateinit var binding: ActivitySearchProductBinding
    private var adapterProductTerlaris: ProductTerlarisFirestoreRecyclerAdapter? =
        null
    private var mAdapter: SearchAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchProductBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mPres = SearchProdukPresenter(this)

        mAdapter = SearchAdapter()

        binding.cartIcon.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        binding.searchEdt.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                return@OnEditorActionListener true
            }
            false
        })

        binding.backIcon.setOnClickListener {
            finish()
        }

        showKeranjangSize()
        showProduct()

    }

    override fun onStart() {
        super.onStart()
        //adapterProductTerlaris?.startListening()
    }

    override fun onStop() {
        super.onStop()
        //adapterProductTerlaris?.stopListening()
    }

    private fun showProduct() {
        binding.rvProduk1.layoutManager = GridLayoutManager(
            this,
            2,
            GridLayoutManager.VERTICAL,
            false
        )
        binding.rvProduk1.addItemDecoration(
            SpacesItemDecoration(
                22
            )
        )
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef.collection("PRODUCT").whereEqualTo("show",true).whereEqualTo("selesai", true)

        query.get().addOnCompleteListener {
            mAdapter!!.setListModels(it.result!!.toObjects(Product::class.java))
            binding.rvProduk1.adapter = mAdapter
        }
        mAdapter!!.notifyDataSetChanged()

        binding.searchEdt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                mAdapter!!.filter.filter(p0.toString())
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                mAdapter!!.filter.filter(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
                //TODO("Not yet implemented")
            }

        })

        mAdapter!!.setOnListItemCliked { id, position ->
            startDetailProductActivity(id)
        }

        mAdapter!!.setOnAddItemCliked { id, position, view ->
//            viewModel.postKeranjang(id).observe(this@SearchProductActivity, {
//                if (it.status == Status.SUCCESS) {
//                    //showMessage("Product ")
////                    (this as MainActivity?)?.showKeranjangSize()
//                } else if (it.status == Status.ERROR) {
//                    showMessage("${it.message}")
//                }
//            })
            if (CheckConection.isNetworkAvailable(this)) {
                view.isEnabled = false
                mPres.postKeranjang(id)
                //enable button dalam 10 detik untuk menghindari double klik
                Handler().postDelayed({
                    view.isEnabled = true
                }, 10000)
            } else {
                showMessage("Mohon periksa kembali konesi internet anda")
            }
        }

        val options = FirestoreRecyclerOptions
            .Builder<Product>()
            .setQuery(query, Product::class.java)
            .build()
        //adapterProductTerlaris = ProductTerlarisFirestoreRecyclerAdapter(options)
        //binding.rvPrductKami.adapter = adapterProductTerlaris
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
//                viewModel.postKeranjang(productId).observe(this@SearchProductActivity, {
//                    if (it.status == Status.SUCCESS) {
//                        //showMessage("Product ")
//                        (this as MainActivity?)?.showKeranjangSize()
//                    } else if (it.status == Status.ERROR) {
//                        showMessage("${it.message}")
//                    }
//                })
                if (CheckConection.isNetworkAvailable(this@SearchProductActivity)) {
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

    private var searchItem: MutableList<Product> = mutableListOf()
    private var searchItemFull: MutableList<Product> = mutableListOf()
    private inner class ProductTerlarisFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<Product>) :
        FirestoreRecyclerAdapter<Product, ProductViewHolder>(
            options
        ), Filterable {
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

        override fun getItemCount(): Int {
            return super.getItemCount()
        }

        fun search(text: String){

            notifyDataSetChanged()
        }

//        private val listFilter: Filter = object : Filter() {
//            override fun performFiltering(charSequence: CharSequence): FilterResults {
//                val filteredList: MutableList<Product> = ArrayList<Product>()
//                if (charSequence.isEmpty()) {
//                    filteredList.addAll(searchItemFull)
//                } else {
//                    val filterPattern = charSequence.toString().toLowerCase().trim { it <= ' ' }
//                    for (item in searchItemFull) {
//                        if (item.namaProduct.toLowerCase().contains(filterPattern)) {
//                            filteredList.add(item)
//                        }
//                    }
//                }
//                val results = FilterResults()
//                results.values = filteredList
//                return results
//            }
//
//            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
//                searchItem.clear()
//                searchItem.addAll(filterResults.values as Collection<Product>)
//                notifyDataSetChanged()
//            }
//        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_product_2,
                parent,
                false
            )
            return ProductViewHolder(view)
        }

        override fun getFilter(): Filter {
            TODO("Not yet implemented")
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

    fun startDetailProductActivity(productId: String) {
        val intent = Intent(this, DetailProductActivity::class.java)
        intent.putExtra("PRODUCT_ID", productId)
        startActivity(intent)
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