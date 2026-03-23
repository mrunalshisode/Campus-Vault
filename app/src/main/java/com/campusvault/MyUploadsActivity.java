package com.campusvault;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyUploadsActivity extends AppCompatActivity {

    RecyclerView recyclerDocs;
    DocumentAdapter adapter;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_uploads);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        recyclerDocs = findViewById(R.id.recyclerDocs);
        Button btnBack = findViewById(R.id.btnBack);

        adapter = new DocumentAdapter(
                this, new ArrayList<>());
        recyclerDocs.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerDocs.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        String uid = mAuth.getCurrentUser().getUid();
        db.collection("documents")
                .whereEqualTo("uploadedBy", uid)
                .get()
                .addOnSuccessListener(query -> {
                    List<Map<String, Object>> docs
                            = new ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        if (data != null) docs.add(data);
                    }
                    adapter.updateList(docs);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}