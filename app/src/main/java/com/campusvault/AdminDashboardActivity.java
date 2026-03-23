package com.campusvault;
import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
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

public class AdminDashboardActivity extends AppCompatActivity {

    TextView tvWelcome, tvTotalUsers,
            tvTotalDocs, tvTotalLogs;
    Button btnLogout;
    LinearLayout cardUsers, cardDocuments,
            cardLogs, cardStudents;
    RecyclerView recyclerUsers;
    UserAdapter userAdapter;
    List<Map<String, Object>> userList = new ArrayList<>();

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        tvWelcome    = findViewById(R.id.tvWelcome);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalDocs  = findViewById(R.id.tvTotalDocs);
        tvTotalLogs  = findViewById(R.id.tvTotalLogs);
        btnLogout    = findViewById(R.id.btnLogout);
        View cardUsers = findViewById(R.id.cardUsers);
        View cardDocuments = findViewById(R.id.cardDocuments);
        View cardLogs = findViewById(R.id.cardLogs);
        View cardStudents = findViewById(R.id.cardStudents);
        recyclerUsers = findViewById(R.id.recyclerUsers);

        // Setup adapter
        userAdapter = new UserAdapter(this, new ArrayList<>());
        recyclerUsers.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerUsers.setAdapter(userAdapter);

        // Load admin name
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        tvWelcome.setText(
                                "Hi, " + name + "! 🛡️");
                    }
                });

        // Load all stats
        loadStats();

        // Card navigation
        cardUsers.setOnClickListener(v ->
                Toast.makeText(this,
                        "Showing all users below",
                        Toast.LENGTH_SHORT).show());

        cardDocuments.setOnClickListener(v ->
                Toast.makeText(this,
                        "Document management coming soon",
                        Toast.LENGTH_SHORT).show());

        cardLogs.setOnClickListener(v ->
                Toast.makeText(this,
                        "Audit logs coming soon",
                        Toast.LENGTH_SHORT).show());

        cardStudents.setOnClickListener(v ->
                Toast.makeText(this,
                        "Student data coming soon",
                        Toast.LENGTH_SHORT).show());

        // Logout
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this,
                    LoginActivity.class));
            finish();
        });
    }

    private void loadStats() {
        // Users
        db.collection("users").get()
                .addOnSuccessListener(q -> {
                    userList.clear();
                    for (var doc : q.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        if (data != null) {
                            data.put("uid", doc.getId());
                            userList.add(data);
                        }
                    }
                    userAdapter.updateList(userList);
                    tvTotalUsers.setText(
                            String.valueOf(userList.size()));
                });

        // Documents
        db.collection("documents").get()
                .addOnSuccessListener(q ->
                        tvTotalDocs.setText(
                                String.valueOf(q.size())));

        // Audit logs
        db.collection("auditLogs").get()
                .addOnSuccessListener(q ->
                        tvTotalLogs.setText(
                                String.valueOf(q.size())));
    }
}