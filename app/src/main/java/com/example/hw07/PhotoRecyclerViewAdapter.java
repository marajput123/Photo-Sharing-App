package com.example.hw07;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class PhotoRecyclerViewAdapter extends RecyclerView.Adapter<PhotoRecyclerViewAdapter.PhotoViewHolder> {

    private ArrayList<Photo> data;
    private String userId;
    private FirebaseAuth auth;
    private Activity activity;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private GalleryFragment.GalleryListener gListener;

    public PhotoRecyclerViewAdapter(ArrayList<Photo> data, String userId, Activity activity, GalleryFragment.GalleryListener gListener){
        this.data = data;
        this.auth = FirebaseAuth.getInstance();
        this.userId = userId;
        this.activity = activity;
        this.gListener = gListener;
    }

    @NonNull
    @Override
    public PhotoRecyclerViewAdapter.PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item_row, parent, false);
        PhotoViewHolder photoViewHolder = new PhotoViewHolder(view, this.gListener);
        return photoViewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull PhotoRecyclerViewAdapter.PhotoViewHolder holder, int position) {
        Photo photo = data.get(position);
        holder.photo = photo;
        if(userId.equals(auth.getUid())){
            holder.deleteIcon.setVisibility(View.VISIBLE);
        }else{
            holder.deleteIcon.setVisibility(View.GONE);
        }
        if(photo.getLikes().contains(auth.getUid())){
            holder.likeIcon.setImageResource(R.drawable.like_favorite);
        }else{
            holder.likeIcon.setImageResource(R.drawable.like_not_favorite);
        }
        Log.d("/demo", "onBindViewHolder: " + auth.getUid() + "->" + photo.getLikes());
        holder.likeIcon.setVisibility(View.VISIBLE);
        holder.likesView.setText(photo.getLikes().size() + " likes ");

        //Recycler view actions
        getPhoto(photo.getPhotoUrl(), holder, photo);
        holder.likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postLike(photo, holder, userId);
            }
        });
        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePhoto(photo);
            }
        });

    }

    //Delete Photo
    private void deletePhoto(Photo photo){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference picRef = storageRef.child(photo.getPhotoUrl());
        picRef.delete().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    db.collection("profiles")
                            .document(auth.getUid())
                            .collection("photos")
                            .document(photo.getId())
                            .delete()
                            .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(activity, "Photo Deleted", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else{
                    Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //post the likes
    private void postLike(Photo photo, PhotoViewHolder holder, String userId){
        if(photo.getLikes().contains(auth.getUid())){
            photo.getLikes().remove(auth.getUid());
        }else{
            photo.getLikes().add(auth.getUid());
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String,Object> newData = new HashMap<>();
        newData.put("likes", photo.getLikes());
        db.collection("profiles")
                .document(userId)
                .collection("photos")
                .document(photo.getId())
                .update(newData)
                .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("/demo", "onComplete: Success");
                }else{
                    Log.d("/demo", "onComplete: Error");
                }
            }
        });
    }

    //Get photo into the imageView
    private void getPhoto(String imgRef, PhotoViewHolder holder, Photo photo){
        StorageReference storageRef = storage.getReference();
        StorageReference pathReference = storageRef.child(imgRef);
        final long ONE_MEGABYTE = 1024 * 1024;
        pathReference.getDownloadUrl().addOnCompleteListener(activity, new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    photo.setPhotoUri(task.getResult());
                    Glide.with(activity).load(task.getResult())
                .into(holder.img);
                }else{
                 Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder{
        ImageView img,deleteIcon,likeIcon;
        TextView likesView;
        Photo photo;
        public PhotoViewHolder(@NonNull View itemView, GalleryFragment.GalleryListener gListener) {
            super(itemView);
            img = itemView.findViewById(R.id.gallery_picture_pic);
            deleteIcon = itemView.findViewById(R.id.gallery_picture_trash);
            likeIcon = itemView.findViewById(R.id.gallery_picture_like);
            likesView = itemView.findViewById(R.id.galleryView_likes);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gListener.onPhotoClick(photo);
                }
            });
        }
    }
}
