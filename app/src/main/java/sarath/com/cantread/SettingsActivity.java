package sarath.com.cantread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    private Switch imagesPreference;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        imagesPreference = findViewById(R.id.imagesSavePreference);

        imagesPreference.setChecked(preferences.getBoolean("local", true));
        imagesPreference.setOnClickListener(this);
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.imagesSavePreference) {
            SharedPreferences.Editor editor = preferences.edit();
            if(imagesPreference.isChecked()) {
                editor.putBoolean("local", true);
                editor.apply();
            } else {
                editor.putBoolean("local", false);
                editor.apply();
            }
        }
    }
}
