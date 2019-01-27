package kutz.connor.Aware;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        final Button logInButton = findViewById(R.id.log_in_button);
        final Button signUpButton = findViewById(R.id.sign_up_button);
        final EditText emailText = findViewById(R.id.sign_up_email_text);
        final EditText passwordText = findViewById(R.id.password_text);

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailText.getText().toString();
                String password = passwordText.getText().toString();

                if(email == null || email == "" || email.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Please enter email address.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(password == null || password == "" || password.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Please enter password.", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Log.d("CK", "log in successful");
                                    logIn(user);
                                } else {
                                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            Log.d("CK", "Current user is " + currentUser.getDisplayName());
            logIn(currentUser);
        }
        else{
            Log.d("CK", "No one logged in");
        }
    }

    private void logIn(FirebaseUser currentUser){
        Log.d("CK", "logging in");
        //get user preferences from fire base
        //put into sharedPrefs
        Intent intent = new Intent(this, TestActivity.class);
        startActivity(intent);
    }
}
