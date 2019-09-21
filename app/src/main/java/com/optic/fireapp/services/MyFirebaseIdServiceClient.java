package com.optic.fireapp.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.optic.fireapp.models.Token;

public class MyFirebaseIdServiceClient extends FirebaseInstanceIdService {


    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // Cuando se ha refrescado el token, nesesitamos actualizar a la base de datos en tiempo real
        updateTokenToServer(refreshedToken);
    }

    private void updateTokenToServer(String refreshedToken) {


        DatabaseReference tokensReference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(refreshedToken);

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            tokensReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
        }

    }
}
