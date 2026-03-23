package com.campusvault;
import android.view.View;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.LinearLayout;
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

public class FacultyDashboardActivity extends AppCompatActivity {

    TextView tvWelcome, tvUploadCount,
            tvMarksCount, tvAttCount;
    Button btnLogout;
    LinearLayout cardUpload, cardAttendance,
            cardMarks, cardMyUploads;
    RecyclerView recyclerMyDocs;
    DocumentAdapter adapter;
    List<Map<String, Object>> myDocs = new ArrayList<>();

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseStorage storage;
    String facultyName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_dashboard);

        mAuth   = FirebaseAuth.getInstance();
        db      = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        tvWelcome     = findViewById(R.id.tvWelcome);
        tvUploadCount = findViewById(R.id.tvUploadCount);
        tvMarksCount  = findViewById(R.id.tvMarksCount);
        tvAttCount    = findViewById(R.id.tvAttCount);
        btnLogout     = findViewById(R.id.btnLogout);
        View cardUpload = findViewById(R.id.cardUpload);
        View cardAttendance = findViewById(R.id.cardAttendance);
        View cardMarks = findViewById(R.id.cardMarks);
        View cardMyUploads = findViewById(R.id.cardMyUploads);
        recyclerMyDocs = findViewById(R.id.recyclerMyDocs);

        // Setup RecyclerView
        adapter = new DocumentAdapter(this, new ArrayList<>());
        recyclerMyDocs.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerMyDocs.setAdapter(adapter);

        // Load faculty info
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        facultyName = doc.getString("name");
                        tvWelcome.setText(
                                "Hi, " + facultyName + "! 👋");
                    }
                });

        // Load stats
        loadStats(uid);

        // Load recent uploads
        loadMyDocuments(uid);

        // Card navigation
        cardUpload.setOnClickListener(v ->
                showUploadDialog());

        if (cardAttendance != null)
            cardAttendance.setOnClickListener(v ->
                    startActivity(new Intent(this,
                            SmartAttendanceActivity.class)));

        cardMarks.setOnClickListener(v ->
                startActivity(new Intent(this,
                        EnterMarksActivity.class)));

        cardMyUploads.setOnClickListener(v ->
                startActivity(new Intent(this,
                        MyUploadsActivity.class)));

        // Logout
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this,
                    LoginActivity.class));
            finish();
        });
    }

    private void loadStats(String uid) {
        // Upload count
        db.collection("documents")
                .whereEqualTo("uploadedBy", uid).get()
                .addOnSuccessListener(q ->
                        tvUploadCount.setText(
                                String.valueOf(q.size())));

        // Marks count
        db.collection("results")
                .whereEqualTo("enteredBy", uid).get()
                .addOnSuccessListener(q ->
                        tvMarksCount.setText(
                                String.valueOf(q.size())));

        // Attendance count
        db.collection("attendance")
                .whereEqualTo("markedBy", uid).get()
                .addOnSuccessListener(q ->
                        tvAttCount.setText(
                                String.valueOf(q.size())));
    }

    private void loadMyDocuments(String uid) {
        db.collection("documents")
                .whereEqualTo("uploadedBy", uid)
                .limit(5)
                .get()
                .addOnSuccessListener(query -> {
                    myDocs.clear();
                    for (var doc : query.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        if (data != null) myDocs.add(data);
                    }
                    adapter.updateList(myDocs);
                });
    }

    // Upload dialog using existing upload screen
    private void showUploadDialog() {
        startActivity(new Intent(this,
                UploadDocumentActivity.class));
    }
}