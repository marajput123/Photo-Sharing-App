package com.example.hw07;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class RegisterFragment extends Fragment {

    final String TAG = "/demo";
    private FirebaseAuth auth;
    EditText emailEdit, passwordEdit, nameEdit;
    Button cancelBtn, registerBtn;
    RegisterListener rListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        getActivity().setTitle("Register");
        auth = FirebaseAuth.getInstance();
        emailEdit = view.findViewById(R.id.registerEdit_email);
        passwordEdit = view.findViewById(R.id.registerEdit_password);
        nameEdit = view.findViewById(R.id.registerEdit_name);
        cancelBtn = view.findViewById(R.id.registerBtn_cancel);
        registerBtn = view.findViewById(R.id.registerBtn_register);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                String name = nameEdit.getText().toString();
                if(email.isEmpty()){
                    Toast.makeText(getActivity(), "Email is empty", Toast.LENGTH_SHORT).show();
                }else if(password.isEmpty()){
                    Toast.makeText(getActivity(), "password is empty", Toast.LENGTH_SHORT).show();
                }else if(name.isEmpty()){
                    Toast.makeText(getActivity(), "name is empty", Toast.LENGTH_SHORT).show();
                }else{
                    Log.d(TAG, "onClick: " + email + password + name);
                    registerUser(email, password, name);
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rListener.onCancel();
            }
        });
        return view;
    }


    private void registerUser(String email, String password, String name){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        HashMap<String, Object> userData = new HashMap<>();
        userData.put("name", name);



        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name).build();
                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                db.collection("profiles").document(auth.getUid()).set(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            rListener.onRegister();
                                        }
                                        else{
                                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }else{
                                Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }else{
                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof RegisterListener){
            rListener = (RegisterListener) context;
        }else{
            throw new Error(context.toString() + " must implement the RegisterListener Interface");
        }
    }

    interface RegisterListener{
        void onRegister();
        void onCancel();
    }
}