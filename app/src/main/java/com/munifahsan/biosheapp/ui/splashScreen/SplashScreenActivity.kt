package com.munifahsan.biosheapp.ui.splashScreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.munifahsan.biosheapp.MainActivity
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.ui.login.LoginActivity
import com.munifahsan.biosheapp.ui.register.RegisterActivity
import com.munifahsan.biosheapp.utils.Constants

@Suppress("DEPRECATION")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        auth = Firebase.auth

//        val friendId = intent.getStringExtra("FRIEND_ID")
//        if (friendId != null){
//            showMessage(friendId)
//        } else {
//            showMessage("kosong")
//        }
        /*
        Change status bar color
         */
        val window = this.window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        val userId: String? = currentUser?.uid
        if (currentUser != null) {
            if (userId != null) {
                //showMessage(userId)
                db.collection("USERS")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            //showMessage("document ada di db")
                            startMainActivity()

                        } else {
                            //showMessage("not exist in db : " + userId)
                            startLoginActivity()
                        }
                    }.addOnFailureListener { exeption ->
                        showMessage(exeption.toString())
                    }
            }
            //startMainActivity()
        } else {
            startLoginActivity()
//            auth.signInAnonymously()
//                .addOnCompleteListener(this) { task ->
//                    if (task.isSuccessful) {
//                        userId?.let { showMessage(it) }
//                        startRegisterActivity()
//                    } else {
//                        showMessage(task.exception.toString())
//                    }
//                }
        }
    }

    private fun startMainActivity() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }

    private fun startRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
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