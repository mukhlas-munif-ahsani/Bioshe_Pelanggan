package com.munifahsan.biosheapp.ui.chatRoom

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.munifahsan.biosheapp.MainActivity
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.databinding.ActivityChatRoomBinding
import com.munifahsan.biosheapp.domain.Chat
import com.munifahsan.biosheapp.domain.ChatRoom
import com.munifahsan.biosheapp.domain.User
import com.munifahsan.biosheapp.utils.Constants
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class ChatRoomActivity : AppCompatActivity() {

    val MSG_TYPE_NOT_ME = 0
    val MSG_TYPE_ME: Int = 1

    private lateinit var binding: ActivityChatRoomBinding
    private val dbChat = FirebaseFirestore.getInstance()
        .collection("CHAT")
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val dbUser = FirebaseFirestore.getInstance()
        .collection("USERS")
    private var adapterChat: ChatRoomActivity.ChatFirestoreRecyclerAdapter? = null

    private var chatRoomId: String = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val friendId = intent.getStringExtra("FRIEND_ID").toString()

        if (!intent.getStringExtra("FRIEND_ID_BG").isNullOrEmpty()){
            getChatRoom(intent.getStringExtra("FRIEND_ID_BG").toString())
            getUser(intent.getStringExtra("FRIEND_ID_BG").toString())
            binding.backIcon.setOnClickListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        } else {
            getChatRoom(friendId)
            getUser(friendId)
            binding.backIcon.setOnClickListener {
                finish()
            }
        }
        //showMessage(friendId.toString())
    }

    private fun getData(chatRoomId: String) {
        getUser(chatRoomId)
        dbChat.document(chatRoomId).get()
            .addOnSuccessListener { documentSnapshot ->
                val chat = documentSnapshot.toObject(ChatRoom::class.java)
                if (chat!!.from != auth.currentUser!!.uid) {
                    getUser(chat.from)
                }
                if (chat.to != auth.currentUser!!.uid) {
                    getUser(chat.to)
                }
            }.addOnFailureListener {

            }
    }

    private fun getChatRoom(friendId: String) {
        /*
        cek apakah chat room sudah di buat (currentId + id)
         */
        dbChat.document(auth.currentUser!!.uid + friendId).get()
            .addOnSuccessListener { documentSnapshot -> //showMessage(id);
                val chat1 = documentSnapshot.toObject(ChatRoom::class.java)
                if (documentSnapshot.exists()) {
                    /*
                    chat room dengan id (currentId + id) di temukan
                    user dapat langsung berkomunikasi chat
                     */
                    //showing chat
                    showChat(chat1!!.id)
                    chatRoomId = chat1.id
                    adapterChat!!.startListening()

                    binding.sendBtn.setOnClickListener {
                        if (binding.textEdt.text.trim().isNotEmpty()) {
                            val chat2 = Chat(
                                "",
                                binding.textEdt.text.toString(),
                                Date(),
                                false,
                                auth.currentUser!!.uid,
                                friendId
                            )
                            sendMessage(chat1.id, chat2, binding.textEdt.text.toString())
                            binding.textEdt.text.clear()
                        }
                    }
                } else {
                    /*
                    chat room dengan id (currentId + id) tidak ditemukan
                    chat room dengan id (id + currentId) ditemukan
                    user dapat langsung berkomunikasi chat
                     */
                    dbChat.document(friendId + auth.currentUser!!.uid).get()
                        .addOnSuccessListener { documentSnapshot2 ->
                            val chat3 = documentSnapshot2.toObject(ChatRoom::class.java)
                            if (documentSnapshot2.exists()) {
                                /*
                                chat room di temukan
                                user dapat langsung berkomunikasi chat
                                */
                                //showing chat
                                showChat(chat3!!.id)
                                chatRoomId = chat3.id
                                adapterChat!!.startListening()

                                binding.sendBtn.setOnClickListener {
                                    if (binding.textEdt.text.trim().isNotEmpty()) {
                                        val chat2 = Chat(
                                            "",
                                            binding.textEdt.text.toString(),
                                            Date(),
                                            false,
                                            auth.currentUser!!.uid,
                                            friendId
                                        )
                                        sendMessage(
                                            chat3.id,
                                            chat2,
                                            binding.textEdt.text.toString()
                                        )
                                        binding.textEdt.text.clear()
                                    }
                                }
                            } else {
                                /*
                                chat room dengan id (currentId + id) dan
                                chat room dengan id (id + currentId)
                                tidak ditemukan
                                */
                                createChatRoom(friendId)
                            }
                        }
                }
            }.addOnFailureListener {
                /*
                some think wrong
                 */
                showMessage("Error : ${it.message}")
            }
    }

    private fun createChatRoom(friendId: String) {
        binding.rvChat.visibility = View.VISIBLE
        binding.shimmerBubble.visibility = View.GONE
        binding.sendBtn.setOnClickListener {
            binding.sendBtn.isEnabled = false
            binding.sendBtn.isClickable = false
            binding.sendBtnIcon.visibility = View.GONE
            binding.sendBtnProgress.visibility = View.VISIBLE

            val chat = ChatRoom(
                "",
                auth.currentUser!!.uid,
                "",
                "",
                friendId,
                "",
                "",
                "",
                0,
                0,
                Timestamp(Date()),
                Timestamp(Date()),
                listOf(auth.currentUser!!.uid, friendId)
            )
            dbChat.document(auth.currentUser!!.uid + friendId).set(chat)
                .addOnSuccessListener { void ->
                    /*
                    chat room di temukan
                    user dapat langsung berkomunikasi chat
                    */
                    //showing chat
                    showChat(auth.currentUser!!.uid + friendId)
                    chatRoomId = auth.currentUser!!.uid + friendId
                    adapterChat!!.startListening()

                    if (binding.textEdt.text.trim().isNotEmpty()) {
                        val chat2 = Chat(
                            "",
                            binding.textEdt.text.toString(),
                            Date(),
                            false,
                            auth.currentUser!!.uid,
                            friendId
                        )
                        sendMessage(
                            auth.currentUser!!.uid + friendId,
                            chat2,
                            binding.textEdt.text.toString()
                        )
                        binding.textEdt.text.clear()
                    }
                }
                .addOnFailureListener {
                    showMessage("Error : ${it.message}")
                }
        }
    }

    private fun getUser(id: String) {
        /*
        cek apakah data USER ada
         */
        Constants.USER_DB.document(id).get().addOnSuccessListener {
            if (it.exists()){
                val user = it.toObject(User::class.java)
                if (user!!.photo_url.isNotEmpty()){
                    Picasso.get()
                        .load(user.photo_url)
                        .placeholder(R.drawable.black_transparent)
                        .into(binding.fotoImage)
                }
                binding.contactName.text = user.nama
                if (user.namaOutlet.isNotEmpty()) {
                    binding.outletName.text = user.namaOutlet
                    binding.outletName.visibility = View.VISIBLE
                }
            } else {
                /*
                ketika data USER tidak di temukan cek data SALES
                 */
                Constants.SALES_DB.document(id).get().addOnSuccessListener {sales->
                    if (sales.exists()){
                        val user = sales.toObject(User::class.java)
                        if (user!!.photo_url.isNotEmpty()){
                            Picasso.get()
                                .load(user.photo_url)
                                .placeholder(R.drawable.black_transparent)
                                .into(binding.fotoImage)
                        }
                        binding.contactName.text = user.nama
                        if (user.namaOutlet.isNotEmpty()) {
                            binding.outletName.text = user.namaOutlet
                            binding.outletName.visibility = View.VISIBLE
                        }
                    } else {
                        /*
                        ketika data SALES tidak di temukan cek data ADMIN
                         */
                        Constants.ADMIN_DB.document(id).get().addOnSuccessListener {admin->
                            if (admin.exists()){
                                val user = admin.toObject(User::class.java)
                                if (user!!.photo_url.isNotEmpty()){
                                    Picasso.get()
                                        .load(user.photo_url)
                                        .placeholder(R.drawable.black_transparent)
                                        .into(binding.fotoImage)
                                }
                                binding.contactName.text = user.nama
                                if (user.namaOutlet.isNotEmpty()) {
                                    binding.outletName.text = user.namaOutlet
                                    binding.outletName.visibility = View.VISIBLE
                                }
                            } else {
                                /*
                                 ketika data ADMIN tidak di temukan cek data DISTRIBUTOR
                                */
                                Constants.DISTRIBUTOR_DB.document(id).get().addOnSuccessListener {distributor->
                                    if (distributor.exists()){
                                        val user = distributor.toObject(User::class.java)
                                        if (user!!.photo_url.isNotEmpty()){
                                            Picasso.get()
                                                .load(user.photo_url)
                                                .placeholder(R.drawable.black_transparent)
                                                .into(binding.fotoImage)
                                        }
                                        binding.contactName.text = user.nama
                                        if (user.namaOutlet.isNotEmpty()) {
                                            binding.outletName.text = user.namaOutlet
                                            binding.outletName.visibility = View.VISIBLE
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sendMessage(chatRoomId: String, chat: Chat, message: String) {
        dbChat.document(chatRoomId).collection("CHAT").add(chat).addOnSuccessListener {
            dbChat.document(chatRoomId).update("peakMessage", it.id)
            binding.sendBtn.isEnabled = true
            binding.sendBtn.isClickable = true
            binding.sendBtnIcon.visibility = View.VISIBLE
            binding.sendBtnProgress.visibility = View.GONE
        }
        dbChat.document(chatRoomId).update("sender", auth.currentUser!!.uid)
        dbChat.document(chatRoomId).update("updated", Timestamp(Date()))
    }

    override fun onStart() {
        super.onStart()
        if (chatRoomId.isNotEmpty()) {
            binding.rvChat.visibility = View.GONE
            binding.shimmerBubble.visibility = View.VISIBLE
            showChat(chatRoomId)
            adapterChat!!.startListening()
        }
    }

    override fun onStop() {
        super.onStop()
        if (chatRoomId.isNotEmpty()) {
            adapterChat!!.stopListening()
        }
    }

    override fun onBackPressed() {
        if (!intent.getStringExtra("FRIEND_ID_BG").isNullOrEmpty()){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }else{
            super.onBackPressed()
        }
    }

    private fun showChat(chatRoomId: String) {
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.reverseLayout = false
        binding.rvChat.layoutManager = linearLayoutManager
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef.collection("CHAT")
            .document(chatRoomId).collection("CHAT")
            .orderBy("send", Query.Direction.ASCENDING)
        val options = FirestoreRecyclerOptions
            .Builder<Chat>()
            .setQuery(query, Chat::class.java)
            .build()
        adapterChat = ChatFirestoreRecyclerAdapter(options)
        adapterChat!!.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.rvChat.scrollToPosition(adapterChat!!.itemCount - 1)
            }
        })
        binding.rvChat.adapter = adapterChat

        query.get().addOnCompleteListener {
            binding.rvChat.visibility = View.VISIBLE
            binding.shimmerBubble.visibility = View.GONE
        }
    }

    private inner class ProductViewHolder(private val view: View) :
        RecyclerView.ViewHolder(
            view
        ) {

        fun setChat(id: String, message: String, seen: Boolean?, send: Date?, sender: String) {
            val messageTxt = view.findViewById<TextView>(R.id.textView_message)
            val time = view.findViewById<TextView>(R.id.textView_time)

            messageTxt.text = message
            //time.text = getTimeDate(send)

            if (getTimeDateNow1(send) == getTimeDateNow2()) {
                time.text = getTimeDateFirebase2(send)
            } else {
                time.text = getTimeDateFirebase(send)
            }

            if (itemViewType == MSG_TYPE_ME) {
                val status = view.findViewById<CardView>(R.id.indicator)
                if (seen == true) {
                    status.visibility = View.VISIBLE
                    status.setCardBackgroundColor(Color.parseColor("#118EEA"))
                } else {
                    status.visibility = View.VISIBLE
                    status.setCardBackgroundColor(Color.parseColor("#B8CCDC"))
                }
            } else {
                dbChat.document(chatRoomId).collection("CHAT").document(id).update("seen", true)
            }
        }
    }

    private inner class ChatFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<Chat>) :
        FirestoreRecyclerAdapter<Chat, ProductViewHolder>(
            options
        ) {
        override fun onBindViewHolder(
            productViewHolder: ProductViewHolder,
            position: Int,
            chatModel: Chat
        ) {
            productViewHolder.setChat(
                chatModel.id,
                chatModel.message,
                chatModel.seen,
                chatModel.send,
                chatModel.sender
            )
        }

        override fun getItemViewType(position: Int): Int {
            return if (getItem(position).sender == auth.currentUser!!.uid) {
                MSG_TYPE_ME
            } else {
                MSG_TYPE_NOT_ME
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view: View = if (viewType == MSG_TYPE_ME) {
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_chat_bubble_me,
                    parent,
                    false
                )
            } else {
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_chat_bubble_not_me,
                    parent,
                    false
                )
            }
            return ProductViewHolder(view)
        }
    }

    private fun getTimeDate(timestamp: Date?): String? {
        return try {
            //Date netDate = (timestamp);
            val sfd = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            sfd.format(timestamp)
        } catch (e: Exception) {
            "date"
        }
    }

    private fun getTimeDateFirebase(timestamp: Date?): String? {
        return try {
            //Date netDate = (timestamp);
            val sfd = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
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
            val sfd = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            sfd.format(timestamp)
        } catch (e: Exception) {
            "date"
        }
    }

    private fun getTimeDateNow2(): String? {
        return try {
            //Date netDate = (timestamp);
            val sfd = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            sfd.format(Date())
        } catch (e: Exception) {
            "date"
        }
    }

    private fun showMessage(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }
}