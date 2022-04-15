package me.remi.espie.brosignal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
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
    private TabLayout tabLayout;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //deleteFile();
        readUserGroups();

        tabLayout = findViewById(R.id.groupName);
        ViewPager2 viewPager2 = findViewById(R.id.groupList);
        adapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> tab.setText("BRO")).attach();

        Fragment setting = new Settings();
        adapter.addFragment(0, setting);

//        userGroups.add(new UserGroup("Mes BROs", "Tous mes BROs réunis", Color.BLUE));
//        userGroups.add(new UserGroup("Bro 2", "test test", Color.LTGRAY));
//        userGroups.add(new UserGroup("test", "YES", Color.GREEN));

        if (userGroups.isEmpty()) {
            userGroups.add(new UserGroup("BRO 1", "Tous mes BROs réunis !", Color.RED));
            userGroups.add(new UserGroup("BRO 2", "Mes autres maxi BROs !", Color.GREEN));
            userGroups.add(new UserGroup("BRO 3", "Et mes giga maxi BROs !", Color.BLUE));
        }
        for (int i = 0; i < userGroups.size(); i++) {
            adapter.addFragment(new BrolistTemplate(userGroups.get(i)));
            //addUserGroupsToDrawer(userGroups.get(i));
        }
        for (int i = 0; i < userGroups.size(); i++) {
            tabLayout.getTabAt(i + 1).setText(userGroups.get(i).getName());
        }
        tabLayout.getTabAt(0).setText("Paramètres");
        tabLayout.getTabAt(1).select();

        //writeJSONtoFile();


        //create bro-sognal button transition
        BitmapDrawable[] drawables = new BitmapDrawable[2];
        drawables[0] = ((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.brosignal, null));
        drawables[1] = ((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.brosignal_color, null));
        transitionSignal = new TransitionDrawable(drawables);
        ImageView callbros = findViewById(R.id.callBros);
        callbros.setImageDrawable(transitionSignal);

        //          create listener
        //for buttons
        findViewById(R.id.callBros).setOnClickListener(view -> launchBroSignal());

    }

    @Override
    protected void onStop() {
        super.onStop();
        writeUserGroups();
    }

    private void deleteFile() {
        File broname = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/broname.txt");
        broname.delete();
        File brolist = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/bros.json");
        brolist.delete();
        File customMessage = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/customMessage.txt");
        customMessage.delete();
    }


    private void readUserGroups() {
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
                        Log.i("json", line);
                        line = reader.readLine();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else System.out.println("empty file");
        } else System.out.println("not a file");
    }

    private void writeUserGroups() {
        File file = new File(getApplicationContext().getFilesDir().getAbsoluteFile() + "/bros.json");
        FileWriter out;
        try {
            out = new FileWriter(file);
            for (UserGroup u : userGroups) {
                out.write(gson.toJson(u) + "\n");
                Log.i("json write : ", gson.toJson(u));
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void addToDrawer(User user, LinearLayout drawer) {
        //create layout
        LinearLayout verticalLayout = new LinearLayout(this);
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLayout.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams verticalParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        verticalParams.setMargins(10, 10, 10, 10);
        verticalLayout.setLayoutParams(verticalParams);
        verticalLayout.setGravity(Gravity.CENTER_HORIZONTAL);


        //set contact thumbnail
        ImageView thumbnail = new ImageView(this);
        if (!user.getContactThumbnails().equals("")) {
            thumbnail.setImageURI(Uri.parse(user.getContactThumbnails()));
        } else {
            thumbnail.setImageURI(Uri.parse("android.resource://me.remi.espie.brosignal/" + R.drawable.ic_baseline_person_24));
            thumbnail.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        }
        thumbnail.setBackgroundColor(Color.LTGRAY);
        thumbnail.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.2f
        ));

        //set other contact data
        TextView contactName = new TextView(this);
        contactName.setText(user.getContactName());
        contactName.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.1f
        ));
        contactName.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView contactNumber = new TextView(this);
        contactNumber.setText(user.getContactNumber());
        contactNumber.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.1f
        ));
        contactNumber.setGravity(Gravity.CENTER_HORIZONTAL);

        //add delete bin next to contact
        ImageView contactBin = new ImageView(this);
        contactBin.setImageURI(Uri.parse("android.resource://me.remi.espie.brosignal/" + R.drawable.ic_baseline_delete_forever_24));
        contactBin.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        contactBin.setOnClickListener(view -> {
            removeUserFromFile(user.getContactID());
            runOnUiThread(() -> drawer.removeView(verticalLayout));
        });
        contactBin.setBackgroundColor(Color.RED);
        contactBin.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.1f
        ));

        //add data to drawer
        verticalLayout.addView(thumbnail);
        verticalLayout.addView(contactName);
        verticalLayout.addView(contactNumber);
        verticalLayout.addView(contactBin);

        drawer.addView(verticalLayout);
    }

    private int getContrastColor(int color) {
        double y = (299 * Color.red(color) + 587 * Color.green(color) + 114 * Color.blue(color)) / 1000;
        return y >= 128 ? Color.BLACK : Color.WHITE;
    }


    private void writeToFile(String filePath, String text) {
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
                //writeUserArrayToFile(userArray);
            } else System.out.println("empty file");
        } else System.out.println("not a file");
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

            int selectedGroup = tabLayout.getSelectedTabPosition() - 1;
            if (selectedGroup < 0) {
                Toast.makeText(this, "Veuillez sélectionner un groupe de BROs", Toast.LENGTH_LONG).show();
                return;
            }

            File customName = new File(getApplicationContext().getFilesDir(), "broname.txt");
            File customMessageFile = new File(getApplicationContext().getFilesDir(), "customMessage.txt");
            String messageText = "";
            String name = "";
            if (customMessageFile.isFile() && customMessageFile.length() != 0L) {
                try {
                    BufferedReader messsageReader = new BufferedReader(new FileReader(customMessageFile.getAbsolutePath()));
                    messageText = messsageReader.readLine();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (customName.isFile() && customName.length() != 0L) {
                try {
                    BufferedReader nameReader = new BufferedReader(new FileReader(customName.getAbsolutePath()));
                    name = nameReader.readLine();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (name.length() != 0) {
                if (messageText.length() == 0) {
                    messageText = "BRO !! Ton BRO " + name + " a besoin d'aide !";
                } else {
                    messageText.replace("$nom", name);
                }
            } else {
                if (messageText.length() == 0) {
                    messageText = "BRO !! Ton BRO anonyme a besoin d'aide !";
                }
            }

            if (!userGroups.isEmpty()) {
                if (userGroups.size() > selectedGroup) {
                    if (!userGroups.get(selectedGroup).getUserList().isEmpty()) {
                        for (User u : userGroups.get(selectedGroup).getUserList()) {
                            smsManager.sendTextMessage(u.getContactNumber(), null, messageText, null, null);
                        }
                    } else
                        Toast.makeText(this, "Vous n'avez pas de bro T_T", Toast.LENGTH_LONG).show();
                } else Log.e("sms", "bad group selection");
            } else {
                Toast.makeText(this, "Vous n'avez pas de bro T_T", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickContact();
        } else if (requestCode == 5 && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendBroSignal();
        } else {
            Toast.makeText(this, "Permission non accordée", Toast.LENGTH_LONG).show();
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK) {
//            if (requestCode == 2) {
//                Uri contentData = data.getData();
//                Cursor phoneNumber;
//                Cursor contactData = getContentResolver().query(contentData, null, null, null, null);
//                if (contactData.moveToFirst()) {
//                    //if contact data exists, create new contact
//                    String contactID = contactData.getString(contactData.getColumnIndex(ContactsContract.Contacts._ID));
//                    String contactName = contactData.getString(contactData.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//                    String contactThumbnails = contactData.getString(contactData.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
//                    String idResult = contactData.getString(contactData.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
//                    int idResultHold = Integer.parseInt(idResult);
//
//                    int selectedGroup = tabLayout.getSelectedTabPosition() + 1;
//
//                    //check if user already exists
//                    //readUserGroups();
//                    if (!userGroups.isEmpty()) {
//                        if (userGroups.size() > selectedGroup) {
//                            UserGroup userGroup = userGroups.get(selectedGroup);
//                            if (!userGroup.getUserList().isEmpty()) {
//                                for (User u : userGroup.getUserList()) {
//                                    if (u.getContactID().equals(contactID)) {
//                                        Toast.makeText(this, "BRO déjà enregistré", Toast.LENGTH_LONG).show();
//                                        return;
//                                    }
//                                }
//                            }
//
//                            if (idResultHold == 1) {
//                                phoneNumber = getContentResolver().query(
//                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                                        null,
//                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactID,
//                                        null,
//                                        null
//                                );
//
//                                String contactNumber = "";
//
//                                //get last phoneNumber
//                                while (phoneNumber.moveToNext()) {
//                                    contactNumber = phoneNumber.getString(phoneNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                                }
//
//                                //create user depending of thumbnail presence
//                                User user;
//                                if (contactThumbnails != null)
//                                    user = new User(
//                                            contactID,
//                                            contactName,
//                                            contactThumbnails,
//                                            contactNumber
//                                    );
//                                else user = new User(
//                                        contactID,
//                                        contactName,
//                                        "",
//                                        contactNumber
//                                );
//
//                                //add user to view and to file
//                                //writeUserToFile(user);
//                                userGroup.addUser(user);
//                                adapter.refreshFragment(selectedGroup - 1, new BrolistTemplate(userGroup));
//
//                                //close data
//                                phoneNumber.close();
//
//                            }
//                        }
//                    }
//                    contactData.close();
//                }
//
//            }
//        } else {
//            Toast.makeText(this, "Veuillez sélectionner un BRO", Toast.LENGTH_LONG).show();
//        }
//    }

}