package com.munifahsan.biosheapp.ui.detailProduk

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.munifahsan.biosheapp.databinding.ActivityDetailProductBinding
import com.munifahsan.biosheapp.domain.Images
import com.munifahsan.biosheapp.ui.keranjang.CartActivity
import com.munifahsan.biosheapp.adapters.ProductImageAdapter
import com.munifahsan.biosheapp.usecase.UserUseCase
import com.munifahsan.biosheapp.utils.CheckConection
import com.munifahsan.biosheapp.viewmodel.UserViewModelFactory
import com.munifahsan.biosheapp.vo.Status
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class DetailProductActivity : AppCompatActivity(), DetailProdukContract.View {
    private lateinit var binding: ActivityDetailProductBinding
    private lateinit var mPres: DetailProdukContract.Presenter
    private var imageAdapter: ProductImageAdapter =
        ProductImageAdapter()
    private val mProductRef: CollectionReference =
        FirebaseFirestore.getInstance().collection("PRODUCT")

    var productId = ""
    private val viewModel by lazy {
        ViewModelProvider(
            this,
            UserViewModelFactory(UserUseCase())
        ).get(DetailProductViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mPres = DetailProdukPresenter(this)

//        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val intent = intent
        productId = intent.getStringExtra("PRODUCT_ID").toString()

        //showMessage(productId.toString())
        mProductRef.document(productId.toString()).collection("IMAGES").orderBy("nomor", Query.Direction.ASCENDING).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    //showMessage("success")
                    for (documentSnapshot: QueryDocumentSnapshot in it.result!!) {
                        val images = documentSnapshot.toObject(Images::class.java)
                        imageAdapter.addCardItem(images)
                    }
                    binding.imageSlider.setSliderAdapter(imageAdapter)
                } else {
                    showMessage(it.exception?.message.toString())
                }
            }

        binding.imageSlider.setIndicatorAnimation(IndicatorAnimationType.WORM) //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        binding.imageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
        binding.imageSlider.autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH
        binding.imageSlider.indicatorSelectedColor = Color.parseColor("#118EEA")
        binding.imageSlider.indicatorUnselectedColor = Color.parseColor("#F5F5F5")
        binding.imageSlider.isAutoCycle = false

        binding.backIcon.setOnClickListener {
            finish()
        }

        binding.namaProdut.visibility = View.INVISIBLE
        binding.namaProdutShimmer.visibility = View.VISIBLE
        viewModel.getProductData(productId.toString(), "nama").observe(this, {
            if (it.status == Status.SUCCESS) {
                binding.namaProdut.visibility = View.VISIBLE
                binding.namaProdutShimmer.visibility = View.INVISIBLE
                binding.namaProdut.text = it.data
            } else if (it.status == Status.ERROR) {
                showMessage("${it.data}")
            }
        })

        binding.linDiscon.visibility = View.INVISIBLE
        binding.linDisconShimmer.visibility = View.VISIBLE
        binding.priceProduct.visibility = View.INVISIBLE
        binding.priceProductShimmer.visibility = View.VISIBLE
        viewModel.getProductDataInt(productId.toString(), "harga").observe(this, {
            if (it.status == Status.SUCCESS) {

                val price = it.data!!.toInt()
                binding.priceDisconTxt.text = rupiahFormat(price)
                binding.priceDisconTxt.paintFlags = binding.priceDisconTxt.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                viewModel.getProductDataInt(productId.toString(), "diskon")
                    .observe(this, { discon ->
                        if (it.status == Status.SUCCESS) {

                            binding.linDiscon.visibility = View.VISIBLE
                            binding.linDisconShimmer.visibility = View.INVISIBLE
                            binding.priceProduct.visibility = View.VISIBLE
                            binding.priceProductShimmer.visibility = View.INVISIBLE

                            if (discon.data!!.toInt() == 0){
                                binding.linDiscon.visibility = View.INVISIBLE
                            } else{
                                binding.linDiscon.visibility = View.VISIBLE
                            }

                            binding.disconTxt.text = discon.data.toString()

                            val disconNum: Int = discon.data.toInt()
                            val disconHarga: Int = price * disconNum / 100

                            binding.priceProduct.text = rupiahFormat(price - disconHarga)

                        } else if (it.status == Status.ERROR) {
                            showMessage("${it.data}")
                        }
                    })

            } else if (it.status == Status.ERROR) {
                showMessage("${it.data}")
            }
        })

        binding.keteranganProduct.visibility = View.INVISIBLE
        binding.keteranganProductShimmer.visibility = View.VISIBLE
        viewModel.getProductData(productId.toString(), "keterangan").observe(this, {
            if (it.status == Status.SUCCESS) {

                binding.keteranganProduct.visibility = View.VISIBLE
                binding.keteranganProductShimmer.visibility = View.INVISIBLE

                binding.keteranganProduct.text = it.data
            } else if (it.status == Status.ERROR) {
                showMessage("${it.data}")
            }
        })

        binding.addKeranjang.setOnClickListener {
            if (CheckConection.isNetworkAvailable(this)){
                binding.addKeranjang.isEnabled = false
                mPres.postKeranjang(productId)
                //delay 1 ment
                Handler().postDelayed({
                    binding.addKeranjang.isEnabled = true
                }, 60000)
            } else {
                showMessage("Mohon periksa kembali konesi internet anda")
            }

        }

        binding.cartIcon.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        showKeranjangSize()
    }

    fun showKeranjangSize() {
        viewModel.getKeranjangItemSize().observe(this, {
            if (it.status == Status.SUCCESS) {
                binding.keranjangSize.text = it.data.toString()
            }
        })
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