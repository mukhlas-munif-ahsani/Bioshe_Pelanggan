package com.munifahsan.biosheapp.ui.transferBayar

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.databinding.ActivityTransferBayarBinding
import com.munifahsan.biosheapp.ui.tampilGambar.ImageViewActivity
import com.squareup.picasso.Picasso

class TransferBayarActivity : AppCompatActivity(), Contract.View {

    private lateinit var binding: ActivityTransferBayarBinding
    private lateinit var mPres: Contract.Presenter
    private val PICK_IMAGE_REQUEST = 1
    private var mImageUri: Uri? = null
    private val dbOrders = FirebaseFirestore.getInstance()
        .collection("ORDERS")
    private var adapterImage: TransferBayarActivity.ImageFirestoreRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferBayarBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mPres = Presenter(this)

        val orderId = intent.getStringExtra("ORDER_ID").toString()

        dbOrders.document(orderId).addSnapshotListener { value, error ->
            if (value!!.getString("orderStatus").equals("MENUNGGU KONFIRMASI")){
                binding.cardStatus.visibility = View.VISIBLE
                binding.status.text = value.getString("orderStatus")
            } else {
                binding.cardStatus.visibility = View.GONE
            }
        }

        mPres.getData(orderId)

        binding.uploadImgBtn.setOnClickListener {
            if (mImageUri == null){
                openFileChooser()
            } else {
                //Log.d("UPLOADBTN", "ga kosin")
                mPres.uploadImage(mImageUri, orderId)
                binding.uploadImgBtn.isClickable = false
                binding.uploadImgBtn.isEnabled = false
                binding.uploadImgBtn.setCardBackgroundColor(Color.parseColor("#B8CCDC"))
            }
        }

        binding.backIcon.setOnClickListener {
            finish()
        }

        showBukti(orderId)
    }

    override fun showTotalBayar(bayar: String){
        binding.jumlahPembayaran.text = bayar
        binding.jumlahPembayaran.visibility = View.VISIBLE
    }

    override fun hideTotalBayar(){
        binding.jumlahPembayaran.visibility = View.INVISIBLE
    }

    override fun showNoRek(no: String){
        binding.noRek.text = no
        binding.noRek.visibility = View.VISIBLE
    }

    override fun hideNoRek(){
        binding.noRek.visibility = View.INVISIBLE
    }

    override fun onStart() {
        super.onStart()
        adapterImage?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterImage?.stopListening()
    }

    private fun showBukti(orderId: String) {
        binding.rvGambar.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef.collection("ORDERS").document(orderId).collection("BUKTI_PEMBAYARAN")
//        query.get().addOnSuccessListener {
//            if (!it.isEmpty){
//                binding.cardStatus.visibility = View.VISIBLE
//            }
//        }
        val options = FirestoreRecyclerOptions
            .Builder<image>()
            .setQuery(query, image::class.java)
            .build()
        adapterImage = ImageFirestoreRecyclerAdapter(options)
        binding.rvGambar.adapter = adapterImage
    }


    private inner class ImageViewHolder(private val view: View) :
        RecyclerView.ViewHolder(
            view
        ) {

        fun setThumbnailProduct(thumnailUrl: String) {
            val image = view.findViewById<ImageView>(R.id.imageView)
            val card = view.findViewById<CardView>(R.id.imageCard)
            Picasso.get()
                .load(thumnailUrl)
                .placeholder(R.drawable.black_transparent)
                .into(image)

            card.setOnClickListener {
                val intent = Intent(this@TransferBayarActivity, ImageViewActivity::class.java)
                intent.putExtra("IMAGE_URL", thumnailUrl)
                startActivity(intent)
            }
        }
    }

    private inner class ImageFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<image>) :
        FirestoreRecyclerAdapter<image, ImageViewHolder>(
            options
        ) {
        override fun onBindViewHolder(
            productViewHolder: ImageViewHolder,
            position: Int,
            productModel: image
        ) {
            productViewHolder.setThumbnailProduct(productModel.imageUrl)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_bukti_image,
                parent,
                false
            )
            return ImageViewHolder(view)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            mImageUri = data.data!!
            binding.uploadingImg.visibility = View.VISIBLE
            binding.uploadBtnTxt.text = "UPLOAD BUKTI"
            mImageUri.let {
                Picasso.get()
                    .load(mImageUri)
                    .placeholder(R.drawable.black_transparent)
                    .into(binding.img)
            }
        }
    }

    override fun showProgress(message: String, progress: Double){
        binding.progress.visibility = View.VISIBLE
        binding.progress.progress = progress.toInt()
    }

    override fun hideProgress(){
        binding.uploadingImg.visibility = View.GONE
        binding.progress.visibility = View.GONE
        mImageUri = null
        binding.uploadBtnTxt.text = "UPLOAD BUKTI PEMBAYARAN"
        binding.uploadImgBtn.isClickable = true
        binding.uploadImgBtn.isEnabled = true
        binding.uploadImgBtn.setCardBackgroundColor(Color.parseColor("#EA5411"))
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            intent,
            PICK_IMAGE_REQUEST
        )
    }
}

class image(
    @DocumentId
    val id: String = "",
    val imageUrl:String = ""
)