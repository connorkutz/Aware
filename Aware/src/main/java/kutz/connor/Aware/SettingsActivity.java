package kutz.connor.Aware;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static java.lang.Thread.sleep;


public class SettingsActivity extends AppCompatActivity {

    protected Switch volumeSwitch;
    protected Switch densitySwitch;
    protected Switch nameSwitch;
    protected Switch noiseSwitch;
    protected Switch realTimeSwitch;
    protected Button developerOptionsButton;
    private DatabaseReference myRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        volumeSwitch = findViewById(R.id.activeVolumeSwitch);
        densitySwitch = findViewById(R.id.crimeDensitySwitch);
        nameSwitch = findViewById(R.id.nameRecognitionSwitch);
        noiseSwitch = findViewById(R.id.noiseRecognitionSwitch);
        realTimeSwitch = findViewById(R.id.realTimeAlertsSwitch);
        developerOptionsButton = findViewById(R.id.developerOptionsButton);


        final FirebaseUser currentUser = getIntent().getParcelableExtra("user");
        final ArrayList<LatLng> crimeList = getIntent().getParcelableArrayListExtra("crimeList");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("UserSettings/");
        myRef = myRef.child(currentUser.getUid());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //update switches to reflect changes
                volumeSwitch.setChecked(MapsActivity.currentUserSettings.activeVolumeEnabled);
                densitySwitch.setChecked(MapsActivity.currentUserSettings.crimeDensityAlertsEnabled);
                nameSwitch.setChecked(MapsActivity.currentUserSettings.nameRecognitionEnabled);
                noiseSwitch.setChecked(MapsActivity.currentUserSettings.noiseRecognitionEnabled);
                realTimeSwitch.setChecked(MapsActivity.currentUserSettings.realTimeAlertsEnabled);

                try {
                    sleep(200);
                }catch(InterruptedException e){
                    Log.d("SettingsActivity", e.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("SettingsActivity","The read failed: " + databaseError.getCode());
            }
        });


        volumeSwitch.setOnCheckedChangeListener(new myOnCheckedChangedListener());
        densitySwitch.setOnCheckedChangeListener(new myOnCheckedChangedListener());
        nameSwitch.setOnCheckedChangeListener(new myOnCheckedChangedListener());
        noiseSwitch.setOnCheckedChangeListener(new myOnCheckedChangedListener());
        realTimeSwitch.setOnCheckedChangeListener(new myOnCheckedChangedListener());
        developerOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, TestActivity.class);
                intent.putExtra("user", currentUser);
                intent.putExtra("crimeList", crimeList);
                startActivity(intent);
            }
        });


    }

    protected class myOnCheckedChangedListener implements CompoundButton.OnCheckedChangeListener{
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
            updateSettings();
            Toast.makeText(SettingsActivity.this, "Updating settings.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void updateSettings(){
        UserSettings currentSettings =
                new UserSettings(nameSwitch.isChecked(),
                volumeSwitch.isChecked(),
                noiseSwitch.isChecked(),
                realTimeSwitch.isChecked(),
                densitySwitch.isChecked());
        myRef.setValue(currentSettings);
    }
}
