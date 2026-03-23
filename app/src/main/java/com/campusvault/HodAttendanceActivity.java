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

public class HodAttendanceActivity
        extends AppCompatActivity {

    RecyclerView recyclerAttendance;
    AttendanceAdapter adapter;
    TextView tvTitle;
    Button btnBack;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(
                R.layout.activity_hod_attendance);

        db = FirebaseFirestore.getInstance();

        recyclerAttendance = findViewById(
                R.id.recyclerAttendance);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);

        adapter = new AttendanceAdapter(
                this, new ArrayList<>());
        recyclerAttendance.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerAttendance.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        // Load all attendance
        db.collection("attendance").get()
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
                            "📋 Class Attendance ("
                                    + list.size() + " records)");
                });
    }
}