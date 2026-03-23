package com.campusvault;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class ResultAdapter extends
        RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    Context context;
    List<Map<String, Object>> list;

    public ResultAdapter(Context context,
                         List<Map<String, Object>> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_result,
                        parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {
        Map<String, Object> item = list.get(position);

        String subject  = (String) item.get("subject");
        String semester = (String) item.get("semester");
        Object marksObj = item.get("marks");

        int marks = 0;
        if (marksObj instanceof Long) {
            marks = ((Long) marksObj).intValue();
        } else if (marksObj instanceof Double) {
            marks = ((Double) marksObj).intValue();
        }

        holder.tvSubjectName.setText(
                subject != null ? subject : "");
        holder.tvSemester.setText(
                semester != null ? semester : "");
        holder.tvMarks.setText(String.valueOf(marks));
        holder.progressMarks.setProgress(marks);

        // Grade and color
        String grade;
        String color;
        if (marks >= 90) {
            grade = "O"; color = "#4CAF50";
        } else if (marks >= 80) {
            grade = "A+"; color = "#8BC34A";
        } else if (marks >= 70) {
            grade = "A"; color = "#7C3AED";
        } else if (marks >= 60) {
            grade = "B+"; color = "#FF9800";
        } else if (marks >= 50) {
            grade = "B"; color = "#FF5722";
        } else {
            grade = "F"; color = "#F44336";
        }

        holder.tvGrade.setText(grade);
        holder.gradeCircle.setBackgroundColor(
                Color.parseColor(color));
        holder.tvMarks.setTextColor(
                Color.parseColor(color));
        holder.progressMarks.setProgressTintList(
                android.content.res.ColorStateList
                        .valueOf(Color.parseColor(color)));
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectName, tvSemester,
                tvMarks, tvGrade;
        LinearLayout gradeCircle;
        ProgressBar progressMarks;

        ViewHolder(View itemView) {
            super(itemView);
            tvSubjectName = itemView.findViewById(
                    R.id.tvSubjectName);
            tvSemester    = itemView.findViewById(
                    R.id.tvSemester);
            tvMarks       = itemView.findViewById(
                    R.id.tvMarks);
            tvGrade       = itemView.findViewById(
                    R.id.tvGrade);
            gradeCircle   = itemView.findViewById(
                    R.id.gradeCircle);
            progressMarks = itemView.findViewById(
                    R.id.progressMarks);
        }
    }
}