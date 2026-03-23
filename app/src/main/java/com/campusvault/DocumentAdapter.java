package com.campusvault;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class DocumentAdapter extends
        RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    Context context;
    List<Map<String, Object>> documentList;

    public DocumentAdapter(Context context,
                           List<Map<String, Object>> documentList) {
        this.context = context;
        this.documentList = documentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_document, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {
        Map<String, Object> doc = documentList.get(position);

        String title = (String) doc.get("title");
        String category = (String) doc.get("category");
        String uploadedBy = (String) doc.get("uploadedByName");
        String fileUrl = (String) doc.get("fileUrl");

        holder.tvDocTitle.setText(title != null ? title : "Untitled");
        holder.tvDocCategory.setText(
                category != null ? category : "General");
        holder.tvDocUploadedBy.setText(
                "By: " + (uploadedBy != null ? uploadedBy : "Faculty"));

        holder.btnDownload.setOnClickListener(v -> {
            if (fileUrl != null && !fileUrl.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(fileUrl));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    public void updateList(List<Map<String, Object>> newList) {
        documentList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDocTitle, tvDocCategory, tvDocUploadedBy;
        Button btnDownload;

        ViewHolder(View itemView) {
            super(itemView);
            tvDocTitle = itemView.findViewById(R.id.tvDocTitle);
            tvDocCategory = itemView.findViewById(R.id.tvDocCategory);
            tvDocUploadedBy =
                    itemView.findViewById(R.id.tvDocUploadedBy);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }
    }
}