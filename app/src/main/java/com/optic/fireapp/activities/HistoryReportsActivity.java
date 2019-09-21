package com.optic.fireapp.activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.optic.fireapp.R;
import com.optic.fireapp.adapters.HistoryReportAdapter;
import com.optic.fireapp.adapters.MessageAdapter;
import com.optic.fireapp.includes.Toolbar;
import com.optic.fireapp.models.Message;
import com.optic.fireapp.models.Report;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class HistoryReportsActivity extends AppCompatActivity {

    // RECYCLERVIEW
    private RecyclerView mRecyclerView;
    private HistoryReportAdapter mAdapter;
    private LinearLayoutManager mLinearManager;

    // FIREBASE
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private ArrayList<Report> mReports = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_reports);
        ButterKnife.bind(this);
        Toolbar.showToolbar(this, "Reportes", true);

        // FIREBASE INSTANCES
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        // RECYCLERVIEW CONFIGURACIONES
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewReports);
        mRecyclerView.setHasFixedSize(true);
        mLinearManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearManager);

        getReports();
    }

    /*
     * METODO QUE CREA EL CHAT ENTRE USUARIOS
     */
    private void getReports() {
        mDatabase.child("Reports").orderByChild("id_user").equalTo(mCurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        Report report = new Report();
                        report.setDate(ds.child("date").getValue().toString());
                        report.setHour(ds.child("hour").getValue().toString());
                        report.setImage(ds.child("image").getValue().toString());
                        report.setDescription(ds.child("description").getValue().toString());
                        report.setId_user(ds.child("id_user").getValue().toString());
                        report.setLat(Double.parseDouble(ds.child("lat").getValue().toString()));
                        report.setLng(Double.parseDouble(ds.child("lng").getValue().toString()));
                        mReports.add(report);
                    }
                    mAdapter = new HistoryReportAdapter(mReports, R.layout.cardview_history_reports, HistoryReportsActivity.this);
                    mRecyclerView.setAdapter(mAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
