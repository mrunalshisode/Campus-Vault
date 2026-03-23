package com.campusvault;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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

public class InternshipsActivity extends AppCompatActivity {

    EditText etCompany, etRole, etDuration,
            etLocation, etStipend;
    Button btnPickOfferLetter,
            btnSaveInternship, btnBack;
    TextView tvOfferFileName;
    RecyclerView recyclerInternships;
    InternshipAdapter adapter;
    List<Map<String, Object>> internshipList
            = new ArrayList<>();

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseStorage storage;
    Uri selectedOfferUri = null;
    String studentName   = "";

    ActivityResultLauncher<String> offerPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedOfferUri = uri;
                            tvOfferFileName.setText(
                                    "Offer letter selected ✓");
                            tvOfferFileName.setTextColor(
                                    android.graphics.Color
                                            .parseColor("#059669"));
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internships);

        mAuth   = FirebaseAuth.getInstance();
        db      = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        etCompany        = findViewById(R.id.etCompany);
        etRole           = findViewById(R.id.etRole);
        etDuration       = findViewById(R.id.etDuration);
        etLocation       = findViewById(R.id.etLocation);
        etStipend        = findViewById(R.id.etStipend);
        btnPickOfferLetter = findViewById(
                R.id.btnPickOfferLetter);
        btnSaveInternship = findViewById(
                R.id.btnSaveInternship);
        btnBack          = findViewById(R.id.btnBack);
        tvOfferFileName  = findViewById(
                R.id.tvOfferFileName);
        recyclerInternships = findViewById(
                R.id.recyclerInternships);

        // Setup RecyclerView
        adapter = new InternshipAdapter(
                this, new ArrayList<>());
        recyclerInternships.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerInternships.setAdapter(adapter);

        // Load student name
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists())
                        studentName = doc.getString("name");
                });

        // Load internships
        loadInternships(uid);

        btnPickOfferLetter.setOnClickListener(v ->
                offerPickerLauncher.launch("*/*"));

        btnSaveInternship.setOnClickListener(v ->
                saveInternship(uid));

        btnBack.setOnClickListener(v -> finish());
    }

    private void saveInternship(String uid) {
        String company  = etCompany.getText()
                .toString().trim();
        String role     = etRole.getText()
                .toString().trim();
        String duration = etDuration.getText()
                .toString().trim();
        String location = etLocation.getText()
                .toString().trim();
        String stipend  = etStipend.getText()
                .toString().trim();

        if (company.isEmpty() || role.isEmpty()
                || duration.isEmpty()) {
            Toast.makeText(this,
                    "Enter company, role and duration",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveInternship.setEnabled(false);

        if (selectedOfferUri != null) {
            uploadOfferThenSave(uid, company,
                    role, duration, location, stipend);
        } else {
            saveToFirestore(uid, company, role,
                    duration, location, stipend, null);
        }
    }

    private void uploadOfferThenSave(String uid,
                                     String company, String role,
                                     String duration, String location,
                                     String stipend) {

        String fileName = System.currentTimeMillis()
                + "_offer";
        StorageReference ref = storage.getReference()
                .child("offer_letters/" + uid
                        + "/" + fileName);

        ref.putFile(selectedOfferUri)
                .addOnSuccessListener(task ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                        saveToFirestore(uid, company,
                                                role, duration, location,
                                                stipend, uri.toString())
                                )
                )
                .addOnFailureListener(e -> {
                    btnSaveInternship.setEnabled(true);
                    Toast.makeText(this,
                            "Upload failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToFirestore(String uid,
                                 String company, String role,
                                 String duration, String location,
                                 String stipend, String offerUrl) {

        Map<String, Object> internship = new HashMap<>();
        internship.put("company",     company);
        internship.put("role",        role);
        internship.put("duration",    duration);
        internship.put("location",    location);
        internship.put("stipend",     stipend);
        internship.put("studentId",   uid);
        internship.put("studentName", studentName);
        internship.put("status",      "Pending");
        internship.put("timestamp",
                System.currentTimeMillis());
        if (offerUrl != null)
            internship.put("offerLetterUrl", offerUrl);

        db.collection("internships").add(internship)
                .addOnSuccessListener(ref -> {
                    btnSaveInternship.setEnabled(true);
                    Toast.makeText(this,
                            "Internship saved!",
                            Toast.LENGTH_SHORT).show();
                    etCompany.setText("");
                    etRole.setText("");
                    etDuration.setText("");
                    etLocation.setText("");
                    etStipend.setText("");
                    selectedOfferUri = null;
                    tvOfferFileName.setText(
                            "No file selected");
                    loadInternships(uid);
                })
                .addOnFailureListener(e -> {
                    btnSaveInternship.setEnabled(true);
                    Toast.makeText(this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadInternships(String uid) {
        db.collection("internships")
                .whereEqualTo("studentId", uid)
                .get()
                .addOnSuccessListener(query -> {
                    internshipList.clear();
                    for (var doc : query.getDocuments()) {
                        Map<String, Object> data =
                                doc.getData();
                        if (data != null)
                            internshipList.add(data);
                    }
                    adapter.updateList(internshipList);
                });
    }
}