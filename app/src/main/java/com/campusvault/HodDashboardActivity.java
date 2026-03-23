package com.campusvault;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class HodDashboardActivity
        extends AppCompatActivity {

    TextView tvWelcome, tvTotalStudents,
            tvDefaulters, tvTotalDocs,
            tvTotalActivities, tvNoDefaulters;
    Button btnLogout;
    View cardAllStudents, cardDefaulters,
            cardClassResults, cardAttendance,
            cardActivities, cardInternships;
    RecyclerView recyclerDefaulters;
    DefaulterAdapter defaulterAdapter;
    List<DefaulterStudent> defaulterList
            = new ArrayList<>();

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hod_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this,
                    LoginActivity.class));
            finish();
            return;
        }

        // Find views
        tvWelcome         = findViewById(R.id.tvWelcome);
        tvTotalStudents   = findViewById(
                R.id.tvTotalStudents);
        tvDefaulters      = findViewById(
                R.id.tvDefaulters);
        tvTotalDocs       = findViewById(
                R.id.tvTotalDocs);
        tvTotalActivities = findViewById(
                R.id.tvTotalActivities);
        tvNoDefaulters    = findViewById(
                R.id.tvNoDefaulters);
        btnLogout         = findViewById(R.id.btnLogout);
        recyclerDefaulters = findViewById(
                R.id.recyclerDefaulters);

        cardAllStudents  = findViewById(
                R.id.cardAllStudents);
        cardDefaulters   = findViewById(
                R.id.cardDefaulters);
        cardClassResults = findViewById(
                R.id.cardClassResults);
        cardAttendance   = findViewById(
                R.id.cardAttendance);
        cardActivities   = findViewById(
                R.id.cardActivities);
        cardInternships  = findViewById(
                R.id.cardInternships);

        // Setup RecyclerView
        defaulterAdapter = new DefaulterAdapter(
                this, new ArrayList<>());
        recyclerDefaulters.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerDefaulters.setAdapter(
                defaulterAdapter);

        // Load HOD name
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name =
                                doc.getString("name");
                        tvWelcome.setText(
                                "HOD: " + name + " 🎓");
                    }
                });

        // Load all stats
        loadStats();

        // Load defaulters
        loadDefaulters();

        // Card navigation
        cardAllStudents.setOnClickListener(v ->
                startActivity(new Intent(this,
                        HodAllStudentsActivity.class)));

        cardDefaulters.setOnClickListener(v ->
                startActivity(new Intent(this,
                        HodDefaultersActivity.class)));

        cardClassResults.setOnClickListener(v ->
                startActivity(new Intent(this,
                        HodResultsActivity.class)));

        cardAttendance.setOnClickListener(v ->
                startActivity(new Intent(this,
                        HodAttendanceActivity.class)));

        cardActivities.setOnClickListener(v ->
                Toast.makeText(this,
                        "Class activities loading...",
                        Toast.LENGTH_SHORT).show());

        cardInternships.setOnClickListener(v ->
                Toast.makeText(this,
                        "Class internships loading...",
                        Toast.LENGTH_SHORT).show());

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this,
                    LoginActivity.class));
            finish();
        });
    }

    private void loadStats() {
        // Total students
        db.collection("students").get()
                .addOnSuccessListener(q ->
                        tvTotalStudents.setText(
                                String.valueOf(q.size())));

        // Total documents
        db.collection("documents").get()
                .addOnSuccessListener(q ->
                        tvTotalDocs.setText(
                                String.valueOf(q.size())));

        // Total activities
        db.collection("activities").get()
                .addOnSuccessListener(q ->
                        tvTotalActivities.setText(
                                String.valueOf(q.size())));
    }

    private void loadDefaulters() {
        // Get all attendance records
        db.collection("attendance").get()
                .addOnSuccessListener(query -> {
                    // Group by rollNo + subject
                    Map<String, Map<String, int[]>>
                            rollSubjectMap = new HashMap<>();

                    for (var doc : query.getDocuments()) {
                        String rollNo =
                                doc.getString("rollNo");
                        String subject =
                                doc.getString("subject");
                        String status =
                                doc.getString("status");

                        if (rollNo == null
                                || subject == null) continue;

                        rollSubjectMap
                                .computeIfAbsent(rollNo,
                                        k -> new HashMap<>())
                                .computeIfAbsent(subject,
                                        k -> new int[]{0, 0});

                        int[] counts = rollSubjectMap
                                .get(rollNo).get(subject);
                        counts[1]++; // total
                        if ("Present".equals(status))
                            counts[0]++; // present
                    }

                    // Find defaulters
                    defaulterList.clear();
                    int defaulterCount = 0;

                    for (Map.Entry<String,
                            Map<String, int[]>> entry
                            : rollSubjectMap.entrySet()) {
                        String rollNo = entry.getKey();
                        Map<String, int[]> subjects =
                                entry.getValue();

                        int totalPresent = 0;
                        int totalClasses = 0;
                        List<String> lowSubjects =
                                new ArrayList<>();

                        for (Map.Entry<String, int[]>
                                subEntry
                                : subjects.entrySet()) {
                            int present = subEntry
                                    .getValue()[0];
                            int total   = subEntry
                                    .getValue()[1];
                            totalPresent += present;
                            totalClasses += total;

                            int pct = total > 0
                                    ? (present * 100 / total)
                                    : 0;
                            if (pct < 75) {
                                lowSubjects.add(
                                        subEntry.getKey()
                                                + " (" + pct + "%)");
                            }
                        }

                        int overallPct = totalClasses > 0
                                ? (totalPresent * 100
                                / totalClasses)
                                : 0;

                        if (overallPct < 75) {
                            defaulterCount++;
                            defaulterList.add(
                                    new DefaulterStudent(
                                            "Roll: " + rollNo,
                                            rollNo,
                                            overallPct,
                                            lowSubjects));
                        }
                    }

                    // Sort by percentage ascending
                    defaulterList.sort((a, b) ->
                            Integer.compare(
                                    a.overallPercent,
                                    b.overallPercent));

                    defaulterAdapter.updateList(
                            defaulterList);
                    tvDefaulters.setText(
                            String.valueOf(defaulterCount));

                    if (defaulterList.isEmpty()) {
                        tvNoDefaulters.setVisibility(
                                View.VISIBLE);
                        recyclerDefaulters.setVisibility(
                                View.GONE);
                    } else {
                        tvNoDefaulters.setVisibility(
                                View.GONE);
                        recyclerDefaulters.setVisibility(
                                View.VISIBLE);
                    }

                    // Load student names for defaulters
                    loadStudentNames();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void loadStudentNames() {
        db.collection("students").get()
                .addOnSuccessListener(query -> {
                    // Map rollNo to name
                    Map<String, String> rollNameMap
                            = new HashMap<>();
                    for (var doc : query.getDocuments()) {
                        String roll =
                                doc.getString("rollNo");
                        String name =
                                doc.getString("fullName");
                        if (roll != null && name != null)
                            rollNameMap.put(roll, name);
                    }

                    // Update defaulter names
                    for (DefaulterStudent d
                            : defaulterList) {
                        String name = rollNameMap
                                .get(d.rollNo);
                        if (name != null) d.name = name;
                    }

                    defaulterAdapter.updateList(
                            defaulterList);
                });
    }
}