package me.remi.espie.brosignal;

import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SettingsFragment extends Fragment {

    private final Settings settings;

    public SettingsFragment() {
        this.settings = Settings.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        SwitchCompat spam = view.findViewById(R.id.spamSwitch);
        spam.setChecked(settings.isSpam());
        spam.setOnClickListener((View v)-> settings.setSpam(spam.isChecked()));

        SwitchCompat showNumbers = view.findViewById(R.id.numberSwitch);
        showNumbers.setChecked(settings.isShowNumbers());
        showNumbers.setOnClickListener((View v)-> {
            settings.setShowNumbers(showNumbers.isChecked());
            ((ViewPagerAdapter) ((ViewPager2) view.getRootView().findViewById(R.id.groupList)).getAdapter()).refreshBrolists();
            });

        TextView customMessage = view.findViewById(R.id.customMessage);
        customMessage.setText(settings.getCustomMessage());
        customMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                settings.setCustomMessage(editable.toString());
            }
        });
        TextView broName = view.findViewById(R.id.broName);
        broName.setText(settings.getBroName());
        broName.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                settings.setBroName(editable.toString());
            }
        });

        return view;
    }

}