package com.linkedin.ncabeen.arrivalalert;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

//TODO: update this based on the comments here - http://stackoverflow.com/questions/29712244/using-googleapiclient-in-a-service
//old school way to do this: http://stackoverflow.com/questions/33022662/android-locationmanager-vs-google-play-services
public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mLocationClient;

    private Location mCurrentLocation;
    LocationRequest mLocationRequest;
    private LatLng mDestination;

    public void onCreate(){
        super.onCreate();
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();

        buildGoogleApiClient();


        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-event-name"));
    }

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            //TODO: need a way to get the desination in the even the service is restarted...
            double[] destionationArray = intent.getDoubleArrayExtra("destination");
            mDestination = new LatLng(destionationArray[0],destionationArray[1]);
            Log.d("receiver", "Got message: " + mDestination.toString());
        }
    };

    public void onDestroy(){
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
        mLocationClient.disconnect();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service: onStartCommand", Toast.LENGTH_SHORT).show();

        mLocationClient.connect();

        double[] destionationArray = intent.getDoubleArrayExtra("destination");
        mDestination = new LatLng(destionationArray[0],destionationArray[1]);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //5 seconds
        mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setSmallestDisplacement(1F); //1 meter

        return Service.START_STICKY;
    }

    //copied from MapsActivity
    protected synchronized void buildGoogleApiClient() {
        //Toast.makeText(this,"buildGoogleApiClient",Toast.LENGTH_SHORT).show();
        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }



    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        Toast.makeText(this, mCurrentLocation.getLatitude() +", "+ mCurrentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
        if (mDestination != null) {
            checkArrival(location,mDestination);
        }

    }

    private void checkArrival(Location location, LatLng destination) {
        float[] distance = new float[1];
        Location.distanceBetween(destination.latitude, destination.longitude, location.getLatitude(), location.getLongitude(), distance);

        if (distance[0] < 400) {
            vibrate();
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub
        Toast.makeText(this, "Service: Connection failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub
        Toast.makeText(this, "Service: Connected", Toast.LENGTH_SHORT).show();
        //if(servicesConnected()) {
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);
        //}

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub
        Toast.makeText(this, "Service: Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean vibrate() {
        //TODO: revert behavior
        Toast.makeText(this,"Arriving at destination", Toast.LENGTH_SHORT).show();


        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Start without a delay
        // Each element then alternates between vibrate, sleep, vibrate, sleep...
        long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};

        // The '-1' here means to vibrate once, as '-1' is out of bounds in the pattern array
        //v.vibrate(pattern, -1);
        v.vibrate(1000);

        return true;
    }

 /*
 //Original service template
    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
*/
}
