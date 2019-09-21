package com.optic.fireapp.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Repo;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.optic.fireapp.FCM.IFCMService;
import com.optic.fireapp.R;
import com.optic.fireapp.common.Common;
import com.optic.fireapp.includes.Toolbar;
import com.optic.fireapp.models.DataMessage;
import com.optic.fireapp.models.FCMResponse;
import com.optic.fireapp.models.Token;
import com.optic.fireapp.utils.CompressorBitmapImage;
import com.optic.fireapp.utils.FileUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportActivity extends AppCompatActivity {

    // VIEWS
    @BindView(R.id.imageViewSelectImageEditCategoryAdmin) ImageView mImageViewSelectImage;
    @BindView(R.id.textViewAddressReport) TextView mTextViewAddress;
    @BindView(R.id.textViewLatReport) TextView mTextViewLat;
    @BindView(R.id.textViewLngReport) TextView mTextViewLng;
    @BindView(R.id.editTextDescriptionReport) TextInputEditText mEditTextDescription;
    @BindView(R.id.btnReport) FloatingActionButton mFabReport;

    // FIREBASE
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private GeoQuery mGeoQuery;

    // GALERIA ATRIBUTOS
    private File mGalleryImageFile;
    private static final int GALLERY_REQUEST = 3;

    // FOTOS ATRIBUTOS
    private static final int REQUEST_IMAGE_CAPTURE= 1;
    private File mPhotoFile;
    private Uri mPhotoUri;
    private String mCurrentPhotoPath;
    private String mCurrentAbsolutePhotoPath;

    // IMAGEN QUE SE VA A ALMACENAR EN EL STORAGE DE FIREBASE
    private byte[] mByteImageCompress;

    // PROGRESS
    private ProgressDialog mProgress;

    // DATOS DE REPORTE
    private String mExtraAddress = "";
    private double mExtraLat = 0;
    private double mExtraLng = 0;
    private String mImageUrl = "";
    private String mDescription = "";

    private int mInserts = 0;
    private int mNotificationsSend = 0;

    // API
    private IFCMService mIFCMService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar.showCustomBackButtonToolbar(this, "Reportes", true);
        // EXTRA INTENT
        ButterKnife.bind(this);

        // FIREBASE INSTANCES
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference().child("images_categories");
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        // EXTRAS
        mExtraAddress = getIntent().getStringExtra("address");
        mExtraLat = getIntent().getDoubleExtra("lat", 0);
        mExtraLng = getIntent().getDoubleExtra("lng", 0);

        mTextViewAddress.setText(mExtraAddress);
        mTextViewLat.setText(String.valueOf(mExtraLat));
        mTextViewLng.setText(String.valueOf(mExtraLng));

        mIFCMService = Common.getFCMService();

        // PROGRESS INSTANCE
        mProgress = new ProgressDialog(this);
    }

    @OnClick(R.id.btnReport)
    void onClickSaveReport() {
        mDescription = mEditTextDescription.getText().toString();
        if (mCurrentPhotoPath != null) {
            if (!mDescription.equals("")) {
                if (mExtraLat != 0 && mExtraLng != 0) {
                    mFabReport.setEnabled(false);
                    mProgress.setMessage("Espere un momento...");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();
                    uploadImage();
                }
                else {
                    Toast.makeText(this, "Los datos de latitud y longitud no son validos", Toast.LENGTH_LONG).show();
                }
            }
            else {
                Toast.makeText(this, "Describe el incidente", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(this, "Sube una imagen", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * ONCLICK SELECCIONAR IMAGEN O FOTO
     */
    @OnClick(R.id.imageViewSelectImageEditCategoryAdmin)
    public void onClickSelectImageOrPhoto() {
        takePhoto();
    }

    /**
     * ALMACENAR IMAGEN EN FIREBASE STORAGE
     */
    private void uploadImage() {

        // COMPROBAR SI SE ELIJIO TOMAR FOTO
        if(mCurrentAbsolutePhotoPath != null) {
            mPhotoFile = new File(mCurrentAbsolutePhotoPath);
            mPhotoUri = Uri.fromFile(mPhotoFile);
            mByteImageCompress = CompressorBitmapImage.getBitmapImageCompress(ReportActivity.this, mPhotoUri.getPath(), 1024,520);
        }

        // COMPROBAR SI SE ELIJIO SELECCIONAR DESDE GALERIA
        if(mGalleryImageFile != null) {
            mByteImageCompress = CompressorBitmapImage.getBitmapImageCompress(ReportActivity.this, mGalleryImageFile.getPath(), 1024,520);
        }

        String push = FirebaseDatabase.getInstance().getReference().push().getKey();
        final StorageReference imageClientStorage = mStorage.child("image_report").child(mCurrentUserId).child(push + ".jpg");
        UploadTask uploadTask = imageClientStorage.putBytes(mByteImageCompress);

        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    imageClientStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // OBTENIENDO LA URL DE LA IMAGEN QUE SE ACABO DE GUARDAR
                            mImageUrl = uri.toString();
                            saveReport();
                        }
                    });
                }
                else {
                    mFabReport.setEnabled(true);
                    mProgress.dismiss();
                    Toast.makeText(ReportActivity.this, "Hubo un error al intentar guardar la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*
     * METODO QUE PERMITE CREAR UNA NUEVA CATEGORIA DE PRODUCTOS Y ALMACENARLA EN FIREBASE
     */
    private void saveReport() {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat formatterHour = new SimpleDateFormat("HH:mm");
        String today = formatter.format(new Date());
        String hour = formatterHour.format(new Date());
        Map<String, Object> data = new HashMap<>();
        data.put("lat", mExtraLat);
        data.put("lng", mExtraLng);
        data.put("timestamp", ServerValue.TIMESTAMP);
        data.put("date", today);
        data.put("hour", hour);
        data.put("image", mImageUrl);
        data.put("description", mDescription);
        data.put("id_user", mCurrentUserId);
        final String pushId = mDatabase.push().getKey();
        mDatabase.child("Reports").child(pushId).setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                DatabaseReference reportGeo = mDatabase.child("ReportsGeo");
                GeoFire geo = new GeoFire(reportGeo);
                geo.setLocation(pushId, new GeoLocation(mExtraLat, mExtraLng));
                getClosestUsers(mExtraLat, mExtraLng);
                Toast.makeText(ReportActivity.this, "El reporte se ha enviado correctamente", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mFabReport.setEnabled(true);
                mProgress.dismiss();
                Toast.makeText(ReportActivity.this, "Error al tratar de subir el reporte", Toast.LENGTH_LONG).show();
            }
        });
    }

    /*
     * METODO QUE PERMITE OBTENER LOS USUARIOS QUE TIENEN ASIGNDAS ZONAS DE MONITOREO
     */
    private void getClosestUsers(double lat, double lng) {
        final ArrayList<String> idFoundList = new ArrayList<>();
        DatabaseReference driverAvailableReference = mDatabase.child("Areas");
        GeoFire geofire = new GeoFire(driverAvailableReference);

        // REALIZANDO UNA CONSULTA A LA POCICION ACTUAL DEL CONDUCTOR MAS CERCANO
        mGeoQuery = geofire.queryAtLocation(new GeoLocation(lat, lng), 2);
        mGeoQuery.removeAllListeners();
        mGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

            // SI ENTRO EN ESTE METODO SIGNIFICA QUE EL CONDUCTOR FUE ENCONTRADO
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                idFoundList.add(key);
            }

            @Override
            public void onKeyExited(String key) {
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            // SI EL CONDUCTOR NO FUE ENCONTRADO EN UN RADIO DE UN KILOMETRO SE INCREMENTA EL RADIO DE BUSQUEDA HASTA ENCONTRAR UNO
            @Override
            public void onGeoQueryReady() {
                final ArrayList<String> idUserList = new ArrayList<>();
                for (String id: idFoundList) {
                    mDatabase.child("AreasUsers").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                if (dataSnapshot.hasChild("id_user")) {
                                    String id_user = dataSnapshot.child("id_user").getValue().toString();
                                    idUserList.add(id_user);
                                    mInserts++;
                                    if (mInserts == idFoundList.size()) {
                                        Set<String> idUserListClear = new LinkedHashSet<String>(idUserList);
                                        mNotificationsSend = idUserListClear.size();
                                        for (String id: idUserListClear) {
                                            sendNotificaction(id);
                                        }

                                    }
                                }
                            }
                            else {
                                mFabReport.setEnabled(true);
                                mProgress.dismiss();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }

        });
    }

    /*
     * ENVIAR NOTIFICACION
     */
    private void sendNotificaction(final String id_user) {

        DatabaseReference tokenReference = FirebaseDatabase.getInstance().getReference()
                .child("Tokens").child(id_user);
        tokenReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {

                    // OBTENIENDO EL TOKEN DEL USUARIO
                    String tk = dataSnapshot.child("token").getValue().toString();
                    Token token = new Token(tk);

                    Map<String, String> content = new HashMap<>();
                    content.put("title", "Reporte de punto caliente");
                    content.put("message", "Un punto caliente ha sido reportado cerca de tu zona");

                    DataMessage dataMessage = new DataMessage(token.getToken(),"high", "4500s", content);
                    mIFCMService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            assert response.body() != null;
                            if(response.body().success == 1) {
                                Log.d("ENTRO", "Notificacion enviada correctamente ID: " + id_user);
                            }
                            mNotificationsSend--;
                            if (mNotificationsSend == 0) {
                                mProgress.dismiss();
                                startActivity(new Intent(ReportActivity.this, MapActivity.class));
                                finish();
                            }
                        }
                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("ENTRO", "Error al enviar la notificacion: " + t.getMessage());
                        }
                    });
                }
                else {
                    mNotificationsSend--;
                    if (mNotificationsSend == 0) {
                        mProgress.dismiss();
                        startActivity(new Intent(ReportActivity.this, MapActivity.class));
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /*
     * METODO PARA PODER TOMAR FOTOS
     */
    private void takePhoto() {
        // INICIAR LA CAMARA DEL TELEFONO
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            File photoFile = null;

            try {
                // CREAR ARCHIVO UNA VEZ SE HAYA TOMADO LA FOTO
                photoFile = createPhotoFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(ReportActivity.this, "com.optic.fireapp", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /*
     * METODO QUE PERMITE CREAR EL ARCHIVO PARA SER ALMACENADA CON LA FOTO TOMADA
     */
    private File createPhotoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Creando un nuevo archivo en el storage de almacenamiento del telefono de imagenes
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photoFile = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = "file:" + photoFile.getAbsolutePath();
        mCurrentAbsolutePhotoPath = photoFile.getAbsolutePath();
        return photoFile;
    }

    /*
     * METODO QUE PERMITE INICIARLIZAR LA GALERIA DEL DISPOSITIVO MOVIL
     */
    private void startGalleryIntent() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    /*
     * OVERRIDE COLOCAR FOTO EN LOS IMAGEBUTTON
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // FOTOS
        // SI RESULT_OK MOSTRAR IMAGEN SELECCIONADA EN EL IMAGEBUTTON
        // SINO RESETAR EL PATH DE LA IMAGEN
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Picasso.with(ReportActivity.this).load(mCurrentPhotoPath).into(mImageViewSelectImage);

        }
        else if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_CANCELED){
            mCurrentAbsolutePhotoPath = null;
        }

        // GALERIA
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            try {
                mGalleryImageFile = FileUtil.from(ReportActivity.this, data.getData());
                mImageViewSelectImage.setImageBitmap(BitmapFactory.decodeFile(mGalleryImageFile.getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(requestCode == GALLERY_REQUEST && resultCode == RESULT_CANCELED){
            mGalleryImageFile = null;
        }

    }

}
