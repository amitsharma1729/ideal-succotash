package me.yashtrivedi.ideal_succotash;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class RequestService extends Service {

    Bundle b;

    NotificationManager notificationManager;
    public RequestService() {
    }

    Firebase firebase;
    ChildEventListener listener;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),13123,i,0);
        Intent iCancel = new Intent(getApplicationContext(),CancelRideIntentService.class);
        iCancel.putExtra(Constants.REQUESTED_USER,Constants.FIREBASE_URL_RIDES.concat("/").concat(b.getString(Constants.REQUESTED_USER, "")));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Requesting Ride")
//                        .setOngoing(true)
                .setContentIntent(pendingIntent)
                .addAction(new NotificationCompat.Action(R.drawable.ic_close_black_24dp, "Cancel", null));
        startForeground(13123, builder.build());
        b = intent.getExtras();
        firebase = new Firebase(Constants.FIREBASE_URL_RIDES.concat("/").concat(b.getString(Constants.REQUESTED_USER, "")));
        Log.d("firebase", firebase.toString());
        listener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("return", dataSnapshot.getValue().toString());
//                activity.update(Integer.parseInt(dataSnapshot.getValue().toString()), position);
                int status = Integer.parseInt(dataSnapshot.getValue().toString());
                if (status == Constants.RIDE_REQUEST_ACCEPTED) {
                    NotificationCompat.Builder notif = new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(b.getString("name") + " (" + b.getString(Constants.KEY_ENCODED_EMAIL) + ")")
                            .setContentText(Utils.statusString(status) + " your request")
                            .setSubText("Car No: " + b.getString(Constants.CAR_NO));
                    notificationManager.notify(12123, notif.build());
                } else if (status == Constants.RIDE_REQUEST_REJECTED) {
                    NotificationCompat.Builder notif = new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(b.getString("name") + " (" + b.getString(Constants.KEY_ENCODED_EMAIL) + ")")
                            .setContentText(Utils.statusString(Constants.RIDE_REQUEST_REJECTED) + " your request");
                    notificationManager.notify(12123, notif.build());
                }
                stopForeground(true);
                firebase.removeEventListener(listener);
                stopSelf();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //activity.remove(position);
                NotificationCompat.Builder notif = new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(b.getString("name") + " (" + b.getString(Constants.KEY_ENCODED_EMAIL) + ")")
                        .setContentText("Cancelled the Ride");
                notificationManager.notify(12123, notif.build());
                stopForeground(true);
                firebase.removeEventListener(listener);
                stopSelf();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };
        firebase.addChildEventListener(listener);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}
