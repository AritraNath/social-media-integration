package com.aritra.loginappdemo;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class fb_activity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String name;
    private String email;
    private String photo_url;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fb_activity);

        mAuth = FirebaseAuth.getInstance();
        ImageView profileDP = findViewById(R.id.profileImage);
        TextView profileName = findViewById(R.id.profileName);
        TextView profileEmail = findViewById(R.id.profileEmail);
        btnLogout = findViewById(R.id.btnLogout);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        email = currentUser.getEmail();
        name = currentUser.getDisplayName();
        photo_url = currentUser.getPhotoUrl().toString();

        Glide.with(this).load(photo_url).into(profileDP);
        profileName.setText(name);
        profileEmail.setText(email);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null)
            logout();

    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        startActivity(new Intent(fb_activity.this,MainActivity.class));
        finish();

    }
}
