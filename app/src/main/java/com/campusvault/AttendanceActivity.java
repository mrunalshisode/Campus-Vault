package com.campusvault;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AttendanceActivity extends AppCompatActivity {

    Spinner spinnerSubject;
    EditText etStudentRoll, etDate;
    Button btnPresent, btnAbsent,
            btnSaveAttendance, btnBack;
    TextView tvSelectedStatus, tvRecordCount,
            tvNoRecords, tvAlreadyMarked;
    RecyclerView recyclerAttendance;
    AttendanceAdapter adapter;
    List<Map<String, Object>> attendanceList
            = new ArrayList<>();

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String selectedStatus = "";
    String facultyName    = "";
    String facultyUid     = "";

    final String[] subjects = {
            "Mathematics",
            "Data Structures",
            "Operating Systems",
            "Database Management",
            "Computer Networks",
            "Software Engineering",
            "Web Technology",
            "Machine Learning"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        facultyUid = mAuth.getCurrentUser().getUid();

        spinnerSubject     = findViewById(
                R.id.spinnerSubject);
        etStudentRoll      = findViewById(
                R.id.etStudentRoll);
        etDate             = findViewById(
                R.id.etDate);
        btnPresent         = findViewById(
                R.id.btnPresent);
        btnAbsent          = findViewById(
                R.id.btnAbsent);
        btnSaveAttendance  = findViewById(
                R.id.btnSaveAttendance);
        btnBack            = findViewById(
                R.id.btnBack);
        tvSelectedStatus   = findViewById(
                R.id.tvSelectedStatus);
        tvRecordCount      = findViewById(
                R.id.tvRecordCount);
        tvNoRecords        = findViewById(
                R.id.tvNoRecords);
        tvAlreadyMarked    = findViewById(
                R.id.tvAlreadyMarked);
        recyclerAttendance = findViewById(
                R.id.recyclerAttendance);

        // Subject spinner
        ArrayAdapter<String> subjectAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item,
                        subjects);
        subjectAdapter.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subjectAdapter);

        // Today's date auto-fill
        String today = new SimpleDateFormat(
                "dd-MM-yyyy", Locale.getDefault())
                .format(new Date());
        etDate.setText(today);

        // RecyclerView setup
        adapter = new AttendanceAdapter(
                this, new ArrayList<>());
        recyclerAttendance.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerAttendance.setAdapter(adapter);

        // Load faculty name
        db.collection("users")
                .document(facultyUid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists())
                        facultyName =
                                doc.getString("name");
                });

        // When subject changes check already marked
        spinnerSubject.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> p, View v,
                            int pos, long id) {
                        checkAlreadyMarked();
                    }
                    @Override
                    public void onNothingSelected(
                            AdapterView<?> p) {}
                });

        btnPresent.setOnClickListener(v -> {
            selectedStatus = "Present";
            tvSelectedStatus.setText(
                    "✓ Present selected");
            tvSelectedStatus.setTextColor(
                    android.graphics.Color
                            .parseColor("#4CAF50"));
            btnPresent.setAlpha(1.0f);
            btnAbsent.setAlpha(0.5f);
        });

        btnAbsent.setOnClickListener(v -> {
            selectedStatus = "Absent";
            tvSelectedStatus.setText(
                    "✗ Absent selected");
            tvSelectedStatus.setTextColor(
                    android.graphics.Color
                            .parseColor("#F44336"));
            btnAbsent.setAlpha(1.0f);
            btnPresent.setAlpha(0.5f);
        });

        btnSaveAttendance.setOnClickListener(
                v -> saveAttendance());

        btnBack.setOnClickListener(v -> finish());

        loadAttendanceRecords();
    }

    // Generate unique document ID
    // This PREVENTS duplicates at database level
    private String getDocId(String rollNo,
                            String subject, String date) {
        // Replace spaces and slashes
        String cleanSubject = subject
                .replaceAll("\\s+", "_");
        String cleanDate = date
                .replaceAll("/", "-");
        return rollNo + "_"
                + cleanSubject + "_" + cleanDate;
    }

    private void checkAlreadyMarked() {
        String subject = spinnerSubject
                .getSelectedItem().toString();
        String date    = etDate.getText()
                .toString().trim();
        String rollNo  = etStudentRoll.getText()
                .toString().trim();

        if (date.isEmpty() || rollNo.isEmpty()) {
            tvAlreadyMarked.setVisibility(View.GONE);
            return;
        }

        String docId = getDocId(rollNo,
                subject, date);

        db.collection("attendance")
                .document(docId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String status =
                                doc.getString("status");
                        tvAlreadyMarked.setVisibility(
                                View.VISIBLE);
                        tvAlreadyMarked.setText(
                                "⚠ Already marked: "
                                        + status + " for Roll "
                                        + rollNo + " in "
                                        + subject + " on " + date
                                        + "\nYou can update below.");
                        tvAlreadyMarked
                                .setBackgroundColor(
                                        android.graphics.Color
                                                .parseColor("#FFF3CD"));
                        tvAlreadyMarked.setTextColor(
                                android.graphics.Color
                                        .parseColor("#856404"));
                    } else {
                        tvAlreadyMarked.setVisibility(
                                View.VISIBLE);
                        tvAlreadyMarked.setText(
                                "✓ Not yet marked for "
                                        + "Roll " + rollNo
                                        + " in " + subject
                                        + " on " + date);
                        tvAlreadyMarked
                                .setBackgroundColor(
                                        android.graphics.Color
                                                .parseColor("#D4EDDA"));
                        tvAlreadyMarked.setTextColor(
                                android.graphics.Color
                                        .parseColor("#155724"));
                    }
                });
    }

    private void saveAttendance() {
        String subject = spinnerSubject
                .getSelectedItem().toString();
        String rollNo  = etStudentRoll.getText()
                .toString().trim();
        String date    = etDate.getText()
                .toString().trim();

        if (rollNo.isEmpty()) {
            Toast.makeText(this,
                    "Enter roll number",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedStatus.isEmpty()) {
            Toast.makeText(this,
                    "Select Present or Absent",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (date.isEmpty()) {
            Toast.makeText(this,
                    "Enter date",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveAttendance.setEnabled(false);

        // Unique doc ID — prevents ANY duplicate
        String docId = getDocId(rollNo,
                subject, date);

        // Check if already exists
        db.collection("attendance")
                .document(docId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String currentStatus =
                                doc.getString("status");
                        btnSaveAttendance.setEnabled(true);

                        // Show update dialog
                        new AlertDialog.Builder(this)
                                .setTitle("⚠ Already Marked!")
                                .setMessage(
                                        "Roll No: " + rollNo
                                                + "\nSubject: " + subject
                                                + "\nDate: " + date
                                                + "\n\nCurrent: "
                                                + currentStatus
                                                + "\nUpdate to: "
                                                + selectedStatus + "?")
                                .setPositiveButton(
                                        "Yes, Update",
                                        (d, w) -> writeRecord(
                                                docId, rollNo,
                                                subject, date,
                                                selectedStatus, true))
                                .setNegativeButton(
                                        "Cancel", null)
                                .show();
                    } else {
                        // New — save directly
                        writeRecord(docId, rollNo,
                                subject, date,
                                selectedStatus, false);
                    }
                })
                .addOnFailureListener(e -> {
                    btnSaveAttendance.setEnabled(true);
                    Toast.makeText(this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // Single write method for both
    // create and update using SET with merge
    private void writeRecord(String docId,
                             String rollNo, String subject,
                             String date, String status,
                             boolean isUpdate) {

        Map<String, Object> record = new HashMap<>();
        record.put("rollNo",       rollNo);
        record.put("subject",      subject);
        record.put("date",         date);
        record.put("status",       status);
        record.put("markedBy",     facultyUid);
        record.put("markedByName", facultyName);
        record.put("timestamp",
                System.currentTimeMillis());
        record.put("docId",        docId);

        // SetOptions.merge() = create if not exists
        // update if exists — NO duplicates possible
        db.collection("attendance")
                .document(docId)
                .set(record, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    btnSaveAttendance.setEnabled(true);
                    String msg = isUpdate
                            ? "✓ Attendance updated!"
                            : "✓ Attendance saved!";
                    Toast.makeText(this, msg,
                            Toast.LENGTH_SHORT).show();
                    resetForm();
                    loadAttendanceRecords();
                })
                .addOnFailureListener(e -> {
                    btnSaveAttendance.setEnabled(true);
                    Toast.makeText(this,
                            "Failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void resetForm() {
        etStudentRoll.setText("");
        selectedStatus = "";
        tvSelectedStatus.setText(
                "No status selected");
        tvSelectedStatus.setTextColor(
                android.graphics.Color
                        .parseColor("#6B7280"));
        btnPresent.setAlpha(1.0f);
        btnAbsent.setAlpha(1.0f);
        tvAlreadyMarked.setVisibility(View.GONE);
    }

    private void loadAttendanceRecords() {
        db.collection("attendance")
                .whereEqualTo("markedBy", facultyUid)
                .get()
                .addOnSuccessListener(query -> {
                    attendanceList.clear();
                    for (var doc : query.getDocuments()) {
                        Map<String, Object> data =
                                doc.getData();
                        if (data != null)
                            attendanceList.add(data);
                    }

                    // Sort newest first in Java
                    attendanceList.sort((a, b) -> {
                        long tA = a.get("timestamp")
                                instanceof Long
                                ? (Long) a.get("timestamp")
                                : 0L;
                        long tB = b.get("timestamp")
                                instanceof Long
                                ? (Long) b.get("timestamp")
                                : 0L;
                        return Long.compare(tB, tA);
                    });

                    adapter.updateList(attendanceList);
                    tvRecordCount.setText(
                            attendanceList.size()
                                    + " records");

                    if (attendanceList.isEmpty()) {
                        tvNoRecords.setVisibility(
                                View.VISIBLE);
                        recyclerAttendance.setVisibility(
                                View.GONE);
                    } else {
                        tvNoRecords.setVisibility(
                                View.GONE);
                        recyclerAttendance.setVisibility(
                                View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Load failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }
}