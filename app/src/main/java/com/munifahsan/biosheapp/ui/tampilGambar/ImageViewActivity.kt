package com.munifahsan.biosheapp.ui.tampilGambar

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.databinding.ActivityImageViewBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URL
import java.util.*

class ImageViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val url = intent.getStringExtra("IMAGE_URL").toString()

        Picasso.get()
            .load(url)
            .placeholder(R.drawable.black_transparent)
            .into(binding.myZoomageView)

        binding.backIcon.setOnClickListener {
            finish()
        }

        val urlImage: URL = URL(
            url
        )

        binding.download.setOnClickListener {
            //progressBar.visibility = View.VISIBLE
            // async task to get / download bitmap from url
            val result: Deferred<Bitmap?> = GlobalScope.async {
                urlImage.toBitmap()
            }

            GlobalScope.launch(Dispatchers.Main) {
                // get the downloaded bitmap
                val bitmap: Bitmap? = result.await()

                // if downloaded then saved it to internal storage
                bitmap?.apply {
                    // get saved bitmap internal storage uri
                    val savedUri: Uri? = saveToInternalStorage(this@ImageViewActivity)

                    // display saved bitmap to image view from internal storage
                    //imageView.setImageURI(savedUri)

                    // show bitmap saved uri in text view
                    //tvSaved.text = savedUri.toString()
                    Toast.makeText(this@ImageViewActivity, savedUri.toString(), Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

    }

}

// extension function to get / download bitmap from url
fun URL.toBitmap(): Bitmap? {
    return try {
        BitmapFactory.decodeStream(openStream())
    } catch (e: IOException) {
        null
    }
}


// extension function to save an image to internal storage
fun Bitmap.saveToInternalStorage(context: Context): Uri? {
    // get the context wrapper instance
    val wrapper = ContextWrapper(context)

    // initializing a new file
    // bellow line return a directory in internal storage
    var file = wrapper.getDir("images", Context.MODE_PRIVATE)

    // create a file to save the image
    file = File(file, "${UUID.randomUUID()}.jpg")

    return try {
        // get the file output stream
        val stream: OutputStream = FileOutputStream(file)

        // compress bitmap
        compress(Bitmap.CompressFormat.JPEG, 100, stream)

        // flush the stream
        stream.flush()

        // close stream
        stream.close()

        // return the saved image uri
        Uri.parse(file.absolutePath)
    } catch (e: IOException) { // catch the exception
        e.printStackTrace()
        null
    }
}

