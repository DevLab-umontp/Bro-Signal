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
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
    private ArrayList<UserGroup> userGroups = new ArrayList<>();
    private int selectedGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deleteFile();
        setBroName();
        readJSONfromFile();
        if (userGroups.isEmpty()) userGroups.add(new UserGroup("Mes BROs", "Tous mes BROs réunis", Color.BLACK));
        for (UserGroup u : userGroups) {
            addUserGroupsToDrawer(u);
        }
        selectedGroup = 0;

        //create bro-sognal button transition
        BitmapDrawable[] drawables = new BitmapDrawable[2];
        drawables[0] = ((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.brosignal, null));
        drawables[1] = ((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.brosignal_color, null));
        transitionSignal = new TransitionDrawable(drawables);
        ImageView callbros = findViewById(R.id.callBros);
        callbros.setImageDrawable(transitionSignal);

        //create listener to text zones
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
    }

    private void deleteFile() {
        File broname = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/broname.txt");
        broname.delete();
        File brolist = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/bros.json");
        brolist.delete();
    }

    private void saveBroName(String text) {
        try {
            FileWriter out = new FileWriter(new File(getApplicationContext().getFilesDir(), "broname.txt"));
            out.write(text);
            out.close();
        } catch (IOException e) {
            Toast.makeText(this, "Sauvegarde du nom de bro impossible !", Toast.LENGTH_LONG).show();
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

    private void readJSONfromFile() {
        File fileName = new File(getApplicationContext().getFilesDir().getAbsoluteFile() + "/bros.json");

        if (fileName.isFile()) {
            long size = fileName.length();
            if (size != 0L) {
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new FileReader(fileName.getAbsolutePath()));
                    String line = reader.readLine();
                    while (line != null) {
                        UserGroup userGroup = gson.fromJson(line, UserGroup.class);
                        userGroups.add(userGroup);
                        line = reader.readLine();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else System.out.println("empty file");
        } else System.out.println("not a file");
    }

    private void addToDrawer(User user, LinearLayout drawer) {
        //create layouts
        LinearLayout horizontalLayout = new LinearLayout(this);
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLayout.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout verticalLayout = new LinearLayout(this);
        verticalLayout.setOrientation(LinearLayout.VERTICAL);

        //set contact thumbnail
        ImageView thumbnail = new ImageView(this);
        if (!user.getContactThumbnails().equals("")) {
            thumbnail.setImageURI(Uri.parse(user.getContactThumbnails()));
        } else {
            thumbnail.setImageURI(Uri.parse("android.resource://me.remi.espie.brosignal/" + R.drawable.ic_baseline_person_24));
        }
        //set other contact data
        TextView contactName = new TextView(this);
        contactName.setText(user.getContactName());
        TextView contactNumber = new TextView(this);
        contactNumber.setText(user.getContactNumber());

        //add delete bin next to contact
        ImageView contactBin = new ImageView(this);
        contactBin.setImageURI(Uri.parse("android.resource://me.remi.espie.brosignal/" + R.drawable.ic_baseline_delete_forever_24));
        contactBin.setColorFilter(getResources().getColor(R.color.design_default_color_error), PorterDuff.Mode.SRC_IN);
        contactBin.setOnClickListener(view -> {
            removeUserFromFile(user.getContactID());
            runOnUiThread(() -> drawer.removeView(horizontalLayout));
        });

        //add data to drawer
        horizontalLayout.addView(thumbnail);
        verticalLayout.addView(contactName);
        verticalLayout.addView(contactNumber);
        horizontalLayout.addView(verticalLayout);
        horizontalLayout.addView(contactBin);

        drawer.addView(horizontalLayout);
    }

    private void addUserGroupsToDrawer(UserGroup u){
        LinearLayout drawer = findViewById(R.id.groupList);

        ScrollView verticalScroll = new ScrollView(this);
        verticalScroll.canScrollVertically(1);
        verticalScroll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.9f
        ));

        LinearLayout horizontalLayout = new LinearLayout(this);
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.1f
        ));

        LinearLayout verticalLayout = new LinearLayout(this);
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        ));

        LinearLayout verticalLayoutTitle = new LinearLayout(this);
        verticalLayoutTitle.setOrientation(LinearLayout.VERTICAL);
        verticalLayoutTitle.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        ));

        LinearLayout verticalLayoutScroll = new LinearLayout(this);
        verticalLayoutScroll.setOrientation(LinearLayout.VERTICAL);


        //set group data
        int groupColor = u.getColor();
        TextView groupName = new TextView(this);
        groupName.setText(u.getName());
        groupName.setTextColor(groupColor);
        groupName.setGravity(Gravity.CENTER_HORIZONTAL);
        groupName.setTextSize(20);
        groupName.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        TextView groupDescription = new TextView(this);
        groupDescription.setText(u.getDescription());
        groupDescription.setTextSize(14);
        groupDescription.setGravity(Gravity.CENTER_HORIZONTAL);
        groupDescription.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        //add delete bin next to contact
        ImageView groupBin = new ImageView(this);
        groupBin.setImageURI(Uri.parse("android.resource://me.remi.espie.brosignal/" + R.drawable.ic_baseline_delete_forever_24));
        groupBin.setColorFilter(getResources().getColor(R.color.design_default_color_error), PorterDuff.Mode.SRC_IN);
        groupBin.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        ));
        groupBin.setOnClickListener(view -> {
            runOnUiThread(() -> drawer.removeView(horizontalLayout));
        });

        ImageView groupPlus = new ImageView(this);
        groupPlus.setImageURI(Uri.parse("android.resource://me.remi.espie.brosignal/" + R.drawable.ic_action_add));
        groupPlus.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        groupPlus.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        ));
        groupPlus.setOnClickListener(view -> {
            addUserGroupsToDrawer(new UserGroup("test", "test", Color.RED));
        });

        //add data to drawer

        verticalLayoutTitle.addView(groupName);
        verticalLayoutTitle.addView(groupDescription);

        horizontalLayout.addView(groupBin);
        horizontalLayout.addView(verticalLayoutTitle);
        horizontalLayout.addView(groupPlus);


        verticalLayout.addView(horizontalLayout);
        verticalScroll.addView(verticalLayoutScroll);
        verticalLayout.addView(verticalScroll);

        for (User user: u.getUserList()) {
            addToDrawer(user, verticalLayoutScroll);
        }

        drawer.addView(verticalLayout);
    }

    private void writeUserToFile(User user) {
        String jsonString = gson.toJson(user);
        writeToFile(getApplicationContext().getFilesDir().getAbsolutePath() + "/bros.json", jsonString);
    }

    private void writeUserArrayToFile(List<User> userArray){
        StringBuilder jsonString = new StringBuilder();
        for (User u: userArray) {
            jsonString.append('\n');
            jsonString.append(gson.toJson(u));
        }
        jsonString.delete(0,1);
        writeToFile(getApplicationContext().getFilesDir().getAbsolutePath() + "/bros.json", jsonString.toString());
    }

    private void writeToFile(String filePath, String text){
        File file = new File(filePath);
        FileWriter out;
        if (file.length() == 0L) {
            try {
                out = new FileWriter(file);
                out.write(text);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                out = new FileWriter(file, true);
                out.write("\n" + text);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void removeUserFromFile(String userId) {
        List<User> userArray = new ArrayList<>();
        File fileName = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/bros.json");
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
                writeUserArrayToFile(userArray);
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

            readJSONfromFile();
            if (!userGroups.isEmpty()) {
                if (userGroups.size() > selectedGroup) {
                    if (!userGroups.get(selectedGroup).getUserList().isEmpty()) {
                        for (User u : userGroups.get(selectedGroup).getUserList()) {
                            smsManager.sendTextMessage(u.getContactNumber(), null, messageText, null, null);
                        }
                    }
                }
            }

            else {
                Toast.makeText(this, "Vous n'avez pas de bro T_T", Toast.LENGTH_LONG).show();
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
                    //if contact data exists, create new contact
                    String contactID = contactData.getString(contactData.getColumnIndex(ContactsContract.Contacts._ID));
                    String contactName = contactData.getString(contactData.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String contactThumbnails = contactData.getString(contactData.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                    String idResult = contactData.getString(contactData.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    int idResultHold = Integer.parseInt(idResult);

                    //check if user already exists
                    readJSONfromFile();
                    if (!userGroups.isEmpty()) {
                        if (userGroups.size() > selectedGroup) {
                            if (!userGroups.get(selectedGroup).getUserList().isEmpty()) {
                                for (User u : userGroups.get(selectedGroup).getUserList()) {
                                    if (u.getContactID().equals(contactID)) {
                                        Toast.makeText(this, "BRO déjà enregistré", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                }
                            }
                        }
                    }

                    if (idResultHold == 1) {
                        phoneNumber = getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactID,
                                null,
                                null
                        );

                        String contactNumber = "";

                        //get last phoneNumber
                        while (phoneNumber.moveToNext()) {
                            contactNumber = phoneNumber.getString(phoneNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        }

                        //create user depending of thumbnail presence
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

                        //add user to view and to file
                        writeUserToFile(user);
                        LinearLayout grouplist = findViewById(R.id.groupList);
                        LinearLayout grouplist2 = (LinearLayout) grouplist.getChildAt(selectedGroup);
                        ScrollView grouplist3 = (ScrollView) grouplist2.getChildAt(1);

                        addToDrawer(user, (LinearLayout) grouplist3.getChildAt(0));

                        //close data
                        phoneNumber.close();
                    }
                    contactData.close();
                }

            }
        } else {
            Toast.makeText(this, "Veuillez sélectionner un BRO", Toast.LENGTH_LONG).show();
        }
    }

}