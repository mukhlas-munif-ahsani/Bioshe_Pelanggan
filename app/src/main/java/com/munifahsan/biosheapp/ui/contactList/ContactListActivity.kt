package com.munifahsan.biosheapp.ui.contactList

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.databinding.ActivityContactListBinding
import com.munifahsan.biosheapp.domain.ChatRoom
import com.munifahsan.biosheapp.domain.User
import com.munifahsan.biosheapp.ui.chatRoom.ChatRoomActivity
import com.squareup.picasso.Picasso
import java.util.*

class ContactListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactListBinding
    private lateinit var auth: FirebaseAuth
    private var mAdapter: ContactListAdapter? = null
    private val dbChat = FirebaseFirestore.getInstance()
        .collection("CHAT")
    private var adminAdapter: AdminFirestoreRecyclerAdapter? = null
    private var salesAdapter: SalesFirestoreRecyclerAdapter? = null
    private var distributorAdapter: DistributorFirestoreRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()

        mAdapter = ContactListAdapter()
        showContact()
        showContactAdmin()
        showContactSales()
        showContactDistributor()

        binding.backIcon.setOnClickListener {
            finish()
        }

        binding.searchIcon.setOnClickListener {
            val transition: Transition = Fade()
            transition.duration = 300
            transition.addTarget(binding.toolbarSearch)

            TransitionManager.beginDelayedTransition(view, transition)
            binding.toolbarSearch.visibility = View.VISIBLE

            binding.searchEdt.requestFocus()
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchEdt, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.backIconSearch.setOnClickListener {
            val transition: Transition = Fade()
            transition.duration = 300
            transition.addTarget(binding.toolbarSearch)

            TransitionManager.beginDelayedTransition(view, transition)
            binding.toolbarSearch.visibility = View.GONE

            binding.searchEdt.text.clear()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchEdt.windowToken, 0)
        }
    }

    override fun onBackPressed() {
        if (binding.toolbarSearch.isVisible) {
            val transition: Transition = Fade()
            transition.duration = 300
            transition.addTarget(binding.toolbarSearch)

            TransitionManager.beginDelayedTransition(binding.root, transition)
            binding.toolbarSearch.visibility = View.GONE

            binding.searchEdt.text.clear()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchEdt.windowToken, 0)
        } else {
            super.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        adminAdapter!!.startListening()
        salesAdapter!!.startListening()
        distributorAdapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        adminAdapter!!.stopListening()
        salesAdapter!!.stopListening()
        distributorAdapter!!.stopListening()
    }

    private fun showContact() {
//        binding.rvContact.layoutManager = object : LinearLayoutManager(this) {
//            override fun canScrollVertically(): Boolean {
//                return false
//            }
//        }
        binding.rvContact.layoutManager = LinearLayoutManager(this)
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef.collection("USERS")
            .whereNotEqualTo(FieldPath.documentId(), auth.currentUser!!.uid)

        query.get().addOnCompleteListener {
            mAdapter!!.setListModels(it.result!!.toObjects(User::class.java))
            binding.rvContact.adapter = mAdapter
        }
        mAdapter!!.notifyDataSetChanged()

        binding.searchEdt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //mAdapter!!.filter.filter(p0.toString())
//                if (mAdapter!!.itemCount == 0) {
//
//                    if (p0.isNullOrEmpty()) {
//                        binding.kosong.visibility = View.INVISIBLE
//                        binding.rvContact.visibility = View.VISIBLE
//                        binding.rvContactAdmin.visibility = View.VISIBLE
//                        binding.rvContactSales.visibility = View.VISIBLE
//                        binding.rvContactDistributor.visibility = View.VISIBLE
//                    } else {
//                        binding.kosong.visibility = View.VISIBLE
//                        binding.rvContact.visibility = View.INVISIBLE
//                        binding.rvContactAdmin.visibility = View.GONE
//                        binding.rvContactSales.visibility = View.GONE
//                        binding.rvContactDistributor.visibility = View.GONE
//                    }
//
//                } else {
//                    binding.kosong.visibility = View.INVISIBLE
//                    binding.rvContact.visibility = View.VISIBLE
//                }
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                mAdapter!!.filter.filter(p0.toString())

                if (mAdapter!!.itemCount == 0) {

                    if (p0.isNullOrEmpty()) {
                        binding.kosong.visibility = View.INVISIBLE
                        binding.rvContact.visibility = View.VISIBLE
                    } else {
                        binding.kosong.visibility = View.VISIBLE
                        binding.rvContact.visibility = View.INVISIBLE
                    }

                } else {
                    binding.kosong.visibility = View.INVISIBLE
                    binding.rvContact.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                if (mAdapter!!.itemCount == 0) {

                    if (p0.isNullOrEmpty()) {
                        binding.kosong.visibility = View.INVISIBLE
                        binding.rvContact.visibility = View.VISIBLE
                        binding.rvContactAdmin.visibility = View.VISIBLE
                        binding.rvContactSales.visibility = View.VISIBLE
                        binding.rvContactDistributor.visibility = View.VISIBLE
                    } else {
                        binding.kosong.visibility = View.VISIBLE
                        binding.rvContact.visibility = View.INVISIBLE
                        binding.rvContactAdmin.visibility = View.GONE
                        binding.rvContactSales.visibility = View.GONE
                        binding.rvContactDistributor.visibility = View.GONE
                    }

                } else {
                    binding.kosong.visibility = View.INVISIBLE
                    binding.rvContact.visibility = View.VISIBLE
                }

                if (p0.isNullOrEmpty()) {
                    binding.admin.visibility = View.VISIBLE
                    binding.sales.visibility = View.VISIBLE
                    binding.distributor.visibility = View.VISIBLE
                    binding.customer.visibility = View.VISIBLE
                    binding.rvContactAdmin.visibility = View.VISIBLE
                    binding.rvContactSales.visibility = View.VISIBLE
                    binding.rvContactDistributor.visibility = View.VISIBLE
                } else {
                    binding.admin.visibility = View.GONE
                    binding.sales.visibility = View.GONE
                    binding.distributor.visibility = View.GONE
                    binding.customer.visibility = View.GONE
                    binding.rvContactAdmin.visibility = View.GONE
                    binding.rvContactSales.visibility = View.GONE
                    binding.rvContactDistributor.visibility = View.GONE
                }
            }

        })

        mAdapter!!.setOnListItemCliked { id, position ->
            //getChatRoom(id)
            navigateToChatRoom(id)
        }
    }

    private fun showContactAdmin() {
        val linearLayoutManager = object : LinearLayoutManager(this) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        binding.rvContactAdmin.layoutManager = linearLayoutManager
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef.collection("ADMIN")
            .whereArrayContains("customer", Firebase.auth.currentUser!!.uid)

        val options = FirestoreRecyclerOptions
            .Builder<User>()
            .setQuery(query, User::class.java)
            .build()
        adminAdapter = AdminFirestoreRecyclerAdapter(options)
        binding.rvContactAdmin.adapter = adminAdapter
    }

    private fun showContactSales() {
        val linearLayoutManager = object : LinearLayoutManager(this) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        binding.rvContactSales.layoutManager = linearLayoutManager
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef.collection("SALES")
            .whereArrayContains("customer", Firebase.auth.currentUser!!.uid)

        val options = FirestoreRecyclerOptions
            .Builder<User>()
            .setQuery(query, User::class.java)
            .build()
        salesAdapter = SalesFirestoreRecyclerAdapter(options)
        binding.rvContactSales.adapter = salesAdapter
    }

    private fun showContactDistributor() {
        val linearLayoutManager = object : LinearLayoutManager(this) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        binding.rvContactDistributor.layoutManager = linearLayoutManager
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef.collection("DISTRIBUTOR")
            .whereArrayContains("customer", Firebase.auth.currentUser!!.uid)

        val options = FirestoreRecyclerOptions
            .Builder<User>()
            .setQuery(query, User::class.java)
            .build()
        distributorAdapter = DistributorFirestoreRecyclerAdapter(options)
        binding.rvContactDistributor.adapter = distributorAdapter
    }

    private fun navigateToChatRoom(id: String) {
        val intent = Intent(this, ChatRoomActivity::class.java)
        intent.putExtra("FRIEND_ID", id)
        startActivity(intent)
    }

    private inner class ContactViewHolder(private val view: View) :
        RecyclerView.ViewHolder(
            view
        ) {
        fun setup(
            id: String,
            nama: String,
            outlet: String,
            foto: String
        ) {
            val namaTxt = view.findViewById<TextView>(R.id.namaUser)
            val image = view.findViewById<ImageView>(R.id.circleImageView_photo_addContact)
            val card = view.findViewById<LinearLayout>(R.id.layout_addContact)
            val outletTxt = view.findViewById<TextView>(R.id.namaOutlet)

            namaTxt.text = nama
            if (foto.isNotEmpty()) {
                Picasso.get().load(foto).into(image)
            }
            outletTxt.text = outlet

            card.setOnClickListener {
                navigateToChatRoom(id)
            }
        }
    }

    private inner class AdminFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<User>) :
        FirestoreRecyclerAdapter<User, ContactViewHolder>(
            options
        ) {
        override fun onBindViewHolder(
            contactViewHolder: ContactViewHolder,
            position: Int,
            model: User
        ) {
            contactViewHolder.setup(
                model.id,
                model.nama,
                model.namaOutlet,
                model.photo_url
            )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_contact_list,
                parent,
                false
            )
            return ContactViewHolder(view)
        }
    }

    private inner class SalesFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<User>) :
        FirestoreRecyclerAdapter<User, ContactViewHolder>(
            options
        ) {
        override fun onBindViewHolder(
            contactViewHolder: ContactViewHolder,
            position: Int,
            model: User
        ) {
            contactViewHolder.setup(
                model.id,
                model.nama,
                model.namaOutlet,
                model.photo_url
            )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_contact_list,
                parent,
                false
            )
            return ContactViewHolder(view)
        }
    }

    private inner class DistributorFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<User>) :
        FirestoreRecyclerAdapter<User, ContactViewHolder>(
            options
        ) {
        override fun onBindViewHolder(
            contactViewHolder: ContactViewHolder,
            position: Int,
            model: User
        ) {

            contactViewHolder.setup(
                model.id,
                model.nama,
                model.namaOutlet,
                model.photo_url
            )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_contact_list,
                parent,
                false
            )
            return ContactViewHolder(view)
        }
    }

    //----------------------------------------------------------------


    private fun startCharRoomActivity(id: String?) {
        Toast.makeText(this, id, Toast.LENGTH_LONG).show()
    }

    private fun getChatRoom(id: String) {
        //useless
        dbChat.document(auth.currentUser!!.uid + id).get()
            .addOnSuccessListener { documentSnapshot -> //showMessage(id);
                val chat = documentSnapshot.toObject(ChatRoom::class.java)
                if (documentSnapshot.exists()) {
                    navigateToChatRoom(chat!!.id)
                } else {
                    dbChat.document(id + auth.currentUser!!.uid).get()
                        .addOnSuccessListener { documentSnapshot2 ->
                            val chat2 = documentSnapshot2.toObject(ChatRoom::class.java)
                            if (documentSnapshot2.exists()) {
                                navigateToChatRoom(chat2!!.id)
                            } else {
                                createChatRoom(id)
                            }
                        }
                }
            }.addOnFailureListener {

            }
    }

    private fun createChatRoom(id: String) {
        //useless
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
                .addOnSuccessListener {
                    navigateToChatRoom(auth.currentUser!!.uid + id)
                }
                .addOnFailureListener {

                }
        }

    }

}