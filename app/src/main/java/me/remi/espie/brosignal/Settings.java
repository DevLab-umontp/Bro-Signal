package me.remi.espie.brosignal;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Settings extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        TextView customMessage = view.findViewById(R.id.customMessage);
        customMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                saveCustomMessage(editable.toString());
            }
        });
        TextView broName = view.findViewById(R.id.broName);
        broName.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                saveBroName(editable.toString());
            }
        });

        setCustomMessage(view);
        setBroName(view);

        // Inflate the layout for this fragment
        return view;
    }

    private void saveCustomMessage(String text) {
        try {
            FileWriter out = new FileWriter(new File(getView().getContext().getFilesDir(), "customMessage.txt"));
            out.write(text);
            out.close();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Sauvegarde du message personnalisé impossible !", Toast.LENGTH_LONG).show();
            System.out.println("writing error");
        }
    }

    private void setCustomMessage(View view) {
        File customMessageFile = new File(view.getContext().getFilesDir(), "customMessage.txt");
        TextView customMessage = view.findViewById(R.id.customMessage);
        if (customMessageFile.isFile()) {
            if (customMessageFile.length() != 0L) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(customMessageFile.getAbsolutePath()));
                    customMessage.setText(reader.readLine());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else System.out.println("empty file");
        } else System.out.println("not a file");
    }

    private void saveBroName(String text) {
        try {
            FileWriter out = new FileWriter(new File(getView().getContext().getFilesDir(), "broname.txt"));
            out.write(text);
            out.close();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Sauvegarde du nom de bro impossible !", Toast.LENGTH_LONG).show();
            System.out.println("writing error");
        }

    }

    private void setBroName(View view) {
        File broname = new File(view.getContext().getFilesDir(), "broname.txt");
        TextView broName = view.findViewById(R.id.broName);
        if (broname.isFile()) {
            if (broname.length() != 0L) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(broname.getAbsolutePath()));
                    broName.setText(reader.readLine());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else System.out.println("empty file");
        } else System.out.println("not a file");

    }

}