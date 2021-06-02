package com.munifahsan.biosheapp.ui.forgetPassword

import android.os.Bundle
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.munifahsan.biosheapp.databinding.ActivityForgetPasswordBinding
import java.util.regex.Matcher
import java.util.regex.Pattern


class ForgetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgetPasswordBinding
    var auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.sendBtn.setOnClickListener {
            val emailAddress = binding.emailEdt.text.toString()
            binding.sendBtn.isEnabled = false
            //cek apakah email tidak kosong
            if (emailAddress.isNotEmpty()) {

                //cek apakah email valid
                if (isEmailValid(emailAddress)) {

                    //kirim link reset ke alamat email
                    auth.sendPasswordResetEmail(emailAddress).addOnCompleteListener {
                        if (it.isSuccessful) {

                            //link berhasil dikirim ke alamat email
                            showMessage("Link reset password berhasil dikirim ke $emailAddress")
                            binding.emailEdt.text!!.clear()
                            binding.sendBtn.isEnabled = true
                            finish()

                        } else {
                            showMessage("Error : ${it.exception!!.message}")
                        }

                    }.addOnFailureListener {
                        showMessage("Error : ${it.message}")
                    }

                } else {
                    binding.emailInLay.error = "Email tidak valid"
                }

            } else {
                binding.emailInLay.error = "Kolom tidak boleh kosong"
            }
        }
    }

    private fun isEmailValid(email: String?): Boolean {
        val pattern: Pattern
        val EMAIL_PATTERN = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        pattern = Pattern.compile(EMAIL_PATTERN)
        val matcher: Matcher = pattern.matcher(email)
        return matcher.matches()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}