package com.munifahsan.biosheapp.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class Constants {
    companion object {

        /*
        MIDTRANS
         */
        const val CLIENT_KEY = "Mid-client-r6PLs6knQqiozSAn"
        const val BASE_URL = "https://bioshe.herokuapp.com/index.php/"
        const val BASE_URLII = "https://api.sandbox.midtrans.com"

        /*
        FIRESTORE DB
         */
        val USER_DB = FirebaseFirestore.getInstance()
            .collection("USERS")
        val SALES_DB = FirebaseFirestore.getInstance()
            .collection("SALES")
        val DISTRIBUTOR_DB = FirebaseFirestore.getInstance()
            .collection("DISTRIBUTOR")
        val ADMIN_DB = FirebaseFirestore.getInstance()
            .collection("ADMIN")
        val CHAT_DB = FirebaseFirestore.getInstance()
            .collection("CHAT")
        val CURRENT_USER = FirebaseAuth.getInstance().currentUser
        val AUTH = FirebaseAuth.getInstance()
        val CURRENT_USER_ID = FirebaseAuth.getInstance().currentUser?.uid
        val KERANJANG_DB = FirebaseFirestore.getInstance()
            .collection("USERS")
            .document(CURRENT_USER_ID.toString())
            .collection("KERANJANG")
        val PRODUK_DB = FirebaseFirestore.getInstance()
            .collection("PRODUCT")
        val TAGIHAN_DB = FirebaseFirestore.getInstance()
            .collection("USERS").document(CURRENT_USER_ID.toString()).collection("TAGIHAN")
        val PAYMENT_HISTORY_DB = FirebaseFirestore.getInstance()
            .collection("PAYMENT_HISTORY")
    }

    private fun getClient(context: Context?): Retrofit? {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client: OkHttpClient.Builder = OkHttpClient.Builder()
        client.readTimeout(60, TimeUnit.SECONDS)
        client.writeTimeout(60, TimeUnit.SECONDS)
        client.connectTimeout(60, TimeUnit.SECONDS)
        client.addInterceptor(interceptor)
        client.addInterceptor { chain ->
            var request = chain.request()
            request = if (context == null) {
                request.newBuilder().build()
            } else {
                request.newBuilder().addHeader("Authorization", "Bearer $CLIENT_KEY").build()
            }
            return@addInterceptor chain.proceed(request)
        }

        return Retrofit.Builder()
            .baseUrl(BASE_URLII)
            .client(client.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}