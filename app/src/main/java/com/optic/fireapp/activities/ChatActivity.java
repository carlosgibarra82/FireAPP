package com.optic.fireapp.activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.optic.fireapp.R;
import com.optic.fireapp.adapters.MessageAdapter;
import com.optic.fireapp.models.Message;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    // VIEWS CUSTOM TOOLBAR
    private View mActionBarView;
    private CircleImageView mCircleImageNameProfileChat;
    private TextView mTextViewNameChat;
    private TextView mTextViewLastConnect;

    // VIEWS CHAT
    @BindView(R.id.circleImageSendMessageChat) CircleImageView mCircleImageSendMessageChat;
    @BindView(R.id.editTextMessageChat) EditText mEditTextSendMessageChat;

    // RECYCLERVIEW
    private RecyclerView mRecyclerViewMessages;
    private FirestoreRecyclerAdapter mAdapter;
    private LinearLayoutManager mLinearManager;

    // FIREBASE
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    // EXTRAS
    private String user_id = "";
    private String name = "";
    private String image = "";

    // ESCRIBIENDO CONFIGURACIONES
    private Timer timer = new Timer();
    private long DELAY = 1000; // in ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        // TOOLBAR PERSONALIZADO INSTANCE
        showCustomToolbar(R.layout.custom_chat_toolbar);

        // VIEWS CUSTOM BAR INSTANCES
        mCircleImageNameProfileChat = (CircleImageView) mActionBarView.findViewById(R.id.circleImageNameProfileChat);
        mTextViewNameChat = (TextView) mActionBarView.findViewById(R.id.textViewNameChat);
        mTextViewLastConnect = (TextView) mActionBarView.findViewById(R.id.textViewLastTimeConnect);

        // EXTRAS INSTANCES
        user_id = getIntent().getStringExtra("user_id");
        name = getIntent().getStringExtra("name");
        image = getIntent().getStringExtra("image");

        // FIREBASE INSTANCES
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        // SETEANDO VALORES A LOS VIEWS
        mTextViewNameChat.setText("Administrador FIREAPP");

        if (image != null) {
            if (!image.equals("")) {
                Picasso.with(ChatActivity.this).load(image).placeholder(R.mipmap.ic_user).into(mCircleImageNameProfileChat);
            }
        }

        // RECYCLERVIEW CONFIGURACIONES
        mRecyclerViewMessages = (RecyclerView) findViewById(R.id.recyclerViewMessages);
        mRecyclerViewMessages.setHasFixedSize(true);
        mLinearManager = new LinearLayoutManager(this);
        mLinearManager.setReverseLayout(true);
        mLinearManager.setStackFromEnd(true);
        mRecyclerViewMessages.setLayoutManager(mLinearManager);

        // CARGAR MENSAJES
        // ADAPTER FIRESTORE INSTANCE
        Query query = mFirestore.collection("Messages").whereEqualTo("id_user", mCurrentUserId).orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Message> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .build();
        mAdapter = new MessageAdapter(firestoreRecyclerOptions, ChatActivity.this);
        mAdapter.notifyDataSetChanged();

        // SCROLL AUTOMATICO A LA PRIMERA POSICION
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mLinearManager.smoothScrollToPosition(mRecyclerViewMessages, null,0);
            }
        });
        mRecyclerViewMessages.setAdapter(mAdapter);
    }

    /**
     * CLICK - ENVIAR MENSAJE
     */
    @OnClick(R.id.circleImageSendMessageChat)
    void onClickSendMessage() {
        sendMessage();
    }

    /**
     * CREAR CHAT EN FIRESTORE
     */
    private void createChat() {
        // SI ES LA PRIMERA VEZ QUE ENVIA UN MENSAJE SE CREA EL CHAT
        mFirestore.collection("Chats").document(mCurrentUserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("ENTRO", "DOCUMENT");

                if (!documentSnapshot.exists()) {
                    Log.d("ENTRO", "DOCUMENT NO EXISTE");
                    Map<String, Object> chatMap = new HashMap();
                    chatMap.put("seenByAdmin", false);
                    chatMap.put("seenByUser", false);
                    chatMap.put("timestamp", System.currentTimeMillis() / 1000);
                    chatMap.put("isWritingUser", false);
                    chatMap.put("isWritingAdmin", false);
                    chatMap.put("id_user", mCurrentUserId);
                    chatMap.put("id_admin", "admin");
                    mFirestore.collection("Chats").document(mCurrentUserId).set(chatMap).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("ENTRO", "ERROR: " + e.getMessage());
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ENTRO: ", "ERROR: "+ e.getMessage());
            }
        });
    }

    /*
     * METODO QUE PERMITE ENVIAR MENSAJES
     */
    private void sendMessage() {
        String message = mEditTextSendMessageChat.getText().toString();
        if (!message.equals("")) {
            Log.d("ENTRO", "Mensaje enviado");
            createChat();
            // CREANDO DATOS DEL MENSAJE EN FIREBASE
            Map<String, Object> messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seenByAdmin", false);
            messageMap.put("seenByUser", false);
            messageMap.put("timestamp", System.currentTimeMillis() / 1000);
            messageMap.put("id_user", mCurrentUserId);
            messageMap.put("id_chat", mCurrentUserId);
            messageMap.put("id_admin", "admin");
            messageMap.put("sendByUser", true);
            messageMap.put("sendByAdmin", false);

            mFirestore.collection("Messages").add(messageMap).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatActivity.this, "Error al tratar de enviar el mensaje", Toast.LENGTH_SHORT).show();
                }
            });
            mEditTextSendMessageChat.setText("");
        }
    }

    /*
     * METODO QUE CREA EL CHAT ENTRE USUARIOS
     */
    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    /**
     * ON STOP
     */
    @Override
    public void onStop() {
        super.onStop();
        mAdapter.startListening();
    }

    /*
     * METODO QUE MUESTRA TOOLBAR PERSONALIZADO
     */
    private void showCustomToolbar(int resource) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActionBarView = inflater.inflate(resource, null);
        actionBar.setCustomView(mActionBarView);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
