package com.optic.fireapp.adapters;
import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.optic.fireapp.R;
import com.optic.fireapp.activities.FullScreenDialog;
import com.optic.fireapp.models.Report;

import java.util.ArrayList;

public class HistoryReportAdapter extends RecyclerView.Adapter<HistoryReportAdapter.ViewHolder> {

    private int resource;
    ArrayList<Report> reports = new ArrayList<>();
    private AppCompatActivity activity;


    public HistoryReportAdapter(ArrayList<Report> reports, int resource, AppCompatActivity activity) {
        this.reports = reports;
        this.resource = resource;
        this.activity = activity;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Report report = reports.get(position);
        holder.mTextViewLat.setText(String.valueOf(report.getLat()));
        holder.mTextViewLng.setText(String.valueOf(report.getLng()));
        holder.mTextViewDate.setText(String.valueOf(report.getDate()));
        holder.mTextViewHour.setText(String.valueOf(report.getHour()));
        holder.mTextViewDescription.setText(String.valueOf(report.getDescription()));

        /*
        if (report.getImage() != null) {
            if (!report.getImage().equals("")) {
                holder.mShowImage.setVisibility(View.VISIBLE);
            }
            else {
                holder.mShowImage.setVisibility(View.GONE);
            }
        }
        else {
            holder.mShowImage.setVisibility(View.GONE);
        }
        */

        holder.mShowImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialog = FullScreenDialog.newInstance(report.getImage());
                dialog.show(activity.getSupportFragmentManager(), "tag");
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
        return new HistoryReportAdapter.ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    /*
     * VIEWHOLDER RECYCLERVIEW
     * CONTIENE TODOS LOS DATOS QUE SE VAN A MOSTRAR EN LOS CARDS DEL RECYCLERVIEW
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public TextView mTextViewLat;
        public TextView mTextViewLng;
        public TextView mTextViewHour;
        public TextView mTextViewDate;
        public TextView mTextViewDescription;
        public LinearLayout mShowImage;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mTextViewLat = (TextView) mView.findViewById(R.id.textViewLatReport);
            mTextViewLng = (TextView) mView.findViewById(R.id.textViewLngReport);
            mTextViewHour = (TextView) mView.findViewById(R.id.textViewHourReport);
            mTextViewDate = (TextView) mView.findViewById(R.id.textViewDateReport);
            mTextViewDescription = (TextView) mView.findViewById(R.id.textViewDescriptionReport);
            mShowImage= (LinearLayout) mView.findViewById(R.id.imageReport);
        }
    }
}
