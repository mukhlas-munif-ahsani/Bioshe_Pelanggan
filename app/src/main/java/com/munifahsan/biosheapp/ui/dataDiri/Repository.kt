package com.munifahsan.biosheapp.ui.dataDiri

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.munifahsan.biosheapp.domain.User

class Repository(private val mListener: Contract.Listener) : Contract.Repository {


    val currentUser = Firebase.auth.currentUser
    val auth = FirebaseAuth.getInstance()
    private val dbOrders = FirebaseFirestore.getInstance()
        .collection("ORDERS")
    private val dbUsers = FirebaseFirestore.getInstance()
        .collection("USERS")

    override fun getData() {
        dbUsers.document(currentUser!!.uid).get().addOnSuccessListener {
            val user = it.toObject(User::class.java)
            mListener.getDataListener(
                user!!.photo_url,
                user.email,
                user.nama,
                user.nik,
                user.gender,
                user.alamatRumah,
                user.noHp,
                user.noWa,
                user.ahliWaris,
                user.namaOutlet,
                user.alamatOutlet
            )
        }.addOnFailureListener {
            mListener.showError(it.message.toString())
        }
    }

    override fun updateData(
        email: String,
        imageUri: Uri?, nik: String,
        nama: String, gender: String,
        alamatRumah: String, noHp: String,
        noWa: String, ahliWaris: String,
        namaOutlet: String, alamatOutlet: String
    ) {
        if (imageUri != null) {
            val fileReference: StorageReference =
                FirebaseStorage.getInstance().reference.child("images/" + currentUser!!.uid + "/" + imageUri.lastPathSegment)
            fileReference
                .putFile(imageUri)
                .addOnSuccessListener {

                    // download uploaded image url
                    fileReference
                        .downloadUrl
                        .addOnSuccessListener {

                            // post data to firestore
                            dbUsers.document(currentUser.uid)
                                .update(
                                    "nama", nama,
                                    "nik", nik,
                                    "gender", gender,
                                    "photo_url", it.toString(),
                                    "alamatRumah", alamatRumah,
                                    "noHp", noHp,
                                    "noWa", noWa,
                                    "ahliWaris", ahliWaris,
                                    "namaOutlet", namaOutlet,
                                    "alamatOutlet", alamatOutlet
                                )
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        getData()
                                    } else {
                                        mListener.showError(task.exception!!.message.toString())
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    mListener.showError(exception.message.toString())
                                }
                        }
                }
                .addOnProgressListener {
//                dataStatus.value = Resource.loading(
//                    (100.0 * it.bytesTransferred / it.totalByteCount),
//                    true
//                )
                }
        } else {
            // post data to firestore
            dbUsers.document(currentUser!!.uid)
                .update(
                    "nama", nama,
                    "nik", nik,
                    "gender", gender,
                    "alamatRumah", alamatRumah,
                    "noHp", noHp,
                    "noWa", noWa,
                    "ahliWaris", ahliWaris,
                    "namaOutlet", namaOutlet,
                    "alamatOutlet", alamatOutlet
                )
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        getData()
                    } else {
                        mListener.showError(task.exception!!.message.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    mListener.showError(exception.message.toString())
                }
        }

    }

}