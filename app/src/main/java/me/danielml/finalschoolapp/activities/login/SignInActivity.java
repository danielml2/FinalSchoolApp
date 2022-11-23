package me.danielml.finalschoolapp.activities.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import me.danielml.finalschoolapp.R;
import me.danielml.finalschoolapp.activities.SplashScreen;
import me.danielml.finalschoolapp.managers.FirebaseManager;

public class SignInActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button signInSubmit;

    private FirebaseManager fbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        fbManager = new FirebaseManager();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        signInSubmit = findViewById(R.id.signInBtn);

        signInSubmit.setOnClickListener((v) -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            fbManager.signIn(email, password, (user) -> {
                Toast.makeText(this, "Welcome back, " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, SplashScreen.class));
            }, (exception) -> {
                exception.printStackTrace();
                Toast.makeText(this, "Failed to sign in! (" + exception.getCause() + ")", Toast.LENGTH_SHORT).show();
            });
        });

    }
}