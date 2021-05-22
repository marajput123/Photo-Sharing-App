package com.example.hw07;

//Muhammad Rajput
//Imraan Boukarfi
//HW07


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginListener, HomeFragment.HomeListener, RegisterFragment.RegisterListener, GalleryFragment.GalleryListener {

    final String TAG = "/demo";
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = auth.getInstance().getCurrentUser();
        if(user == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.containerView, new LoginFragment()).commit();
        }else{
            getSupportFragmentManager().beginTransaction().replace(R.id.containerView, new HomeFragment()).commit();
        }
    }

    @Override
    public void onLogin() {
        getSupportFragmentManager().beginTransaction().replace(R.id.containerView, new HomeFragment()).commit();
    }

    @Override
    public void onCreateUser() {
        getSupportFragmentManager().beginTransaction().replace(R.id.containerView, new RegisterFragment()).commit();
    }

    @Override
    public void onLogout() {
        getSupportFragmentManager().beginTransaction().replace(R.id.containerView, new LoginFragment()).commit();
    }

    @Override
    public void onClickUser(String userId) {
        getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.containerView, GalleryFragment.newInstance(userId)).commit();
    }

    @Override
    public void onRegister() {
        getSupportFragmentManager().beginTransaction().replace(R.id.containerView, new HomeFragment()).commit();
    }

    @Override
    public void onCancel() {
        getSupportFragmentManager().beginTransaction().replace(R.id.containerView, new LoginFragment()).commit();
    }

    @Override
    public void onPhotoClick(Photo photo) {
        getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.containerView, CommentFragment.newInstance(photo)).commit();
    }
}