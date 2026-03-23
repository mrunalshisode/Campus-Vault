package com.campusvault;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DefaulterAdapter extends
        RecyclerView.Adapter<DefaulterAdapter.ViewHolder> {

    Context context;
    List<DefaulterStudent> list;

    public DefaulterAdapter(Context context,
                            List<DefaulterStudent> list) {
        this.context = context;
        this.list    = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_defaulter,
                        parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {
        DefaulterStudent s = list.get(position);

        holder.tvDefaulterName.setText(s.name);
        holder.tvDefaulterRoll.setText(
                "Roll: " + s.rollNo);
        holder.tvDefaulterPercent.setText(
                s.overallPercent + "%");

        // Show low subjects
        StringBuilder lowSubjects = new StringBuilder(
                "Low in: ");
        for (String sub : s.lowSubjects) {
            lowSubjects.append(sub).append(", ");
        }
        String subStr = lowSubjects.toString();
        if (subStr.endsWith(", ")) {
            subStr = subStr.substring(
                    0, subStr.length() - 2);
        }
        holder.tvDefaulterSubjects.setText(subStr);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(
            List<DefaulterStudent> newList) {
        list = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {
        TextView tvDefaulterName, tvDefaulterRoll,
                tvDefaulterSubjects,
                tvDefaulterPercent;

        ViewHolder(View itemView) {
            super(itemView);
            tvDefaulterName    = itemView.findViewById(
                    R.id.tvDefaulterName);
            tvDefaulterRoll    = itemView.findViewById(
                    R.id.tvDefaulterRoll);
            tvDefaulterSubjects = itemView.findViewById(
                    R.id.tvDefaulterSubjects);
            tvDefaulterPercent = itemView.findViewById(
                    R.id.tvDefaulterPercent);
        }
    }
}