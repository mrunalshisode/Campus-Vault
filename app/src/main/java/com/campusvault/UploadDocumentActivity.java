package com.campusvault;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

public class UploadDocumentActivity extends AppCompatActivity {

    EditText etDocTitle, etDocCategory;
    TextView tvFileName;
    Button btnPickFile, btnUpload, btnBack;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseStorage storage;
    Uri selectedFileUri = null;
    String facultyName = "";

    ActivityResultLauncher<String> filePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedFileUri = uri;
                            tvFileName.setText(getFileName(uri));
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_document);

        mAuth   = FirebaseAuth.getInstance();
        db      = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        etDocTitle    = findViewById(R.id.etDocTitle);
        etDocCategory = findViewById(R.id.etDocCategory);
        tvFileName    = findViewById(R.id.tvFileName);
        btnPickFile   = findViewById(R.id.btnPickFile);
        btnUpload     = findViewById(R.id.btnUpload);
        btnBack       = findViewById(R.id.btnBack);

        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists())
                        facultyName = doc.getString("name");
                });

        btnPickFile.setOnClickListener(v ->
                filePickerLauncher.launch("application/pdf"));

        btnUpload.setOnClickListener(v -> uploadDocument());
        btnBack.setOnClickListener(v -> finish());
    }

    private void uploadDocument() {
        String title    = etDocTitle.getText()
                .toString().trim();
        String category = etDocCategory.getText()
                .toString().trim();

        if (title.isEmpty() || category.isEmpty()
                || selectedFileUri == null) {
            Toast.makeText(this,
                    "Fill all fields and pick a file",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading...");
        pd.show();
        btnUpload.setEnabled(false);

        String uid = mAuth.getCurrentUser().getUid();
        String fileName = System.currentTimeMillis()
                + "_" + getFileName(selectedFileUri);
        StorageReference ref = storage.getReference()
                .child("documents/" + fileName);

        ref.putFile(selectedFileUri)
                .addOnSuccessListener(task ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    Map<String, Object> data
                                            = new HashMap<>();
                                    data.put("title",      title);
                                    data.put("category",   category);
                                    data.put("fileUrl",    uri.toString());
                                    data.put("uploadedBy", uid);
                                    data.put("uploadedByName", facultyName);
                                    data.put("timestamp",
                                            System.currentTimeMillis());

                                    db.collection("documents").add(data)
                                            .addOnSuccessListener(r -> {
                                                pd.dismiss();
                                                btnUpload.setEnabled(true);
                                                Toast.makeText(this,
                                                        "Uploaded!",
                                                        Toast.LENGTH_SHORT).show();
                                                finish();
                                            });
                                })
                )
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    btnUpload.setEnabled(true);
                    Toast.makeText(this,
                            "Failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor c = getContentResolver()
                    .query(uri, null, null, null, null)) {
                if (c != null && c.moveToFirst()) {
                    int i = c.getColumnIndex(
                            OpenableColumns.DISPLAY_NAME);
                    if (i >= 0) result = c.getString(i);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1)
                result = result.substring(cut + 1);
        }
        return result;
    }
}