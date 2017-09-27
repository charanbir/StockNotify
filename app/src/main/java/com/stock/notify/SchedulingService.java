package com.stock.notify;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


/**
 * This {@code IntentService} does the app's actual work.
 * {@code AlarmReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class SchedulingService extends IntentService {
    public static final String TAG = "SchedulingService";
    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;
    GeneralUtils generalUtils = new GeneralUtils();
    AlarmReceiver alarm = new AlarmReceiver();
    private NotificationManager mNotificationManager;

    public SchedulingService() {
        super("SchedulingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            if (generalUtils.hasNewRecommendations(getApplicationContext())) {
                sendNotification(getString(R.string.new_notification));
                Log.i(TAG, getString(R.string.new_notification));
            } else {
                Log.i(TAG, "Nothing to buy, no new notification");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Release the wake lock provided by the BroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);
    }

    // Post a notification
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NotifyMessage.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(msg)
                        .setStyle(new NotificationCompat.BigTextStyle()
                        );

        mBuilder.setContentIntent(contentIntent);
        mBuilder.setAutoCancel(true);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

//    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
//    r.play();

    }

}
