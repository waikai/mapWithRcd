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
import java.util.Random;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{


    // init TAG
    private static final String TAG = "MainActivity";
    public static AmazonClientManager clientManager = null;

    private Boolean found = false;
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
    private Object lock;
    // for marker
    MarkerOptions[] mp = new MarkerOptions[10];

    private double currLatitude;
    private double currLongitude;
//    private DynamoDBManager mDynamoDBManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lock = new Object();

        // init location information
        mLocalInfos = new ArrayList<>();
        LocalInfo l0 = new LocalInfo(40.75368, -74.157532, 42, 0);
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

        Thread initthread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 1; i <=10; i++) {
                       int tmp =  getMyV(i);
                        Log.i("show me this", tmp + "");
                        mLocalInfos.get(i-1).maxDb = tmp;
                    }
                    for (int i=1; i<=10; i++) {
                        LocalInfo lf = mLocalInfos.get(i-1);
                        if (lf.maxDb == -1) {
                            mp[i - 1] = new MarkerOptions().position(new LatLng(lf.lat, lf.lng)).title("WE NEED YOU").icon(BitmapDescriptorFactory.fromResource(R.drawable.q));
                        }else {
                            mp[i - 1] = new MarkerOptions().position(new LatLng(lf.lat, lf.lng)).title(lf.maxDb+ " dB");
                        }
                    }
                    synchronized (lock){
                        found = true;
                        lock.notify();
                    }

                }catch (Exception e) {
                    Exception ne = e;
                    return;
                }
            }
        });
        initthread.start();



        // location
        for (int i=1; i<=10; i++) {
            LocalInfo lf = mLocalInfos.get(i-1);
            if (lf.maxDb == -1) {
                mp[i - 1] = new MarkerOptions().position(new LatLng(lf.lat, lf.lng)).title("WE NEED YOU").icon(BitmapDescriptorFactory.fromResource(R.drawable.q));
            }else {
                mp[i - 1] = new MarkerOptions().position(new LatLng(lf.lat, lf.lng)).title(lf.maxDb+ " dB");
            }
        }

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
                        final int lo = getCurrLo(currLatitude, currLongitude);
                        Log.i("here is the lo map ", lo+"");
                        final int currMax = max/count;
                        Thread upthread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                update(lo, currMax);
                            }
                        });
                        upthread.start();

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

    private int getCurrLo(double currLat, double currLng) {
        for (LocalInfo lf : mLocalInfos) {
            if ((int)(currLat*10000) == (int)(lf.lat*10000)){
                return lf.lab + 1;
            }
        }
        return 1;
    }


    private int getMyV(int i) {
        clientManager = new AmazonClientManager(this);
        DynamoDBManager manager = new DynamoDBManager();
        int v = manager.getUserPreference(i).getvoice();
        return v;
    }
    private void update(int id, int newV) {
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
        synchronized (lock){
            while (found == false){
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

            mapReady = true;
            mMap = map;
            mMap.setMyLocationEnabled(true);
            LatLng newYork = new LatLng(40.7536, -74.1550);
            CameraPosition target = CameraPosition.builder().target(newYork).zoom(15).tilt(45).build();

            for (int i = 1; i <= 10; i++) {
                LocalInfo lf = mLocalInfos.get(i - 1);
                int n = lf.maxDb;
                if (n >= 60) {
                    mMap.addMarker(mp[i - 1]);
                    mMap.addCircle(new CircleOptions().center(new LatLng(lf.lat, lf.lng)).radius(120).strokeColor(Color.RED).fillColor(Color.argb(61, 255, 0, 0)));
                } else if (n != -1) {
                    mMap.addMarker(mp[i - 1]);
                    mMap.addCircle(new CircleOptions().center(new LatLng(lf.lat, lf.lng)).radius(120).strokeColor(Color.GREEN).fillColor(Color.argb(64, 0, 255, 0)));
                } else {
                    mMap.addMarker(mp[i - 1]);
                    mMap.addCircle(new CircleOptions().center(new LatLng(lf.lat, lf.lng)).radius(120).strokeColor(Color.GRAY).fillColor(Color.argb(64, 128, 128, 128)));
                }
            }
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
