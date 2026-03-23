package com.campusvault;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class UserAdapter extends
        RecyclerView.Adapter<UserAdapter.ViewHolder> {

    Context context;
    List<Map<String, Object>> userList;

    public UserAdapter(Context context,
                       List<Map<String, Object>> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {
        Map<String, Object> user = userList.get(position);

        String name  = (String) user.get("name");
        String email = (String) user.get("email");
        String role  = (String) user.get("role");

        holder.tvUserName.setText(
                name != null ? name : "Unknown");
        holder.tvUserEmail.setText(
                email != null ? email : "");
        holder.tvUserRole.setText(
                "Role: " + (role != null ? role : "student"));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateList(List<Map<String, Object>> newList) {
        userList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail, tvUserRole;

        ViewHolder(View itemView) {
            super(itemView);
            tvUserName  = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole  = itemView.findViewById(R.id.tvUserRole);
        }
    }
}