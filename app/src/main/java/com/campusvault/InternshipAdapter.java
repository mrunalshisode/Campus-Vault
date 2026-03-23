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

public class InternshipAdapter extends
        RecyclerView.Adapter<InternshipAdapter.ViewHolder> {

    Context context;
    List<Map<String, Object>> list;

    public InternshipAdapter(Context context,
                             List<Map<String, Object>> list) {
        this.context = context;
        this.list    = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_internship,
                        parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {
        Map<String, Object> item = list.get(position);

        String company  = (String) item.get("company");
        String role     = (String) item.get("role");
        String duration = (String) item.get("duration");
        String location = (String) item.get("location");
        String status   = (String) item.get("status");
        String stipend  = (String) item.get("stipend");
        String offerUrl = (String) item.get("offerLetterUrl");

        holder.tvCompany.setText(
                company != null ? company : "");
        holder.tvRole.setText(
                role != null ? role : "");
        holder.tvDuration.setText(
                duration != null ? duration : "");
        holder.tvLocation.setText(
                "📍 " + (location != null
                        ? location : ""));
        holder.tvInternStatus.setText(
                status != null ? status : "Pending");
        holder.tvStipend.setText(
                stipend != null && !stipend.isEmpty()
                        ? "₹" + stipend + "/mo" : "Unpaid");

        // Click to open offer letter
        if (offerUrl != null && !offerUrl.isEmpty()) {
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(offerUrl));
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
        TextView tvCompany, tvRole, tvDuration,
                tvLocation, tvInternStatus, tvStipend;

        ViewHolder(View itemView) {
            super(itemView);
            tvCompany      = itemView.findViewById(
                    R.id.tvCompany);
            tvRole         = itemView.findViewById(
                    R.id.tvRole);
            tvDuration     = itemView.findViewById(
                    R.id.tvDuration);
            tvLocation     = itemView.findViewById(
                    R.id.tvLocation);
            tvInternStatus = itemView.findViewById(
                    R.id.tvInternStatus);
            tvStipend      = itemView.findViewById(
                    R.id.tvStipend);
        }
    }
}