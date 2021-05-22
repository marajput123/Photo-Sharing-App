package com.example.hw07;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class GalleryFragment extends Fragment {

    final String TAG = "/demo";
    private FirebaseAuth auth;
    GalleryListener gListener;
    ArrayList<Photo> photos = new ArrayList();
    LinearLayoutManager layoutManager;
    PhotoRecyclerViewAdapter adapter;
    RecyclerView recyclerView;

    private static final String ARG_PARAM_USERID = "userId";


    private String userId;


    public static GalleryFragment newInstance(String userId) {
        GalleryFragment fragment = new GalleryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_USERID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_PARAM_USERID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        getActivity().setTitle("Photo Gallery");

        recyclerView = view.findViewById(R.id.galleryListView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PhotoRecyclerViewAdapter(photos, userId, getActivity(), gListener);
        recyclerView.setAdapter(adapter);

        getPhotos();

        return view;
    }

    private void getPhotos(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        db.collection("profiles")
                .document(userId)
                .collection("photos")
                .addSnapshotListener( new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        photos.clear();
                        for(DocumentSnapshot doc: value.getDocuments()){
                            String userId = doc.getReference().getParent().getParent().getId();
                            Photo photo = new Photo((ArrayList<String>) doc.get("likes"), (String)doc.get("photoUrl"), doc.getId(), userId);
                            photos.add(photo);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof  GalleryListener){
            gListener = (GalleryListener) context;
        }else{
            throw new Error(context.toString() + " must implement the GalleryListener Interface");
        }
    }

    interface GalleryListener{
        void onPhotoClick(Photo photo);
    }
}