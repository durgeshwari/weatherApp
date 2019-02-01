package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {

    // Constants:
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "dc68a23a503c3c3ca4ca87415c7b3b91";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION_PROVIDER here:
    String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    // TODO: Declare a LocationManager and a LocationListener here:

    LocationManager mLocationManager;
    LocationListener mLocationListener;

    private int REQUEST_CODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton)findViewById(R.id.changeCityButton);


        // TODO: Add an OnClickListener to the changeCityButton here:

        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent=new Intent(
                        WeatherController.this,changeCity.class
                );
                startActivity(myIntent);
            }
        });
    }


    // TODO: Add onResume() here:
    protected void onResume() {
        super.onResume();

        getWeatherOfCurrentLocation();
        Intent myIntent =getIntent();
        String city =myIntent.getStringExtra("city");
        if(city!=null){
            getWeatherforNewCity(city);
        }
         else {
             Log.d("CLIMA","GETTING WEATHER FOR CURRENT LOCATION");
         getWeatherOfCurrentLocation();
        }
    }


    // TODO: Add getWeatherForNewCity(String city) here:

    public void getWeatherforNewCity(String city) {
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);
        letsDoSomeNetworking(params);
    }

    // TODO: Add getWeatherForCurrentLocation() here:
    private void getWeatherOfCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Log.d("CLIMA", "onLocationChanged ");
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());
                Log.d("CLIMA", "LONGITUDE IS " + longitude);
                Log.d("CLIMA", "LATITUDE IS " + latitude);

                RequestParams params = new RequestParams();
                params.put("lon",longitude);
                params.put("lat",latitude);
                params.put("appid",APP_ID);
                letsDoSomeNetworking(params);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d("CLIMA", "ON PROVIDER DISABLED");
            }
        };


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);

            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

        @Override
        public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("CLIMA", "PERMISSION GRANTED");
                    getWeatherOfCurrentLocation();
                } else Log.d("CLIMA", "PERMISSION DENIED");
            }
        }



    // TODO: Add letsDoSomeNetworking(RequestParams params) here
    private void letsDoSomeNetworking(RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("CLIMA", "SUCCESS" + response.toString());
                WeatherDataModel weather =WeatherDataModel.fromJSON(response);
                updateUI(weather);
            }

            public void onFaliure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                Log.d("CLIMA", "FAILURE" + e.toString());
                Log.d("CLIMA", "SUCCESS CODE" + statusCode);
                Toast.makeText(WeatherController.this, "REquest failed", Toast.LENGTH_SHORT).show();
            }

        });
    }

    // TODO: Add updateUI() here:
    private void updateUI(WeatherDataModel weather){
        mTemperatureLabel.setText(weather.getTemperature());
        mCityLabel.setText(weather.getCity());
        int resourceID=getResources().getIdentifier(weather.getIconName(),"drawable",getPackageName());
        mWeatherImage.setImageResource(resourceID);
    }


    // TODO: Add onPause() here:
@Override
protected void onPause(){
    super.onPause();
    if(mLocationManager!=null)
        mLocationManager.removeUpdates(mLocationListener);
}
}