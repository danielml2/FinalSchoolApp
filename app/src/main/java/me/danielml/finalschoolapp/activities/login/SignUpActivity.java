package me.danielml.finalschoolapp.activities.login;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import me.danielml.finalschoolapp.R;
import me.danielml.finalschoolapp.managers.FirebaseManager;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseManager manager;

    private EditText nameInput, emailInput, passwordInput, passwordConfirmation;
    private Button signUpSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signUpSubmit = findViewById(R.id.signUpBtn);


        emailInput = findViewById(R.id.newUserEmail);
        passwordInput = findViewById(R.id.newUserPassword);
        passwordConfirmation = findViewById(R.id.newUserPasswordConfirmation);
        nameInput = findViewById(R.id.newUserName);




        signUpSubmit.setOnClickListener((v) -> {
            if(isValidInfo()) {
                String email = emailInput.getText().toString();
                String password = emailInput.getText().toString();

            }
        });

    }

    public boolean isValidInfo() {

        String name = nameInput.getText().toString();
        String password = passwordInput.getText().toString(), passwordConfirm = passwordConfirmation.getText().toString();

        if(password.length() < 8)
            Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show();
        else if(!password.equals(passwordConfirm))
            Toast.makeText(this, "Password and confirmation password need to be the same", Toast.LENGTH_SHORT).show();
        else if(name.length() < 2)
            Toast.makeText(this, "Name needs to be longer than 1 character.", Toast.LENGTH_SHORT).show();
        else if(emailInput.getText().toString().length() < 5)
            Toast.makeText(this, "Email must be longer than 5 characters", Toast.LENGTH_SHORT).show();
        else
            return true;

        return false;
    }
}