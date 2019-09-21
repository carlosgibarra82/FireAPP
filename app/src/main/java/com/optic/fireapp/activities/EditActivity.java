package com.optic.fireapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.optic.fireapp.R;
import com.optic.fireapp.includes.Toolbar;
import com.optic.fireapp.utils.CompressorBitmapImage;
import com.optic.fireapp.utils.FileUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditActivity extends AppCompatActivity {

    // VISTAS
    @BindView(R.id.imageViewEditClient)
    ImageView mImageViewEditClient;
    @BindView(R.id.editTextNameEditClient)
    TextInputEditText mEditTextName;
    @BindView(R.id.editTextEmailEditClient) TextInputEditText mEditTextEmail;
    @BindView(R.id.editTextPhoneEditClient) TextInputEditText mEditTextPhone;

    // FIREBASE
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    // PROGRESS DIALOG
    private ProgressDialog mProgress;

    // GALLERY EXTRA
    private static final int GALLERY_REQUEST = 1;

    // VARIABLE NESESARIAS PARA COMPRIMIR IMAGENES
    private File mImageData;

    // VARIABLES QUE CONTENDRAN LOS DATOS DEL PERFIL DEL USUARIO
    private String mName = "";
    private String mEmail = "";
    private String mPhone = "";
    private String mImage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);
        Toolbar.showToolbar(this, "Editar cuenta", true);

        // FIREBASE INSTANCIAS
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        // PROGRESSBAR INSTANCIA
        mProgress = new ProgressDialog(this);

        // ESTABLECIENDO LOS DATOS DEL CLIENTE EN LOS CAMPOS DE EDICION
        setClientData();

    }

    /*
     * METODO QUE PERMITE ESTABLECER LOS DATOS DEL USUARIO
     */
    public void setClientData() {
        DatabaseReference clientReference = mDatabase.child("Users").child("Reporters").child(mCurrentUserId);
        clientReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {


                    // ESTABLECIENDO LOS VALORES EN LOS EDIT TEXT
                    if(dataSnapshot.hasChild("name")) {
                        mName = dataSnapshot.child("name").getValue().toString();
                        mEditTextName.setText(mName);
                    }

                    if(dataSnapshot.hasChild("email")) {
                        mEmail = dataSnapshot.child("email").getValue().toString();
                        mEditTextEmail.setText(mEmail);
                    }

                    if(dataSnapshot.hasChild("phone")) {
                        mPhone = dataSnapshot.child("phone").getValue().toString();
                        mEditTextPhone.setText(mPhone);
                    }

                    if(dataSnapshot.hasChild("image")) {
                        mImage = dataSnapshot.child("image").getValue().toString();

                        if(!mImage.equals("default") && !mImage.equals("")) {
                            Picasso.with(EditActivity.this).load(mImage).into(mImageViewEditClient);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /*
     * ONCLICK - INICIALIZAR LA GALERIA PARA SELECCIONAR UNA IMAGEN
     */
    @OnClick(R.id.imageViewEditClient)
    void onClickStartGalleryIntent () {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    /*
     * ONCLICK - EDITAR DATOS DEL CLIENTE
     */
    @OnClick(R.id.btnEditClient)
    void onClickEditClient (){

        final String name = mEditTextName.getText().toString();
        final String email = mEditTextEmail.getText().toString().trim();
        final String phone = mEditTextPhone.getText().toString().trim();

        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(phone)) {

            mProgress.setMessage("Actualizando datos...");
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();

            // SI EL USUARIO SELECCIONO UNA IMAGEN NUEVA DE LA GALERIA LA ACTUALIZO EN EL STORAGE DE FIREBASE
            // SINO SOLO INGRESO LOS NUEVOS DATOS DE NOMBRE, TELEFONO Y EMAIÑ
            if(mImageData != null) {

                // COMPRIMIENDO LA IMAGEN Y TRANFORMANDOLA A BITMAP
                byte[] thumb_image = CompressorBitmapImage.getBitmapImageCompress(this, mImageData.getPath(), 300,300);
                final StorageReference imageClientStorage = mStorage.child("images_clients").child(mCurrentUserId + ".jpg");
                UploadTask uploadTask = imageClientStorage.putBytes(thumb_image);
                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            imageClientStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    // OBTENIENDO LA URL DE LA IMAGEN QUE SE ACABO DE GUARDAR
                                    String imageUrl = uri.toString();

                                    updateClientInfo(name, email, phone, imageUrl);
                                }
                            });

                        }
                        else {
                            mProgress.dismiss();
                            Toast.makeText(EditActivity.this, "Hubo un error al intentar guardar la imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
            else {
                updateClientInfo(name, email, phone, mImage);
            }



        }
        else {
            Toast.makeText(this, "Los datos no pueden estar vacios", Toast.LENGTH_SHORT).show();
        }

    }

    /*
     * METODO QUE ACTUALIZAR ALMACENAR LOS DATOS DEL CLIENTE EN DATABASE DE FIREBASE
     */
    private void updateClientInfo(String name, String email, String phone, String imageUrl) {



        final DatabaseReference clientReference = mDatabase.child("Users").child("Reporters").child(mCurrentUserId);

        Map<String, Object> clientMap = new HashMap<>();
        clientMap.put("name", name);
        clientMap.put("email", email);
        clientMap.put("phone", phone);
        clientMap.put("image", imageUrl);

        clientReference.updateChildren(clientMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mProgress.dismiss();
                Intent mapIntent = new Intent(EditActivity.this, MapActivity.class);
                startActivity(mapIntent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mProgress.dismiss();
                Toast.makeText(EditActivity.this, "Hubo un error al intentar guardar los datos del usuario", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /*
     * METODO QUE ESTABLECE LA IMAGEN EN LA VISTA CUANDO EL USUARIO HA SELECCIONADO UNA DE LA GALERIA
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            try {
                mImageData = FileUtil.from(this, data.getData());
                mImageViewEditClient.setImageBitmap(BitmapFactory.decodeFile(mImageData.getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
