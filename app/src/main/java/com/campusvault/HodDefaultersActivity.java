package com.campusvault;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HodDefaultersActivity
        extends AppCompatActivity {

    RecyclerView recyclerDefaulters;
    DefaulterAdapter adapter;
    TextView tvTitle;
    Button btnBack;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(
                R.layout.activity_hod_defaulters);

        db = FirebaseFirestore.getInstance();

        recyclerDefaulters = findViewById(
                R.id.recyclerDefaulters);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);

        adapter = new DefaulterAdapter(
                this, new ArrayList<>());
        recyclerDefaulters.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerDefaulters.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadDefaulters();
    }

    private void loadDefaulters() {
        db.collection("attendance").get()
                .addOnSuccessListener(query -> {
                    Map<String, Map<String, int[]>>
                            rollSubjectMap = new HashMap<>();

                    for (var doc : query.getDocuments()) {
                        String rollNo =
                                doc.getString("rollNo");
                        String subject =
                                doc.getString("subject");
                        String status =
                                doc.getString("status");

                        if (rollNo == null
                                || subject == null) continue;

                        rollSubjectMap
                                .computeIfAbsent(rollNo,
                                        k -> new HashMap<>())
                                .computeIfAbsent(subject,
                                        k -> new int[]{0, 0});

                        int[] counts = rollSubjectMap
                                .get(rollNo).get(subject);
                        counts[1]++;
                        if ("Present".equals(status))
                            counts[0]++;
                    }

                    List<DefaulterStudent> defaulterList
                            = new ArrayList<>();

                    for (Map.Entry<String,
                            Map<String, int[]>> entry
                            : rollSubjectMap.entrySet()) {
                        String rollNo = entry.getKey();
                        Map<String, int[]> subjects =
                                entry.getValue();

                        int totalPresent = 0;
                        int totalClasses = 0;
                        List<String> lowSubjects =
                                new ArrayList<>();

                        for (Map.Entry<String, int[]>
                                subEntry
                                : subjects.entrySet()) {
                            int present =
                                    subEntry.getValue()[0];
                            int total =
                                    subEntry.getValue()[1];
                            totalPresent += present;
                            totalClasses += total;

                            int pct = total > 0
                                    ? (present * 100 / total)
                                    : 0;
                            if (pct < 75) {
                                lowSubjects.add(
                                        subEntry.getKey()
                                                + " (" + pct + "%)");
                            }
                        }

                        int overallPct = totalClasses > 0
                                ? (totalPresent * 100
                                / totalClasses)
                                : 0;

                        if (overallPct < 75) {
                            defaulterList.add(
                                    new DefaulterStudent(
                                            "Roll: " + rollNo,
                                            rollNo,
                                            overallPct,
                                            lowSubjects));
                        }
                    }

                    defaulterList.sort((a, b) ->
                            Integer.compare(
                                    a.overallPercent,
                                    b.overallPercent));

                    adapter.updateList(defaulterList);
                    tvTitle.setText(
                            "⚠ Defaulters ("
                                    + defaulterList.size() + ")");
                });
    }
}