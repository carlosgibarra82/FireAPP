package com.optic.fireapp.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.optic.fireapp.R;
import com.optic.fireapp.models.Message;
import com.optic.fireapp.utils.GetTimeAgo;


import java.text.DecimalFormat;
import java.util.Date;

public class MessageAdapter extends FirestoreRecyclerAdapter<Message, MessageAdapter.ViewHolder> {

    private Activity activity;
    private ProgressDialog mProgress;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    // FORMATEAR DECIMAL 5000.0 A 5000
    DecimalFormat format=new DecimalFormat("0.#");

    public MessageAdapter(@NonNull FirestoreRecyclerOptions<Message> options, Activity activity){
            super(options);
            this.activity=activity;
            mAuth = FirebaseAuth.getInstance();
            mCurrentUserId = mAuth.getCurrentUser().getUid();
            mFirestore = FirebaseFirestore.getInstance();
            mProgress=new ProgressDialog(activity);
    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final Message message) {
        DocumentSnapshot document = getSnapshots().getSnapshot(holder.getAdapterPosition());

        // OBTENIENDO EL ID DEL MENSAJE
        final String id_message = document.getId();

        //final String message_id = message.messagesId;
        if(message.isSendByUser()) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.setMargins(150, 0,0,0);
            holder.mLinearLayoutMessages.setLayoutParams(params);
            holder.mLinearLayoutMessages.setPadding(30,20,25,20);
            holder.mLinearLayoutMessages.setBackground(activity.getResources().getDrawable(R.drawable.custom_button_chat_yellow));
            holder.mImageViewSeenMessage.setVisibility(View.VISIBLE);
        }
        else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.setMargins(0, 0,150,0);
            holder.mLinearLayoutMessages.setLayoutParams(params);
            holder.mLinearLayoutMessages.setPadding(30,20,-40,20);
            holder.mLinearLayoutMessages.setBackground(activity.getResources().getDrawable(R.drawable.custom_button_chat_gray));
            holder.mImageViewSeenMessage.setVisibility(View.INVISIBLE);
            // ACTUALIZANDO EL MENSAJE A VISTO POR EL USUARIO
            //messageUpdateReference.document(message_id).update("seen", true);
        }

        // COLOR DEL VISTO EN LOS MENSAJES
        if(message.isSeenByadmin()) {
            holder.mImageViewSeenMessage.setImageResource(R.drawable.icon_seen_blue);
        }
        else {
            holder.mImageViewSeenMessage.setImageResource(R.drawable.icon_seen_gray);
        }

        String today = GetTimeAgo.getDateDDMMYYY((new Date().getTime()));
        String messageTime = GetTimeAgo.getDateDDMMYYY((message.getTimestamp() * 1000));

        if (today.equals(messageTime)) {
            holder.mTextViewTimestampMessage.setText(GetTimeAgo.timeFormatAMPM(message.getTimestamp() * 1000));
        }
        else {
            holder.mTextViewTimestampMessage.setText(GetTimeAgo.getTimeAgo((message.getTimestamp() * 1000), activity));
        }

        holder.mTextViewMessage.setText(message.getMessage());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            View view= LayoutInflater.from(parent.getContext())
            .inflate(R.layout.cardview_messages, parent,false);
            return new ViewHolder(view);
    }

    /*
     * VIEWHOLDER RECYCLERVIEW
     * CONTIENE TODOS LOS DATOS QUE SE VAN A MOSTRAR EN LOS CARDS DEL RECYCLERVIEW
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public TextView mTextViewMessage;
        public TextView mTextViewTimestampMessage;
        public ImageView mImageViewSeenMessage;
        public LinearLayout mLinearLayoutMessages;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mTextViewMessage = (TextView) mView.findViewById(R.id.textViewMessage);
            mTextViewTimestampMessage = (TextView) mView.findViewById(R.id.textViewTimestampMessage);
            mImageViewSeenMessage = (ImageView) mView.findViewById(R.id.imageViewSeenMessage);
            mLinearLayoutMessages = (LinearLayout) mView.findViewById(R.id.linearLayoutMessages);
        }
    }
}
