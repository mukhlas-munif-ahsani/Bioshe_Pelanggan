package com.munifahsan.biosheapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.munifahsan.biosheapp.R
import com.munifahsan.biosheapp.ui.chatRoom.ChatRoomActivity

class FirebaseMessageService : FirebaseMessagingService() {

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        Constants.USER_DB.document(Constants.CURRENT_USER_ID.toString()).update("tokenId", newToken).addOnCompleteListener {
            Log.d("NEW TOKEN", newToken)
        }
    }

//    private fun showMessage(msg: String) {
//        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
//    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

//        val intent = Intent(this, ChatRoomActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        val pendingIntent = PendingIntent.getActivity(
//            this, 0 /* Request code */, intent,
//            PendingIntent.FLAG_ONE_SHOT
//        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        Log.d("TAG FCM", "Message Received")

        val messageTitle = remoteMessage.getData().get("title")
        val messageBody = remoteMessage.getData().get("body")
        val clickAction = remoteMessage.notification!!.clickAction
        //val dataMessage = remoteMessage.data["message"]
        val friendId = remoteMessage.getData().get("FRIEND_ID_FG")

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.bioshe_icon)
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
//            .setContentIntent(pendingIntent)

        val clickIntent = Intent(clickAction)
        //clickIntent.putExtra("message", dataMessage)
        clickIntent.putExtra("FRIEND_ID", friendId)

        val resultPendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            clickIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        notificationBuilder.setContentIntent(resultPendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        val id = System.currentTimeMillis().toInt()
        notificationManager.notify(id /* ID of notification */, notificationBuilder.build())
    }
}