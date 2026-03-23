package com.campusvault;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class ActivitiesAdapter extends
        RecyclerView.Adapter<ActivitiesAdapter.ViewHolder> {

    Context context;
    List<Map<String, Object>> list;

    public ActivitiesAdapter(Context context,
                             List<Map<String, Object>> list) {
        this.context = context;
        this.list    = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_activity,
                        parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {
        Map<String, Object> item = list.get(position);

        String title  = (String) item.get("title");
        String type   = (String) item.get("type");
        String date   = (String) item.get("date");
        String status = (String) item.get("status");
        String certUrl = (String) item.get("certificateUrl");

        holder.tvActivityTitle.setText(
                title != null ? title : "");
        holder.tvActivityType.setText(
                type != null ? type : "");
        holder.tvActivityDate.setText(
                date != null ? date : "");
        holder.tvActivityStatus.setText(
                status != null ? status : "Pending");

        // Icon based on type
        if (type != null) {
            switch (type) {
                case "Hackathon":
                    holder.tvActivityIcon.setText("💻");
                    break;
                case "Sports":
                    holder.tvActivityIcon.setText("⚽");
                    break;
                case "Cultural":
                    holder.tvActivityIcon.setText("🎭");
                    break;
                case "Competition":
                    holder.tvActivityIcon.setText("🥇");
                    break;
                case "Workshop":
                    holder.tvActivityIcon.setText("🔧");
                    break;
                default:
                    holder.tvActivityIcon.setText("🏆");
            }
        }

        // Click to open certificate
        if (certUrl != null && !certUrl.isEmpty()) {
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(certUrl));
                context.startActivity(intent);
            });
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
        TextView tvActivityTitle, tvActivityType,
                tvActivityDate, tvActivityStatus,
                tvActivityIcon;

        ViewHolder(View itemView) {
            super(itemView);
            tvActivityTitle  = itemView.findViewById(
                    R.id.tvActivityTitle);
            tvActivityType   = itemView.findViewById(
                    R.id.tvActivityType);
            tvActivityDate   = itemView.findViewById(
                    R.id.tvActivityDate);
            tvActivityStatus = itemView.findViewById(
                    R.id.tvActivityStatus);
            tvActivityIcon   = itemView.findViewById(
                    R.id.tvActivityIcon);
        }
    }
}