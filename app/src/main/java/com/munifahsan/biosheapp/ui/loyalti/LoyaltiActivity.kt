package com.munifahsan.biosheapp.ui.loyalti

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.databinding.ActivityLoyaltiBinding
import com.munifahsan.biosheapp.domain.Loyalti
import com.munifahsan.biosheapp.ui.loyalti.CardAdapter.ShadowTransformer


class LoyaltiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoyaltiBinding
    private lateinit var loyaltiAdapter: LoyaltiCardAdapter

    //private lateinit var shadowTransformer: ShadowTransformer
    private var shadowTransformer: ShadowTransformer? = null
    private val argbEvaluator = ArgbEvaluator()

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoyaltiBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        loyaltiAdapter = LoyaltiCardAdapter()

        loyaltiAdapter.addCardItem(
            Loyalti(
                "asdgads",
                "Loyalti Silver",
                ContextCompat.getDrawable(this, R.drawable.silver_ic)!!,
                ContextCompat.getColor(this,
                    R.color.silver
                )
            )
        )
        loyaltiAdapter.addCardItem(
            Loyalti(
                "asdgads",
                "Loyalti Gold",
                ContextCompat.getDrawable(this, R.drawable.gold_ic)!!,
                ContextCompat.getColor(this,
                    R.color.gold
                )
            )
        )
        loyaltiAdapter.addCardItem(
            Loyalti(
                "asdgads",
                "Loyalti Platinum",
                ContextCompat.getDrawable(this, R.drawable.platinum_ic)!!,
                ContextCompat.getColor(this,
                    R.color.platinum
                )
            )
        )
        loyaltiAdapter.addCardItem(
            Loyalti(
                "asdgads",
                "Loyalti Diamond",
                ContextCompat.getDrawable(this, R.drawable.diamond_ic)!!,
                ContextCompat.getColor(this,
                    R.color.diamond
                )
            )
        )

        shadowTransformer = ShadowTransformer(binding.viewPager, loyaltiAdapter)
        shadowTransformer!!.enableScaling(true)
        binding.viewPager.adapter = loyaltiAdapter
        binding.viewPager.setPageTransformer(false, shadowTransformer)
        binding.viewPager.offscreenPageLimit = 3
        val margin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            (5 * 2).toFloat(),
            resources.displayMetrics
        ).toInt()
        binding.viewPager.pageMargin = -margin

        val colors_temp = arrayOf(
            ContextCompat.getColor(this, R.color.silver_muda),
            ContextCompat.getColor(this,R.color.gold_muda),
            ContextCompat.getColor(this,R.color.platinum_muda),
            ContextCompat.getColor(this,R.color.diamond_muda),
        )

        val peringatan = arrayOf(
            "Naikan level ke Loyalti Gold dengan menyelesaikan beberapa persyaratan dari kami",
            "Kamu belum sampai ke level Loyalti Gold dan sekarang kamu mempunyai Loyalti Silver",
            "Kamu belum sampai ke level Loyalti Platinum dan sekarang kamu mempunyai Loyalti Silver",
            "Kamu belum sampai ke level Loyalti Diamond dan sekarang kamu mempunyai Loyalti Silver"
        )

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (position < loyaltiAdapter.count - 1 && position < colors_temp.size - 1) {
                    binding.background.setBackgroundColor(
                        argbEvaluator.evaluate(
                            positionOffset,
                            colors_temp[position],
                            colors_temp[position + 1]
                        ) as Int
                    )
                } else {
                    binding.background.setBackgroundColor(colors_temp[colors_temp.size - 1])
                }

//                if (position < loyaltiAdapter.count - 1 && position < peringatan.size - 1) {
//                    binding.peringatan.text = peringatan[position]
//                } else {
//                    binding.peringatan.text = peringatan[peringatan.size - 1]
//                }
            }

            override fun onPageSelected(position: Int) {

                when (position) {
                    0 -> {
                        binding.peringatan.text = "Naikan level ke Loyalti Gold dengan menyelesaikan beberapa persyaratan dari kami"
                        binding.judulKeuntungan.text = "Keuntungan Loyalti Silver"
                        binding.keuntungan1.text = "- Dapat melakukan cicilan sampai maksimal Rp 1.000.000"
                    }
                    1 -> {
                        binding.peringatan.text = "Kamu belum sampai ke level Loyalti Gold dan sekarang kamu mempunyai Loyalti Silver"
                        binding.judulKeuntungan.text = "Keuntungan Loyalti Gold"
                        binding.keuntungan1.text = "- Dapat melakukan cicilan sampai maksimal Rp 2.000.000"
                    }
                    2 -> {
                        binding.peringatan.text = "Kamu belum sampai ke level Loyalti Platinum dan sekarang kamu mempunyai Loyalti Silver"
                        binding.judulKeuntungan.text = "Keuntungan Loyalti Platinum"
                        binding.keuntungan1.text = "- Dapat melakukan cicilan sampai maksimal Rp 3.000.000"
                    }
                    3 -> {
                        binding.peringatan.text = "Kamu belum sampai ke level Loyalti Diamond dan sekarang kamu mempunyai Loyalti Silver"
                        binding.judulKeuntungan.text = "Keuntungan Loyalti Diamond"
                        binding.keuntungan1.text = "- Dapat melakukan cicilan sampai maksimal Rp 5.000.000"
                    }
                }
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })

        binding.backIcon.setOnClickListener {
            finish()
        }
    }
}