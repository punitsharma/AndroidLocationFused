package punit.com.fusedlocationandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Punit on 2/25/2015.
 */
public class MainActivity extends Activity implements View.OnClickListener, GoogleMap.OnMarkerDragListener {

    private static final String TAG = "MyActivity";
    Button btnFusedLocation, btnLocAddress;
    TextView tvLocation, locAddress;
    FusedLocationService fusedLocationService;
    Location location;
    String locationResult;
    private GoogleMap mMap;
    MarkerOptions markerOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        fusedLocationService = new FusedLocationService(this);

        setContentView(R.layout.activity_main);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        locAddress = (TextView) findViewById(R.id.tvLocationAddress);
        btnLocAddress = (Button) findViewById(R.id.btnGPSShowLocationAddress);
        btnFusedLocation = (Button) findViewById(R.id.btnGPSShowLocation);
        btnFusedLocation.setOnClickListener(this);
        btnLocAddress.setOnClickListener(this);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getLocationInBackground();
            }
        }, 5000);


    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }


    private void setUpMapIfNeeded() {
        if (mMap != null) {
            return;
        }
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapView)).getMap();
        mMap.setOnMarkerDragListener(this);
    }

    protected GoogleMap getMap() {
        setUpMapIfNeeded();
        return mMap;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGPSShowLocationAddress:
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LocationAddress locationAddress = new LocationAddress();
                    locationAddress.getAddressFromLocation(latitude, longitude,
                            getApplicationContext(), new GeocoderHandler());
                } else {
                    showSettingsAlert();
                }

                break;

            case R.id.btnGPSShowLocation:
                tvLocation.setText(locationResult);
                break;

            default:
                break;
        }
    }


    public void getLocationInBackground() {
        location = fusedLocationService.getLocation();
        locationResult = "";
        if (null != location) {
            Log.i(TAG, location.toString());
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            float accuracy = location.getAccuracy();
            double elapsedTimeSecs = (double) location.getElapsedRealtimeNanos()
                    / 1000000000.0;
            String provider = location.getProvider();
            double altitude = location.getAltitude();
            locationResult = "Latitude: " + latitude + "\n" +
                    "Longitude: " + longitude + "\n" +
                    "Altitude: " + altitude + "\n" +
                    "Accuracy: " + accuracy + "\n" +
                    "Elapsed Time: " + elapsedTimeSecs + " secs" + "\n" +
                    "Provider: " + provider + "\n";


            getMap().setMyLocationEnabled(true);
            CircleOptions circleOptions = new CircleOptions().center(new LatLng(location.getLatitude(), location.getLongitude())).radius(10000).strokeWidth(2).strokeColor(Color.TRANSPARENT)
                    .fillColor(Color.parseColor("#500084d3"));

            getMap().addCircle(circleOptions);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 11);
            getMap().animateCamera(cameraUpdate);

            markerOption = new MarkerOptions();
            mMap.addMarker(markerOption
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title("Location")
                    .draggable(true)).showInfoWindow();
        } else {
            locationResult = "Location Not Available!";
        }
    }


    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                MainActivity.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        MainActivity.this.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

     @Override
     public void onMarkerDragStart(Marker marker) {

     }

     @Override
     public void onMarkerDrag(Marker marker) {

     }

     @Override
     public void onMarkerDragEnd(Marker marker) {
         LatLng dragPosition = marker.getPosition();
         double dragLat = dragPosition.latitude;
         double dragLong = dragPosition.longitude;

         LocationAddress locationAddress = new LocationAddress();
         locationAddress.getAddressFromLocation(dragLat, dragLong,
                 getApplicationContext(), new GeocoderHandler());
         Toast.makeText(getApplicationContext(), "Address Changed !!", Toast.LENGTH_LONG).show();
     }

    class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            locAddress.setText(locationAddress);
        }
    }
}