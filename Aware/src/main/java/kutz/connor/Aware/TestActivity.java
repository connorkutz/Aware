package kutz.connor.Aware;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;


public class TestActivity extends AppCompatActivity {
    ArrayList<Alert> myAlerts;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Alerts");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myAlerts = (ArrayList<Alert>) dataSnapshot.getValue();
                if (myAlerts != null) {
                    Log.d("MapsActivity", "currentAlerts: " + myAlerts.toString());
                }
            }
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("MapsActivity","The read failed: " + databaseError.getCode());
            }
        });

        findViewById(R.id.createAlertButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapsActivity.createAlert("test alert");
            }
        });
        findViewById(R.id.clearAlertsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapsActivity.clearAllAlerts();
            }
        });
        findViewById(R.id.createCrimeAlertButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                MapsActivity.createSampleCrimeAlert();
            }
        });

    }


}
