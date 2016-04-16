package com.example.kwang27.secmap;

import android.graphics.Color;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{


    // init TAG
    private static final String TAG = "MainActivity";
    public static AmazonClientManager clientManager = null;


    private ArrayList<LocalInfo> mLocalInfos;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private MediaRecorder recorder;
    private File myFile;
    private int max;
    private int count;
    GoogleMap mMap;
    boolean mapReady=false;
    Button btnMap;
    Button btnse;
    Button btnRcd;
    // for marker
    MarkerOptions[] mp = new MarkerOptions[10];
    MarkerOptions m1;
    MarkerOptions m2;
    MarkerOptions m3;
    MarkerOptions m4;
    MarkerOptions m5;
    MarkerOptions m6;
    MarkerOptions m7;
    MarkerOptions m8;
    MarkerOptions m9;
    MarkerOptions m0;
    private double currLatitude;
    private double currLongitude;
//    private DynamoDBManager mDynamoDBManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                updata(2, 54);
            }
        });
        thread.start();
        mLocalInfos = new ArrayList<>();
        LocalInfo l0 = new LocalInfo(40.746723, -74.188729, 42, 0);
        mLocalInfos.add(l0);
        LocalInfo l1 = new LocalInfo(40.7440, -74.1574, 88, 1);
        mLocalInfos.add(l1);
        LocalInfo l2 = new LocalInfo(40.7350, -74.1574, 80, 2);
        mLocalInfos.add(l2);
        LocalInfo l3 = new LocalInfo(40.7536, -74.1550, 32, 3);
        mLocalInfos.add(l3);
        LocalInfo l4 = new LocalInfo(40.7536, -74.1632, 50, 4);
        mLocalInfos.add(l4);
        LocalInfo l5 = new LocalInfo(40.7556, -74.1511, 60, 5);
        mLocalInfos.add(l5);
        LocalInfo l6 = new LocalInfo(40.7566, -74.1524, 43, 6);
        mLocalInfos.add(l6);
        LocalInfo l7 = new LocalInfo(40.742723, -74.178729, 44, 7);
        mLocalInfos.add(l7);
        LocalInfo l8 = new LocalInfo(40.744723, -74.178729, 48, 8);
        mLocalInfos.add(l8);
        LocalInfo l9 = new LocalInfo(40.742723, -74.173729, 72, 9);
        mLocalInfos.add(l9);

        mp[1] = new MarkerOptions().position(new LatLng(l1.lat, l1.lng)).title(l1.maxDb.toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.q));
        m2 = new MarkerOptions().position(new LatLng(40.7350, -74.1574)).title("80dB");
        m3 = new MarkerOptions().position(new LatLng(40.7536, -74.1550)).title("42dB");
        m4 = new MarkerOptions().position(new LatLng(40.7536, -74.1632)).title("42dB").icon(BitmapDescriptorFactory.fromResource(R.drawable.q));
        m5 = new MarkerOptions().position(new LatLng(40.7556, -74.1511)).title("42dB");
        m6 = new MarkerOptions().position(new LatLng(40.7566, -74.1524)).title("42dB");
        m7 = new MarkerOptions().position(new LatLng(40.742723, -74.178729)).title("42dB");
        m8 = new MarkerOptions().position(new LatLng(40.744723, -74.178729)).title("42dB");
        m9 = new MarkerOptions().position(new LatLng(40.742723, -74.173729)).title("42dB");
        m0 = new MarkerOptions().position(new LatLng(40.746723, -74.188729)).title("42dB");
        // location
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();



        btnMap = (Button) findViewById(R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapReady){
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        });
        btnse = (Button)findViewById(R.id.btnSatellite);
        btnse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapReady){
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }
            }
        });
        btnRcd = (Button)findViewById(R.id.btnHybrid);
        View.OnLongClickListener rec = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {


                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    Toast.makeText(getApplicationContext(), "media file can find", Toast.LENGTH_LONG).show();
                    return true;
                }
                try{
                    Random rand = new Random();
                    int n = rand.nextInt(4000);
                    String s = "" + n;
                    myFile = File.createTempFile(rand.nextInt(4000)+"a",".amr", Environment.getExternalStorageDirectory());
                    Toast.makeText(getApplicationContext(), "Recording....", Toast.LENGTH_SHORT).show();
                    recorder = new MediaRecorder();
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                    recorder.setOutputFile(myFile.getAbsolutePath());
                    recorder.setMaxDuration(5000);
                    recorder.prepare();
                    max = 0;
                    count = 0;
                    recorder.start();

                    double db =  updateMicStatus();

                    Log.i("i can got value", max+"");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        };
        btnRcd.setOnLongClickListener(rec);

        View.OnTouchListener endRec = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Toast.makeText(getApplicationContext(), "Record End", Toast.LENGTH_SHORT).show();
                    Log.i("is the right value? : ", max/count +"");
                    if (myFile != null) {

                        Toast.makeText(getApplicationContext(), "Record Succ", Toast.LENGTH_LONG).show();
                        recorder.stop();
                        recorder.reset();
                        recorder.release();
                        recorder = null;
                    }
                    return true;
                }
                return false;
            }
        };
        btnRcd.setOnTouchListener(endRec);



        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private int getMyV(int i) {
        clientManager = new AmazonClientManager(this);
        DynamoDBManager manager = new DynamoDBManager();
        int v = manager.getUserPreference(i).getvoice();
        return v;
    }
    private void updata(int id, int newV) {
        clientManager = new AmazonClientManager(this);
        DynamoDBManager.insertUsers1(id, newV);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
    @Override
    public void onLocationChanged(Location location) {
//        Log.v("LOG_TAG", "Changed");
        currLatitude = location.getLatitude();
        currLongitude = location.getLongitude();
//        Log.v("LOG_la", currLatitude+"");
//        Log.v("LOG_lo", currLongitude+"");
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i("LOG_TAG", "suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("LOG_TAG", "Failed");

    }



    //map
    @Override
    public void onMapReady(GoogleMap map) {
        mapReady = true;
        mMap = map;
        mMap.setMyLocationEnabled(true);
        LatLng newYork = new LatLng(40.7536, -74.1550);
        CameraPosition target = CameraPosition.builder().target(newYork).zoom(15).tilt(45).build();
        mMap.addMarker(mp[1]);
        mMap.addCircle(new CircleOptions().center(new LatLng(40.7350, -74.1574)).radius(150).strokeColor(Color.RED).fillColor(Color.argb(61,255, 0 ,0)));
        mMap.addCircle(new CircleOptions().center(new LatLng(40.7536, -74.1550)).radius(150).strokeColor(Color.GREEN).fillColor(Color.argb(64, 0, 255,0)));
        mMap.addMarker(m2);
        mMap.addMarker(m3);
        mMap.addMarker(m4);
        mMap.addMarker(m5);
        mMap.addMarker(m6);
        mMap.addMarker(m7);
        mMap.addMarker(m8);
        mMap.addMarker(m9);
        mMap.addMarker(m0);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(target));
    }

    private int BASE = 1;
    private int SPACE = 100;

    private  double updateMicStatus() {
        if (recorder != null) {
            double ratio = (double)recorder.getMaxAmplitude()/BASE;
            double db = 0;
            if (ratio > 1) db = 20*Math.log10(ratio);
            max += (int) db;
            count++;
            Log.d("tag", "fvb" + max);
            mHander.postDelayed(mUpdateMicStatusTimer, SPACE);
            return db;
        }
        return 0;

    }
    private final android.os.Handler mHander = new android.os.Handler();
    private Runnable mUpdateMicStatusTimer = new Runnable() {
        @Override
        public void run() {
            updateMicStatus();
        }
    };


}
