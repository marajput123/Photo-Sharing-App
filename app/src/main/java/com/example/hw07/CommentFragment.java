package com.example.hw07;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class CommentFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_PHOTO = "photo";

    private Photo photo;
    final String TAG = "/demo";

    ImageView imgView;
    TextView commentView;
    EditText commentEdit;
    Button postBtn;
    LinearLayoutManager layoutManager;
    CommentRecyclerViewAdapter adapter;
    RecyclerView recyclerView;
    ArrayList<Comment> comments = new ArrayList<>();

    public CommentFragment() {
        // Required empty public constructor
    }

    public static CommentFragment newInstance(Photo photo) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_PHOTO, photo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            photo =(Photo) getArguments().getSerializable(ARG_PARAM_PHOTO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comment, container, false);
        getActivity().setTitle("Comments");
        imgView = view.findViewById(R.id.commentImage_pic);
        commentView = view.findViewById(R.id.commentView_comSize);
        commentEdit = view.findViewById(R.id.commentEdit_comment);
        postBtn = view.findViewById(R.id.commentBtn_post);
        Glide.with(getActivity()).load(photo.getPhotoUri())
                .into(imgView);
        recyclerView = view.findViewById(R.id.commentListView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new CommentRecyclerViewAdapter(comments, photo, getActivity());
        recyclerView.setAdapter(adapter);
        getComments();


        //Fragment Actions
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(commentEdit.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(), "Comment is empty", Toast.LENGTH_SHORT).show();
                }else{
                    postComment();
                }
            }
        });
        return view;
    }

    private void getComments(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("profiles")
                .document(photo.getUserId())
                .collection("photos")
                .document(photo.getId())
                .collection("comments")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        comments.clear();
                        for(DocumentSnapshot doc : value.getDocuments()){
                            Comment comment = new Comment((String)doc.get("text"), (String)doc.get("name"), (String)doc.get("userId"), (String)doc.getId());
                            comments.add(comment);
                        }
                        commentView.setText(comments.size() + " comments");
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void postComment(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        HashMap<String, Object> comment = new HashMap<>();

        comment.put("name", auth.getCurrentUser().getDisplayName());
        comment.put("userId", auth.getCurrentUser().getUid());
        comment.put("text", commentEdit.getText().toString());

        db.collection("profiles")
                .document(photo.getUserId())
                .collection("photos")
                .document(photo.getId())
                .collection("comments")
                .add(comment)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(!task.isSuccessful()) {
                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        commentEdit.setText("");
                    }
                });
    }
}