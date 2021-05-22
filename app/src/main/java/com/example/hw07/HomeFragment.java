package com.example.hw07;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HomeFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseStorage storage;
    final String TAG = "/demo";
    private static final int PICK_IMAGE = 100;
    ArrayList<QueryDocumentSnapshot> documents = new ArrayList<>();
    UserAdapter adapter;

    Button logoutBtn, myProfileBtn, takePhotoBtn;
    HomeListener hListener;
    ListView listView;
    ProgressBar pBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        getActivity().setTitle("Home");
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        logoutBtn = view.findViewById(R.id.homeBtn_logout);
        myProfileBtn = view.findViewById(R.id.homeBtn_myPictures);
        takePhotoBtn = view.findViewById(R.id.homeBtn_takePhoto);
        listView = view.findViewById(R.id.homeListView);
        pBar = view.findViewById(R.id.homeProgessBar);
        adapter = new UserAdapter(getContext(), R.layout.user_row_item,documents);
        listView.setAdapter(adapter);

        //Start the actions
        getProfiles();
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                hListener.onLogout();
            }
        });
        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, PICK_IMAGE);
            }
        });

        myProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hListener.onClickUser(auth.getUid());
            }
        });

        return view;
    }


    //Get all the profiles for the users
    private void getProfiles(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("profiles").get().addOnCompleteListener(getActivity(), new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    documents.clear();
                    for(QueryDocumentSnapshot document: task.getResult()){
                        if(!(document.getId().equals(auth.getUid()))){
                            documents.add(document);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(getActivity(), "Could not find users", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    //List adapter to display user
    public class UserAdapter extends ArrayAdapter<QueryDocumentSnapshot>{

        public UserAdapter(@NonNull Context context, int resource, @NonNull List<QueryDocumentSnapshot> objects) {
            super(context, resource, objects);
        }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_row_item, parent, false);
            }
            QueryDocumentSnapshot doc = getItem(position);
            TextView textViewName = convertView.findViewById(R.id.userItemView);
            textViewName.setText(doc.get("name").toString());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hListener.onClickUser(doc.getId());
                }
            });
            return convertView;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == -1 && requestCode == PICK_IMAGE && data.getData() != null){
            Toast.makeText(getActivity(), "Image being sent, please wait", Toast.LENGTH_SHORT).show();
            Uri imageUri = data.getData();
            String imgId = UUID.randomUUID().toString();
            StorageReference storageRef = storage.getReference();
            StorageReference imgRef = storageRef.child(auth.getUid()+"/"+ imgId +".jpeg");
            UploadTask uploadTask = imgRef.putFile(imageUri);
            uploadTask.addOnProgressListener(getActivity(), new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    pBar.setVisibility(View.VISIBLE);
                    pBar.setProgress((int) progress);
                    Log.d(TAG, "onProgress: " + progress);
                }
            })
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        saveImageRef(imgId);
                    }else{
                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void saveImageRef(String uuid){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        HashMap<String,Object> hashMap = new HashMap<>();
        ArrayList<String> likes = new ArrayList<>();
        hashMap.put("likes", likes);
        hashMap.put("photoUrl", auth.getUid()+"/"+uuid+".jpeg");
        db.collection("profiles")
                .document(auth.getUid())
                .collection("photos")
                .document(uuid)
                .set(hashMap)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            pBar.setVisibility(View.GONE);
                            Toast.makeText(getActivity(), "Image Sent", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof HomeListener){
            hListener = (HomeListener) context;
        }else{
            throw new Error(context.toString() + " must implement the LoginListener interface");
        }
    }

    interface HomeListener{
        void onLogout();
        void onClickUser(String userId);
    }
}