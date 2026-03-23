package com.campusvault;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class DocumentsActivity extends AppCompatActivity {

    RecyclerView recyclerDocuments;
    DocumentAdapter adapter;
    List<Map<String, Object>> allDocs = new ArrayList<>();
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);

        db = FirebaseFirestore.getInstance();

        recyclerDocuments = findViewById(
                R.id.recyclerDocuments);
        Button btnBack = findViewById(R.id.btnBack);

        adapter = new DocumentAdapter(
                this, new ArrayList<>());
        recyclerDocuments.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerDocuments.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadDocuments();
    }

    private void loadDocuments() {
        db.collection("documents").get()
                .addOnSuccessListener(query -> {
                    allDocs.clear();
                    for (var doc : query.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        if (data != null) allDocs.add(data);
                    }
                    adapter.updateList(allDocs);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}