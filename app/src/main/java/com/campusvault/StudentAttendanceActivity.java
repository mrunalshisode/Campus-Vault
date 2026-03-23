package com.campusvault;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentAttendanceActivity
        extends AppCompatActivity {

    TextView tvOverallPercent, tvOverallStatus;
    Button btnBack;
    RecyclerView recyclerSubjectAttendance;
    SubjectAttendanceAdapter adapter;
    List<SubjectAttendance> subjectList = new ArrayList<>();

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        tvOverallPercent = findViewById(
                R.id.tvOverallPercent);
        tvOverallStatus  = findViewById(
                R.id.tvOverallStatus);
        btnBack          = findViewById(R.id.btnBack);
        recyclerSubjectAttendance = findViewById(
                R.id.recyclerSubjectAttendance);

        adapter = new SubjectAttendanceAdapter(
                this, new ArrayList<>());
        recyclerSubjectAttendance.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerSubjectAttendance.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadMyAttendance();
    }

    private void loadMyAttendance() {
        // First get student's roll number
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("students").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String rollNo = doc.getString("rollNo");
                        if (rollNo != null) {
                            fetchAttendanceByRoll(rollNo);
                        } else {
                            Toast.makeText(this,
                                    "Please complete your profile first",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this,
                                "Profile not found. Please fill profile first",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void fetchAttendanceByRoll(String rollNo) {
        db.collection("attendance")
                .whereEqualTo("rollNo", rollNo)
                .get()
                .addOnSuccessListener(query -> {
                    // Group by subject
                    Map<String, SubjectAttendance> subjectMap
                            = new HashMap<>();

                    for (var doc : query.getDocuments()) {
                        String subject = doc.getString("subject");
                        String status  = doc.getString("status");

                        if (subject == null) continue;

                        if (!subjectMap.containsKey(subject)) {
                            subjectMap.put(subject,
                                    new SubjectAttendance(subject));
                        }

                        SubjectAttendance sa =
                                subjectMap.get(subject);
                        if ("Present".equals(status)) {
                            sa.present++;
                        } else {
                            sa.absent++;
                        }
                    }

                    subjectList.clear();
                    subjectList.addAll(subjectMap.values());
                    adapter.updateList(subjectList);

                    // Calculate overall
                    calculateOverall();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error loading attendance: "
                                        + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void calculateOverall() {
        if (subjectList.isEmpty()) {
            tvOverallPercent.setText("0%");
            tvOverallStatus.setText(
                    "No attendance records found");
            return;
        }

        int totalPresent = 0;
        int totalClasses = 0;

        for (SubjectAttendance sa : subjectList) {
            totalPresent += sa.present;
            totalClasses += sa.getTotal();
        }

        int overall = totalClasses > 0
                ? (int) ((totalPresent * 100.0)
                / totalClasses)
                : 0;

        tvOverallPercent.setText(overall + "%");

        if (overall >= 75) {
            tvOverallPercent.setTextColor(
                    android.graphics.Color
                            .parseColor("#4CAF50"));
            tvOverallStatus.setText(
                    "Good standing ✓");
            tvOverallStatus.setTextColor(
                    android.graphics.Color
                            .parseColor("#4CAF50"));
        } else {
            tvOverallPercent.setTextColor(
                    android.graphics.Color
                            .parseColor("#F44336"));
            tvOverallStatus.setText(
                    "⚠ Below 75% — Attendance shortage!");
            tvOverallStatus.setTextColor(
                    android.graphics.Color
                            .parseColor("#F44336"));
        }
    }
}