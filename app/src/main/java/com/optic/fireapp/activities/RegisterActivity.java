package com.optic.fireapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.optic.fireapp.R;
import com.optic.fireapp.includes.NetworkUtils;
import com.optic.fireapp.includes.Toolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends AppCompatActivity {

    // VIEWS
    @BindView(R.id.editTextNameRegisterClient) TextInputEditText mEditTextName;
    @BindView(R.id.editTextEmailRegisterClient) TextInputEditText mEditTextEmail;
    @BindView(R.id.editTextPhoneRegisterClient) TextInputEditText mEditTextPhone;
    @BindView(R.id.editTextDocumentRegisterClient) TextInputEditText mEditTextDocument;
    @BindView(R.id.editTextPasswordRegisterClient) TextInputEditText mEditTextPassword;
    @BindView(R.id.editTextPasswordConfirmRegisterClient) TextInputEditText mEditTextConfirmPassword;
    @BindView(R.id.btnRegisteClient) Button mButtonRegister;

    // FIREBASE
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // PROGRESS
    private ProgressDialog mProgress;

    // CAMPOS DEL USUARIO
    String mName;
    String mEmail;
    String mPhone;
    String mDocument;
    String mPassword;
    String mPasswordConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        Toolbar.showToolbar(this, "Registro de usuario", true);

        // PROGRESS INSTANCIA
        mProgress = new ProgressDialog(this);

        // FIREBASE INSTANCES
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(10);
        mEditTextPhone.setFilters(filterArray);

    }
    /*
     * ONCLICK - REGISTRAR UN NUEVO CLIENTE EN FIREBASE
     */
    @OnClick(R.id.btnRegisteClient)
    void onClickRegisterClient() {

        if (NetworkUtils.isNetworkConnected(this)) {
            mName = mEditTextName.getText().toString();
            mEmail = mEditTextEmail.getText().toString().trim();
            mDocument = mEditTextDocument.getText().toString().trim();
            mPhone = mEditTextPhone.getText().toString().trim();
            mPassword = mEditTextPassword.getText().toString().trim();
            mPasswordConfirm = mEditTextConfirmPassword.getText().toString().trim();

            if(!TextUtils.isEmpty(mName) && !TextUtils.isEmpty(mEmail) && !TextUtils.isEmpty(mPhone) &&  !TextUtils.isEmpty(mDocument) && !TextUtils.isEmpty(mPassword) && !TextUtils.isEmpty(mPasswordConfirm)) {
                if (mName.length() >= 3) {

                    if (mPhone.length() == 10) {
                        if(mPassword.length() >= 6) {

                            if(mPassword.equals(mPasswordConfirm)) {

                                if(isEmailValid(mEmail)) {
                                    mProgress.setTitle("Registrando Usuario");
                                    mProgress.setMessage("Espere un momento...");
                                    mProgress.show();
                                    registerUserInFirebase();
                                }
                                else {
                                    Toast.makeText(this, "No es un email valido", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(RegisterActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                            }
                        }

                        else {
                            Toast.makeText(RegisterActivity.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(this, "El telefono debe tener 10 digitos", Toast.LENGTH_SHORT).show();
                    }

                }
                else {
                    Toast.makeText(this, "El nombre debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
                }



            }
            else {
                Toast.makeText(RegisterActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "No hay conexion a internet", Toast.LENGTH_SHORT).show();
        }

    }
    /**
     * GUARDAR DATOS DEL USUARIO
     */
    private void saveUserData() {

        // OBTENER ID DEL USUARIO QUE SE CREO
        String user_id = mAuth.getCurrentUser().getUid();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", mName);
        userMap.put("email", mEmail);
        userMap.put("phone", mPhone);
        userMap.put("document", mDocument);
        userMap.put("password", mPassword);
        userMap.put("state", "Activo");
        userMap.put("image", "");

        DatabaseReference userReference = mDatabase.child("Users").child("Reporters").child(user_id);
        userReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Registro de usuario exitoso", Toast.LENGTH_SHORT).show();
                    Intent mapIntent = new Intent(RegisterActivity.this, MapActivity.class);
                    mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mapIntent);
                    finish();
                } else {
                    mProgress.dismiss();
                    mAuth.signOut();
                    Toast.makeText(RegisterActivity.this, "Hubo un error al registrar el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

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
     * METODO QUE REGISTRA AL USUARIO EN FIREBASE
     */
    private void registerUserInFirebase() {

        mAuth.createUserWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    saveUserData();
                }
                else {
                    mProgress.dismiss();
                    Toast.makeText(RegisterActivity.this, "El usuario ya existe", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
