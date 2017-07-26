package net.rimoto.intlphoneinput.demo;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;

import net.rimoto.intlphoneinput.IntlPhoneInput;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private String language = "en";
    private EditText some_field = null;
    private IntlPhoneInput phone = null;
    private TextInputLayout fragment_user_configurations__inputlayout_email = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        some_field = (EditText) findViewById(R.id.some_field);
        phone = (IntlPhoneInput) findViewById(R.id.phone);
        fragment_user_configurations__inputlayout_email = (TextInputLayout) findViewById(R.id.fragment_user_configurations__inputlayout_email);

        fragment_user_configurations__inputlayout_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (language.equalsIgnoreCase("en")) {
                    language = "pt_BR";
                    changeLocation("pt_BR");
                    some_field.setHint(getString(R.string.fragment_signup_step_two_lbl_email));
                    Intent refresh = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(refresh);
                    finish();

                } else {
                    language = "en";
                    changeLocation("en");
                    some_field.setHint(getString(R.string.fragment_signup_step_two_lbl_email));
                    Intent refresh = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(refresh);
                    finish();
                }
            }
        });

//        phone.setHint("");
//        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
//        Locale locale = Resources.getSystem().getConfiguration().locale;
//        int countryCode = 0;
//        if (locale != null) {
//            countryCode = phoneUtil.getCountryCodeForRegion(locale.getCountry());
//            phone.getPhoneNumberEditText().setText("+" + String.valueOf(countryCode));
//        }
    }

    public void changeLocation(String languageCode) {

        Locale test = Locale.getDefault();
        Locale test2 = Resources.getSystem().getConfiguration().locale;

        Resources res = getApplicationContext().getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        res.updateConfiguration(config, dm);
    }
}
