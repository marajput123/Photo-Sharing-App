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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    EditText emailEdit, passwordEdit;
    Button loginBtn, newAccountBtn;
    LoginListener lListener;
    private FirebaseAuth auth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        getActivity().setTitle("Login");
        emailEdit = view.findViewById(R.id.loginEdit_email);
        passwordEdit = view.findViewById(R.id.loginEdit_password);
        loginBtn = view.findViewById(R.id.loginBtn_login);
        newAccountBtn = view.findViewById(R.id.loginBtn_createAccount);
        auth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                if(email.isEmpty()){
                    Toast.makeText(getActivity(), "Email is empty", Toast.LENGTH_SHORT).show();
                }else if(password.isEmpty()){
                    Toast.makeText(getActivity(), "password is empty", Toast.LENGTH_SHORT).show();
                }else{
                    loginUser(email, password);
                }
            }
        });

        newAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lListener.onCreateUser();
            }
        });


        return view;
    }

    private void loginUser(String email, String password){
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    lListener.onLogin();
                }else{
                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof  LoginListener){
            lListener = (LoginListener) context;
        }else{
            throw new Error(context.toString() + " must implement the LoginListener interface");
        }
    }

    interface LoginListener{
        void onLogin();
        void onCreateUser();
    }
}