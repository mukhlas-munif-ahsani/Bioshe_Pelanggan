package com.munifahsan.biosheapp.ui.reward

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.databinding.ActivityRewardBinding
import com.munifahsan.biosheapp.domain.Reward
import com.munifahsan.biosheapp.utils.SpacesItemDecoration
import com.squareup.picasso.Picasso

class RewardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRewardBinding
    private val dbUsers = FirebaseFirestore.getInstance()
        .collection("USERS")
    val currentUser = Firebase.auth.currentUser
    private var adapterReward: RewardFirestoreRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRewardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.backIcon.setOnClickListener {
            finish()
        }

        dbUsers.document(currentUser!!.uid).addSnapshotListener { value, error ->
            binding.points.text = value!!.getLong("bioshePoints").toString()
        }

        showReward()
    }

    override fun onStart() {
        super.onStart()
        adapterReward!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterReward!!.stopListening()
    }

    private fun showReward() {
        binding.rvReward.layoutManager = GridLayoutManager(
            this,
            2,
            GridLayoutManager.VERTICAL,
            false
        )
        binding.rvReward.addItemDecoration(
            SpacesItemDecoration(
                24
            )
        )
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef.collection("REWARD")
        val options = FirestoreRecyclerOptions
            .Builder<Reward>()
            .setQuery(query, Reward::class.java)
            .build()
        adapterReward = RewardFirestoreRecyclerAdapter(options)
        binding.rvReward.adapter = adapterReward
    }

    private inner class RewardViewHolder(private val view: View) :
        RecyclerView.ViewHolder(
            view
        ) {

        fun setContent(id: String, nama: String, thumnailUrl: String, points: Int) {
            val image = view.findViewById<ImageView>(R.id.thumbnailImage)
            val namaTxt = view.findViewById<TextView>(R.id.rewardName)
            val pointsTxt = view.findViewById<TextView>(R.id.points)
            val rewardItem = view.findViewById<CardView>(R.id.rewardBtn)
            val ambilBtn = view.findViewById<CardView>(R.id.ambilBtn)
            namaTxt.text = nama
            ("$points Points").also { pointsTxt.text = it }
            Picasso.get()
                .load(thumnailUrl)
                .placeholder(R.drawable.black_transparent)
                .into(image)

            dbUsers.document(currentUser!!.uid).addSnapshotListener { value, error ->
                val currentPoints = value!!.getLong("bioshePoints")!!.toInt()

                rewardItem.setOnClickListener {
                    if (currentPoints >= points){
                        showMessage("cukup")
                    } else {
                        showMessage("kurang")
                    }
                }

                ambilBtn.setOnClickListener {
                    if (currentPoints >= points){
                        showMessage("cukup")
                    } else {
                        showMessage("kurang")
                    }
                }
            }
        }

    }

    private inner class RewardFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<Reward>) :
        FirestoreRecyclerAdapter<Reward, RewardViewHolder>(
            options
        ) {
        override fun onBindViewHolder(
            viewHolder: RewardViewHolder,
            position: Int,
            model: Reward
        ) {
            viewHolder.setContent(model.id, model.nama, model.thumbnail, model.points)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_reward,
                parent,
                false
            )
            return RewardViewHolder(view)
        }
    }

    fun showMessage(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}

