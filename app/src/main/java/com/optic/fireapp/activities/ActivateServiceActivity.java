package com.optic.fireapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.optic.fireapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActivateServiceActivity extends AppCompatActivity {

    // VIEWS
    @BindView(R.id.textViewPhoneCentral) TextView mTextViewPhone;


    // FIREBASE
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String mCurrentUserId;
    private DatabaseReference mStateReference;
    private ValueEventListener mListenerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate_service);
        ButterKnife.bind(this);

        // FIREBASE INSTACIAS
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // VERIFICAR SI EL CONDUCTOR ESTA ACTIVO
        checkIfUserIsActive();
        getAdminData();
    }

    /**
     * CLICK LOGOUT
     */
    @OnClick(R.id.btnLogout)
    void onClickLogout() {
        logout();
    }

    /*
     * METODO QUE PERMITE TERMINAR LA SESION ACTUAL DEL USUARIO
     */
    private void logout() {
        mAuth.signOut();
        Intent typeUserIntent = new Intent(ActivateServiceActivity.this, MainActivity.class);
        startActivity(typeUserIntent);
        finish();
    }

    /**
     * OBTENER EL TELEFONO DE LA CENTRAL
     */
    private void getAdminData() {
        mDatabase.child("Config").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("phone")) {
                        String phone = dataSnapshot.child("phone").getValue().toString();
                        mTextViewPhone.setText("Tel: " + phone);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /*
     * VERIFICAR SI EL CONDUCTOR ESTA ACTIVO - ES DECIR CANCELO LA COUTA
     */
    private void checkIfUserIsActive() {
        mStateReference = mDatabase.child("Users").child("Reporters").child(mCurrentUserId).child("state");
        mListenerState = mStateReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String state = dataSnapshot.getValue().toString();
                    if(state.equals("Activo") || state.equals("Verificado")) {
                        mStateReference.removeEventListener(mListenerState);
                        Intent mapIntent = new Intent(ActivateServiceActivity.this, MapActivity.class);
                        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mapIntent);
                        finish();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * ON DESTROY
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mStateReference != null) {
            mStateReference.removeEventListener(mListenerState);
        }
    }

}
