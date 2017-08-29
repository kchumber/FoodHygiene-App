package com.kallumchumber.foodhygiene_14024343;



import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;


public class location_search extends AppCompatActivity {

    private double lat;
    private double lon;
    private String latitude;
    private String longitude;
    private String id;
    private String BusinessName;
    private String AddressLine1;
    private String AddressLine2;
    private String AddressLine3;
    private String PostCode;
    private String RatingValue;
    private String Distance;
    private String responseBody;
    private TableLayout table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_search);

        // Set the three navigation buttons
        Button btn1 = (Button) findViewById(R.id.location);
        if (btn1 != null) {
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(location_search.this, location_search.class));
                }
            });
        }



        // Location Listener
        Context context = this.getApplicationContext();
        LocationManager locMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        try {
            locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    // Get the values from the GPS and call the other program methods
                    lat = location.getLatitude();
                    lon = location.getLongitude();

                    // Convert the latitude and longitude values to String
                    latitude = String.valueOf(lat);
                    longitude = String.valueOf(lon);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            });
        } catch (SecurityException se) {
            se.printStackTrace();
        }

    }


    /*
     Method called when Find Me button clicked
     takes the GPS info and starts the get_locations method.
     */
    public void update_location(View v) {
        // Show results for query
        get_locations();
    }


    private void get_locations() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ConnectivityManager connMngr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMngr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            HttpURLConnection urlConnection = null;

            String address = "http://sandbox.kriswelsh.com/hygieneapi/hygiene.php?op=s_loc&lat=" + latitude + "&long=" + longitude;
            try {
                URL url = new URL(address);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStreamReader ins = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader in = new BufferedReader(ins);
                String line;
                responseBody = "";
                while ((line = in.readLine()) != null) {
                    responseBody = responseBody + line;
                }
                ins.close();
                in.close();

                parseJSON(responseBody);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                assert urlConnection != null;
                urlConnection.disconnect();
            }
        }
    }



    private void parseJSON(String responseBody) {
        try {

            // server response JSON
            JSONArray data = new JSONArray(responseBody);

            // Table where data will be displayed, first remove previous data
            table = (TableLayout) findViewById(R.id.table_location);
            if (table != null) {
                table.removeAllViews();
            }

            for (int i = 0; i < data.length(); i++) {
                id = data.getJSONObject(i).getString("id");
                BusinessName = data.getJSONObject(i).getString("BusinessName");
                if (BusinessName.length() > 25) {
                    BusinessName = BusinessName.substring(0, 25) + "...";
                }
                AddressLine1 = data.getJSONObject(i).getString("AddressLine1");
                AddressLine2 = data.getJSONObject(i).getString("AddressLine2");
                AddressLine3 = data.getJSONObject(i).getString("AddressLine3");

                // If the address field is too long, it is shortened
                if (AddressLine1.length() > 40) {
                    AddressLine1 = AddressLine1.substring(0, 25) + "...";
                }
                PostCode = data.getJSONObject(i).getString("PostCode");
                String Rating = data.getJSONObject(i).getString("RatingValue");
                if (Rating.equals("-1")) {
                    Rating = "exempt";
                }
                RatingValue = "rating " + Rating ;
                Distance = data.getJSONObject(i).getString("DistanceKM");
                if (Float.parseFloat(Distance) < 0.1) {
                    Distance = "< 0.1";
                } else {
                    Distance = round(Distance);
                }

                // Insert rows in table one by one
                createTable();
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }



    // add locations to table rows and the rows to the table

    private void createTable() {

        // Create row in table for Business Name
        TableRow tr1 = new TableRow(this);
        tr1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        // Insert the Business Name into the table row
        TextView name = new TextView(this);
        name.setText(BusinessName);
        tr1.addView(name);
        // Insert the rating value into the table row
        View rating = new View(this);
            TextView Rating = new TextView(this);
            Rating.setText(RatingValue);
            tr1.addView(Rating);


        tr1.addView(rating);
        tr1.setClickable(true);
        tr1.setTag(id);
        tr1.setOnClickListener(tableRowOnClickListener);

        //address line 1
        TableRow tr2 = new TableRow(this);
        tr2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        // Insert address line 1 and postcode into table row
        TextView address1 = new TextView(this);
        address1.setText(AddressLine1);
        tr2.addView(address1);

        //distance from location
        TextView distance = new TextView(this);
        String distnce = "dist: " + Distance + "km";
        distance.setText(distnce);
        distance.setPadding(30, 0, 0, 0);
        tr2.addView(distance);
        tr2.setClickable(true);
        tr2.setTag(id);
        tr2.setOnClickListener(tableRowOnClickListener);

        // address line 2
        TableRow tr3 = new TableRow(this);
        tr3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        // Insert address line 2
        TextView address2 = new TextView(this);
        address2.setText(AddressLine2);
        tr3.addView(address2);

        // Add the rows to table
        table.addView(tr1);
        table.addView(tr2);
        table.addView(tr3);

        // Only add address line 3 if it exists
        if (AddressLine3 != null && !AddressLine3.isEmpty()) {
            TableRow tr4 = new TableRow(this);
            tr4.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
            TextView address3 = new TextView(this);
            address3.setText(AddressLine3);
            tr4.addView(address3);
            table.addView(tr4);
        }

        // Create a row in the table for the postcode
        TableRow tr5 = new TableRow(this);
        tr5.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        TextView postcode = new TextView(this);
        postcode.setText(PostCode);
        tr5.addView(postcode);
        tr5.setPadding(0,0,0,30);
        table.addView(tr5);


        table.setPadding(30, 0, 0, 40);
    }


    //open maps activity when clicked in table

    private View.OnClickListener tableRowOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(location_search.this, MapsActivity.class);

            // Put the JSON array as a string into the intent
            intent.putExtra("JSON", responseBody);

            startActivity(intent);
        }
    };



    private static String round(String value) {
        double doubleValue = Double.parseDouble(value);
        BigDecimal bd = new BigDecimal(doubleValue);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return String.valueOf(bd.doubleValue());
    }
}