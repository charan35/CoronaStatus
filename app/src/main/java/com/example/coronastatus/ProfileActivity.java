package com.example.coronastatus;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.coronastatus.State.StatewiseDataActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    String confirmed;
    String active;
    String date;
    String recovered;
    String deaths;
    String newConfirmed;
    String newDeaths;
    String newRecovered;
    String totalTests;
    String oldTests;
    int testsInt;
    String totalTestsCopy;
    public static int confirmation = 0;
    public static boolean isRefreshed;
    private long backPressTime;
    private Toast backToast;

    TextView text;
    TextView textView_confirmed, textView_confirmed_new, textView_active, textView_active_new, textView_recovered, textView_recovered_new, textView_death, textView_death_new, textView_tests, textView_date, textView_tests_new, textview_time;
    ProgressDialog progressDialog;
    SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseReference DataRef;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        DataRef = FirebaseDatabase.getInstance().getReference();

        textView_confirmed = findViewById(R.id.confirmed_textView);
        textView_confirmed_new = findViewById(R.id.confirmed_new_textView);
        textView_active = findViewById(R.id.active_textView);
        textView_active_new = findViewById(R.id.active_new_textView);
        textView_recovered = findViewById(R.id.recovered_textView);
        textView_recovered_new = findViewById(R.id.recovered_new_textView);
        textView_death = findViewById(R.id.death_textView);
        textView_death_new = findViewById(R.id.death_new_textView);
        textView_tests = findViewById(R.id.tests_textView);
        textView_date = findViewById(R.id.date_textView);
        textView_tests_new = findViewById(R.id.tests_new_textView);
        swipeRefreshLayout = findViewById(R.id.main_refreshLayout);
        textview_time = findViewById(R.id.time_textView);
        text = findViewById(R.id.text);

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        Objects.requireNonNull(getSupportActionBar()).setTitle("CoronaStatus (India)");

        showProgressDialog();
        fetchData();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefreshed = true;
                fetchData();
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(ProfileActivity.this, "Data refreshed!", Toast.LENGTH_SHORT).show();
            }
        });

        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this);
        SharedPreferences.Editor editor = mPreferences.edit();
        String name = mPreferences.getString("Hello", "");
        text.setText(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dl = (DrawerLayout)findViewById(R.id.activity_main);
        t = new ActionBarDrawerToggle(this,dl,R.string.Open,R.string.Close);
        dl.addDrawerListener(t);
        t.syncState();

        nv = (NavigationView)findViewById(R.id.navigation_view);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.news:
                        startActivity(new Intent(ProfileActivity.this,NewsActivity.class));break;
                    case R.id.safety:
                        startActivity(new Intent(ProfileActivity.this,SafetyActivity.class));break;
                    case R.id.settings:
                        startActivity(new Intent(ProfileActivity.this,SampleActivity.class));break;
                    case R.id.logout:
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(ProfileActivity.this,LoginActivity.class));
                        finish();
                        Toast.makeText(ProfileActivity.this, "Logout",Toast.LENGTH_SHORT).show();break;
                    default:
                        return true;
                }
                return true;


            }


        });



        View navView =  nv.inflateHeaderView(R.layout.nav_header);
        final TextView Name = (TextView) navView.findViewById(R.id.name);
        final TextView Email = (TextView) navView.findViewById(R.id.email);



        if(text.getText().toString().equals("True")) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DataRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
            DataRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Name.setText(dataSnapshot.child("name").getValue().toString());
                    Email.setText(dataSnapshot.child("email").getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        else if(text.getText().toString().equals("False"))
        {
            Name.setText("Name");
            Email.setText("Your Email");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(t.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onBackPressed() {

        if (backPressTime + 800 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            return;
        } else {
            backToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressTime = System.currentTimeMillis();
    }

    public void fetchData() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String apiUrl = "https://api.covid19india.org/data.json";
        final PieChart mPieChart = findViewById(R.id.piechart);
        mPieChart.clearChart();

        //Fetching the API from URL
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //Since the objects of JSON are in an Array we need to define the array from which we can fetch objects
                    JSONArray jsonArray = response.getJSONArray("statewise");
                    JSONObject statewise = jsonArray.getJSONObject(0);


                    confirmed = statewise.getString("confirmed");
                    active = statewise.getString("active");
                    date = statewise.getString("lastupdatedtime");
                    recovered = statewise.getString("recovered");
                    deaths = statewise.getString("deaths");
                    newConfirmed = statewise.getString("deltaconfirmed");
                    newDeaths = statewise.getString("deltadeaths");
                    newRecovered = statewise.getString("deltarecovered");
                    if (isRefreshed) {
                        //Inserting the fetched data into variables
                        Runnable progressRunnable = new Runnable() {

                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                progressDialog.cancel();

                                String activeCopy = active;
                                String deathsCopy = deaths;
                                String recoveredCopy = recovered;
                                String confirmedNewCopy = newConfirmed;


                                int confirmedInt = Integer.parseInt(confirmed);
                                confirmed = NumberFormat.getInstance().format(confirmedInt);
                                textView_confirmed.setText(confirmed);

                                int newConfirmedInt = Integer.parseInt(newConfirmed);
                                newConfirmed = NumberFormat.getInstance().format(newConfirmedInt);
                                textView_confirmed_new.setText("+" + newConfirmed);

                                int activeInt = Integer.parseInt(active);
                                active = NumberFormat.getInstance().format(activeInt);
                                textView_active.setText(active);

                                //We need to calculate new active cases since it doesn't exist in API
                                int newActive = (Integer.parseInt(confirmedNewCopy)) - ((Integer.parseInt(newRecovered)) + Integer.parseInt(newDeaths));
                                textView_active_new.setText("+" + NumberFormat.getInstance().format(newActive));

                                int recoveredInt = Integer.parseInt(recovered);
                                recovered = NumberFormat.getInstance().format(recoveredInt);
                                textView_recovered.setText(recovered);

                                int recoveredNewInt = Integer.parseInt(newRecovered);
                                newRecovered = NumberFormat.getInstance().format(recoveredNewInt);
                                textView_recovered_new.setText("+" + newRecovered);

                                int deathsInt = Integer.parseInt(deaths);
                                deaths = NumberFormat.getInstance().format(deathsInt);
                                textView_death.setText(deaths);

                                int deathsNewInt = Integer.parseInt(newDeaths);
                                newDeaths = NumberFormat.getInstance().format(deathsNewInt);
                                textView_death_new.setText("+" + newDeaths);

                                String dateFormat = formatDate(date, 1);
                                textView_date.setText(dateFormat);

                                String timeFormat = formatDate(date, 2);
                                textview_time.setText(timeFormat);

                                mPieChart.addPieSlice(new PieModel("Active", Integer.parseInt(activeCopy), Color.parseColor("#007afe")));
                                mPieChart.addPieSlice(new PieModel("Recovered", Integer.parseInt(recoveredCopy), Color.parseColor("#08a045")));
                                mPieChart.addPieSlice(new PieModel("Deceased", Integer.parseInt(deathsCopy), Color.parseColor("#F6404F")));

                                mPieChart.startAnimation();
                                fetchTests();
                            }
                        };
                        Handler pdCanceller = new Handler();
                        pdCanceller.postDelayed(progressRunnable, 0);
                    } else {
                        //Inserting the fetched data into variables
                        if (!date.isEmpty()) {
                            Runnable progressRunnable = new Runnable() {

                                @SuppressLint("SetTextI18n")
                                @Override
                                public void run() {
                                    progressDialog.cancel();

                                    String activeCopy = active;
                                    String deathsCopy = deaths;
                                    String recoveredCopy = recovered;
                                    String confirmedNewCopy = newConfirmed;

                                    int confirmedInt = Integer.parseInt(confirmed);
                                    confirmed = NumberFormat.getInstance().format(confirmedInt);
                                    textView_confirmed.setText(confirmed);

                                    int newConfirmedInt = Integer.parseInt(newConfirmed);
                                    newConfirmed = NumberFormat.getInstance().format(newConfirmedInt);
                                    textView_confirmed_new.setText("+" + newConfirmed);

                                    int activeInt = Integer.parseInt(active);
                                    active = NumberFormat.getInstance().format(activeInt);
                                    textView_active.setText(active);

//                                    //We need to calculate new active cases since it doesn't exist in API
//                                    int newActive = (Integer.parseInt(confirmedNewCopy)) - ((Integer.parseInt(newRecovered)) + Integer.parseInt(newDeaths));
//                                    textView_active_new.setText("+" + NumberFormat.getInstance().format(newActive));

                                    int recoveredInt = Integer.parseInt(recovered);
                                    recovered = NumberFormat.getInstance().format(recoveredInt);
                                    textView_recovered.setText(recovered);

                                    int recoveredNewInt = Integer.parseInt(newRecovered);
                                    newRecovered = NumberFormat.getInstance().format(recoveredNewInt);
                                    textView_recovered_new.setText("+" + newRecovered);

                                    int deathsInt = Integer.parseInt(deaths);
                                    deaths = NumberFormat.getInstance().format(deathsInt);
                                    textView_death.setText(deaths);

                                    int deathsNewInt = Integer.parseInt(newDeaths);
                                    newDeaths = NumberFormat.getInstance().format(deathsNewInt);
                                    textView_death_new.setText("+" + newDeaths);


                                    String dateFormat = formatDate(date, 1);
                                    textView_date.setText(dateFormat);

                                    String timeFormat = formatDate(date, 2);
                                    textview_time.setText(timeFormat);

                                    mPieChart.addPieSlice(new PieModel("Active", Integer.parseInt(activeCopy), Color.parseColor("#007afe")));
                                    mPieChart.addPieSlice(new PieModel("Recovered", Integer.parseInt(recoveredCopy), Color.parseColor("#08a045")));
                                    mPieChart.addPieSlice(new PieModel("Deceased", Integer.parseInt(deathsCopy), Color.parseColor("#F6404F")));

                                    mPieChart.addPieSlice(new PieModel());


                                    mPieChart.startAnimation();
                                    fetchTests();
                                }
                            };
                            Handler pdCanceller = new Handler();
                            pdCanceller.postDelayed(progressRunnable, 1000);
                            confirmation = 1;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public String formatDate(String date, int testCase) {
        Date mDate = null;
        String dateFormat;
        try {
            mDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).parse(date);
            if (testCase == 0) {
                dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(mDate);
                return dateFormat;
            } else if (testCase == 1) {
                dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US).format(mDate);
                return dateFormat;
            } else if (testCase == 2) {
                dateFormat = new SimpleDateFormat("hh:mm a", Locale.US).format(mDate);
                return dateFormat;
            } else {
                Log.d("error", "Wrong input! Choose from 0 to 2");
                return "Error";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return date;
        }
    }

    public void fetchTests() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String apiUrl = "https://api.covid19india.org/data.json";
        JsonObjectRequest jsonObjectRequestTests = new JsonObjectRequest(Request.Method.GET, apiUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("tested");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject statewise = jsonArray.getJSONObject(i);
                        totalTests = statewise.getString("totalsamplestested");
                    }

                    for (int i = 0; i < jsonArray.length() - 1; i++) {
                        JSONObject statewise = jsonArray.getJSONObject(i);
                        oldTests = statewise.getString("totalsamplestested");
                    }
                    if (totalTests.isEmpty()) {
                        for (int i = 0; i < jsonArray.length() - 1; i++) {
                            JSONObject statewise = jsonArray.getJSONObject(i);
                            totalTests = statewise.getString("totalsamplestested");
                        }
                        totalTestsCopy = totalTests;
                        testsInt = Integer.parseInt(totalTests);
                        totalTests = NumberFormat.getInstance().format(testsInt);
                        textView_tests.setText(totalTests);


                        for (int i = 0; i < jsonArray.length() - 2; i++) {
                            JSONObject statewise = jsonArray.getJSONObject(i);
                            oldTests = statewise.getString("totalsamplestested");
                        }
                        int testsNew = (Integer.parseInt(totalTestsCopy)) - (Integer.parseInt(oldTests));
                        textView_tests_new.setText("[+" + NumberFormat.getInstance().format(testsNew) + "]");

                    } else {
                        totalTestsCopy = totalTests;
                        testsInt = Integer.parseInt(totalTests);
                        totalTests = NumberFormat.getInstance().format(testsInt);
                        textView_tests.setText(totalTests);

                        if (oldTests.isEmpty()) {
                            for (int i = 0; i < jsonArray.length() - 2; i++) {
                                JSONObject statewise = jsonArray.getJSONObject(i);
                                oldTests = statewise.getString("totalsamplestested");
                            }
                        }
                        long testsNew = (Integer.parseInt(totalTestsCopy)) - (Integer.parseInt(oldTests));
                        textView_tests_new.setText("+" + NumberFormat.getInstance().format(testsNew));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonObjectRequestTests);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void showProgressDialog() {
        progressDialog = new ProgressDialog(ProfileActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        Runnable progressRunnable = new Runnable() {

            @Override
            public void run() {
                if (confirmation != 1) {
                    progressDialog.cancel();
                    Toast.makeText(ProfileActivity.this, "Internet slow/not available", Toast.LENGTH_SHORT).show();
                }
            }
        };
        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 8000);
    }

    public void openStatewise(View view) {
        Intent intent = new Intent(this, StatewiseDataActivity.class);
        startActivity(intent);
    }

    public void openMoreInfo(View view) {
        Intent intent = new Intent(this, WorldDataActivity.class);
        startActivity(intent);
    }


}
