package com.munifahsan.biosheapp.ui.register

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.zxing.integration.android.IntentIntegrator
import com.munifahsan.biosheapp.MainActivity
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.utils.CaptureAct
import com.munifahsan.biosheapp.databinding.ActivityRegisterBinding
import com.munifahsan.biosheapp.domain.Chat
import com.munifahsan.biosheapp.domain.ChatRoom
import com.munifahsan.biosheapp.domain.Users
import com.munifahsan.biosheapp.ui.login.LoginActivity
import com.munifahsan.biosheapp.usecase.UserUseCase
import com.munifahsan.biosheapp.utils.Constants
import com.munifahsan.biosheapp.utils.FileUtil
import com.munifahsan.biosheapp.viewmodel.UserViewModelFactory
import com.munifahsan.biosheapp.vo.Status
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var auth: FirebaseAuth
    private val PICK_IMAGE_REQUEST = 1
    private var mImageUri: Uri? = null
    private var salesId: String = ""
    private val dbChat = FirebaseFirestore.getInstance()
        .collection("CHAT")
    private val db = FirebaseFirestore.getInstance()
        .collection("USERS")
    private var actualImage: File? = null
    private var compressedImage: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Gender dropdown
        val items = listOf("LAKI-LAKI", "PEREMPUAN")
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, items)
        binding.genderAuto.setAdapter(adapter)

        //firebase sign in anonymous
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {

            binding.progressRel.visibility = View.VISIBLE
            binding.salesQrButton.isEnabled = false
            binding.progressTitle.text = "Menyiapkan form..."

            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        binding.salesQrButton.isEnabled = true
                        binding.progressRel.visibility = View.GONE
                    } else {
                        showMessage(task.exception.toString())
                    }
                }
        } else {
            binding.progressRel.visibility = View.GONE
        }

        binding.salesQrButton.setOnClickListener {
            scanCode()
        }

        binding.daftarBtn.setOnClickListener {
            register(salesId)
        }

        binding.imgProfileImage.setOnClickListener {
            openFileChooser()
        }

        binding.goToLogin.setOnClickListener {
            auth.currentUser?.delete()
            startLoginActivity()
        }
        //observeForUserDetails("3cophpwZMPtTaYfuB8j8")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        auth.currentUser?.delete()
    }

    private fun register(idSales: String) {

        val email = binding.emailEdt.text.toString()
        val pass = binding.passwordEdt.text.toString()
        val confirmPass = binding.confirmPasswordEdt.text.toString()
//        val userName = binding.usernameEdt.text.toString()
        val imageUri: Uri = actualImage!!.absoluteFile.toUri()
        val nik = binding.nikEdt.text.toString()
        val nama = binding.namaEdt.text.toString()
        val namaBelakang = binding.namaBelakangEdt.text.toString()
        val gender = binding.genderAuto.text.toString()
        val alamatRumah = binding.alamatEdt.text.toString()
        val noHp = binding.nohpEdt.text.toString()
        val noWa = binding.nowaEdt.text.toString()
        val ahliWaris = binding.ahliWarisEdt.text.toString()
        val kota = binding.kotaEdt.text.toString()
        val kodePos = binding.kodePosEdt.text.toString()
        val namaOutlet = binding.namaOutletEdt.text.toString()
        val alamatOutlet = binding.alamatOutletEdt.text.toString()

        if (isFormValid(
                email,
                pass,
                confirmPass,
                imageUri,
                nik,
                nama,
                namaBelakang,
                gender,
                alamatRumah,
                noHp,
                noWa,
                ahliWaris,
                kota,
                kodePos,
                namaOutlet,
                alamatOutlet,
                idSales
            )
        ) {

            compressedImage?.let {
                //showing progress box
                binding.progressRel.visibility = View.VISIBLE
                binding.progressTitle.text = "Membuat akun"

                registerAndPostUser(
                    email,
                    pass,
                    "CUSTOMER",
                    it.absoluteFile.toUri(),
                    nik,
                    nama,
                    namaBelakang,
                    gender,
                    alamatRumah,
                    noHp,
                    noWa,
                    ahliWaris,
                    kota, kodePos,
                    "",
                    namaOutlet,
                    alamatOutlet,
                    idSales
                )
            }
        }
    }

    private fun createChatRoom(id: String) {
        if (id != auth.currentUser!!.uid) {
            val chat = ChatRoom(
                "",
                auth.currentUser!!.uid,
                "",
                "",
                id,
                "",
                "",
                "",
                0,
                0,
                Timestamp(Date()),
                Timestamp(Date()),
                listOf(auth.currentUser!!.uid, id)
            )
            dbChat.document(auth.currentUser!!.uid + id).set(chat)
                .addOnCompleteListener {
                    val chat = Chat(
                        "",
                        "Hallo",
                        Date(),
                        false,
                        auth.currentUser!!.uid
                    )
                    sendMessage(auth.currentUser!!.uid + id, chat, "")
                    //navigateToChatRoom(auth.currentUser!!.uid + id)
                }
                .addOnFailureListener {

                }
        }

    }

    private fun sendMessage(chatRoomId: String, chat: Chat, message: String) {
        dbChat.document(chatRoomId).collection("CHAT").add(chat).addOnSuccessListener {
            dbChat.document(chatRoomId).update("peakMessage", it.id)
        }
        dbChat.document(chatRoomId).update("sender", auth.currentUser!!.uid)
        dbChat.document(chatRoomId).update("updated", Timestamp(Date()))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {

                //showMessage(result.contents)
                getSalesData(result.contents)

            } else {
                Toast.makeText(this, "No Result", Toast.LENGTH_LONG).show();
                //finish()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
            //navigateToHomePtg()
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
//            mImageUri = data.data!!
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

    private fun getSalesData(salesId: String) {
        this.salesId = salesId
        Constants.SALES_DB.document(salesId).get().addOnSuccessListener {
            if (it.exists()){
                binding.salesQrButton.text = it.getString("nama")
                enableInput()
            }else{
                showMessage("ID Sales tidak ditemukan")
            }
        }.addOnFailureListener {
            showMessage("Error : ${it.message.toString()}")
        }
    }

    private fun enableInput() {
        binding.qrWarning.visibility = View.GONE
        binding.emailInLay.isEnabled = true
//        binding.usernameInLay.isEnabled = true
        binding.passwordInLay.isEnabled = true
        binding.confirmPasswordInLay.isEnabled = true
        binding.imgProfileImage.visibility = View.VISIBLE
        binding.nikInLay.isEnabled = true
        binding.namaInLay.isEnabled = true
        binding.namaBelakangInLay.isEnabled = true
        binding.genderInLay.isEnabled = true
        binding.alamatInLay.isEnabled = true
        binding.nohpInLay.isEnabled = true
        binding.nowaInLay.isEnabled = true
        binding.ahliWarisInLay.isEnabled = true
        binding.kotaInLay.isEnabled = true
        binding.kodePosInLay.isEnabled = true
        binding.namaOutletInLay.isEnabled = true
        binding.alamatOutletInLay.isEnabled = true
        binding.daftarBtn.isEnabled = true
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
                compressedImage = Compressor.compress(this@RegisterActivity, imageFile) {
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

    private fun registerAndPostUser(
        email: String, pass: String,
        level: String,
        imageUri: Uri, nik: String,
        nama: String, namaBelakang: String, gender: String,
        alamatRumah: String, noHp: String,
        noWa: String, ahliWaris: String, kota: String, kodePos: String, kodeKota: String,
        namaOutlet: String, alamatOutlet: String, idSales: String
    ) {

        //val dataStatus = MutableLiveData<Resource<Boolean>>()
        val credential = EmailAuthProvider.getCredential(email, pass)

        //Link anonymous auth to email and pass
        if (auth.currentUser != null) {
            auth.currentUser!!.linkWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser!!.uid

                        createChatRoom(idSales)

                        //Upload profile photo to firebase storage
                        val fileReference: StorageReference =
                            FirebaseStorage.getInstance().reference.child("images/" + userId + "/" + imageUri.lastPathSegment)
                        fileReference
                            .putFile(imageUri)
                            .addOnSuccessListener {

                                // download uploaded image url
                                fileReference
                                    .downloadUrl
                                    .addOnSuccessListener {

                                        // data model
                                        val userData = Users(
                                            nama,
                                            namaBelakang,
                                            email,
                                            level,
                                            it.toString(),
                                            nik,
                                            gender,
                                            alamatRumah,
                                            noHp,
                                            noWa,
                                            ahliWaris,
                                            kota,
                                            kodePos,
                                            kodeKota,
                                            namaOutlet,
                                            alamatOutlet,
                                            idSales,
                                            0,
                                            0,
                                            0,
                                            "SILVER",
                                            0
                                        )

                                        // post data to firestore
                                        db.document(userId)
                                            .set(userData)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    binding.horizontalProgressBar.visibility =
                                                        View.INVISIBLE
                                                    showMessage("Register Berhasil")
                                                    startMainActivity()
                                                } else {
                                                    showMessage("Error : ${task.exception!!.message}")
                                                }
                                            }
                                            .addOnFailureListener { exception ->
                                                showMessage("Error : ${exception.message}")
                                            }
                                    }
                            }
                            .addOnProgressListener {
                                binding.horizontalProgressBar.visibility = View.VISIBLE
                                binding.horizontalProgressBar.progress =
                                    (100.0 * it.bytesTransferred / it.totalByteCount).toInt()
                                binding.progressTxt.text = "Mengunggah data..."
                            }
                    } else {
                        showMessage("Error : ${task.exception!!.message}")
                        binding.progressRel.visibility = View.GONE
                    }
                }
        }
    }

    private fun isFormValid(
        email: String,
        pass: String,
        confirmPass: String,
        imageUri: Uri?, nik: String,
        nama: String, namaBelakang: String, gender: String,
        alamatRumah: String, noHp: String,
        noWa: String, ahliWaris: String, kota: String, kodePos: String,
        namaOutlet: String, alamatOutlet: String, idSales: String
    ): Boolean {
        var isValid = true
        if (email.isEmpty()) {
            isValid = false
            binding.emailInLay.error = "Email tidak boleh kosong"
        }

        if (email.isNotEmpty() && isEmailValid(email)) {
            binding.emailInLay.isErrorEnabled = false
        }

        if (!isEmailValid(email)) {
            isValid = false
            binding.emailInLay.error = "Email tidak valid"
        }

//        if (userName.isEmpty()) {
//            binding.usernameInLay.error = "Username tidak boleh kosong"
//        }
//
//        if (userName.isNotEmpty()) {
//            binding.usernameInLay.isErrorEnabled = false
//        }

        if (pass.isEmpty()) {
            isValid = false
            binding.passwordInLay.error = "Password tidak boleh kosong"
        }

        if (pass.isNotEmpty()) {
            binding.passwordInLay.isErrorEnabled = false
        }

        if (confirmPass.isEmpty()) {
            isValid = false
            binding.confirmPasswordInLay.error = "Confirm password tidak boleh kosong"
        }

        if (confirmPass.isNotEmpty() && pass != confirmPass) {
            isValid = false
            binding.confirmPasswordInLay.error = "Confirm password tidak sama"
        }

        if (confirmPass.isNotEmpty()){
            binding.confirmPasswordInLay.isErrorEnabled = false
        }

        if (imageUri == null) {
            isValid = false
            binding.profileImageError.visibility = View.VISIBLE
            binding.profileImageError.text = "Foto wajib di isi"
        } else {
            binding.profileImageError.visibility = View.GONE
        }

        if (nik.isEmpty()) {
            isValid = false
            binding.nikInLay.error = "NIK tidak boleh kosong"
        }

        if (nik.isNotEmpty()) {
            binding.nikInLay.isErrorEnabled = false
        }

        if (nama.isEmpty()) {
            isValid = false
            binding.namaInLay.error = "Nama tidak boleh kosong"
        }

        if (nama.isNotEmpty()) {
            binding.namaInLay.isErrorEnabled = false
        }

        if (namaBelakang.isEmpty()) {
            isValid = false
            binding.namaBelakangInLay.error = "Nama belakang tidak boleh kosong"
        }

        if (namaBelakang.isNotEmpty()) {
            binding.namaBelakangInLay.isErrorEnabled = false
        }

        if (gender.isEmpty()) {
            isValid = false
            binding.genderInLay.error = "Jenis kelamin tidak boleh kosong"
        }

        if (gender.isNotEmpty()) {
            binding.genderInLay.isErrorEnabled = false
        }

        if (alamatRumah.isEmpty()) {
            isValid = false
            binding.alamatInLay.error = "Alamat tidak boleh kosong"
        }

        if (alamatRumah.isNotEmpty()) {
            binding.alamatInLay.isErrorEnabled = false
        }

        if (noHp.isEmpty()) {
            isValid = false
            binding.nohpInLay.error = "No Hp tidak boleh kosong"
        }

        if (noHp.isNotEmpty()) {
            binding.nohpInLay.isErrorEnabled = false
        }

        if (noWa.isEmpty()) {
            isValid = false
            binding.nowaInLay.error = "No Wa tidak boleh kosong"
        }

        if (noWa.isNotEmpty()) {
            binding.nowaInLay.isErrorEnabled = false
        }

        if (ahliWaris.isEmpty()) {
            isValid = false
            binding.ahliWarisInLay.error = "Ahli waris tidak boleh kosong"
        }

        if (ahliWaris.isNotEmpty()) {
            binding.ahliWarisInLay.isErrorEnabled = false
        }

        if (kota.isEmpty()) {
            isValid = false
            binding.kotaInLay.error = "Kota tidak boleh kosong"
        }

        if (kota.isNotEmpty()) {
            binding.kotaInLay.isErrorEnabled = false
        }

        if (kodePos.isEmpty()) {
            isValid = false
            binding.kodePosInLay.error = "Kode pos tidak boleh kosong"
        }

        if (kodePos.isNotEmpty()) {
            binding.kodePosInLay.isErrorEnabled = false
        }

        if (namaOutlet.isEmpty()) {
            isValid = false
            binding.namaOutletInLay.error = "Nama Outlet tidak boleh kosong"
        }

        if (namaOutlet.isNotEmpty()) {
            binding.namaOutletInLay.isErrorEnabled = false
        }

        if (alamatOutlet.isEmpty()) {
            isValid = false
            binding.alamatOutletInLay.error = "Alamat Outlet tidak boleh kosong"
        }

        if (alamatOutlet.isNotEmpty()) {
            binding.alamatOutletInLay.isErrorEnabled = false
        }

        return isValid
    }

    private fun scanCode() {
        val integrator = IntentIntegrator(this)
        integrator.captureActivity = CaptureAct::class.java
        integrator.setOrientationLocked(false)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Scanning Code")
        integrator.initiateScan()
    }

    private fun isEmailValid(email: String?): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val EMAIL_PATTERN = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        pattern = Pattern.compile(EMAIL_PATTERN)
        matcher = pattern.matcher(email)
        return matcher.matches()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}