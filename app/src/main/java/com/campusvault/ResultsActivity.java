package com.campusvault;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResultsActivity extends AppCompatActivity {

    TextView tvCGPA, tvTotalSubjects,
            tvHighestMarks, tvLowestMarks, tvEmpty;
    Spinner spinnerSemester;
    Button btnBack;
    RecyclerView recyclerResults;
    ResultAdapter adapter;

    List<Map<String, Object>> allResults = new ArrayList<>();
    List<Map<String, Object>> filteredResults
            = new ArrayList<>();

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String studentRollNo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        tvCGPA          = findViewById(R.id.tvCGPA);
        tvTotalSubjects = findViewById(
                R.id.tvTotalSubjects);
        tvHighestMarks  = findViewById(
                R.id.tvHighestMarks);
        tvLowestMarks   = findViewById(
                R.id.tvLowestMarks);
        tvEmpty         = findViewById(R.id.tvEmpty);
        spinnerSemester = findViewById(
                R.id.spinnerSemester);
        btnBack         = findViewById(R.id.btnBack);
        recyclerResults = findViewById(
                R.id.recyclerResults);

        // Setup RecyclerView
        adapter = new ResultAdapter(this, new ArrayList<>());
        recyclerResults.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerResults.setAdapter(adapter);

        // Setup semester spinner
        String[] semesters = {
                "All Semesters",
                "Semester 1", "Semester 2",
                "Semester 3", "Semester 4",
                "Semester 5", "Semester 6",
                "Semester 7", "Semester 8"
        };
        ArrayAdapter<String> semAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item,
                        semesters);
        semAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(semAdapter);

        spinnerSemester.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent, View view,
                            int position, long id) {
                        String selected = semesters[position];
                        if (position == 0) {
                            adapter.updateList(allResults);
                        } else {
                            filterBySemester(selected);
                        }
                    }
                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {}
                });

        btnBack.setOnClickListener(v -> finish());

        // Load results
        loadResults();
    }

    private void loadResults() {
        String uid = mAuth.getCurrentUser().getUid();

        // Get roll number first
        db.collection("students").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        studentRollNo = doc.getString("rollNo");
                        if (studentRollNo != null) {
                            fetchResults();
                        } else {
                            Toast.makeText(this,
                                    "Please complete your profile first",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void fetchResults() {
        db.collection("results")
                .whereEqualTo("rollNo", studentRollNo)
                .get()
                .addOnSuccessListener(query -> {
                    allResults.clear();
                    for (var doc : query.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        if (data != null) allResults.add(data);
                    }
                    adapter.updateList(allResults);
                    calculateStats();

                    tvEmpty.setVisibility(
                            allResults.isEmpty()
                                    ? View.VISIBLE : View.GONE);
                    recyclerResults.setVisibility(
                            allResults.isEmpty()
                                    ? View.GONE : View.VISIBLE);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void calculateStats() {
        if (allResults.isEmpty()) {
            tvCGPA.setText("--");
            return;
        }

        int total = 0, highest = 0,
                lowest = 100, count = 0;

        for (Map<String, Object> r : allResults) {
            Object marksObj = r.get("marks");
            int marks = 0;
            if (marksObj instanceof Long)
                marks = ((Long) marksObj).intValue();
            else if (marksObj instanceof Double)
                marks = ((Double) marksObj).intValue();

            total   += marks;
            count++;
            if (marks > highest) highest = marks;
            if (marks < lowest)  lowest  = marks;
        }

        // Convert percentage to GPA (out of 10)
        double avg  = (double) total / count;
        double cgpa = avg / 10.0;

        tvCGPA.setText(
                String.format("%.1f", cgpa));
        tvTotalSubjects.setText(
                String.valueOf(count));
        tvHighestMarks.setText(
                String.valueOf(highest));
        tvLowestMarks.setText(
                String.valueOf(lowest));
    }

    private void filterBySemester(String semester) {
        filteredResults.clear();
        for (Map<String, Object> r : allResults) {
            if (semester.equals(r.get("semester"))) {
                filteredResults.add(r);
            }
        }
        adapter.updateList(filteredResults);
    }
}