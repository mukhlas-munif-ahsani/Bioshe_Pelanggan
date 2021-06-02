package com.munifahsan.biosheapp.ui.pageAkun

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.munifahsan.biosheapp.domain.User

class Repository (val mListener: AkunContract.Listener) : AkunContract.Repository {
    private val dbUsers = FirebaseFirestore.getInstance()
        .collection("USERS")

    val currentUser = Firebase.auth.currentUser

    override fun getData(){
        currentUser?.let {
            dbUsers.document(currentUser.uid).get().addOnSuccessListener {
                val user = it.toObject(User::class.java)
                mListener.getDataListener(user!!.photo_url, user.nama, user.loyalti, user.id)
            }.addOnFailureListener {
                mListener.showError(it.message.toString())
            }
        }
    }
}