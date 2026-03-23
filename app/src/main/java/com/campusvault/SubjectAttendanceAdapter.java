package com.campusvault;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SubjectAttendanceAdapter extends
        RecyclerView.Adapter<SubjectAttendanceAdapter.ViewHolder> {

    Context context;
    List<SubjectAttendance> list;

    public SubjectAttendanceAdapter(Context context,
                                    List<SubjectAttendance> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_subject_attendance,
                        parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {
        SubjectAttendance item = list.get(position);

        holder.tvSubjectName.setText(item.subject);
        holder.tvPresentCount.setText(
                "Present: " + item.present);
        holder.tvAbsentCount.setText(
                "Absent: " + item.absent);

        int percent = item.getPercentage();
        holder.tvSubjectPercent.setText(percent + "%");
        holder.progressAttendance.setProgress(percent);

        // Color based on percentage
        if (percent >= 75) {
            holder.tvSubjectPercent.setTextColor(
                    android.graphics.Color
                            .parseColor("#4CAF50"));
            holder.progressAttendance.setProgressTintList(
                    android.content.res.ColorStateList
                            .valueOf(android.graphics.Color
                                    .parseColor("#4CAF50")));
            holder.tvWarning.setText("Good ✓");
            holder.tvWarning.setTextColor(
                    android.graphics.Color
                            .parseColor("#4CAF50"));
        } else {
            holder.tvSubjectPercent.setTextColor(
                    android.graphics.Color
                            .parseColor("#F44336"));
            holder.progressAttendance.setProgressTintList(
                    android.content.res.ColorStateList
                            .valueOf(android.graphics.Color
                                    .parseColor("#F44336")));
            holder.tvWarning.setText("Low ⚠");
            holder.tvWarning.setTextColor(
                    android.graphics.Color
                            .parseColor("#FF6200"));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(List<SubjectAttendance> newList) {
        list = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectName, tvSubjectPercent,
                tvPresentCount, tvAbsentCount, tvWarning;
        ProgressBar progressAttendance;

        ViewHolder(View itemView) {
            super(itemView);
            tvSubjectName    = itemView.findViewById(
                    R.id.tvSubjectName);
            tvSubjectPercent = itemView.findViewById(
                    R.id.tvSubjectPercent);
            tvPresentCount   = itemView.findViewById(
                    R.id.tvPresentCount);
            tvAbsentCount    = itemView.findViewById(
                    R.id.tvAbsentCount);
            tvWarning        = itemView.findViewById(
                    R.id.tvWarning);
            progressAttendance = itemView.findViewById(
                    R.id.progressAttendance);
        }
    }
}