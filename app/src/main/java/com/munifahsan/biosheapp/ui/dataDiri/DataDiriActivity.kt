package com.munifahsan.biosheapp.ui.dataDiri

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.munifahsan.biosheapp.utils.FileUtil
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.databinding.ActivityDataDiriBinding
import com.squareup.picasso.Picasso
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import id.zelory.compressor.loadBitmap
import kotlinx.coroutines.launch
import java.io.File

class DataDiriActivity : AppCompatActivity(), Contract.View {
    private lateinit var binding: ActivityDataDiriBinding
    private lateinit var mPres: Contract.Presenter
    private val PICK_IMAGE_REQUEST = 1
    private var mImageUri: Uri? = null
    private var actualImage: File? = null
    private var compressedImage: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataDiriBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mPres = Presenter(this)
        mPres.getData()

        val items = listOf("LAKI-LAKI", "PEREMPUAN")
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, items)
        binding.genderAuto.setAdapter(adapter)

        disableInput()

        binding.editBtn.setOnClickListener {
            if (binding.namaInLay.isEnabled) {
                disableInput()
                if (compressedImage != null) {
                    mPres.updateData(
                        binding.emailEdt.text.toString(),
                        compressedImage!!.absoluteFile.toUri(), binding.nikEdt.text.toString(),
                        binding.namaEdt.text.toString(),
                        binding.genderAuto.text.toString(),
                        binding.alamatEdt.text.toString(),
                        binding.nohpEdt.text.toString(),
                        binding.nowaEdt.text.toString(),
                        binding.ahliWarisEdt.text.toString(),
                        binding.namaOutletEdt.text.toString(),
                        binding.alamatOutletEdt.text.toString()
                    )
                } else {
                    mPres.updateData(
                        binding.emailEdt.text.toString(),
                        null, binding.nikEdt.text.toString(),
                        binding.namaEdt.text.toString(),
                        binding.genderAuto.text.toString(),
                        binding.alamatEdt.text.toString(),
                        binding.nohpEdt.text.toString(),
                        binding.nowaEdt.text.toString(),
                        binding.ahliWarisEdt.text.toString(),
                        binding.namaOutletEdt.text.toString(),
                        binding.alamatOutletEdt.text.toString()
                    )
                }

            } else {
                enableInput()
            }
        }

        binding.backIcon.setOnClickListener {
            finish()
        }

        binding.imgProfileImage.setOnClickListener {
            openFileChooser()
        }
    }

    private fun enableInput() {
        binding.imgProfileImage.isEnabled = true
        binding.imgProfileImage.isClickable = true
        //binding.emailInLay.isEnabled = true
        binding.namaInLay.isEnabled = true
        binding.nikInLay.isEnabled = true
        binding.genderInLay.isEnabled = true
        binding.alamatInLay.isEnabled = true
        binding.nohpInLay.isEnabled = true
        binding.nowaInLay.isEnabled = true
        binding.ahliWarisInLay.isEnabled = true
        binding.namaOutletInLay.isEnabled = true
        binding.alamatOutletInLay.isEnabled = true
        binding.editBtn.text = "SIMPAN"
    }

    private fun disableInput() {
        binding.imgProfileImage.isEnabled = false
        binding.imgProfileImage.isClickable = false
        binding.emailInLay.isEnabled = false
        binding.namaInLay.isEnabled = false
        binding.nikInLay.isEnabled = false
        binding.genderInLay.isEnabled = false
        binding.alamatInLay.isEnabled = false
        binding.nohpInLay.isEnabled = false
        binding.nowaInLay.isEnabled = false
        binding.ahliWarisInLay.isEnabled = false
        binding.namaOutletInLay.isEnabled = false
        binding.alamatOutletInLay.isEnabled = false
        binding.editBtn.text = "EDIT"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            //mImageUri = data.data!!
            //binding.uploadBtnTxt.text = "UPLOAD BUKTI"
//            mImageUri.let {
//                Picasso.get()
//                    .load(mImageUri)
//                    .placeholder(R.drawable.black_transparent)
//                    .into(binding.imgProfileImage)
//            }
            actualImage = FileUtil.from(this, data.data)
            customCompressImage()
        }
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

    private fun customCompressImage() {
        actualImage?.let { imageFile ->
            lifecycleScope.launch {
                // Default compression with custom destination file
                /*compressedImage = Compressor.compress(this@MainActivity, imageFile) {
                    default()
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.also {
                        val file = File("${it.absolutePath}${File.separator}my_image.${imageFile.extension}")
                        destination(file)
                    }
                }*/

                // Full custom
                compressedImage = Compressor.compress(this@DataDiriActivity, imageFile) {
                    resolution(1280, 720)
                    quality(80)
                    format(Bitmap.CompressFormat.JPEG)
                    size(1_097_152) // 2 MB
                }
                setCompressedImage()
            }
        } ?: showMessage("Please choose an image!")
    }

    private fun setCompressedImage() {
        compressedImage?.let {
            binding.imgProfileImage.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
//            binding.textSize.text = String.format("Size : %s", getReadableFileSize(it.length()))
//            binding.upload.isEnabled = true
            //showMessage("Compressed image save in ${it.absoluteFile.toURI()}")
            //Toast.makeText(this, "Compressed image save in ${it.absoluteFile.toURI()}", Toast.LENGTH_LONG).show()
            Log.d("Compressor", "Compressed image save in " + it.path)
        }
    }

    override fun showPhoto(url: String) {
        Picasso.get().load(url).into(binding.imgProfileImage)
    }

    override fun showEmail(email: String) {
        binding.emailEdt.setText(email)
        binding.emailInLay.visibility = View.VISIBLE
    }

    override fun hideEmail() {
        binding.emailInLay.visibility = View.INVISIBLE
    }

    override fun showNama(nama: String) {
        binding.namaEdt.setText(nama)
        binding.namaInLay.visibility = View.VISIBLE
    }

    override fun hideNama() {
        binding.namaInLay.visibility = View.INVISIBLE
    }

    override fun showNik(nik: String) {
        binding.nikEdt.setText(nik)
        binding.nikInLay.visibility = View.VISIBLE
    }

    override fun hideNik() {
        binding.nikInLay.visibility = View.INVISIBLE
    }

    override fun showGender(gender: String) {
        binding.genderAuto.setText(gender)
        binding.genderInLay.visibility = View.VISIBLE
    }

    override fun hideGender() {
        binding.genderInLay.visibility = View.INVISIBLE
    }

    override fun showAlamatRumah(alamat: String) {
        binding.alamatEdt.setText(alamat)
        binding.alamatInLay.visibility = View.VISIBLE
    }

    override fun hideAlamatRumah() {
        binding.alamatInLay.visibility = View.INVISIBLE
    }

    override fun showNoHp(no: String) {
        binding.nohpEdt.setText(no)
        binding.nohpInLay.visibility = View.VISIBLE
    }

    override fun hideNoHp() {
        binding.nohpInLay.visibility = View.INVISIBLE
    }

    override fun showNoWa(no: String) {
        binding.nowaEdt.setText(no)
        binding.nowaInLay.visibility = View.VISIBLE
    }

    override fun hideNoWa() {
        binding.nowaInLay.visibility = View.INVISIBLE
    }

    override fun showAhliWaris(ahli: String) {
        binding.ahliWarisEdt.setText(ahli)
        binding.ahliWarisInLay.visibility = View.VISIBLE
    }

    override fun hideAhliWaris() {
        binding.ahliWarisInLay.visibility = View.INVISIBLE
    }

    override fun showNamaOutlet(outlet: String) {
        binding.namaOutletEdt.setText(outlet)
        binding.namaOutletInLay.visibility = View.VISIBLE
    }

    override fun hideNamaOutlet() {
        binding.namaOutletInLay.visibility = View.INVISIBLE
    }

    override fun showAlamatOutlet(alamat: String) {
        binding.alamatOutletEdt.setText(alamat)
        binding.alamatOutletInLay.visibility = View.VISIBLE
    }

    override fun hideAlamatOutlet() {
        binding.alamatOutletInLay.visibility = View.INVISIBLE
    }

    override fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}