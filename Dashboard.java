package com.example.anantmantrimony.Activity;

import static android.content.ContentValues.TAG;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anantmantrimony.Api.ApiServiceCall;
import com.example.anantmantrimony.Api.Api_Interface;
import com.example.anantmantrimony.Fragment.HomeFragment;
import com.example.anantmantrimony.Fragment.MatchFragment;
import com.example.anantmantrimony.Fragment.MyProfile;
import com.example.anantmantrimony.Fragment.InboxFragment;
import com.example.anantmantrimony.Fragment.SearchFragment;
import com.example.anantmantrimony.InternError;
import com.example.anantmantrimony.Model.PersonalDetailsData;
import com.example.anantmantrimony.R;
import com.example.anantmantrimony.Util.AppStatus;
import com.example.anantmantrimony.Util.InternetCheckCallback;
import com.example.anantmantrimony.Util.SessionManager;
import com.example.anantmantrimony.Util.Utility;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Dashboard extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "Dashboard";
    public static BottomNavigationView navigation;

    public static TextView tv_title, sign;
    public static Context context;
    public static ImageView img_menu, img_logout;

    ProgressDialog progressDialog;
    boolean isMatchListOpen = true;
    public static String ErrorMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        initView();
        loadInitialDesign();
        context = getApplicationContext();
        setClickEvent();
    }

    private void setClickEvent() {

    }

    private void loadInitialDesign() {
        img_menu.setVisibility(View.GONE);
        img_menu.setImageResource(R.drawable.outline_menu_24);
        tv_title.setText("Dashboard");
        tv_title.setVisibility(View.VISIBLE);

        sign.setVisibility(View.GONE);
        progressDialog = new ProgressDialog(Dashboard.this);
        progressDialog.setMessage("Please wait..");
        progressDialog.setCancelable(false);
        navigation.setOnNavigationItemSelectedListener(this);
        ErrorMessage = getIntent().getStringExtra("ErrorMessage");

        navigation.setSelectedItemId(R.id.nav_match);
        MatchFragment matchFragment = new MatchFragment();
        loadFragment(matchFragment);
    }

    private void initView() {
        tv_title = findViewById(R.id.title_text);
        img_menu = findViewById(R.id.img_menu);
        sign = findViewById(R.id.sign);

        img_logout = findViewById(R.id.img_logout);
        navigation = findViewById(R.id.navigation);
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame_container, fragment);
            fragmentTransaction.commit();

            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        int fragments = getSupportFragmentManager().getBackStackEntryCount();
        Log.d(TAG, "onBackPressed: frg" + fragments);
        if (isMatchListOpen) {
            closeApp();
        } else {
            loadFragment(new MatchFragment());
            navigation.setSelectedItemId(R.id.nav_match);
        }
    }

    private void closeApp() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

        AppStatus.RegisterConnectivity(this, new InternetCheckCallback() {
            @Override
            public void GetResponse(String requestType, String response) {
                if (response.equalsIgnoreCase("disconnected")) {
                    startActivity(new Intent(Dashboard.this, InternError.class));

                }
            }
        });
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_dash:
                tv_title.setText("Dashboard");
                img_logout.setVisibility(View.GONE);
                HomeFragment home = new HomeFragment();
                loadFragment(home);
                isMatchListOpen = false;
                return true;
            case R.id.nav_match:
                tv_title.setText("Matches");
                img_logout.setVisibility(View.GONE);
                MatchFragment matchFragment = new MatchFragment();
                loadFragment(matchFragment);
                isMatchListOpen = true;
                return true;


            case R.id.nav_search:
                tv_title.setText("Search");
                img_logout.setVisibility(View.GONE);
                SearchFragment searchFragment = new SearchFragment();
                loadFragment(searchFragment);
                isMatchListOpen = false;

                return true;
            case R.id.nav_inbox:
                tv_title.setText("Inbox");
                img_logout.setVisibility(View.GONE);
                InboxFragment notificationFragment = new InboxFragment();
                loadFragment(notificationFragment);
                isMatchListOpen = false;
                return true;
            case R.id.nav_profile:
                tv_title.setText("My Profile");
                img_logout.setVisibility(View.GONE);
                MyProfile myProfile = new MyProfile();
                loadFragment(myProfile);
                isMatchListOpen = false;
                return true;
        }

        return false;
    }

    public static int getAge(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        Integer ageInt = new Integer(age);
        return ageInt;
    }


    @Override
    protected void onPause() {
        super.onPause();
        Utility.appContext.getSessionManager().setSteps(0);
        AppStatus.unRegisterConnectivity(Dashboard.this);
        Log.d(TAG, "onPause: ");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        Utility.appContext.getSessionManager().setSteps(0);
        Utility.appContext.getSessionManager().setKillApp(true);
    }
}