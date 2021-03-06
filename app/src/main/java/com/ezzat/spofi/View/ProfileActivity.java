package com.ezzat.spofi.View;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.ezzat.spofi.Model.Location;
import com.ezzat.spofi.Model.User;
import com.ezzat.spofi.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private User currentUser;
    private CircleImageView profile;
    private NumberProgressBar progressBar;
    private TextView username,email,points,country, city, locationTv, phone, gender;
    private Button change;
    private LocationManager locationManager;
    private Switch notify, sms;
    TextView selectCity;
    String[] listItems;
    boolean[] checkedItems;
    ArrayList<Integer> mUserItems = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        currentUser = (User) (getIntent().getBundleExtra("send")).getSerializable("user");
        profile = findViewById(R.id.profile);
        username = findViewById(R.id.user_profile_name);
        email = findViewById(R.id.email);
        progressBar = findViewById(R.id.prog);
        points = findViewById(R.id.points);
        country = findViewById(R.id.country);
        city = findViewById(R.id.city);
        locationTv = findViewById(R.id.location);
        change = findViewById(R.id.change);
        phone = findViewById(R.id.phone);
        gender = findViewById(R.id.gender);
        notify = findViewById(R.id.notify);
        sms = findViewById(R.id.sms);
        addToViews();
    }

    private void addToViews() {
        username.setText(currentUser.getName());
        email.setText(currentUser.getEmail());
        progressBar.setProgress(currentUser.getRate());
        points.setText(currentUser.getPoints()+"");
        country.setText(currentUser.getLocation().getCountry());
        city.setText(currentUser.getLocation().getCity());
        locationTv.setText(currentUser.getLocation().getLang() + " : " + currentUser.getLocation().getLat());
        phone.setText(currentUser.getPhone());
        gender.setText(currentUser.getGender().toString());
        notify.setChecked(currentUser.isNotify());
        sms.setChecked(currentUser.isSms());

        if (!currentUser.getPhotoUrl().equals("")) {
            Glide.with(getApplicationContext()).load(currentUser.getPhotoUrl())
                    .thumbnail(0.5f)
                    .into(profile);
        }

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();

                } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    getLocation();
                }
                // Write a message to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Users").child(currentUser.getId()).child("location");
                myRef.setValue(currentUser.getLocation());
            }
        });

        notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUser.setNotify(notify.isChecked());
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Users").child(currentUser.getId()).child("notify");
                myRef.setValue(notify.isChecked());
            }
        });

        sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUser.setNotify(sms.isChecked());
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Users").child(currentUser.getId()).child("sms");
                myRef.setValue(sms.isChecked());
            }
        });

        selectCity = (TextView) findViewById(R.id.selectCity);
        listItems = getResources().getStringArray(R.array.countries_array);
        checkedItems = new boolean[listItems.length];
        selectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(ProfileActivity.this);
                mBuilder.setTitle(R.string.dialog_title);
                mBuilder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
//                        if (isChecked) {
//                            if (!mUserItems.contains(position)) {
//                                mUserItems.add(position);
//                            }
//                        } else if (mUserItems.contains(position)) {
//                            mUserItems.remove(position);
//                        }
                        if(isChecked){
                            mUserItems.add(position);
                        }else{
                            mUserItems.remove((Integer.valueOf(position)));
                        }
                    }
                });
                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        String item = "";
                        for (int i = 0; i < mUserItems.size(); i++) {
                            item = item + listItems[mUserItems.get(i)];
                            if (i != mUserItems.size() - 1) {
                                item = item + ", ";
                            }
                        }
                        selectCity.setText(item);
                    }
                });

                mBuilder.setNegativeButton(R.string.dismiss_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                mBuilder.setNeutralButton(R.string.clear_all_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        for (int i = 0; i < checkedItems.length; i++) {
                            checkedItems[i] = false;
                            mUserItems.clear();
                            selectCity.setText("");
                        }
                    }
                });

                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
            }
        });

    }


    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (ProfileActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {
            android.location.Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            android.location.Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            android.location.Location location2 = locationManager.getLastKnownLocation(LocationManager. PASSIVE_PROVIDER);

            if (location != null) {
                double latti = location.getLatitude();
                double longi = location.getLongitude();
                locationTv.setText(String.valueOf(latti) + " : " + String.valueOf(longi));
                currentUser.setLocation(new Location(currentUser.getLocation().getCountry(), currentUser.getLocation().getCity(), String.valueOf(longi), String.valueOf(latti)));
            } else  if (location1 != null) {
                double latti = location1.getLatitude();
                double longi = location1.getLongitude();
                locationTv.setText(String.valueOf(latti) + " : " + String.valueOf(longi));
                currentUser.setLocation(new Location(currentUser.getLocation().getCountry(), currentUser.getLocation().getCity(), String.valueOf(longi), String.valueOf(latti)));
            } else  if (location2 != null) {
                double latti = location2.getLatitude();
                double longi = location2.getLongitude();
                locationTv.setText(String.valueOf(latti) + " : " + String.valueOf(longi));
                currentUser.setLocation(new Location(currentUser.getLocation().getCountry(), currentUser.getLocation().getCity(), String.valueOf(longi), String.valueOf(latti)));
            }else{
                Toast.makeText(ProfileActivity.this,"Unble to Trace your location",Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setMessage("Please Turn ON your GPS Connection")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


}
