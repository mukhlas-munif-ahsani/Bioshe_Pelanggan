package com.munifahsan.biosheapp.ui.pageAkun

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.databinding.FragmentAkunBinding
import com.munifahsan.biosheapp.ui.login.LoginActivity
import com.munifahsan.biosheapp.ui.dataDiri.DataDiriActivity
import com.munifahsan.biosheapp.ui.loyalti.LoyaltiActivity
import com.munifahsan.biosheapp.ui.reward.RewardActivity
import com.squareup.picasso.Picasso


class AkunFragment : Fragment(), AkunContract.View {

    private lateinit var mPres: AkunContract.Presenter

    private var _binding: FragmentAkunBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var auth = Firebase.auth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding = FragmentAkunBinding.inflate(inflater, container, false)
        val view = binding.root

        mPres = Presenter(this)

        mPres.getData()

        binding.relLogoutBtn.setOnClickListener {
            val builder = AlertDialog.Builder(activity!!)
            builder.setTitle("Logout")
            builder.setMessage("Apakah anda yakin ingin keluar dari aplikasi ?")
            builder.setPositiveButton("Ya") { _, _ ->
                auth.signOut()
                startLoginActivity()
            }
            builder.setNegativeButton("Tidak") { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
        }

        /*
        Change status bar color
         */
        val window: Window = activity!!.window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(activity!!, R.color.biru_dasar)

        binding.salesQrButton.setOnClickListener {
            val transition: Transition = Fade()
            transition.duration = 600
            transition.addTarget(R.id.rel_qr)

            TransitionManager.beginDelayedTransition(view, transition)

            binding.relQr.visibility = View.VISIBLE
        }

        binding.linQr.setOnClickListener {

            val transition: Transition = Fade()
            transition.duration = 400
            transition.addTarget(R.id.rel_qr)

            TransitionManager.beginDelayedTransition(view, transition)

            binding.relQr.visibility = View.GONE
        }

        binding.qrClose.setOnClickListener {

            val transition: Transition = Fade()
            transition.duration = 400
            transition.addTarget(R.id.rel_qr)

            TransitionManager.beginDelayedTransition(view, transition)

            binding.relQr.visibility = View.GONE
        }

        binding.cardQr.setOnClickListener {

        }

        binding.dataDiriBtn.setOnClickListener {
            val intent = Intent(activity, DataDiriActivity::class.java)
            startActivity(intent)
        }

        binding.loyaltiBtn.setOnClickListener {
            val intent = Intent(activity, LoyaltiActivity::class.java)
            startActivity(intent)
        }

        binding.rewardBtn.setOnClickListener {
            val intent = Intent(activity, RewardActivity::class.java)
            startActivity(intent)
        }

        binding.whatappBtn.setOnClickListener {
            openWhatsApp()
        }

        binding.telegramBtn.setOnClickListener {
            openTelegram()
        }

        binding.instagramBtn.setOnClickListener {
            openInstagram()
        }

        binding.facebookBtn.setOnClickListener {
            openFacebook()
        }

        binding.emailBtn.setOnClickListener {
            openEmail()
        }

        loadQrCode("mungkin bisa jadi")
        return view
    }

    override fun loadQrCode(data: String) {
        val qrgEncoder = QRGEncoder(data, null, QRGContents.Type.TEXT, 800)
        qrgEncoder.colorBlack = activity!!.resources.getColor(R.color.black)
        qrgEncoder.colorWhite = activity!!.resources.getColor(R.color.white)
        try {
            val bitmap = qrgEncoder.bitmap
            binding.imageViewQr.setImageBitmap(bitmap)
            binding.imageViewQr.scaleType = ImageView.ScaleType.CENTER_CROP
        } catch (e: Exception) {
            Log.v("QR Eror", e.toString())
        }
    }

    private fun openWhatsApp() {
        val smsNumber = "6281358813859" // E164 format without '+' sign
        val pm = activity?.packageManager
        try {
            val waIntent = Intent(Intent.ACTION_SEND)
            waIntent.type = "text/plain"
            val text = "Assalamualaikum..."
            val info = pm?.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA)
            //Check if package exists or not. If not then code
            //in catch block will be called
            waIntent.setPackage("com.whatsapp")
            waIntent.putExtra(Intent.EXTRA_TEXT, text)
            waIntent.putExtra("jid", "$smsNumber@s.whatsapp.net") //phone number without "+" prefix
            startActivity(Intent.createChooser(waIntent, "Share with"))
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(activity, "WhatsApp not Installed", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun openTelegram() {
        try {
            val telegramIntent = Intent(Intent.ACTION_VIEW)
            telegramIntent.data = Uri.parse("https://telegram.me/biosheofficial")
            startActivity(telegramIntent)
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(activity, "Telegram not Installed", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun openInstagram() {
        try {
            val telegramIntent = Intent(Intent.ACTION_VIEW)
            telegramIntent.data = Uri.parse("https://www.instagram.com/bioshe.official/")
            startActivity(telegramIntent)
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(activity, "Instagram not Installed", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun openFacebook() {
        try {
            val facebookIntent = Intent(Intent.ACTION_VIEW)
            val facebookUrl: String = getFacebookPageURL(activity!!)
            facebookIntent.data = Uri.parse(facebookUrl)
            startActivity(facebookIntent)
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(activity, "Facebook not Installed", Toast.LENGTH_SHORT)
                .show()
        }

    }

    private var FACEBOOK_URL =
        "https://web.facebook.com/Biosheofficial-107412997827488/?_rdc=1&_rdr"
    private var FACEBOOK_PAGE_ID = "YourPageName"

    //method to get the right URL to use in the intent
    private fun getFacebookPageURL(context: Context): String {
        val packageManager = context.packageManager
        return try {
            val versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode
            if (versionCode >= 3002850) { //newer versions of fb app
                "fb://facewebmodal/f?href=$FACEBOOK_URL"
            } else { //older versions of fb app
                "fb://page/$FACEBOOK_PAGE_ID"
            }
        } catch (e: PackageManager.NameNotFoundException) {
            FACEBOOK_URL //normal web url
        }
    }

    private fun openEmail() {
        try {
            val email = Intent(Intent.ACTION_SENDTO)
            email.data = Uri.parse("mailto:bioshe.official@gmail.com")
            email.putExtra(Intent.EXTRA_SUBJECT, "Subject")
            email.putExtra(Intent.EXTRA_TEXT, "My Email message")
            startActivity(email)
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(activity, "Gmail not Installed", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun showPhoto(url: String) {
        Picasso.get()
            .load(url)
            .placeholder(R.drawable.black_transparent)
            .into(binding.profileImage)
    }

    override fun hidePhoto() {

    }

    override fun showNama(nama: String) {
        binding.userName.text = nama
        binding.userName.visibility = View.VISIBLE
    }

    override fun hideNama() {
        binding.userName.visibility = View.INVISIBLE
    }

    override fun showLoyalti(loyalti: String) {
        binding.loyalti.text = loyalti
        binding.loyalti.visibility = View.VISIBLE

        if (loyalti == "SILVER") {
            binding.loyalti.text = "Loyalti Silver"
            binding.loyalti.setTextColor(resources.getColor(R.color.silver))
            binding.medalIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    activity!!,
                    R.drawable.silver_ic
                )
            )
        }

        if (loyalti == "GOLD") {
            binding.loyalti.text = "Loyalti Gold"
            binding.loyalti.setTextColor(resources.getColor(R.color.gold))
            binding.medalIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    activity!!,
                    R.drawable.gold_ic
                )
            )
        }

        if (loyalti == "PLATINUM") {
            binding.loyalti.text = "Loyalti Platinum"
            binding.loyalti.setTextColor(resources.getColor(R.color.platinum))
            binding.medalIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    activity!!,
                    R.drawable.platinum_ic
                )
            )
        }

        if (loyalti == "DIAMOND") {
            binding.loyalti.text = "Loyalti Diamond"
            binding.loyalti.setTextColor(resources.getColor(R.color.diamond))
            binding.medalIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    activity!!,
                    R.drawable.diamond_ic
                )
            )
        }
    }

    override fun hideLoyalti() {
        binding.loyalti.visibility = View.INVISIBLE
    }

    private fun startLoginActivity() {
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity!!.finish()
    }
}