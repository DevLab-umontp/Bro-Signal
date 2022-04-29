package me.remi.espie.brosignal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final SmsManager smsManager = SmsManager.getDefault();
    private FusedLocationProviderClient fusedLocationClient;

    private final Gson gson = new Gson();
    private boolean sendDelay = false;
    private boolean emergency = false;
    private TransitionDrawable transitionSignal;
    private TransitionDrawable transitionSignalEmergency;
    private final ArrayList<UserGroup> userGroups = new ArrayList<>();
    private TabLayout tabLayout;

    Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //deleteFile();
        readUserGroups();
        settings = readUserData();
        SettingsFragment settingsFragment = new SettingsFragment();

        tabLayout = findViewById(R.id.groupName);
        ViewPager2 viewPager2 = findViewById(R.id.groupList);
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager2, this::setTabText).attach();

        adapter.addFragment(0, settingsFragment);

        if (userGroups.isEmpty()) {
            userGroups.add(new UserGroup("BRO 1", "Tous mes BROs réunis !", "", Color.RED, userGroups));
            userGroups.add(new UserGroup("BRO 2", "Mes autres maxi BROs !", "", Color.GREEN, userGroups));
            userGroups.add(new UserGroup("BRO 3", "Et mes giga maxi BROs !", "", Color.BLUE, userGroups));
        }
        for (int i = 0; i < userGroups.size(); i++) {
            adapter.addFragment(new BrolistFragment(userGroups.get(i)));
        }

        tabLayout.getTabAt(1).select();

        //create bro-sognal button transition
        BitmapDrawable[] drawables = new BitmapDrawable[2];
        drawables[0] = ((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.brosignal, null));
        drawables[1] = ((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.brosignal_color, null));
        transitionSignal = new TransitionDrawable(drawables);

        BitmapDrawable[] drawablesEmergency = new BitmapDrawable[2];
        drawablesEmergency[0] = ((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.brosignal, null));
        drawablesEmergency[1] = ((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.brosignal_emergency_color, null));
        transitionSignalEmergency = new TransitionDrawable(drawablesEmergency);

        ImageView callbros = findViewById(R.id.callBros);
        callbros.setImageDrawable(transitionSignal);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //          create listener
        //for buttons
        findViewById(R.id.callBros).setOnClickListener(view -> launchBroSignal());

        // intent for widget
        if (getIntent() != null && getIntent().getAction().equals("callbros")){
            sendBroSignal();
        }
        if (getIntent() != null && getIntent().getAction().equals("mybroname")){

        }
        findViewById(R.id.panicButton1).setOnClickListener(view -> launchBroGPSAlert());
        findViewById(R.id.panicButton2).setOnClickListener(view -> launchBroGPSAlert());
    }

    private void setTabText(TabLayout.Tab tab, int position) {
        if (position == 0) {
            tab.setText("Paramètres");
        } else {
            tab.setText(userGroups.get(position - 1).getName());
        }
    }

    @Override
    protected void onStop() {
        saveData();
        super.onStop();
    }

    @Override
    protected void onPause() {
        saveData();
        super.onPause();
    }

    private void saveData() {
        writeUserGroups();
        writeUserData();
    }

    private void deleteFile() {
        File broname = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/broname.txt");
        broname.delete();
        File brolist = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/bros.json");
        brolist.delete();
        File customMessage = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/customMessage.txt");
        customMessage.delete();
        File setting = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/settings.json");
        setting.delete();
    }

    private Settings readUserData() {
        File fileName = new File(getApplicationContext().getFilesDir().getAbsoluteFile() + "/settings.json");
        if (fileName.isFile()) {
            long size = fileName.length();
            if (size != 0L) {
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new FileReader(fileName.getAbsolutePath()));
                    String line = reader.readLine();
                    Log.i("json read", line);
                    Settings settings = gson.fromJson(line, Settings.class);
                    return settings.setInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                    deleteFile();
                }
            } else System.out.println("empty setting file");
        } else System.out.println("not a setting file");
        return Settings.getInstance("", "", false, false);
    }

    private void writeUserData() {
        File file = new File(getApplicationContext().getFilesDir().getAbsoluteFile() + "/settings.json");
        FileWriter out;
        try {
            out = new FileWriter(file);
            out.write(gson.toJson(settings));
            Log.i("json write", gson.toJson(settings));
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            } else System.out.println("empty user group file");
        } else System.out.println("not a user group file");
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

    private void launchBroSignal() {
        if (!checkSMSPerm()) requestSMSPerm();
        else {
            UserGroup userGroup = checkValidGroup();
            if (userGroup != null) sendBroSignal(userGroup);
        }
    }

    private void sendBroSignal(UserGroup userGroup) {
        ImageView callBros = findViewById(R.id.callBros);
        if (emergency){
            emergency=false;
            callbrosAnimation(callBros);
        }
        if (settings.isSpam() || (!settings.isSpam() && !sendDelay)) {
            if (!sendDelay) {
                sendDelay = true;
                callBros.setImageDrawable(transitionSignal);
                transitionSignal.startTransition(50);
                callbrosAnimation(callBros);

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
            }

            String name = settings.getBroName();

            String messageText;
            if (userGroup.getCustomMessage().equals("")) {
                messageText = settings.getCustomMessage();
            } else {
                messageText = userGroup.getCustomMessage();
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


            for (User u : userGroup.getUserList()) {
                smsManager.sendTextMessage(u.getContactNumber(), null, messageText, null, null);
            }
            Toast.makeText(this, R.string.contacted, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkLocationPerm() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPerm() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 7);
    }

    @SuppressLint("MissingPermission")
    private void launchBroGPSAlert() {
        ImageView callBros = this.findViewById(R.id.callBros);
        if (!emergency) {
            if (!checkLocationPerm()) {
                requestLocationPerm();
            } else {
                UserGroup userGroup = checkValidGroup();
                if (userGroup != null) {

                    emergency = true;
                    callBros.setImageDrawable(transitionSignalEmergency);
                    transitionSignalEmergency.startTransition(250);

                    new Thread() {
                        public void run() {
                            boolean layer = true;
                            while (emergency) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                layer = !layer;
                                transitionSignalEmergency.reverseTransition(250);
                            }
                            if (layer) transitionSignalEmergency.reverseTransition(250);
                        }
                    }.start();


                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        getLocation(userGroup);
                    } else {
                        LocationRequest locationRequest = LocationRequest.create();
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationRequest.setExpirationTime(10000);
                        locationRequest.setFastestInterval(5000);

                        LocationSettingsRequest.Builder locationSettingRequest = new LocationSettingsRequest.Builder();
                        locationSettingRequest.addLocationRequest(locationRequest);
                        locationSettingRequest.setAlwaysShow(true);
                        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
                        settingsClient.checkLocationSettings(locationSettingRequest.build()).addOnSuccessListener(this, locationSettingsResponse -> getLocation(userGroup))
                                .addOnFailureListener(this, e -> {
                                    if (e instanceof ResolvableApiException) {
                                        try {
                                            ((ResolvableApiException) e).startResolutionForResult(MainActivity.this, 101);
                                        } catch (IntentSender.SendIntentException sendIntentException) {
                                            sendIntentException.printStackTrace();
                                        }
                                    }
                                });
                    }
                }
            }
        } else {
            emergency = false;
        }
    }

    private void callbrosAnimation(ImageView callBros){
        callBros.animate().setDuration(250).scaleXBy(-0.1f).scaleYBy(-0.1f)
                .withEndAction(() -> callBros.animate().setDuration(250).scaleXBy(0.1f).scaleYBy(0.1f));
    }

    @SuppressLint("MissingPermission")
    private void getLocation(UserGroup userGroup) {
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(this, location -> sendBroAlert(location, userGroup));
    }

    private void sendBroAlert(Location location, UserGroup userGroup) {

        String name = settings.getBroName();

        String messageText;

        if (name.length() != 0) {
            if (location != null) {
                Date d = new Date(location.getTime());
                DateFormat format = new SimpleDateFormat("HH'h'mm:ss", Locale.getDefault());
                String date = format.format(d);
                messageText = "BRO !! Ton BRO " + name + " a une urgence ! Il était aux là : https://google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude() + "/ à " + date;
            } else {
                messageText = "BRO !! Ton BRO " + name + " a une urgence ! Malheuresement, il n'a pas pu t'envoyer sa position !";
            }
        } else {
            if (location != null) {
                Date d = new Date(location.getTime());
                DateFormat format = new SimpleDateFormat("HH'h'mm:ss", Locale.getDefault());
                String date = format.format(d);
                messageText = "BRO !! Ton BRO a une urgence ! Il était aux là : https://google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude() + " à " + date;
            } else {
                messageText = "BRO !! Ton BRO a une urgence ! Malheuresement, il n'a pas pu t'envoyer sa position !";
            }
        }


        for (User u : userGroup.getUserList()) {
            smsManager.sendTextMessage(u.getContactNumber(), null, messageText, null, null);
        }
        Toast.makeText(this, R.string.contacted, Toast.LENGTH_SHORT).show();

    }

    private UserGroup checkValidGroup() {
        int selectedGroup = tabLayout.getSelectedTabPosition() - 1;
        if (selectedGroup < 0) {
            Toast.makeText(this, R.string.select_bro_group, Toast.LENGTH_SHORT).show();
            return null;
        }

        if (!userGroups.isEmpty()) {
            if (userGroups.size() > selectedGroup) {
                if (!userGroups.get(selectedGroup).getUserList().isEmpty()) {
                    return userGroups.get(selectedGroup);
                } else
                    Toast.makeText(this, R.string.no_bro, Toast.LENGTH_SHORT).show();
            } else Log.e("sms", "bad group selection");
        } else {
            Toast.makeText(this, R.string.no_bro, Toast.LENGTH_SHORT).show();
        }
        return null;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ViewPager2 viewPager2 = findViewById(R.id.groupList);
            ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager2.getAdapter();
            BrolistFragment fragment = (BrolistFragment) adapter.getFragment(tabLayout.getSelectedTabPosition());
            fragment.pickContact();
        } else if (requestCode == 5 && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            UserGroup userGroup = checkValidGroup();
            if (userGroup != null) sendBroSignal(userGroup);
        } else if (requestCode == 7 && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            UserGroup userGroup = checkValidGroup();
            if (userGroup != null) getLocation(userGroup);
        } else {
            Toast.makeText(this, R.string.unauthorized, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 101) {
                UserGroup userGroup = checkValidGroup();
                if (userGroup != null) getLocation(userGroup);
            }
        }
    }

}