package android.example.com.squawker.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkProvider;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class SquawkFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = SquawkFirebaseMessagingService.class.getSimpleName();
    private static final int NOTIFICATION_MAX_CHARACTERS = 30;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        Map<String, String> data = remoteMessage.getData();

        // Check if message contains a data payload.
        if (data.size() > 0) {

            Log.d(TAG, "Message data payload: " + data);

            sendNotification(data);
            insertSquawk(data);


//            if (/* Check if data needs to be processed by long running job */ true) {
//                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
//                scheduleJob();
//            } else {
//                // Handle message within 10 seconds
//                handleNow();
//            }

        }

        // Check if message contains a notification payload.
//        if (remoteMessage.getNotification() != null) {
//            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
//        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void insertSquawk(final Map<String, String> data) {
        AsyncTask<Void, Void, Void> insertSquawk = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                ContentValues newMessage = new ContentValues();
                newMessage.put(SquawkContract.COLUMN_AUTHOR, data.get(SquawkContract.COLUMN_AUTHOR));
                newMessage.put(SquawkContract.COLUMN_AUTHOR_KEY, data.get(SquawkContract.COLUMN_AUTHOR_KEY));
                newMessage.put(SquawkContract.COLUMN_MESSAGE, data.get(SquawkContract.COLUMN_MESSAGE).trim());
                newMessage.put(SquawkContract.COLUMN_DATE, data.get(SquawkContract.COLUMN_DATE));

                getContentResolver().insert(SquawkProvider.SquawkMessages.CONTENT_URI, newMessage);

                return null;
            }
        };

        insertSquawk.execute();
    }

    private void sendNotification(Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String author = data.get(SquawkContract.COLUMN_AUTHOR);
        String message = data.get(SquawkContract.COLUMN_MESSAGE);

        if (message.length() > NOTIFICATION_MAX_CHARACTERS) {
            message = message.substring(0, NOTIFICATION_MAX_CHARACTERS) + "\u2026";
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_duck)
                .setContentTitle(String.format(getString(R.string.notification_message), author))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }
}
