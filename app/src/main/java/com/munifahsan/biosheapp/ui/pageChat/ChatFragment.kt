package com.munifahsan.biosheapp.ui.pageChat

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.databinding.FragmentChatBinding
import com.munifahsan.biosheapp.domain.ChatRoom
import com.munifahsan.biosheapp.domain.User
import com.munifahsan.biosheapp.ui.chatRoom.ChatRoomActivity
import com.munifahsan.biosheapp.ui.contactList.ContactListActivity
import com.munifahsan.biosheapp.utils.Constants
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var adapterChat: ChatListFirestoreRecyclerAdapter? = null
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val dbChatRoom = FirebaseFirestore.getInstance()
        .collection("CHAT")
    private val dbUser = FirebaseFirestore.getInstance()
        .collection("USERS")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.floatingActionButtonAddChatPage.setOnClickListener {
            startContactListActivity()
        }

        //showChatList()

        return view
    }

    override fun onStart() {
        super.onStart()
        binding.rvChat.visibility = View.GONE
        binding.shimmerChat.visibility = View.VISIBLE
        showChatList()
        adapterChat!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterChat!!.stopListening()
    }

    private fun showChatList() {
        val linearLayoutManager = LinearLayoutManager(activity)
        binding.rvChat.layoutManager = linearLayoutManager
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef.collection("CHAT")
            .whereArrayContains("speakers", auth.currentUser!!.uid)
            .orderBy("updated", Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions
            .Builder<ChatRoom>()
            .setQuery(query, ChatRoom::class.java)
            .build()
        adapterChat = ChatListFirestoreRecyclerAdapter(options)
        binding.rvChat.adapter = adapterChat
        query.get().addOnSuccessListener {
            binding.rvChat.visibility = View.VISIBLE
            binding.shimmerChat.visibility = View.GONE
        }
    }

    private inner class ProductViewHolder(private val view: View) :
        RecyclerView.ViewHolder(
            view
        ) {

        fun setId(
            id: String,
            from: String,
            fromName: String,
            fromPhoto: String,
            to: String,
            toName: String,
            toPhoto: String,
            peakMessage: String,
            updated: Timestamp?
        ) {
            val photo = view.findViewById<ImageView>(R.id.photoChatList)
            val nama = view.findViewById<TextView>(R.id.textView_nama_chatList)
            val peakMessages = view.findViewById<TextView>(R.id.textView_peakMessage_chatList)
            val time = view.findViewById<TextView>(R.id.date)
            val numPeak = view.findViewById<TextView>(R.id.textView_notifNumPeak_chatList)
            val cardNumPeak = view.findViewById<CardView>(R.id.cardView_notifNumPeak_chatList)
            val linItem = view.findViewById<LinearLayout>(R.id.linearLayout_itemChatList)
            val indicator = view.findViewById<CardView>(R.id.indicator)

            linItem.setOnClickListener {
                if (from != auth.currentUser!!.uid) {
                    navigateToChatRoom(from)
                }

                if (to != auth.currentUser!!.uid) {
                    navigateToChatRoom(to)
                }

            }

            if (from != auth.currentUser!!.uid) {
                /*
                ambil data user dan update data chat
                 */
                dbUser.document(from).get().addOnSuccessListener {
                    if (it.exists()) {
                        val user = it.toObject(User::class.java)
                        dbChatRoom.document(id).update("fromName", user!!.nama)
                        dbChatRoom.document(id).update("fromPhoto", user.photo_url)
                    } else {
                        /*
                        ketika data USER tidak di temukan cek data SALES
                        */
                        Constants.SALES_DB.document(from).get().addOnSuccessListener { sales ->
                            if (sales.exists()) {
                                val user = sales.toObject(User::class.java)
                                dbChatRoom.document(id).update("fromName", user!!.nama)
                                dbChatRoom.document(id).update("fromPhoto", user.photo_url)
                            } else {
                                /*
                                ketika data SALES tidak ditemukan cek data DISTRIBUTOR
                                */
                                Constants.DISTRIBUTOR_DB.document(from).get()
                                    .addOnSuccessListener { distributor ->
                                        if (distributor.exists()) {
                                            val user = distributor.toObject(User::class.java)
                                            dbChatRoom.document(id).update("fromName", user!!.nama)
                                            dbChatRoom.document(id)
                                                .update("fromPhoto", user.photo_url)
                                        } else {
                                            /*
                                            ketika data DISTRIBUTOR tidak ditemukan cek data ADMIN
                                            */
                                            Constants.ADMIN_DB.document(from).get()
                                                .addOnSuccessListener { admin ->
                                                    if (admin.exists()) {
                                                        val user = admin.toObject(User::class.java)
                                                        dbChatRoom.document(id)
                                                            .update("fromName", user!!.nama)
                                                        dbChatRoom.document(id)
                                                            .update("fromPhoto", user.photo_url)
                                                    }
                                                }
                                        }
                                    }

                            }
                        }
                    }
                }

                dbChatRoom.document(id).collection("CHAT").whereEqualTo("sender", from)
                    .whereEqualTo("seen", false).addSnapshotListener { value, error ->
                        if (value!!.size() == 0) {
                            cardNumPeak.visibility = View.INVISIBLE
                        } else {
                            cardNumPeak.visibility = View.VISIBLE
                            numPeak.text = value.size().toString()
                        }
                    }

                nama.text = fromName
                if (fromPhoto.isNotEmpty()) {
                    Picasso.get()
                        .load(fromPhoto)
                        .placeholder(R.drawable.black_transparent)
                        .into(photo)
                }
            }

            if (to != auth.currentUser!!.uid) {
                /*
                ambil data user dan update data chat
                */
                dbUser.document(to).get().addOnSuccessListener {
                    if (it.exists()) {
                        val user = it.toObject(User::class.java)
                        dbChatRoom.document(id).update("toName", user!!.nama)
                        dbChatRoom.document(id).update("toPhoto", user.photo_url)
                    } else {
                        /*
                        ketika data USER tidak di temukan cek data SALES
                        */
                        Constants.SALES_DB.document(to).get().addOnSuccessListener { sales ->
                            if (sales.exists()) {
                                val user = sales.toObject(User::class.java)
                                dbChatRoom.document(id).update("fromName", user!!.nama)
                                dbChatRoom.document(id).update("fromPhoto", user.photo_url)
                            } else {
                                /*
                                ketika data SALES tidak ditemukan cek data DISTRIBUTOR
                                */
                                Constants.DISTRIBUTOR_DB.document(to).get()
                                    .addOnSuccessListener { distributor ->
                                        if (distributor.exists()) {
                                            val user = distributor.toObject(User::class.java)
                                            dbChatRoom.document(id).update("fromName", user!!.nama)
                                            dbChatRoom.document(id)
                                                .update("fromPhoto", user.photo_url)
                                        } else {
                                            /*
                                            ketika data DISTRIBUTOR tidak ditemukan cek data ADMIN
                                            */
                                            Constants.ADMIN_DB.document(to).get()
                                                .addOnSuccessListener { admin ->
                                                    if (admin.exists()) {
                                                        val user = admin.toObject(User::class.java)
                                                        dbChatRoom.document(id)
                                                            .update("fromName", user!!.nama)
                                                        dbChatRoom.document(id)
                                                            .update("fromPhoto", user.photo_url)
                                                    }
                                                }
                                        }
                                    }

                            }
                        }
                    }
                }

                dbChatRoom.document(id).collection("CHAT").whereEqualTo("sender", to)
                    .whereEqualTo("seen", false).addSnapshotListener { value, error ->
                        if (value!!.size() == 0) {
                            cardNumPeak.visibility = View.INVISIBLE
                        } else {
                            cardNumPeak.visibility = View.VISIBLE
                            numPeak.text = value.size().toString()
                        }
                    }

                nama.text = toName
                if (toPhoto.isNotEmpty()) {
                    Picasso.get()
                        .load(toPhoto)
                        .placeholder(R.drawable.black_transparent)
                        .into(photo)
                }
            }

            dbChatRoom.document(id).collection("CHAT").document(peakMessage)
                .addSnapshotListener { value, error ->
                    if (value != null) {
                        peakMessages.text = value.getString("message")
                        if (value.getString("sender").equals(auth.currentUser!!.uid)) {
                            indicator.visibility = View.VISIBLE
                        } else {
                            indicator.visibility = View.GONE
                        }

                        if (value.getBoolean("seen") == true) {
                            indicator.setCardBackgroundColor(Color.parseColor("#118EEA"))
                        } else {
                            indicator.setCardBackgroundColor(Color.parseColor("#B8CCDC"))
                        }
                    }
                }

//            dbChatRoom.document(id).addSnapshotListener { value, error ->
//                peakMessages.text = value!!.getString("peakMessage")
//            }


            if (getTimeDateNow1(updated!!.toDate()) == getTimeDateNow2()) {
                time.text = getTimeDateFirebase2(updated.toDate())
            } else {
                time.text = getTimeDateFirebase(updated.toDate())
            }
        }
    }

    private inner class ChatListFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<ChatRoom>) :
        FirestoreRecyclerAdapter<ChatRoom, ProductViewHolder>(
            options
        ) {
        override fun onBindViewHolder(
            productViewHolder: ProductViewHolder,
            position: Int,
            model: ChatRoom
        ) {
            productViewHolder.setId(
                model.id,
                model.from,
                model.fromName,
                model.fromPhoto,
                model.to,
                model.toName,
                model.toPhoto,
                model.peakMessage,
                model.updated
            )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_list_chat,
                parent,
                false
            )
            return ProductViewHolder(view)
        }
    }

    private fun getTimeDateFirebase(timestamp: Date?): String? {
        return try {
            //Date netDate = (timestamp);
            val sfd = SimpleDateFormat("dd/MM/yy, hh:mm a", Locale.getDefault())
            sfd.format(timestamp)
        } catch (e: Exception) {
            "date"
        }
    }

    private fun getTimeDateFirebase2(timestamp: Date?): String? {
        return try {
            //Date netDate = (timestamp);
            val sfd = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sfd.format(timestamp)
        } catch (e: Exception) {
            "date"
        }
    }

    private fun getTimeDateNow1(timestamp: Date?): String? {
        return try {
            //Date netDate = (timestamp);
            val sfd = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            sfd.format(timestamp)
        } catch (e: Exception) {
            "date"
        }
    }

    private fun getTimeDateNow2(): String? {
        return try {
            //Date netDate = (timestamp);
            val sfd = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            sfd.format(Date())
        } catch (e: Exception) {
            "date"
        }
    }

    private fun navigateToChatRoom(id: String) {
        val intent = Intent(activity, ChatRoomActivity::class.java)
        intent.putExtra("FRIEND_ID", id)
        startActivity(intent)
    }

    private fun startContactListActivity() {
        val intent = Intent(activity, ContactListActivity::class.java)
        startActivity(intent)
    }

}