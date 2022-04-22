package me.remi.espie.brosignal;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

public class SettingsFragment extends Fragment {

    private final Settings settings;

    public SettingsFragment(Settings settings) {
        this.settings = settings;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        CheckBox spam = view.findViewById(R.id.spamCheckBox);
        spam.setChecked(settings.isSpam());
        spam.setOnClickListener((View v)-> settings.setSpam(spam.isChecked()));

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