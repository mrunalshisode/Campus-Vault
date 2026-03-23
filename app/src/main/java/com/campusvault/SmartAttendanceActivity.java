package com.campusvault;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SmartAttendanceActivity
        extends AppCompatActivity {

    Spinner spinnerSubject, spinnerYear, spinnerDept;
    EditText etDate;
    Button btnLoadStudents, btnSaveAll, btnBack;
    TextView tvStudentCount, tvPresentCount;
    LinearLayout layoutSaveBar;
    RecyclerView recyclerStudents;

    StudentAttendanceMarkAdapter adapter;
    List<Map<String, Object>> studentList
            = new ArrayList<>();

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String facultyName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_attendance);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        spinnerSubject   = findViewById(
                R.id.spinnerSubject);
        spinnerYear      = findViewById(
                R.id.spinnerYear);
        spinnerDept      = findViewById(
                R.id.spinnerDept);
        etDate           = findViewById(R.id.etDate);
        btnLoadStudents  = findViewById(
                R.id.btnLoadStudents);
        btnSaveAll       = findViewById(R.id.btnSaveAll);
        btnBack          = findViewById(R.id.btnBack);
        tvStudentCount   = findViewById(
                R.id.tvStudentCount);
        tvPresentCount   = findViewById(
                R.id.tvPresentCount);
        layoutSaveBar    = findViewById(
                R.id.layoutSaveBar);
        recyclerStudents = findViewById(
                R.id.recyclerStudents);

        // Subjects
        String[] subjects = {
                "Mathematics", "Data Structures",
                "Operating Systems",
                "Database Management",
                "Computer Networks",
                "Software Engineering",
                "Web Technology",
                "Machine Learning"
        };
        ArrayAdapter<String> subAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item,
                        subjects);
        subAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subAdapter);

        // Years
        String[] years = {"FE", "SE", "TE", "BE"};
        ArrayAdapter<String> yearAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item,
                        years);
        yearAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // Departments
        String[] depts = {
                "Computer Science and Engineering",
                "Information Technology",
                "Electronics and Telecommunication",
                "Mechanical Engineering",
                "Civil Engineering",
                "Electrical Engineering"
        };
        ArrayAdapter<String> deptAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item,
                        depts);
        deptAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerDept.setAdapter(deptAdapter);

        // Set today's date
        String today = new SimpleDateFormat(
                "dd/MM/yyyy", Locale.getDefault())
                .format(new Date());
        etDate.setText(today);

        // Load faculty name
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists())
                        facultyName =
                                doc.getString("name");
                });

        btnLoadStudents.setOnClickListener(v ->
                loadStudents());

        btnSaveAll.setOnClickListener(v ->
                saveAllAttendance());

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadStudents() {
        String year = spinnerYear
                .getSelectedItem().toString();
        String dept = spinnerDept
                .getSelectedItem().toString();
        String date = etDate.getText()
                .toString().trim();

        if (date.isEmpty()) {
            Toast.makeText(this,
                    "Please enter date",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnLoadStudents.setEnabled(false);
        btnLoadStudents.setText("Loading...");

        // Query students by year AND department
        db.collection("students")
                .whereEqualTo("year", year)
                .whereEqualTo("department", dept)
                .get()
                .addOnSuccessListener(query -> {
                    studentList.clear();

                    for (var doc : query.getDocuments()) {
                        Map<String, Object> data =
                                doc.getData();
                        if (data != null) {
                            // Add doc ID as uid if missing
                            if (!data.containsKey("uid"))
                                data.put("uid", doc.getId());
                            studentList.add(data);
                        }
                    }

                    btnLoadStudents.setEnabled(true);
                    btnLoadStudents.setText(
                            "Load Students →");

                    if (studentList.isEmpty()) {
                        Toast.makeText(this,
                                "No students found for "
                                        + year + " - " + dept,
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Setup adapter
                    adapter =
                            new StudentAttendanceMarkAdapter(
                                    this, studentList,
                                    presentCount -> {
                                        tvPresentCount.setText(
                                                "Present: "
                                                        + presentCount);
                                    });

                    recyclerStudents.setLayoutManager(
                            new LinearLayoutManager(this));
                    recyclerStudents.setAdapter(adapter);

                    // Show save bar
                    layoutSaveBar.setVisibility(
                            View.VISIBLE);
                    tvStudentCount.setText(
                            studentList.size()
                                    + " students");
                    tvPresentCount.setText("Present: 0");

                    Toast.makeText(this,
                            studentList.size()
                                    + " students loaded! Tap P/A"
                                    + " to mark attendance.",
                            Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    btnLoadStudents.setEnabled(true);
                    btnLoadStudents.setText(
                            "Load Students →");
                    Toast.makeText(this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void saveAllAttendance() {
        if (adapter == null
                || studentList.isEmpty()) {
            Toast.makeText(this,
                    "Load students first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String subject = spinnerSubject
                .getSelectedItem().toString();
        String year    = spinnerYear
                .getSelectedItem().toString();
        String dept    = spinnerDept
                .getSelectedItem().toString();
        String date    = etDate.getText()
                .toString().trim();
        String uid     = mAuth.getCurrentUser()
                .getUid();

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Saving attendance...");
        pd.show();
        btnSaveAll.setEnabled(false);

        Map<String, Boolean> attendanceMap =
                adapter.getAttendanceMap();

        // Use batch write for efficiency
        com.google.firebase.firestore.WriteBatch batch
                = db.batch();

        for (Map.Entry<String, Boolean> entry :
                attendanceMap.entrySet()) {

            String studentUid = entry.getKey();
            boolean isPresent = entry.getValue();

            // Get student data
            Map<String, Object> student =
                    adapter.getStudentByUid(studentUid);
            String rollNo = student != null
                    ? (String) student.get("rollNo")
                    : "";
            String name   = student != null
                    ? (String) student.get("fullName")
                    : "";

            Map<String, Object> record = new HashMap<>();
            record.put("studentUid",   studentUid);
            record.put("studentName",  name);
            record.put("rollNo",       rollNo);
            record.put("subject",      subject);
            record.put("year",         year);
            record.put("department",   dept);
            record.put("date",         date);
            record.put("status",
                    isPresent ? "Present" : "Absent");
            record.put("markedBy",     uid);
            record.put("markedByName", facultyName);
            record.put("timestamp",
                    System.currentTimeMillis());

            // Add to batch
            batch.set(db.collection("attendance")
                    .document(), record);
        }

        // Commit batch
        batch.commit()
                .addOnSuccessListener(unused -> {
                    pd.dismiss();
                    btnSaveAll.setEnabled(true);

                    int presentCount = 0;
                    for (boolean val :
                            attendanceMap.values())
                        if (val) presentCount++;

                    Toast.makeText(this,
                            "Saved! Present: "
                                    + presentCount + " / "
                                    + studentList.size(),
                            Toast.LENGTH_LONG).show();

                    // Write audit log
                    Map<String, Object> log =
                            new HashMap<>();
                    log.put("action",
                            "attendance_marked");
                    log.put("subject",     subject);
                    log.put("date",        date);
                    log.put("year",        year);
                    log.put("department",  dept);
                    log.put("markedBy",    uid);
                    log.put("markedByName", facultyName);
                    log.put("totalStudents",
                            studentList.size());
                    log.put("presentCount", presentCount);
                    log.put("timestamp",
                            System.currentTimeMillis());
                    db.collection("auditLogs").add(log);

                    finish();
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    btnSaveAll.setEnabled(true);
                    Toast.makeText(this,
                            "Error saving: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}