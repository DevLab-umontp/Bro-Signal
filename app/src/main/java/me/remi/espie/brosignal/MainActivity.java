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
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

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



        new TabLayoutMediator(tabLayout, viewPager2, this::setTabText).attach();

        Fragment setting = new Settings();
        adapter.addFragment(0, setting);

        if (userGroups.isEmpty()) {
            userGroups.add(new UserGroup("BRO 1", "Tous mes BROs réunis !", "", Color.RED, userGroups));
            userGroups.add(new UserGroup("BRO 2", "Mes autres maxi BROs !", "", Color.GREEN, userGroups));
            userGroups.add(new UserGroup("BRO 3", "Et mes giga maxi BROs !", "", Color.BLUE, userGroups));
        }
        for (int i = 0; i < userGroups.size(); i++) {
            adapter.addFragment(new BrolistTemplate(userGroups.get(i)));
        }

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

    private void setTabText(TabLayout.Tab tab, int position){
        if (position == 0){
            tab.setText("Paramètres");
        }
        else{
            tab.setText(userGroups.get(position-1).getName());
        }
    }

    @Override
    protected void onStop() {
        writeUserGroups();
        super.onStop();
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
                        Log.i("json read", line);
                        UserGroup userGroup = gson.fromJson(line, UserGroup.class);
                        userGroup.setParentList(userGroups);
                        userGroups.add(userGroup);
                        line = reader.readLine();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    deleteFile();
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
                Log.i("json write", gson.toJson(u));
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                    messageText = messageText.replace("$nom", name);
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

}