package com.munifahsan.biosheapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.munifahsan.biosheapp.databinding.ActivityMainBinding
import com.munifahsan.biosheapp.ui.pageAkun.AkunFragment
import com.munifahsan.biosheapp.ui.keranjang.CartActivity
import com.munifahsan.biosheapp.ui.pageChat.ChatFragment
import com.munifahsan.biosheapp.ui.pageHome.HomeFragment
import com.munifahsan.biosheapp.ui.pageTransaksi.TransaksiFragment
import com.munifahsan.biosheapp.usecase.UserUseCase
import com.munifahsan.biosheapp.utils.CheckConection
import com.munifahsan.biosheapp.utils.Constants
import com.munifahsan.biosheapp.viewmodel.MainViewModel
import com.munifahsan.biosheapp.viewmodel.UserViewModelFactory
import com.munifahsan.biosheapp.vo.Status
import org.joda.time.DateTime
import org.joda.time.Months
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
        .collection("USERS")
    val currentUser = Firebase.auth.currentUser

    lateinit var mainHandler: Handler
    var isConected = false
    private val updateTextTask = object : Runnable {
        override fun run() {
            if (CheckConection.isNetworkAvailable(this@MainActivity)) {
                binding.ofline.visibility = View.GONE
                isConected = true
                changeNotifBarColor(this@MainActivity, R.color.biru_dasar)
            } else {
                binding.ofline.visibility = View.VISIBLE
                isConected = false
                changeNotifBarColor(this@MainActivity, R.color.black)
            }
            mainHandler.postDelayed(this, 2000)
        }
    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.itemHome -> {
                    val fragment = HomeFragment.newInstance()
                    addFragment(fragment)
                    if (!CheckConection.isNetworkAvailable(this)) {
                        showMessage("Mohon periksa kembali konesi internet anda")
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.itemChat -> {
                    val fragment = ChatFragment()
                    addFragment(fragment)
                    if (!CheckConection.isNetworkAvailable(this)) {
                        showMessage("Mohon periksa kembali konesi internet anda")
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.itemCart -> {
                    if (CheckConection.isNetworkAvailable(this)) {
                        startCartActivity()
                    } else {
                        showMessage("Mohon periksa kembali konesi internet anda")
                    }
                }
                R.id.itemTransaksi -> {
                    val fragment = TransaksiFragment()
                    addFragment(fragment)
                    if (!CheckConection.isNetworkAvailable(this)) {
                        showMessage("Mohon periksa kembali konesi internet anda")
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.itemAkun -> {
                    val fragment = AkunFragment()
                    addFragment(fragment)
                    if (!CheckConection.isNetworkAvailable(this)) {
                        showMessage("Mohon periksa kembali konesi internet anda")
                    }
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    private fun addFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.mtrl_bottom_sheet_slide_in,
                R.anim.mtrl_bottom_sheet_slide_out
            )
            .replace(R.id.fl_container, fragment, fragment.javaClass.getSimpleName())
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bot_nav_menu, menu)
        val cartItem = menu?.findItem(R.id.itemCart)
        cartItem?.isEnabled = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Bottom navigation bar
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(
            mOnNavigationItemSelectedListener
        )

        mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(updateTextTask)

        val fragment = HomeFragment.newInstance()
        addFragment(fragment)

        //cart onclick
        binding.cardViewKeranjang.setOnClickListener {
            if (CheckConection.isNetworkAvailable(this)) {
                startCartActivity()
            } else {
                showMessage("Mohon periksa kembali konesi internet anda")
            }
        }

        //FCM
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG FCM", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            Constants.USER_DB.document(Constants.CURRENT_USER_ID.toString())
                .update("tokenId", token).addOnCompleteListener {
                    //showMessage("Token Saved : $token")
                }
            // Log and toast
            val msg = "Token : ${token.toString()}"
            Log.d("TAG FCM", msg)
            //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })

        showTotalChat(FirebaseFirestore.getInstance().collection("CHAT"))

        showKeranjangSize()
        setupTagihan()
    }

    private fun Int.showBadge(number: Int) {
        val badge = binding.bottomNavigationView.getOrCreateBadge(this)
        badge.verticalOffset = 10
        badge.isVisible = true
        if (number != 0) {
            badge.number = number
        } else {
            badge.clearNumber()
        }
        badge.backgroundColor = ContextCompat.getColor(this@MainActivity, R.color.kuning)
    }

    private fun Int.hideBadge() {
        binding.bottomNavigationView.removeBadge(this)
    }

    public fun showKeranjangSize() {
        db.document(currentUser?.uid.toString())
            .collection("KERANJANG").addSnapshotListener { value, error ->
                if (value != null) {
                    binding.keranjangSize.text = value.size().toString()
                    for (field in value) {
                        /*
                        update apakah produk tersedia
                        */
                        Constants.PRODUK_DB.document(field.getString("productId").toString())
                            .get()
                            .addOnSuccessListener {
                                if (it.exists()) {
                                    if (!it.getBoolean("show")!!) {
                                        Constants.KERANJANG_DB.document(field.id).delete()
                                    }
                                } else {
                                    Constants.KERANJANG_DB.document(field.id).delete()
                                }
                            }
                    }
                }

                if (value != null) {
                    if (value.size() == 0) {
                        val transition: Transition = Fade()
                        transition.duration = 300
                        transition.addTarget(binding.keranjangSizeCard)

                        TransitionManager.beginDelayedTransition(binding.root, transition)
                        binding.keranjangSizeCard.visibility = View.INVISIBLE
                    } else {
                        val transition: Transition = Fade()
                        transition.duration = 300
                        transition.addTarget(binding.keranjangSizeCard)

                        TransitionManager.beginDelayedTransition(binding.root, transition)
                        binding.keranjangSizeCard.visibility = View.VISIBLE
                    }
                }

            }

    }

    private fun showTotalChat(ref: CollectionReference) {
        ref.whereArrayContains("speakers", auth.currentUser!!.uid)
            .addSnapshotListener { value, error ->
                if (value != null) {
                    for (field in value) {
                        val from = field.getString("from")
                        val to = field.getString("to")

                        if (from != auth.currentUser!!.uid){
                            Constants.CHAT_DB.document(field.id).collection("CHAT")
                                .whereEqualTo("seen", false)
                                .whereEqualTo("receiver", auth.currentUser!!.uid)
                                .addSnapshotListener { value2, error ->
                                    if (value2 != null) {
                                        Constants.CHAT_DB.document(field.id)
                                            .update("fromUnreadChat", value2.size())
                                    }
                                }
                        }

                        if (to != auth.currentUser!!.uid){
                            Constants.CHAT_DB.document(field.id).collection("CHAT")
                                .whereEqualTo("seen", false)
                                .whereEqualTo("receiver", auth.currentUser!!.uid)
                                .addSnapshotListener { value2, error ->
                                    if (value2 != null) {
                                        Constants.CHAT_DB.document(field.id)
                                            .update("toUnreadChat", value2.size())
                                    }
                                }
                        }

                    }

                }
            }

        ref.whereArrayContains("speakers", auth.currentUser!!.uid)
            .addSnapshotListener { value3, error ->
                val unreadChat = ArrayList<Int>()
                if (value3 != null) {
                    for (field in value3) {
                        val from = field.getString("from")
                        val to = field.getString("to")

                        if (from != auth.currentUser!!.uid){
                            if (field.getLong("fromUnreadChat") != null){
                                val a = field.getLong("fromUnreadChat")!!.toInt()
                                unreadChat.add(a)
                            }
                        }

                        if (to != auth.currentUser!!.uid){
                            if (field.getLong("toUnreadChat") != null){
                                val a = field.getLong("toUnreadChat")!!.toInt()
                                unreadChat.add(a)
                            }
                        }

                    }

                    if (unreadChat.sum() != 0) {
                        R.id.itemChat.showBadge(unreadChat.sum())
                    } else {
                        R.id.itemChat.hideBadge()
                    }
                }

            }

    }

    private fun changeNotifBarColor(context: Context, color: Int) {
        /*
        Change status bar color
        */
        val window: Window = window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(context, color)
    }

    private fun setupTagihan() {
        Constants.TAGIHAN_DB.addSnapshotListener { value, error ->
            if (value != null) {
                for (field in value) {
                    val deadline = field.getDate("deadLinePembayaran")
                    val tagihan = field.getLong("jumlahPembayaran")!!.toInt()
                    val bungaTagihan = field.getLong("bunga")!!.toInt()
                    if (Date().after(deadline!!)) {
                        val bunga = (tagihan * bungaTagihan / 100) * getBulanTelat(deadline)
                        //totalBayar.text = rupiahFormat(tagihan + bunga)
                        Constants.TAGIHAN_DB.document(field.id)
                            .update("jumlahPembayaranTelat", tagihan + bunga)
                        //tanggalDeadline.setTextColor(ContextCompat.getColor(this@TagihanActivity, R.color.orange))
                    }
                }
            }
        }
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

    private fun startCartActivity() {
        //showMessage("keranjang")
        val intent = Intent(this, CartActivity::class.java)
        startActivity(intent)
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

}