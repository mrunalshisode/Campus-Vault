package com.campusvault;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HodAllStudentsActivity
        extends AppCompatActivity {

    RecyclerView recyclerStudents;
    UserAdapter adapter;
    TextView tvCount;
    Button btnBack;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(
                R.layout.activity_hod_all_students);

        db = FirebaseFirestore.getInstance();

        recyclerStudents = findViewById(
                R.id.recyclerStudents);
        tvCount  = findViewById(R.id.tvCount);
        btnBack  = findViewById(R.id.btnBack);

        adapter = new UserAdapter(
                this, new ArrayList<>());
        recyclerStudents.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerStudents.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        db.collection("users")
                .whereEqualTo("role", "student")
                .get()
                .addOnSuccessListener(query -> {
                    List<Map<String, Object>> list
                            = new ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        Map<String, Object> data =
                                doc.getData();
                        if (data != null) list.add(data);
                    }
                    adapter.updateList(list);
                    tvCount.setText(
                            list.size() + " students");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}