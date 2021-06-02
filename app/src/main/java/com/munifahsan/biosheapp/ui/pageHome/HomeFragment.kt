package com.munifahsan.biosheapp.ui.pageHome

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.domain.Product
import com.munifahsan.biosheapp.ui.detailProduk.DetailProductActivity
import com.munifahsan.biosheapp.ui.cariProduk.SearchProductActivity
import com.munifahsan.biosheapp.utils.SpacesItemDecoration
import com.munifahsan.biosheapp.ui.notif.NotifActivity
import com.munifahsan.biosheapp.ui.promo.PromoActivity
import com.munifahsan.biosheapp.ui.reward.RewardActivity
import com.munifahsan.biosheapp.databinding.FragmentHomeBinding
import com.munifahsan.biosheapp.ui.keranjang.CartActivity
import com.munifahsan.biosheapp.ui.tagihan.TagihanActivity
import com.munifahsan.biosheapp.utils.CheckConection
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import org.joda.time.DateTime
import org.joda.time.Months
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class HomeFragment : Fragment(), HomeContract.View {

    private lateinit var mPres: HomeContract.Presenter

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    var myProductId: String = ""
    private var groupAdapter = GroupAdapter<ViewHolder>()
    private lateinit var auth: FirebaseAuth
    private var adapterProduct1: ProductFirestoreRecyclerAdapter? = null
    private var adapterProduct2: ProductTerlarisFirestoreRecyclerAdapter? = null
    private var mPromoAdapter: PromoAdapter = PromoAdapter()
    private val mPromoRef: CollectionReference =
        FirebaseFirestore.getInstance().collection("PROMO")
    private val dbUsers = FirebaseFirestore.getInstance()
        .collection("USERS")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        _binding!!.searchBtn.setOnClickListener {
            val intent = Intent(activity, SearchProductActivity::class.java)
            startActivity(intent)
        }

        binding.productLihatSemua.setOnClickListener {
            val intent = Intent(activity, SearchProductActivity::class.java)
            startActivity(intent)
        }

        binding.notifIcon.setOnClickListener {
            val intent = Intent(activity, NotifActivity::class.java)
            startActivity(intent)
        }

        binding.bayarBtn.setOnClickListener {
            val intent = Intent(activity, TagihanActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        mPres = HomePresenter(this)

        mPromoRef.whereEqualTo("selesai", true).whereEqualTo("show", true)
            .get().addOnSuccessListener { value ->
                //showMessage("success")
                if (value != null) {
                    for (field in value) {
                        //val promo = documentSnapshot.toObject(Promo::class.java)
                        mPromoAdapter.addCardItem(
                            PromoModel(
                                field.id,
                                field.getString("imageThumbnail")
                            )
                        )
                    }
                }
//                if (error != null) {
//                    showMessage("Error : $error")
//                    Log.d("HomeFragmentError : ", "$error")
//                }

                binding.imageSliderPromoHome.setSliderAdapter(mPromoAdapter)
            }

        binding.imageSliderPromoHome.setIndicatorAnimation(IndicatorAnimationType.DROP) //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        binding.imageSliderPromoHome.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
        binding.imageSliderPromoHome.autoCycleDirection =
            SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH
        binding.imageSliderPromoHome.indicatorSelectedColor = Color.parseColor("#118EEA")
        binding.imageSliderPromoHome.indicatorUnselectedColor = Color.parseColor("#F5F5F5")
        binding.imageSliderPromoHome.scrollTimeInSec = 5
        binding.imageSliderPromoHome.isAutoCycle = true
        binding.imageSliderPromoHome.startAutoCycle()

        mPromoAdapter.setOnListItemCliked { id, position ->
            val intent = Intent(activity, PromoActivity::class.java)
            intent.putExtra("PROMO_ID", id)
            startActivity(intent)
            //showMessage(id)
        }

        showBioshePoints()
        showTagihan()
        showProduct1()
        //dekorasi untuk jarak atar item yang di tampilkan
        binding.rvProduk2.addItemDecoration(
            SpacesItemDecoration(
                22
            )
        )
        showProduct2()
    }

    override fun onStart() {
        super.onStart()
        binding.rvProduk1.visibility = View.GONE
        binding.shimmerProduk1.visibility = View.VISIBLE
        binding.rvProduk2.visibility = View.GONE
        binding.shimmerProduk2.visibility = View.VISIBLE
        showProduct1()
        showProduct2()
        adapterProduct1?.startListening()
        adapterProduct2?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterProduct1?.stopListening()
        adapterProduct2?.stopListening()
    }

    private fun showBioshePoints() {
        binding.bioshePoints.visibility = View.INVISIBLE
        binding.bioshePointsShimmer.visibility = View.VISIBLE

        dbUsers.document(auth.currentUser!!.uid).addSnapshotListener { value, error ->
            if (value != null) {
                binding.bioshePoints.text = value.getLong("bioshePoints").toString()
                binding.bioshePoints.visibility = View.VISIBLE
                binding.bioshePointsShimmer.visibility = View.INVISIBLE
            }

            if (error != null) {
                showMessage("Error : $error")
                Log.d("HomeFragmentError : ", "$error")
            }
        }

        binding.bioshePoints.setOnClickListener {
            val intent = Intent(activity, RewardActivity::class.java)
            startActivity(intent)
        }

        binding.headerImage.setOnClickListener {
            val intent = Intent(activity, RewardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showTagihan() {
        //binding.deadLine.visibility = View.INVISIBLE
        //binding.deadLineShimmer.visibility = View.VISIBLE
        binding.jumlahTagihan.visibility = View.INVISIBLE
        binding.tagihanShimmer.visibility = View.VISIBLE

        dbUsers.document(auth.currentUser!!.uid).collection("TAGIHAN").whereEqualTo("dibayar", false)
            .addSnapshotListener { value, error ->
                val tagihan = ArrayList<Int>()
                if (value != null) {
                    for (field in value) {
                        val a = field.getLong("jumlahPembayaran")
                        val b = field.getDate("deadLinePembayaran")
                        val c = field.getLong("bunga")
                        val d = field.getLong("jumlahPembayaranTelat")

                        if (a != null && b != null && c != null) {
                            if (Date().after(b)) {
                                val bunga = (a.toInt() * c.toInt() / 100) * getBulanTelat(b)
                                tagihan.add(d!!.toInt())
                            } else {
                                tagihan.add(a.toInt())
                            }
                        }

                    }
                }
                binding.jumlahTagihan.text = rupiahFormat(tagihan.sum())
                binding.jumlahTagihan.visibility = View.VISIBLE
                binding.tagihanShimmer.visibility = View.INVISIBLE
            }
    }

    private fun showProduct1() {

        binding.rvProduk1.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        val rootRef = FirebaseFirestore.getInstance()
        val query =
            rootRef.collection("PRODUCT").whereEqualTo("show", true).whereEqualTo("selesai", true)
                .orderBy("sold", Query.Direction.DESCENDING).limit(8)

        val options = FirestoreRecyclerOptions
            .Builder<Product>()
            .setQuery(query, Product::class.java)
            .build()
        adapterProduct1 = ProductFirestoreRecyclerAdapter(options)
        binding.rvProduk1.adapter = adapterProduct1

        query.get().addOnSuccessListener {
            binding.rvProduk1.visibility = View.VISIBLE
            binding.shimmerProduk1.visibility = View.GONE
        }
    }

    private fun showProduct2() {
        binding.rvProduk2.visibility = View.GONE
        binding.shimmerProduk2.visibility = View.VISIBLE
        //gridLayout agar item yang ditempilkan berbentuk grid
        binding.rvProduk2.layoutManager = GridLayoutManager(
            activity,
            2,
            GridLayoutManager.VERTICAL,
            false
        )

        val rootRef = FirebaseFirestore.getInstance()
        val query =
            rootRef.collection("PRODUCT").whereEqualTo("show", true).whereEqualTo("selesai", true)
        query.get().addOnSuccessListener {
            binding.rvProduk2.visibility = View.VISIBLE
            binding.shimmerProduk2.visibility = View.GONE
        }
        val options = FirestoreRecyclerOptions
            .Builder<Product>()
            .setQuery(query, Product::class.java)
            .build()
        adapterProduct2 = ProductTerlarisFirestoreRecyclerAdapter(options)
        binding.rvProduk2.adapter = adapterProduct2
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
                if (CheckConection.isNetworkAvailable(activity!!)) {
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

    private inner class ProductFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<Product>) :
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
                R.layout.item_product,
                parent,
                false
            )
            return ProductViewHolder(view)
        }
    }

    private inner class ProductTerlarisFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<Product>) :
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
        if (activity != null) {
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    fun startDetailProductActivity(productId: String) {
        val intent = Intent(activity, DetailProductActivity::class.java)
        intent.putExtra("PRODUCT_ID", productId)
        startActivity(intent)
    }

    private fun getBulanTelat(deadline: Date?): Int {
        val sfd = SimpleDateFormat("M", Locale.getDefault())
        val calendar1 = Calendar.getInstance()
        val calendar2 = Calendar.getInstance()
        calendar1.time = deadline!!
        calendar2.time = Date()
        val date1 = DateTime().withDate(
            calendar1.get(Calendar.YEAR),
            sfd.format(deadline).toInt(),
            calendar1.get(Calendar.DAY_OF_MONTH)
        )
        val date2 = DateTime().withDate(
            calendar2.get(Calendar.YEAR),
            sfd.format(Date()).toInt(),
            calendar2.get(Calendar.DAY_OF_MONTH)
        )

        return Months.monthsBetween(date1, date2).months
    }

}