package me.danielml.finalschoolapp.activities.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import me.danielml.finalschoolapp.R;

public class UserLoginActivity extends AppCompatActivity {

    private Button signIn, signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        signIn = findViewById(R.id.signInScreenBtn);
        signUp = findViewById(R.id.signUpScreenBtn);

        signIn.setOnClickListener((v) -> startActivity(new Intent(this, SignInActivity.class)));
        signUp.setOnClickListener((v) -> startActivity(new Intent(this, SignUpActivity.class)));

    }
}