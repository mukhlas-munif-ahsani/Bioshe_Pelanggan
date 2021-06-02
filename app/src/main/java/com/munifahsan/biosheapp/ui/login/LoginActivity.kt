package com.munifahsan.biosheapp.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.munifahsan.biosheapp.utils.Constants
import com.munifahsan.biosheapp.MainActivity
import com.munifahsan.biosheapp.databinding.ActivityLoginBinding
import com.munifahsan.biosheapp.ui.forgetPassword.ForgetPasswordActivity
import com.munifahsan.biosheapp.ui.register.RegisterActivity
import java.util.regex.Matcher
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.masukBtn.setOnClickListener {
            login()
        }

        binding.goToRegister.setOnClickListener {
            //cek apakah sudah login dengan akun lain
            if (Firebase.auth.currentUser != null) {
                //keluarkan user terlebih dahulu
                Firebase.auth.signOut()

                startRegisterActivity()
            } else {
                startRegisterActivity()
            }

        }

        binding.lupaPass.setOnClickListener {
            val intent = Intent(this, ForgetPasswordActivity::class.java)
            startActivity(intent)
        }

    }

    private fun login() {
        val email = binding.loginEmailEdt.text.toString()
        val pass = binding.loginPassEdt.text.toString()
        if (isValidForm(email, pass)) {
            binding.loginProgressRel.visibility = View.VISIBLE
            binding.loginProgressTitle.text = "Mengecek akun"
                //signin
                Constants.AUTH.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {

                        //cek user data
//                            showMessage(Constants.CURRENT_USER_ID.toString())
                        Constants.USER_DB.document(Firebase.auth.currentUser!!.uid).get()
                            .addOnSuccessListener { result ->
                                if (result.exists()) {
                                    startMainActivity()
                                } else {
                                    showMessage("Data user tidak ditemukan")
//                                    showMessage(Constants.CURRENT_USER_ID.toString())
                                    binding.loginProgressRel.visibility = View.GONE
                                }
                            }
                    } else {
                        showMessage("Login gagal | Error : " + it.exception!!.message.toString())
                        binding.loginProgressRel.visibility = View.GONE
                    }
                }.addOnFailureListener {
                    showMessage(it.message.toString())
                    binding.loginProgressRel.visibility = View.GONE
                }
        }
    }

    private fun isValidForm(email: String, pass: String): Boolean {
        var isValid = true
        if (email.isEmpty()) {
            isValid = false
            binding.loginEmailInLay.error = "Email tidak boleh kosong"
        }

        if (email.isNotEmpty() && !isEmailValid(email)) {
            isValid = false
            binding.loginEmailInLay.error = "Email tidak valid"
        }

        if (email.isNotEmpty()) {
            binding.loginEmailInLay.isErrorEnabled = false
        }

        if (pass.isEmpty()) {
            isValid = false
            binding.loginPassInLay.error = "Password tidak boleh kosong"
        }

        if (pass.isNotEmpty()) {
            binding.loginPassInLay.isErrorEnabled = false
        }
        return isValid
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

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}