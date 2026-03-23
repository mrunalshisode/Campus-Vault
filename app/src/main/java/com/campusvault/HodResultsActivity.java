package com.campusvault;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HodResultsActivity
        extends AppCompatActivity {

    RecyclerView recyclerResults;
    ResultAdapter adapter;
    TextView tvTitle;
    Button btnBack;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(
                R.layout.activity_hod_results);

        db = FirebaseFirestore.getInstance();

        recyclerResults = findViewById(
                R.id.recyclerResults);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);

        adapter = new ResultAdapter(
                this, new ArrayList<>());
        recyclerResults.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerResults.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        // Load all results
        db.collection("results").get()
                .addOnSuccessListener(query -> {
                    List<Map<String, Object>> list
                            = new ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        Map<String, Object> data =
                                doc.getData();
                        if (data != null) list.add(data);
                    }
                    adapter.updateList(list);
                    tvTitle.setText(
                            "📊 Class Results ("
                                    + list.size() + ")");
                });
    }
}