package com.campusvault;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivitiesActivity extends AppCompatActivity {

    EditText etActivityTitle, etActivityDate,
            etActivityDescription;
    Spinner spinnerActivityType;
    Button btnPickCertificate, btnSaveActivity, btnBack;
    TextView tvCertFileName;
    RecyclerView recyclerActivities;
    ActivitiesAdapter adapter;
    List<Map<String, Object>> activitiesList
            = new ArrayList<>();

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseStorage storage;
    Uri selectedCertUri = null;
    String studentName = "";

    ActivityResultLauncher<String> certPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedCertUri = uri;
                            tvCertFileName.setText(
                                    "Certificate selected ✓");
                            tvCertFileName.setTextColor(
                                    android.graphics.Color
                                            .parseColor("#059669"));
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities);

        mAuth   = FirebaseAuth.getInstance();
        db      = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        etActivityTitle =
                findViewById(R.id.etActivityTitle);
        etActivityDate  =
                findViewById(R.id.etActivityDate);
        etActivityDescription =
                findViewById(R.id.etActivityDescription);
        spinnerActivityType =
                findViewById(R.id.spinnerActivityType);
        btnPickCertificate =
                findViewById(R.id.btnPickCertificate);
        btnSaveActivity =
                findViewById(R.id.btnSaveActivity);
        btnBack = findViewById(R.id.btnBack);
        tvCertFileName =
                findViewById(R.id.tvCertFileName);
        recyclerActivities =
                findViewById(R.id.recyclerActivities);

        // Setup type spinner
        String[] types = {
                "Hackathon", "Competition",
                "Sports", "Cultural",
                "Workshop", "Seminar",
                "NSS/NCC", "Other"
        };
        ArrayAdapter<String> typeAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item,
                        types);
        typeAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerActivityType.setAdapter(typeAdapter);

        // Setup RecyclerView
        adapter = new ActivitiesAdapter(
                this, new ArrayList<>());
        recyclerActivities.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerActivities.setAdapter(adapter);

        // Load student name
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists())
                        studentName = doc.getString("name");
                });

        // Load existing activities
        loadActivities(uid);

        btnPickCertificate.setOnClickListener(v ->
                certPickerLauncher.launch("*/*"));

        btnSaveActivity.setOnClickListener(v ->
                saveActivity(uid));

        btnBack.setOnClickListener(v -> finish());
    }

    private void saveActivity(String uid) {
        String title = etActivityTitle.getText()
                .toString().trim();
        String date  = etActivityDate.getText()
                .toString().trim();
        String desc  = etActivityDescription.getText()
                .toString().trim();
        String type  = spinnerActivityType
                .getSelectedItem().toString();

        if (title.isEmpty() || date.isEmpty()) {
            Toast.makeText(this,
                    "Enter title and date",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveActivity.setEnabled(false);

        if (selectedCertUri != null) {
            uploadCertThenSave(uid, title,
                    type, date, desc);
        } else {
            saveToFirestore(uid, title,
                    type, date, desc, null);
        }
    }

    private void uploadCertThenSave(String uid,
                                    String title, String type,
                                    String date, String desc) {

        String fileName = System.currentTimeMillis()
                + "_cert";
        StorageReference ref = storage.getReference()
                .child("certificates/" + uid
                        + "/" + fileName);

        ref.putFile(selectedCertUri)
                .addOnSuccessListener(task ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                        saveToFirestore(uid, title,
                                                type, date, desc,
                                                uri.toString())
                                )
                )
                .addOnFailureListener(e -> {
                    btnSaveActivity.setEnabled(true);
                    Toast.makeText(this,
                            "Upload failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToFirestore(String uid,
                                 String title, String type,
                                 String date, String desc,
                                 String certUrl) {

        Map<String, Object> activity = new HashMap<>();
        activity.put("title",       title);
        activity.put("type",        type);
        activity.put("date",        date);
        activity.put("description", desc);
        activity.put("studentId",   uid);
        activity.put("studentName", studentName);
        activity.put("status",      "Pending");
        activity.put("timestamp",
                System.currentTimeMillis());
        if (certUrl != null)
            activity.put("certificateUrl", certUrl);

        db.collection("activities").add(activity)
                .addOnSuccessListener(ref -> {
                    btnSaveActivity.setEnabled(true);
                    Toast.makeText(this,
                            "Activity saved!",
                            Toast.LENGTH_SHORT).show();
                    etActivityTitle.setText("");
                    etActivityDate.setText("");
                    etActivityDescription.setText("");
                    selectedCertUri = null;
                    tvCertFileName.setText(
                            "No certificate selected");
                    loadActivities(uid);
                })
                .addOnFailureListener(e -> {
                    btnSaveActivity.setEnabled(true);
                    Toast.makeText(this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadActivities(String uid) {
        db.collection("activities")
                .whereEqualTo("studentId", uid)
                .get()
                .addOnSuccessListener(query -> {
                    activitiesList.clear();
                    for (var doc : query.getDocuments()) {
                        Map<String, Object> data =
                                doc.getData();
                        if (data != null)
                            activitiesList.add(data);
                    }
                    adapter.updateList(activitiesList);
                });
    }
}