package me.remi.espie.brosignal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final SmsManager smsManager = SmsManager.getDefault();
    private final Gson gson = new Gson();
    private boolean sendDelay = false;
    private TransitionDrawable transitionSignal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toast.makeText(this, "Loadé", Toast.LENGTH_LONG).show()
        //deleteFile()
        for (User u : readJSONfromFile()) {
            addToDrawer(u);
        }
        setBroName();
        TextView broName = findViewById(R.id.broName);
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
        findViewById(R.id.callBros).setOnClickListener(view -> launchBroSignal());
        findViewById(R.id.addBroButton).setOnClickListener(view -> addBro());


        BitmapDrawable[] drawables = new BitmapDrawable[2];
        drawables[0] = ((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.brosignal, null));
        drawables[1] = ((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.brosignal_color, null));
        transitionSignal = new TransitionDrawable(drawables);
        ImageView callbros = findViewById(R.id.callBros);
        callbros.setImageDrawable(transitionSignal);
    }

    private void deleteFile() {
        getApplicationContext().deleteFile(getApplicationContext().getFilesDir().getAbsolutePath() + "/broname.txt");
        getApplicationContext().deleteFile(getApplicationContext().getFilesDir().getAbsolutePath() + "/bros.json");
    }

    private void saveBroName(String text) {
        try {
            FileWriter out = new FileWriter(new File(getApplicationContext().getFilesDir(), "broname.txt"));
            out.write(text);
            out.close();
        } catch (IOException e) {
            System.out.println("writing error");
        }
    }

    private void setBroName() {
        File broname = new File(getApplicationContext().getFilesDir(), "broname.txt");
        TextView broName = findViewById(R.id.broName);
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

    private List<User> readJSONfromFile() {
        List<User> userArray = new ArrayList<>();
        File fileName = new File(getApplicationContext().getFilesDir().getAbsoluteFile() + "/bros.json");
        //Toast.makeText(this, "Loadé", Toast.LENGTH_LONG).show()
        //fileName.delete()
        if (fileName.isFile()) {
            long size = fileName.length();
            if (size != 0L) {
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new FileReader(fileName.getAbsolutePath()));
                    String line = reader.readLine();
                    while (line != null) {
                        User user = gson.fromJson(line, User.class);
                        userArray.add(user);
                        line = reader.readLine();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else System.out.println("empty file");
        } else System.out.println("not a file");
        return userArray;
    }

    private void addToDrawer(User user) {
        LinearLayout drawer = findViewById(R.id.broList);

        ImageView thumbnail = new ImageView(this);
        if (!user.getContactThumbnails().equals("")) {
            thumbnail.setImageURI(Uri.parse(user.getContactThumbnails()));
        } else {
            thumbnail.setImageURI(Uri.parse("android.resource://me.remi.espie.brosignal/" + R.drawable.ic_baseline_person_24));
        }
        TextView contactName = new TextView(this);
        contactName.setText(user.getContactName());
        TextView contactNumber = new TextView(this);
        contactNumber.setText(user.getContactNumber());
        ImageView contactBin = new ImageView(this);
        contactBin.setImageURI(Uri.parse("android.resource://me.remi.espie.brosignal/" + R.drawable.ic_baseline_delete_forever_24));
        contactBin.setColorFilter(getResources().getColor(R.color.design_default_color_error), PorterDuff.Mode.SRC_IN);

        LinearLayout horizontalLayout = new LinearLayout(this);
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout verticalLayout = new LinearLayout(this);
        verticalLayout.setOrientation(LinearLayout.VERTICAL);

        horizontalLayout.addView(thumbnail);
        verticalLayout.addView(contactName);
        verticalLayout.addView(contactNumber);
        horizontalLayout.addView(verticalLayout);
        horizontalLayout.addView(contactBin);

        horizontalLayout.setGravity(Gravity.CENTER_VERTICAL);

        drawer.addView(horizontalLayout);

        contactBin.setOnClickListener(view -> {
            removeJSONfromFile(MainActivity.this.getApplicationContext().getFilesDir().getAbsolutePath() + "/bros.json", user.getContactID());
            runOnUiThread(() -> drawer.removeView(horizontalLayout));
        });
    }

    private void writeJSONtoFile(String filePath, User user) {
        File file = new File(filePath);
        String jsonString = gson.toJson(user);
        FileWriter out;
        if (file.length() == 0L) {
            try {
                out = new FileWriter(file);
                out.write(jsonString);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                out = new FileWriter(file, true);
                out.write(jsonString);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void removeJSONfromFile(String filePath, String userId) {
        List<User> userArray = new ArrayList<>();
        File fileName = new File(filePath);
        //Toast.makeText(this, "Loadé", Toast.LENGTH_LONG).show()
        //fileName.delete()
        if (fileName.isFile()) {
            long size = fileName.length();
            if (size != 0L) {
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new FileReader(fileName.getAbsolutePath()));
                    String line = reader.readLine();
                    while (line != null) {
                        User user = gson.fromJson(line, User.class);
                        if (!user.getContactID().equals(userId)) userArray.add(user);
                        line = reader.readLine();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                fileName.delete();
                for (User user : userArray) {
                    writeJSONtoFile(filePath, user);
                }
            } else System.out.println("empty file");
        } else System.out.println("not a file");
    }

    private boolean checkContactPerm() {
        return ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestContactPerm() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 5);
    }

    private boolean checkSMSPerm() {
        return ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSMSPerm() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 5);
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 2);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickContact();
        } else if (requestCode == 5 && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendBroSignal();
        } else {
            Toast.makeText(this, "Permission non accordée", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 2) {
                Uri contentData = data.getData();
                Cursor phoneNumber;
                Cursor contactData = getContentResolver().query(contentData, null, null, null, null);
                if (contactData.moveToFirst()) {
                    String contactID = contactData.getString(contactData.getColumnIndex(ContactsContract.Contacts._ID));
                    String contactName = contactData.getString(contactData.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String contactThumbnails = contactData.getString(contactData.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                    String idResult = contactData.getString(contactData.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    int idResultHold = Integer.parseInt(idResult);

                    if (idResultHold == 1) {
                        phoneNumber = getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactID,
                                null,
                                null
                        );

                        String contactNumber = "";

                        while (phoneNumber.moveToNext()) {
                            contactNumber = phoneNumber.getString(phoneNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        }

                        User user;
                        if (contactThumbnails != null)
                            user = new User(
                                    contactID,
                                    contactName,
                                    contactThumbnails,
                                    contactNumber
                            );
                        else user = new User(
                                contactID,
                                contactName,
                                "",
                                contactNumber
                        );

                        List<User> arrayUser = readJSONfromFile();
                        for (User u : arrayUser) {
                            if (u.getContactID().equals(contactID)) {
                                Toast.makeText(this, "BRO déjà enregistré", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        addToDrawer(user);
                        writeJSONtoFile(getApplicationContext().getFilesDir().getAbsolutePath() + "/bros.json", user);
                        phoneNumber.close();
                    }
                    contactData.close();
                }

            }
        } else {
            Toast.makeText(this, "Veuillez sélectionner un BRO", Toast.LENGTH_LONG).show();
        }
    }


    private void launchBroSignal() {
        if (!checkSMSPerm()) requestSMSPerm();
        else {
            sendBroSignal();
        }
    }

    private void sendBroSignal() {
        if (!sendDelay) {
            sendDelay = true;
            ImageView callBros = findViewById(R.id.callBros);
            transitionSignal.startTransition(10);
            callBros.animate().setDuration(250).scaleXBy(-0.1f).scaleYBy(-0.1f)
                    .withEndAction(() -> callBros.animate().setDuration(250).scaleXBy(0.1f).scaleYBy(0.1f));

            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(750);
                        //runOnUiThread(() -> callBros.setImageURI(Uri.parse("android.resource://me.remi.espie.brosignal/" + R.drawable.brosignal)));

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    transitionSignal.reverseTransition(150);
                    sendDelay = false;
                }
            }.start();

            TextView broName = findViewById(R.id.broName);
            String messageText = "BRO !! ";
            if (broName.getText().

                    length() == 0) messageText += "Ton BRO anonyme a besoin d'aide !";
            else messageText += "Ton BRO " + broName.getText() + " a besoin d'aide !";

            List<User> userArray = readJSONfromFile();
            if (userArray.isEmpty())
                Toast.makeText(this, "Vous n'avez pas de bro T_T", Toast.LENGTH_LONG).show();
            else {
                for (User u : userArray) {
                    smsManager.sendTextMessage(u.getContactNumber(), null, messageText, null, null);
                }
            }
        }
    }

    private void addBro() {
        if (checkContactPerm()) {
            pickContact();
        } else {
            requestContactPerm();
        }
    }
}