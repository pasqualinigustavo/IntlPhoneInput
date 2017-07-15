package net.rimoto.intlphoneinput.demo;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private String language = "en";
    private EditText fragment_user_configurations__edittext_email = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment_user_configurations__edittext_email = (EditText) findViewById(R.id.fragment_user_configurations__edittext_email);

        fragment_user_configurations__edittext_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (language.equalsIgnoreCase("en")) {
                    language = "br";
                    changeLocation("br");
                    fragment_user_configurations__edittext_email.setHint(getString(R.string.fragment_signup_step_two_lbl_email));
                    Intent refresh = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(refresh);
                    finish();

                } else {
                    language = "en";
                    changeLocation("en");
                    fragment_user_configurations__edittext_email.setHint(getString(R.string.fragment_signup_step_two_lbl_email));
                    Intent refresh = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(refresh);
                    finish();
                }
            }
        });

    }

    public void changeLocation(String languageCode) {

        Locale test = Locale.getDefault();
        Locale test2  = Resources.getSystem().getConfiguration().locale;

        Resources res = getApplicationContext().getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        res.updateConfiguration(config, dm);
    }
}
