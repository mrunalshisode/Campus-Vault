package com.campusvault;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class AttendanceAdapter extends
        RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    Context context;
    List<Map<String, Object>> list;

    public AttendanceAdapter(Context context,
                             List<Map<String, Object>> list) {
        this.context = context;
        this.list    = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_attendance,
                        parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {
        Map<String, Object> item = list.get(position);

        String roll    = (String) item.get("rollNo");
        String subject = (String) item.get("subject");
        String date    = (String) item.get("date");
        String status  = (String) item.get("status");

        holder.tvAttRoll.setText(
                "Roll: " + (roll != null ? roll : ""));
        holder.tvAttSubject.setText(
                subject != null ? subject : "");
        holder.tvAttDate.setText(
                "📅 " + (date != null ? date : ""));
        holder.tvAttStatus.setText(
                status != null ? status : "");

        // Color + background based on status
        if ("Present".equals(status)) {
            holder.tvAttStatus.setTextColor(
                    Color.parseColor("#4CAF50"));
            holder.tvAttStatus.setBackgroundColor(
                    Color.parseColor("#E8F5E9"));
        } else {
            holder.tvAttStatus.setTextColor(
                    Color.parseColor("#F44336"));
            holder.tvAttStatus.setBackgroundColor(
                    Color.parseColor("#FFEBEE"));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(
            List<Map<String, Object>> newList) {
        list = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {
        TextView tvAttRoll, tvAttSubject,
                tvAttDate, tvAttStatus;

        ViewHolder(View itemView) {
            super(itemView);
            tvAttRoll    = itemView.findViewById(
                    R.id.tvAttRoll);
            tvAttSubject = itemView.findViewById(
                    R.id.tvAttSubject);
            tvAttDate    = itemView.findViewById(
                    R.id.tvAttDate);
            tvAttStatus  = itemView.findViewById(
                    R.id.tvAttStatus);
        }
    }
}