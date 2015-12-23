package net.rimoto.intlphoneinput;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

public class IntlPhoneInput extends RelativeLayout {
    private final String DEFAULT_COUNTRY = Locale.getDefault().getCountry();

    // UI Views
    private Spinner mCountrySpinner;
    private EditText mPhoneEdit;

    //Adapters
    private CountrySpinnerAdapter mCountrySpinnerAdapter;
    private PhoneNumberWatcher mPhoneNumberWatcher = new PhoneNumberWatcher(DEFAULT_COUNTRY);

    //Util
    private PhoneNumberUtil mPhoneUtil = PhoneNumberUtil.getInstance();

    // Fields
    private Country mSelectedCountry;
    private CountriesFetcher.CountryList mCountries;
    private IntlPhoneInputListener mIntlPhoneInputListener;

    /**
     * Constructor
     * @param context Context
     */
    public IntlPhoneInput(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor
     * @param context Context
     * @param attrs AttributeSet
     */
    public IntlPhoneInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Init after constructor
     */
    private void init() {
        inflate(getContext(), R.layout.intl_phone_input, this);

        /**+
         * Country spinner
         */
        mCountrySpinner = (Spinner) findViewById(R.id.intl_phone_edit__country);
        mCountrySpinnerAdapter = new CountrySpinnerAdapter(getContext());
        mCountrySpinner.setAdapter(mCountrySpinnerAdapter);

        mCountries = CountriesFetcher.getCountries(getContext());
        mCountrySpinnerAdapter.addAll(mCountries);
        mCountrySpinner.setOnItemSelectedListener(mCountrySpinnerListener);

        /**
         * Phone text field
         */
        mPhoneEdit = (EditText) findViewById(R.id.intl_phone_edit__phone);
        mPhoneEdit.addTextChangedListener(mPhoneNumberWatcher);

        setDefault();
    }

    /**
     * Hide keyboard from phoneEdit field
     */
    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mPhoneEdit.getWindowToken(), 0);
    }

    /**
     * Set default value
     *  Will try to retrieve phone number from device
     */
    public void setDefault() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            String phone = telephonyManager.getLine1Number();
            if(phone != null && !phone.isEmpty()) {
                this.setNumber(telephonyManager.getLine1Number());
            } else {
                String iso = telephonyManager.getNetworkCountryIso();
                setEmptyDefault(iso);
            }
        } catch (SecurityException e) {
            setEmptyDefault();
        }
    }

    /**
     * Set default value with
     * @param iso ISO2 of country
     */
    public void setEmptyDefault(String iso) {
        if(iso == null || iso.isEmpty()) {
            iso = DEFAULT_COUNTRY;
        }
        int defaultIdx = mCountries.indexOfIso(iso);
        mSelectedCountry = mCountries.get(defaultIdx);
        mCountrySpinner.setSelection(defaultIdx);
    }

    /**
     * Alias for setting empty string of default settings from the device (using locale)
     */
    private void setEmptyDefault() {
        setEmptyDefault(null);
    }

    /**
     * Set hint number for country
     */
    private void setHint() {
        if(mPhoneEdit != null) {
            Phonenumber.PhoneNumber phoneNumber = mPhoneUtil.getExampleNumberForType(mSelectedCountry.getIso(), PhoneNumberUtil.PhoneNumberType.MOBILE);
            if (phoneNumber != null) {
                mPhoneEdit.setHint(mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL));
            }
        }
    }

    /**
     * Spinner listener
     */
    private AdapterView.OnItemSelectedListener mCountrySpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mSelectedCountry = mCountrySpinnerAdapter.getItem(position);
            mPhoneNumberWatcher = new PhoneNumberWatcher(mSelectedCountry.getIso());

            setHint();
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    /**
     * Phone number watcher
     */
    private class PhoneNumberWatcher extends PhoneNumberFormattingTextWatcher {
        private boolean lastValidity;

        @SuppressWarnings("unused")
        public PhoneNumberWatcher() {
            super();
        }

        //TODO solve it! support for android kitkat
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public PhoneNumberWatcher(String countryCode) {
            super(countryCode);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            super.onTextChanged(s, start, before, count);
            try {
                Phonenumber.PhoneNumber phoneNumber = mPhoneUtil.parse(s.toString(), mSelectedCountry.getIso());
                int countryIdx = mCountries.indexOfIso(mPhoneUtil.getRegionCodeForNumber(phoneNumber));
                mCountrySpinner.setSelection(countryIdx);
            } catch (NumberParseException ignored) {}

            if(mIntlPhoneInputListener != null) {
                boolean validity = isValid();
                if (validity != lastValidity) {
                    mIntlPhoneInputListener.done(IntlPhoneInput.this, validity);
                }
                lastValidity = validity;
            }
        }
    }

    /**
     * Set Number
     * @param number E.164 format or national format(for selected country)
     */
    public void setNumber(String number) {
        try {
            Phonenumber.PhoneNumber phoneNumber = mPhoneUtil.parse(number, mSelectedCountry.getIso());
            mPhoneEdit.setText(mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL));

            int countryIdx = mCountries.indexOfIso(mPhoneUtil.getRegionCodeForNumber(phoneNumber));
            mCountrySpinner.setSelection(countryIdx);
        } catch (NumberParseException ignored){}
    }

    /**
     * Get number
     * @return Phone number in E.164 format | null on error
     */
    @SuppressWarnings("unused")
    public String getNumber() {
        try {
            Phonenumber.PhoneNumber phoneNumber = mPhoneUtil.parse(mPhoneEdit.getText().toString(), mSelectedCountry.getIso());
            return mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException ignored){
            return null;
        }
    }

    /**
     * Get PhoneNumber object
     * @return PhonenUmber | null on error
     */
    @SuppressWarnings("unused")
    public Phonenumber.PhoneNumber getPhoneNumber() {
        try {
            return mPhoneUtil.parse(mPhoneEdit.getText().toString(), mSelectedCountry.getIso());
        } catch (NumberParseException ignored){
            return null;
        }
    }

    /**
     * Get selected country
     * @return Country
     */
    @SuppressWarnings("unused")
    public Country getSelectedCountry() {
        return mSelectedCountry;
    }

    /**
     * Check if number is valid
     * @return boolean
     */
    @SuppressWarnings("unused")
    public boolean isValid() {
        try {
            Phonenumber.PhoneNumber phoneNumber = mPhoneUtil.parse(mPhoneEdit.getText().toString(), mSelectedCountry.getIso());
            return mPhoneUtil.isValidNumber(phoneNumber);
        } catch (NumberParseException ignored){
            return false;
        }
    }

    /**
     * Add validation listener
     * @param listener IntlPhoneInputListener
     */
    public void onValidityChange(IntlPhoneInputListener listener) {
        mIntlPhoneInputListener = listener;
    }


    /**
     * Simple validation listener
     */
    public interface IntlPhoneInputListener {
        void done(View view, boolean isValid);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mPhoneEdit.setEnabled(enabled);
        mCountrySpinner.setEnabled(enabled);
    }
}