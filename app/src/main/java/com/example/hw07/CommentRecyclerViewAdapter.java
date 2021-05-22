package com.example.hw07;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewAdapter.CommentViewHolder> {

    ArrayList<Comment> data;
    Photo photo;
    Activity activity;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    final String TAG = "/demo";

    public CommentRecyclerViewAdapter(ArrayList<Comment> data, Photo photo, Activity activity){
        this.data = data;
        this.photo = photo;
        this.activity = activity;
    }

    @NonNull
    @Override
    public CommentRecyclerViewAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_row_item, parent, false);
        CommentViewHolder commentViewHolder = new CommentViewHolder(view);
        return commentViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = data.get(position);
        holder.nameView.setText(comment.getName());
        holder.commentView.setText(comment.getText());
        if(comment.getUserId().equals(auth.getUid())){
            holder.deleteIcon.setVisibility(View.VISIBLE);
        }else{
            holder.deleteIcon.setVisibility(View.GONE);
        }
        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteComment(comment, photo);
            }
        });
    }

    private void deleteComment(Comment comment, Photo photo){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("profiles")
                .document(photo.getUserId())
                .collection("photos")
                .document(photo.getId())
                .collection("comments")
                .document(comment.getId())
                .delete()
                .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(activity, "Deleted!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder{
        TextView nameView, commentView;
        ImageView deleteIcon;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.comment_row_name);
            commentView = itemView.findViewById(R.id.comment_row_comment);
            deleteIcon = itemView.findViewById(R.id.comment_row_bin);
        }
    }
}
