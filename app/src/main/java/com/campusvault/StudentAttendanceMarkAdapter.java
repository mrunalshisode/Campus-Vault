package com.campusvault;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentAttendanceMarkAdapter extends
        RecyclerView.Adapter<StudentAttendanceMarkAdapter
                .ViewHolder> {

    Context context;
    List<Map<String, Object>> studentList;

    // Track attendance status for each student
    // true = Present, false = Absent
    Map<String, Boolean> attendanceMap = new HashMap<>();

    // Listener to update present count in activity
    OnAttendanceChangeListener listener;

    public interface OnAttendanceChangeListener {
        void onChanged(int presentCount);
    }

    public StudentAttendanceMarkAdapter(
            Context context,
            List<Map<String, Object>> studentList,
            OnAttendanceChangeListener listener) {
        this.context     = context;
        this.studentList = studentList;
        this.listener    = listener;

        // Default all to Absent
        for (Map<String, Object> s : studentList) {
            String uid = (String) s.get("uid");
            if (uid != null)
                attendanceMap.put(uid, false);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_attendance_student,
                        parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {
        Map<String, Object> student =
                studentList.get(position);

        String uid      = (String) student.get("uid");
        String name     = (String) student.get("fullName");
        String rollNo   = (String) student.get("rollNo");
        String dept     = (String) student.get("department");
        String year     = (String) student.get("year");

        holder.tvStudentName.setText(
                name != null ? name : "Unknown");
        holder.tvRollNo.setText(
                rollNo != null ? rollNo : "--");
        holder.tvStudentDept.setText(
                (year != null ? year : "")
                        + " · "
                        + (dept != null
                        ? dept.substring(0,
                        Math.min(dept.length(), 15))
                        : ""));

        // Set button state
        boolean isPresent = attendanceMap
                .getOrDefault(uid, false);
        updateButton(holder.btnToggleAttendance,
                isPresent);

        // Toggle on click
        holder.btnToggleAttendance
                .setOnClickListener(v -> {
                    boolean current = attendanceMap
                            .getOrDefault(uid, false);
                    boolean newStatus = !current;
                    attendanceMap.put(uid, newStatus);
                    updateButton(
                            holder.btnToggleAttendance,
                            newStatus);

                    // Count present
                    int presentCount = 0;
                    for (boolean val :
                            attendanceMap.values()) {
                        if (val) presentCount++;
                    }
                    if (listener != null)
                        listener.onChanged(presentCount);
                });
    }

    private void updateButton(Button btn,
                              boolean isPresent) {
        if (isPresent) {
            btn.setText("P");
            btn.setBackgroundTintList(
                    android.content.res.ColorStateList
                            .valueOf(android.graphics.Color
                                    .parseColor("#4CAF50")));
        } else {
            btn.setText("A");
            btn.setBackgroundTintList(
                    android.content.res.ColorStateList
                            .valueOf(android.graphics.Color
                                    .parseColor("#F44336")));
        }
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    // Get final attendance map to save
    public Map<String, Boolean> getAttendanceMap() {
        return attendanceMap;
    }

    // Get student data by uid
    public Map<String, Object> getStudentByUid(
            String uid) {
        for (Map<String, Object> s : studentList) {
            if (uid.equals(s.get("uid"))) return s;
        }
        return null;
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvRollNo,
                tvStudentDept;
        Button btnToggleAttendance;

        ViewHolder(View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(
                    R.id.tvStudentName);
            tvRollNo      = itemView.findViewById(
                    R.id.tvRollNo);
            tvStudentDept = itemView.findViewById(
                    R.id.tvStudentDept);
            btnToggleAttendance = itemView.findViewById(
                    R.id.btnToggleAttendance);
        }
    }
}