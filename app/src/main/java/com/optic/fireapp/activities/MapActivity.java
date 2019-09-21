package com.optic.fireapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.optic.fireapp.BuildConfig;
import com.optic.fireapp.R;
import com.optic.fireapp.common.Common;
import com.optic.fireapp.models.Token;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    // GOOGLE MAPS API
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private SupportMapFragment mapFragment;
    private Marker mDriverMarker;
    private final static int LOCATION_REQUEST_CODE = 1;

    // FIREBASE
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String mCurrentUserId;

    // VARIABLE UTILIZADA PARA ALMACENAR LA LATITUD Y LONGITUD DEL CONDUCTOR
    private LatLng mDriverLatLng;

    // VARIABLE QUE SE UTILIZA PARA SABER SI YA SE CARGO TOTALMENTE LA UBICACION DEL USUARIO
    private boolean mGpsIsWorking = false;

    private DatabaseReference mStateInactiveReference;
    private ValueEventListener mListenerStateInactive;

    private DatabaseReference mReportsRef;
    private ValueEventListener mListenerReports;

    private boolean mLocationWasLoaded = false;

    // PROGRESS
    private ProgressDialog mProgress;

    // MENU ACTUALIZAR APP
    private MenuItem mUpdateMenu;

    PowerManager.WakeLock mWakeLock;

    private String mAddress = "";

    /*
     * ESTABLECE LA POSICION ACTUAL DEL CONDUCTOR EN EL MAPA DE GOOGLE Y GUARDA LOS DATOS DE LA POSICION EN FIREBASE (CADA SEGUNDO)
     */
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    Common.mLastLocation = location;

                    mDriverLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (!mLocationWasLoaded) {
                        dismissProgressDialog();
                        mLocationWasLoaded = true;
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(mDriverLatLng)
                                        .zoom(15f)
                                        .build()
                        ));
                    }
                    /*
                    if (mDriverMarker != null) {
                        mDriverMarker.remove();
                    }

                    if(mDriverLatLng != null) {
                        mDriverMarker = mMap.addMarker(new MarkerOptions().position(mDriverLatLng).title("Tu posicion")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_user_location_blue)));
                        //mDriverMarker = mMap.addMarker(new MarkerOptions().position(mDriverLatLng).title("Tu posicion"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(mDriverLatLng));
                    }
                    */
                    mGpsIsWorking = true;
                    getReports();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        // MOSTRANDO EL TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        // GOOGLE MAP INSTANCIA
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapClient);
        mapFragment.getMapAsync(this);

        // PROGRESS INSTANCE
        mProgress = new ProgressDialog(this);

        // FIREBASE INSTACIAS
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // ACTUALIZAR TOKEN DE LAS NOTIFICACIONES
        updateFirebaseToken();

        // VERIFICAR SI EL CONDUCTOR ESTA INACTIVO
        checkIfUserIsInactive();

        // MENU DE NAVEGACION INSTANCIAS
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setUserInfoInNavigationView(navigationView);

    }

    /**
     *  DETENER WAKE LOCK
     */
    private void stopWakeLock() {
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }

    /**
     * CLICK - IR A REPORTE
     */
    @OnClick(R.id.btnReportMap)
    void onClickReport() {
        if (mDriverLatLng != null) {
            getNameAddress();
            Intent intent = new Intent(MapActivity.this, ReportActivity.class);
            intent.putExtra("lat", mDriverLatLng.latitude);
            intent.putExtra("lng", mDriverLatLng.longitude);
            intent.putExtra("address", mAddress);
            startActivity(intent);
        } else {
            Toast.makeText(this, "No se encuentra tu ubicación", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * ONCLICK - CENTRAR POSICION
     */
    @OnClick(R.id.imageButtonCenterPosition)
    void onClickCenterPosition() {
        if (GPSIsActivated() && mGpsIsWorking) {

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(mDriverLatLng)
                            .zoom(15f)
                            .build()
            ));
            //sendNotificationDriverArrived();

        } else if (GPSIsActivated() && !mGpsIsWorking) {
            Toast.makeText(this, "Tu localizacion no esta bien configurada", Toast.LENGTH_SHORT).show();
        } else {
            buildAlertMessageNoGps();
        }
    }

    /**
     * OBTENER REPORTES DE LOS USUARIOS
     */
    private void getReports() {
        long now = new Date().getTime() + (2*3600*1000);
        long last = new Date().getTime() - (2*24*60*60*1000);
        mReportsRef = mDatabase.child("Reports");
        mListenerReports = mReportsRef.orderByChild("timestamp").startAt(last).endAt(now).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMap.clear();
                if (dataSnapshot.exists()) {
                    HashMap<String, String> images = new HashMap<String, String>();

                    // Add keys and values (Country, City)
                    //capitalCities.put("England", "London");
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.hasChild("lat") && ds.hasChild("lng")) {
                            String image = "", date = "", hour = "", description = "";
                            if (ds.hasChild("image")) {
                                image = ds.child("image").getValue().toString();
                            }
                            if (ds.hasChild("date")) {
                                date = ds.child("date").getValue().toString();
                            }
                            if (ds.hasChild("hour")) {
                                hour = ds.child("hour").getValue().toString();
                            }
                            if (ds.hasChild("description")) {
                                description = ds.child("description").getValue().toString();
                            }
                            double lat = Double.parseDouble(ds.child("lat").getValue().toString());
                            double lng = Double.parseDouble(ds.child("lng").getValue().toString());
                            LatLng latLng = new LatLng(lat, lng);
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title("Fecha: " + date + "\n" +
                                            "Hora: " + hour)
                                    .snippet(
                                            "Lat: " + lat + "\n" +
                                            "Lng: " + lng + "\n" +
                                            "Descripción: " + description)
                                    .icon(BitmapDescriptorFactory
                                            .fromResource(R.drawable.icon_fire)));
                            marker.setTag(ds.getKey());
                            images.put(ds.getKey(), image);
                        }
                    }
                    mMap.setInfoWindowAdapter(new PopupAdapter(MapActivity.this, getLayoutInflater(), images));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * OBTENER NOMBRE DE LA DIRECCION DONDE ESTA EL USUARIO
     */
    private void getNameAddress() {
        Geocoder geocoder = new Geocoder(MapActivity.this);

        try {
            List<Address> addressList = geocoder.getFromLocation(mDriverLatLng.latitude, mDriverLatLng.longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                String locality = addressList.get(0).getAddressLine(0);
                String country = addressList.get(0).getCountryName();
                String city = addressList.get(0).getLocality();
                mAddress = locality;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * MOSTRAR PROGRESS DIALOG
     */
    private void showProgressDialog(String message) {
        mProgress.setMessage(message);
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();
    }

    /**
     * DIALOG - DESVANCER
     */
    private void dismissProgressDialog() {
        if (mProgress != null) {
            if (mProgress.isShowing()) {
                mProgress.dismiss();
            }

        }
    }

    /**
     * VERIFICAR VERSION DE LA APLICACION
     */
    private void checkAPPVersion() {

        final int versionCode = BuildConfig.VERSION_CODE;

        mDatabase.child("VersionDriver").child("code").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    int code = Integer.parseInt(dataSnapshot.getValue().toString());
                    mUpdateMenu.setVisible(true);

                    if (code != versionCode) {
                        mUpdateMenu.setVisible(false);
                        mUpdateMenu.setTitle("Actualiza tu aplicacion");
                        Toast.makeText(MapActivity.this, "Por favor actualiza tu aplicacion en Playstore", Toast.LENGTH_LONG).show();
                        //startActivity(new Intent(MapDriverActivity.this, UpdateYourAPPActivity.class));
                        //finish();
                    } else {
                        mUpdateMenu.setVisible(false);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /*
     * METODO QUE PERMITE ESTABLECER LOS DATOS DEL CLIENTE EN EL MENU DE NAVEGACION
     */
    private void setUserInfoInNavigationView(NavigationView navigationView) {

        View headerView = navigationView.getHeaderView(0);
        final TextView textViewUserName = (TextView) headerView.findViewById(R.id.textViewNameNav);
        final TextView textViewUserEmail = (TextView) headerView.findViewById(R.id.textViewEmailNav);
        final CircleImageView circleImageUser = (CircleImageView) headerView.findViewById(R.id.circleImageUserNav);

        mDatabase.child("Users").child("Reporters").child(mCurrentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            if (dataSnapshot.hasChild("name")) {
                                String name = dataSnapshot.child("name").getValue().toString();
                                textViewUserName.setText(name);
                            }

                            if (dataSnapshot.hasChild("email")) {
                                String email = dataSnapshot.child("email").getValue().toString();
                                textViewUserEmail.setText(email);
                            }

                            if (dataSnapshot.hasChild("image")) {
                                String image = dataSnapshot.child("image").getValue().toString();

                                if (!image.equals("default") && !image.equals("")) {
                                    Picasso.with(MapActivity.this).load(image).into(circleImageUser);
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
     * METODO QUE PERMITE ACTUALIZAR EL TOKEN PARA RECIBIR LAS NOTIFICACIONES
     */
    private void updateFirebaseToken() {
        DatabaseReference tokensReference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            tokensReference.child(mCurrentUserId).setValue(token);
        }
    }

    /**
     * VERIFICAR SI EL CONDUCTOR ESTA INACTIVO
     */
    private void checkIfUserIsInactive() {
        mStateInactiveReference = mDatabase.child("Users").child("Reporters").child(mCurrentUserId).child("state");
        mListenerStateInactive = mStateInactiveReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    String state = dataSnapshot.getValue().toString();
                    if (state.equals("Inactivo") || state.equals("new_user") || state.equals("Rechazado")) {
                        Intent activateIntent = new Intent(MapActivity.this, ActivateServiceActivity.class);
                        activateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(activateIntent);
                        finish();
                    }
                } else {
                    Intent activateIntent = new Intent(MapActivity.this, ActivateServiceActivity.class);
                    activateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(activateIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * CUANDO EL MAPA DE GOOGLE SE HAYA CARGADO TOTALMENTE
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // ESTILO DE MAPA COMO EL DE UBER
        try {
            boolean isSuccess = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.uber_style_map));
            if (!isSuccess) {
                Toast.makeText(this, "Error: El mapa no cargo los estilos", Toast.LENGTH_SHORT).show();
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        // LOS DATOS DE LA UBICACION SE REFRESCARAN CADA 1000 MILISEGUNDOS EN EL MAPA DE GOOGLE
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(250);
        mLocationRequest.setFastestInterval(250);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(3);

        connectDriver();
    }

    /*
     * CONFIGURAR LOCALIZACION CUANDO LOS PERMISOS HAYAN SIDO CONCEDIDOS
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    if (!GPSIsActivated()) {
                        buildAlertMessageNoGps();
                    }
                    else {
                        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    }
                    //mMap.setMyLocationEnabled(true);

                }
            } else {
                Toast.makeText(MapActivity.this, "Por favor proporciona los permisos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
     * VERIFICAR PERMISOS DE LOCALIZACION
     * SI NO HAY PERMISOS MOSTRAR UNA ALERT DIALOG PARA QUE EL USUARIO LOS ASIGNE
     */
    public void checkLocationPermitions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Conceder permisos")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(
                                        MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    /*
     * METODO QUE PERMITE SETEAR LA UBICACION ACTUAL DEL CONDUCTOR EN EL MAPA
     */
    private void connectDriver() {

        // SI LA VERSION DE ANDROID ES MAYOR A MARSMALLOW ENTONCES VERIFICAR PERMISOS DE UBICACION
        // SINO SETEAR DIRECTAMENTE LA UBICACION ACTUAL DEL CLIENTE EN EL MAPA
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (!GPSIsActivated()) {

                    //verificateIfDriverIsAvailable();
                    buildAlertMessageNoGps();

                }
                else {

                    if (ActivityCompat.checkSelfPermission(MapActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    showProgressDialog("Buscando tu posicion...");

                }

                //mMap.setMyLocationEnabled(true);
                //mMap.getUiSettings().setMyLocationButtonEnabled(false);
            } else {
                checkLocationPermitions();
            }
        } else {

            if (!GPSIsActivated()) {
                buildAlertMessageNoGps();
            }
            else {
                if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                showProgressDialog("Buscando tu posicion...");
            }
            //mMap.setMyLocationEnabled(true);
            //mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    /*
     * METODO QUE SIRVE PARA SABER SI EL USUARIO TIENE EL GPP ACTIVADO
     */
    public boolean GPSIsActivated() {
        boolean gpsIsActivated = true;
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            gpsIsActivated = false;
        }
        return gpsIsActivated;
    }

    /**
     * CUANDO EL GPS DEL TELEFONO HA SIDO DETECTADO
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && GPSIsActivated()) {
            if (ActivityCompat.checkSelfPermission(
                    MapActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                return;
            }
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            showProgressDialog("Buscando tu posicion...");
        }
        if (requestCode == 1 && !GPSIsActivated()) {
            buildAlertMessageNoGps();
        }

    }

    /*
     * MOSTRAR VENTANA EMERGENTE IR A ACTIVACION DE GPS
     */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicación para continuar")
                .setCancelable(false)
                .setPositiveButton("Ir a configuraciones", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
                    }
                });

        final AlertDialog alert = builder.create();
        alert.show();
    }


    /**
     * ELIMINAR EL TOKEN DE NOTIFICACIONES
     */
    private void deleteToken() {
        DatabaseReference tokensReference = FirebaseDatabase.getInstance().getReference("Tokens").child(mCurrentUserId);
        tokensReference.removeValue();
    }

    /*
     * METODO QUE PERMITE TERMINAR LA SESION ACTUAL DEL USUARIO
     */
    private void logout() {
        deleteToken();
        mAuth.signOut();
        Intent typeUserIntent = new Intent(MapActivity.this, MainActivity.class);
        startActivity(typeUserIntent);
        finish();
    }

    /**
     * MENU DE NAVEGACION
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
            //super.onBackPressed();
        }
    }

    /**
     * NAVIGATION BAR
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // ACCIONES MENU
        if (id == R.id.nav_action_edit) {
            startActivity(new Intent(MapActivity.this, EditActivity.class));
        }

        if (id == R.id.nav_action_chat) {
            startActivity(new Intent(MapActivity.this, ChatActivity.class));
        }

         if (id == R.id.nav_action_history) {
            startActivity(new Intent(MapActivity.this, HistoryReportsActivity.class));
         }

        // ACCION CERRAR SESION DEL CLIENTE
        if (id == R.id.nav_action_logout) {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*
     * OBTENIENDO EXTRAS
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if(getIntent().hasExtra("updateData")) {
            Toast.makeText(this, "Se han actualizado los datos", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(getIntent());
        }
    }

    /**
     * ON DESTROY
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mStateInactiveReference != null)
            mStateInactiveReference.removeEventListener(mListenerStateInactive);

        if (mReportsRef != null)
            mReportsRef.removeEventListener(mListenerReports);

        stopWakeLock();
    }

    /**
     * INFLAR MENU
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.update_app_menu, menu);
        mUpdateMenu = menu.findItem(R.id.action_update_app);
        mUpdateMenu.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * ACCIONES DEL MENU
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_update_app) {
            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * CLASE PARA MOSTRAR INFORMACION EN EL ICONO DEL REPORTE
     */
    class PopupAdapter implements GoogleMap.InfoWindowAdapter {
        private View popup = null;
        private LayoutInflater inflater = null;
        private HashMap<String, String> images = null;
        private Context ctxt = null;
        private Marker lastMarker = null;

        PopupAdapter(Context ctxt, LayoutInflater inflater,
                     HashMap<String, String> images) {
            this.ctxt = ctxt;
            this.inflater = inflater;
            this.images = images;

        }

        @Override
        public View getInfoWindow(Marker marker) {
            return (null);
        }

        @SuppressLint("InflateParams")
        @Override
        public View getInfoContents(Marker marker) {
            if (popup == null) {
                popup = inflater.inflate(R.layout.popup, null);
            }

            if (lastMarker == null
                    || !lastMarker.getId().equals(marker.getId())) {
                lastMarker = marker;

                TextView tv = (TextView) popup.findViewById(R.id.title);

                tv.setText(marker.getTitle());
                tv = (TextView) popup.findViewById(R.id.snippet);
                tv.setText(marker.getSnippet());

                String image = images.get(marker.getTag());
                Log.d("IMAGEN", "Marker: " + marker.getTag());
                Log.d("IMAGEN", "url: " + image);
                ImageView icon = (ImageView) popup.findViewById(R.id.icon);

                if (image == null) {
                    icon.setVisibility(View.GONE);
                } else {
                    Picasso.with(ctxt).load(image)
                            .into(icon, new MarkerCallback(marker));
                }
            }

            return (popup);
        }

        class MarkerCallback implements Callback {
            Marker marker = null;

            MarkerCallback(Marker marker) {
                this.marker = marker;
            }

            @Override
            public void onError() {
                Log.e(getClass().getSimpleName(), "Error loading thumbnail!");
            }

            @Override
            public void onSuccess() {
                if (marker != null && marker.isInfoWindowShown()) {
                    marker.showInfoWindow();
                }
            }
        }
    }

}







