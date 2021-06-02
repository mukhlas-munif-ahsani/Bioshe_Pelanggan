package com.munifahsan.biosheapp.domain

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.munifahsan.biosheapp.vo.Resource

class UserDao {

    val currentUser = Firebase.auth.currentUser
    val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
        .collection("USERS")

    fun getUserData(userId: String, fbVar: String): LiveData<Resource<String>> {
        val data = MutableLiveData<Resource<String>>()

        //get data from firestore
        db.document(userId)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    data.value = Resource.success(it.getString(fbVar))
                } else {
                    Log.d("document", "dosnt exist")
                    data.value = Resource.error("", "ID Sales tidak di temukan")
                }
            }
            .addOnFailureListener {
                data.value = Resource.error("Error", it.message!!)
            }
        return data
    }

    fun getUserDataInt(userId: String, fbVar: String): LiveData<Resource<Long>> {
        val data = MutableLiveData<Resource<Long>>()

        //get data from firestore
        db.document(userId)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    data.value = Resource.success(it.getLong(fbVar))
                } else {
                    Log.d("document", "dosnt exist")
                    data.value = Resource.error("Data tidak di temukan", 0)
                }
            }
            .addOnFailureListener {
                data.value = Resource.error("Error : ${it.message!!}", 0)
            }
        return data
    }

    fun editKeranjangData(
        fbVar: String,
        fbValue: Int
    ): LiveData<Resource<String>> {
        val dataStatus = MutableLiveData<Resource<String>>()

        db.document(currentUser?.uid.toString())
            .update(fbVar, fbValue)
            .addOnSuccessListener {
                dataStatus.value = Resource.success("updated")
            }
            .addOnFailureListener {
                dataStatus.value = Resource.error("Error", it.message)
            }

        return dataStatus
    }

    fun registerAndPostUser(
        email: String, pass: String,
        level: String,
        imageUri: Uri, nik: String,
        nama: String, gender: String,
        alamatRumah: String, noHp: String,
        noWa: String, ahliWaris: String,
        namaOutlet: String, alamatOutlet: String, idSales: String
    ): LiveData<Resource<Boolean>> {

        val dataStatus = MutableLiveData<Resource<Boolean>>()
        val credential = EmailAuthProvider.getCredential(email, pass)

        //Link anonymous auth to email and pass
        currentUser!!.linkWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = currentUser.uid

                    //Upload profile photo to firebase storage
                    val fileReference: StorageReference =
                        FirebaseStorage.getInstance().reference.child("images/" + userId + "/" + imageUri.lastPathSegment)
                    fileReference
                        .putFile(imageUri)
                        .addOnSuccessListener {

                            // download uploaded image url
                            fileReference
                                .downloadUrl
                                .addOnSuccessListener {

                                    // data model
                                    val userData = Users(
                                        nama,"",
                                        email,
                                        level,
                                        it.toString(),
                                        nik,
                                        gender,
                                        alamatRumah,
                                        noHp,
                                        noWa,
                                        ahliWaris,
                                        "","","",
                                        namaOutlet,
                                        alamatOutlet,
                                        idSales,
                                        0,
                                        0,
                                        0,
                                        "SILVER"
                                    )

                                    // post data to firestore
                                    db.document(userId)
                                        .set(userData)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                dataStatus.value = Resource.success(true)
                                            } else {
                                                dataStatus.value =
                                                    Resource.error(task.exception?.message!!, false)
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            dataStatus.value =
                                                Resource.error(exception.message!!, false)
                                        }
                                }
                        }
                        .addOnProgressListener {
                            dataStatus.value = Resource.loading(
                                (100.0 * it.bytesTransferred / it.totalByteCount),
                                true
                            )
                        }
                } else {
                    dataStatus.value = Resource.error(task.exception?.message!!, false)
                }
            }

        return dataStatus
    }

    fun login(email: String, pass: String): LiveData<Resource<Boolean>> {
        val dataStatus = MutableLiveData<Resource<Boolean>>()
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    dataStatus.value = Resource.success(true)
                } else {
                    dataStatus.value = Resource.error(it.exception?.message.toString(), false)
                }
            }.addOnFailureListener {
                dataStatus.value = Resource.error(it.message.toString(), false)
            }
        return dataStatus
    }
}