package com.campusvault;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

public class StudentProfileActivity extends AppCompatActivity {

    EditText etFullName, etRollNo, etYear,
            etMentor, etPhone, etDivision;
    Spinner spinnerDept;
    Button btnSaveProfile, btnChangePhoto, btnBack;
    ImageView imgProfile;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseStorage storage;
    Uri selectedImageUri = null;

    ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedImageUri = uri;
                            imgProfile.setImageURI(uri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        mAuth   = FirebaseAuth.getInstance();
        db      = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        etFullName   = findViewById(R.id.etFullName);
        etRollNo     = findViewById(R.id.etRollNo);
        etYear       = findViewById(R.id.etYear);
        etMentor     = findViewById(R.id.etMentor);
        etPhone      = findViewById(R.id.etPhone);
        etDivision   = findViewById(R.id.etDivision);
        spinnerDept  = findViewById(R.id.spinnerDept);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnBack        = findViewById(R.id.btnBack);
        imgProfile     = findViewById(R.id.imgProfile);

        // Department spinner
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

        loadProfile();

        btnChangePhoto.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*"));

        btnSaveProfile.setOnClickListener(v ->
                saveProfile());

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadProfile() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("students").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        etFullName.setText(
                                doc.getString("fullName"));
                        etRollNo.setText(
                                doc.getString("rollNo"));
                        etYear.setText(
                                doc.getString("year"));
                        etMentor.setText(
                                doc.getString("mentor"));
                        etPhone.setText(
                                doc.getString("phone"));
                        etDivision.setText(
                                doc.getString("division"));

                        // Set department spinner
                        String dept =
                                doc.getString("department");
                        if (dept != null) {
                            ArrayAdapter adapter =
                                    (ArrayAdapter) spinnerDept
                                            .getAdapter();
                            int pos = adapter.getPosition(dept);
                            if (pos >= 0)
                                spinnerDept.setSelection(pos);
                        }

                        // Load photo
                        String photoUrl =
                                doc.getString("photoUrl");
                        if (photoUrl != null
                                && !photoUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(photoUrl)
                                    .circleCrop()
                                    .placeholder(android.R.drawable
                                            .ic_menu_camera)
                                    .into(imgProfile);
                        }
                    }
                });
    }

    private void saveProfile() {
        String fullName  = etFullName.getText()
                .toString().trim();
        String rollNo    = etRollNo.getText()
                .toString().trim();
        String year      = etYear.getText()
                .toString().trim();
        String mentor    = etMentor.getText()
                .toString().trim();
        String phone     = etPhone.getText()
                .toString().trim();
        String division  = etDivision.getText()
                .toString().trim();
        String dept      = spinnerDept
                .getSelectedItem().toString();

        if (fullName.isEmpty() || rollNo.isEmpty()
                || year.isEmpty()) {
            Toast.makeText(this,
                    "Please fill name, roll no and year",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveProfile.setEnabled(false);

        if (selectedImageUri != null) {
            uploadPhotoThenSave(fullName, rollNo,
                    year, dept, division, mentor, phone);
        } else {
            saveToFirestore(fullName, rollNo,
                    year, dept, division,
                    mentor, phone, null);
        }
    }

    private void uploadPhotoThenSave(String fullName,
                                     String rollNo, String year,
                                     String dept, String division,
                                     String mentor, String phone) {

        String uid = mAuth.getCurrentUser().getUid();
        StorageReference photoRef = storage.getReference()
                .child("profile_photos/"
                        + uid + ".jpg");

        photoRef.putFile(selectedImageUri)
                .addOnSuccessListener(task ->
                        photoRef.getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                        saveToFirestore(fullName,
                                                rollNo, year, dept,
                                                division, mentor, phone,
                                                uri.toString())
                                )
                )
                .addOnFailureListener(e -> {
                    btnSaveProfile.setEnabled(true);
                    Toast.makeText(this,
                            "Photo upload failed",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToFirestore(String fullName,
                                 String rollNo, String year,
                                 String dept, String division,
                                 String mentor, String phone,
                                 String photoUrl) {

        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> profile = new HashMap<>();
        profile.put("fullName",   fullName);
        profile.put("rollNo",     rollNo);
        profile.put("year",       year);
        profile.put("department", dept);
        profile.put("division",   division);
        profile.put("mentor",     mentor);
        profile.put("phone",      phone);
        profile.put("uid",        uid);
        if (photoUrl != null)
            profile.put("photoUrl", photoUrl);

        db.collection("students").document(uid)
                .set(profile)
                .addOnSuccessListener(unused -> {
                    btnSaveProfile.setEnabled(true);
                    Toast.makeText(this,
                            "Profile saved!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSaveProfile.setEnabled(true);
                    Toast.makeText(this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}