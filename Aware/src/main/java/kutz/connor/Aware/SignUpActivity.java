package kutz.connor.Aware;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static kutz.connor.Aware.TestActivity.MY_PERMISSIONS_REQUEST_FINE_LOCATION;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        final Button createAccountButton = findViewById(R.id.create_account_button);
        final TextView createAccountEmail = findViewById(R.id.sign_up_email_text);
        final TextView createAccountPassword1 = findViewById(R.id.sign_up_password_text);
        final TextView createAccountPassword2 = findViewById(R.id.sign_up_password2_text);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = createAccountEmail.getText().toString();
                String password1 = createAccountPassword1.getText().toString();
                String password2 = createAccountPassword2.getText().toString();


                if (email.equals("") || email.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please enter email address.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password1.equals("") || password1.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please enter password.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password2.equals("") || password2.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please enter password.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password2.equals(password1)) {
                    Toast.makeText(SignUpActivity.this, "Passwords are not the same.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (ContextCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SignUpActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                }
                if (ContextCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SignUpActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                }
                if (ContextCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SignUpActivity.this, "Location Service required to use Aware", Toast.LENGTH_SHORT).show();
                }
                else{
                    createUser(email, password1);
                }
            }
        });
    }

    private void createUser(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("CK", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            initializeUserSettings(user);
                            Intent intent = new Intent(SignUpActivity.this, MapsActivity.class);
                            intent.putExtra("user", user);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("CK", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initializeUserSettings(@Nullable FirebaseUser user){
        if(user != null) {
            DatabaseReference myRef = database.getReference("UserSettings/");
            myRef.child(user.getUid()).setValue(new UserSettings());
        }
        else{
            Log.d("SignUpActivity", "failed to initialize new user settings(user is null");
        }
    }
}
