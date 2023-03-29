package me.danielml.finalschoolapp.activities.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import me.danielml.finalschoolapp.R;

public class UserLoginActivity extends AppCompatActivity {

    private Button signIn, signUp;
    private Animation fadeIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        signIn = findViewById(R.id.signInScreenBtn);
        signUp = findViewById(R.id.signUpScreenBtn);

        signIn.setOnClickListener((v) -> startActivity(new Intent(this, SignInActivity.class)));
        signUp.setOnClickListener((v) -> startActivity(new Intent(this, SignUpActivity.class)));

        fadeIn = AnimationUtils.loadAnimation(this, R.anim.user_login_fade);
    }

    @Override
    protected void onStart() {
        super.onStart();

        signIn.startAnimation(fadeIn);
        signUp.startAnimation(fadeIn);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}