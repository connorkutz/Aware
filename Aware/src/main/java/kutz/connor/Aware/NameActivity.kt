package kutz.connor.Aware

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseUser

class NameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name)

        val user = intent.getParcelableExtra<FirebaseUser>("user")
        val continueButton = findViewById<Button>(R.id.continueButton)
        continueButton.setOnClickListener{
            val intent = Intent(this@NameActivity, MapsActivity::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
        }
    }
}
