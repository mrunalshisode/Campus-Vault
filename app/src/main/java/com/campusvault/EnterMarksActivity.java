package com.campusvault;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class EnterMarksActivity extends AppCompatActivity {

    EditText etRollNo, etMarks;
    Spinner spinnerSubject, spinnerSemester;
    Button btnSaveMarks, btnBack;
    TextView tvStatus;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String facultyName = "";
    String facultyUid  = "";

    final String[] subjects = {
            "Mathematics", "Data Structures",
            "Operating Systems",
            "Database Management",
            "Computer Networks",
            "Software Engineering",
            "Web Technology", "Machine Learning"
    };

    final String[] semesters = {
            "Semester 1", "Semester 2",
            "Semester 3", "Semester 4",
            "Semester 5", "Semester 6",
            "Semester 7", "Semester 8"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_marks);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        facultyUid = mAuth.getCurrentUser().getUid();

        etRollNo        = findViewById(R.id.etRollNo);
        etMarks         = findViewById(R.id.etMarks);
        spinnerSubject  = findViewById(
                R.id.spinnerSubject);
        spinnerSemester = findViewById(
                R.id.spinnerSemester);
        btnSaveMarks    = findViewById(
                R.id.btnSaveMarks);
        btnBack         = findViewById(R.id.btnBack);

        // Try to find status TextView
        // (add it to XML if missing)
        tvStatus = findViewById(R.id.tvMarksStatus);

        ArrayAdapter<String> subAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item,
                        subjects);
        subAdapter.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subAdapter);

        ArrayAdapter<String> semAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item,
                        semesters);
        semAdapter.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(semAdapter);

        // Load faculty info
        db.collection("users")
                .document(facultyUid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists())
                        facultyName =
                                doc.getString("name");
                });

        // Check existing marks when roll changes
        etRollNo.setOnFocusChangeListener(
                (v, hasFocus) -> {
                    if (!hasFocus) checkExistingMarks();
                });

        btnSaveMarks.setOnClickListener(
                v -> saveMarks());
        btnBack.setOnClickListener(v -> finish());
    }

    private String getDocId(String rollNo,
                            String subject, String semester) {
        String cleanSubject = subject
                .replaceAll("\\s+", "_");
        String cleanSem = semester
                .replaceAll("\\s+", "_");
        return rollNo + "_"
                + cleanSubject + "_" + cleanSem;
    }

    private void checkExistingMarks() {
        String rollNo   = etRollNo.getText()
                .toString().trim();
        String subject  = spinnerSubject
                .getSelectedItem().toString();
        String semester = spinnerSemester
                .getSelectedItem().toString();

        if (rollNo.isEmpty()) return;

        String docId = getDocId(rollNo,
                subject, semester);

        db.collection("results")
                .document(docId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Object marksObj =
                                doc.get("marks");
                        int marks = marksObj
                                instanceof Long
                                ? ((Long) marksObj)
                                .intValue() : 0;
                        if (tvStatus != null) {
                            tvStatus.setVisibility(
                                    android.view.View
                                            .VISIBLE);
                            tvStatus.setText(
                                    "⚠ Marks already entered:"
                                            + " " + marks
                                            + "/100 for Roll "
                                            + rollNo + " in "
                                            + subject + " ("
                                            + semester + ")"
                                            + "\nSaving will update it.");
                            tvStatus.setBackgroundColor(
                                    android.graphics.Color
                                            .parseColor("#FFF3CD"));
                            tvStatus.setTextColor(
                                    android.graphics.Color
                                            .parseColor("#856404"));
                        }
                    } else {
                        if (tvStatus != null)
                            tvStatus.setVisibility(
                                    android.view.View
                                            .GONE);
                    }
                });
    }

    private void saveMarks() {
        String rollNo   = etRollNo.getText()
                .toString().trim();
        String marksStr = etMarks.getText()
                .toString().trim();
        String subject  = spinnerSubject
                .getSelectedItem().toString();
        String semester = spinnerSemester
                .getSelectedItem().toString();

        if (rollNo.isEmpty()) {
            Toast.makeText(this,
                    "Enter roll number",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (marksStr.isEmpty()) {
            Toast.makeText(this,
                    "Enter marks",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int marks = Integer.parseInt(marksStr);
        if (marks < 0 || marks > 100) {
            Toast.makeText(this,
                    "Marks must be 0-100",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveMarks.setEnabled(false);

        // Unique doc ID prevents duplicates
        String docId = getDocId(rollNo,
                subject, semester);

        Map<String, Object> result = new HashMap<>();
        result.put("rollNo",        rollNo);
        result.put("subject",       subject);
        result.put("semester",      semester);
        result.put("marks",         marks);
        result.put("enteredBy",     facultyUid);
        result.put("enteredByName", facultyName);
        result.put("timestamp",
                System.currentTimeMillis());
        result.put("docId",         docId);

        // SET with merge — no duplicates possible
        db.collection("results")
                .document(docId)
                .set(result, SetOptions.merge())
                .addOnSuccessListener(ref -> {
                    btnSaveMarks.setEnabled(true);
                    Toast.makeText(this,
                            "✓ Marks saved!",
                            Toast.LENGTH_SHORT).show();
                    etRollNo.setText("");
                    etMarks.setText("");
                    if (tvStatus != null)
                        tvStatus.setVisibility(
                                android.view.View.GONE);
                })
                .addOnFailureListener(e -> {
                    btnSaveMarks.setEnabled(true);
                    Toast.makeText(this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}