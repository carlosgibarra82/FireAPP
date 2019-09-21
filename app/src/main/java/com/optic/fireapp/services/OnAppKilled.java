package com.optic.fireapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.reactivex.annotations.Nullable;

public class OnAppKilled extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);



        if(FirebaseAuth.getInstance().getCurrentUser() != null) {

            if(FirebaseAuth.getInstance().getCurrentUser().getUid() != null) {

                String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference rideReservationReference = FirebaseDatabase.getInstance().getReference().child("ReservationRide").child(user_id);
                rideReservationReference.removeValue();

            }

        }

    }
}
