package com.campusvault;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentDashboardActivity
        extends AppCompatActivity {

    TextView tvWelcome, tvSubInfo;
    TextView tvAttStat, tvDocStat, tvActStat;
    Button btnLogout;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        // Check login
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this,
                    LoginActivity.class));
            finish();
            return;
        }

        // Find views safely
        tvWelcome  = findViewById(R.id.tvWelcome);
        tvSubInfo  = findViewById(R.id.tvSubInfo);
        tvAttStat  = findViewById(R.id.tvAttStat);
        tvDocStat  = findViewById(R.id.tvDocStat);
        tvActStat  = findViewById(R.id.tvActStat);
        btnLogout  = findViewById(R.id.btnLogout);

        View cardProfile    =
                findViewById(R.id.cardProfile);
        View cardAttendance =
                findViewById(R.id.cardAttendance);
        View cardResults    =
                findViewById(R.id.cardResults);
        View cardActivities =
                findViewById(R.id.cardActivities);
        View cardInternships =
                findViewById(R.id.cardInternships);
        View cardDocuments  =
                findViewById(R.id.cardDocuments);

        // Load user name
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && tvWelcome != null) {
                        String name = doc.getString("name");
                        tvWelcome.setText(
                                "Hi, " + name + "! 👋");
                    }
                });

        // Load student profile info
        db.collection("students").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && tvSubInfo != null) {
                        String year =
                                doc.getString("year");
                        String dept =
                                doc.getString("department");
                        tvSubInfo.setText(
                                (year != null ? year : "SE")
                                        + " · "
                                        + (dept != null ? dept
                                        : "Computer Science"));
                    }
                });

        // Load stats
        loadStats(uid);

        // Card click listeners
        if (cardProfile != null)
            cardProfile.setOnClickListener(v ->
                    startActivity(new Intent(this,
                            StudentProfileActivity.class)));

        if (cardAttendance != null)
            cardAttendance.setOnClickListener(v ->
                    startActivity(new Intent(this,
                            StudentAttendanceActivity.class)));

        if (cardResults != null)
            cardResults.setOnClickListener(v ->
                    startActivity(new Intent(this,
                            ResultsActivity.class)));

        if (cardActivities != null)
            cardActivities.setOnClickListener(v ->
                    startActivity(new Intent(this,
                            ActivitiesActivity.class)));

        if (cardInternships != null)
            cardInternships.setOnClickListener(v ->
                    startActivity(new Intent(this,
                            InternshipsActivity.class)));

        if (cardDocuments != null)
            cardDocuments.setOnClickListener(v ->
                    startActivity(new Intent(this,
                            DocumentsActivity.class)));

        // Logout
        if (btnLogout != null)
            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                startActivity(new Intent(this,
                        LoginActivity.class));
                finish();
            });
    }

    private void loadStats(String uid) {
        // Documents count
        if (tvDocStat != null)
            db.collection("documents").get()
                    .addOnSuccessListener(q ->
                            tvDocStat.setText(
                                    String.valueOf(q.size())));

        // Activities count
        if (tvActStat != null)
            db.collection("activities")
                    .whereEqualTo("studentId", uid)
                    .get()
                    .addOnSuccessListener(q ->
                            tvActStat.setText(
                                    String.valueOf(q.size())));

        // Attendance percentage
        db.collection("students").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String rollNo =
                                doc.getString("rollNo");
                        if (rollNo != null
                                && tvAttStat != null) {
                            db.collection("attendance")
                                    .whereEqualTo("rollNo",
                                            rollNo)
                                    .get()
                                    .addOnSuccessListener(q -> {
                                        int present = 0,
                                                total   = 0;
                                        for (var d :
                                                q.getDocuments()) {
                                            total++;
                                            if ("Present".equals(
                                                    d.getString(
                                                            "status")))
                                                present++;
                                        }
                                        int pct = total > 0
                                                ? (present * 100
                                                / total) : 0;
                                        tvAttStat.setText(
                                                pct + "%");
                                    });
                        } else if (tvAttStat != null) {
                            tvAttStat.setText("N/A");
                        }
                    }
                });
    }
}