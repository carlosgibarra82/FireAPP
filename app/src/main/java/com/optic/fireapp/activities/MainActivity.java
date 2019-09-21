package com.optic.fireapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.optic.fireapp.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    // VIEWS
    @BindView(R.id.editTextEmailLogin) TextInputEditText mEditTextEmail;
    @BindView(R.id.editTextPasswordLogin) TextInputEditText mEditTextPassword;
    @BindView(R.id.btnLogin) Button mButtonLogin;

    // FIREBASE
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser mFirebaseUser;


    // PROGRESS
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        FirebaseApp.initializeApp(this);
        //Toolbar.showToolbar(this, "Inicio de sesion", true);

        // FIREBASE INSTANCIAS
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // PROGRESS INSTANCIA
        mProgress = new ProgressDialog(this);
    }

    /*
     * ONCLICK VALIDAR USUARIO REGISTRADO EN FIREBASE
     */
    @OnClick(R.id.btnLogin)
    void onClickValidateUser() {

        String email = mEditTextEmail.getText().toString().trim();
        String password = mEditTextPassword.getText().toString().trim();

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

            mProgress.setMessage("Espere un momento...");
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();

            if(isEmailValid(email)) {
                login(email, password);
            }
            else {
                Toast.makeText(this, "No es un correo electronico valido", Toast.LENGTH_SHORT).show();
            }

        }
        else {
            Toast.makeText(MainActivity.this, "Correo electronico y contraseña son requeridos", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * CLICK - IR A REGISTRO
     */
    @OnClick(R.id.btnRegister)
    void onClickGoToRegister() {
        startActivity(new Intent(MainActivity.this, RegisterActivity.class));
    }

    /*
     * ONCLICK - IR A REESTABLCER PASSWORD
     */
    /*
    @OnClick(R.id.textViewForgotPassword)
    void onClickForgotPassword() {
        Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
        startActivity(intent);
    }
    */

    /*
     * VERIFICAR QUE SEA UN EMAIL VALIDO
     */
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /*
     * METODO QUE VALIDA EL LOGIN DE USUARIO EN FIREBASE
     */
    private void login(final String email, final String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()) {

                    String id_user = FirebaseAuth.getInstance().getUid();
                    DatabaseReference clientsReference = mDatabase.child("Users").child("Firemans").child(id_user);

                    clientsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            mProgress.dismiss();

                            if(dataSnapshot.exists()) {
                                Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
                                mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mapIntent);
                                finish();
                            }
                            else {
                                mAuth.signOut();
                                Toast.makeText(MainActivity.this, "No es un usuario valido", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            mAuth.signOut();
                        }
                    });

                }
                else {
                    mProgress.dismiss();
                    Toast.makeText(MainActivity.this, "Los datos no son correctos vuelve a intentarlo", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    /*
     * VERIFICAR SI EL USUARIO ESTA LOGEADO O NO - SI ESTA LOGEADO ESTA LO ENVIO AL MAPA DEL CLIENTE
     */
    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
            mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mapIntent);
            finish();
        }
    }

}
