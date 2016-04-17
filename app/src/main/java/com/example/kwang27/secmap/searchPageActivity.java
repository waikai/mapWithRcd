package com.example.kwang27.secmap;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import java.text.DecimalFormat;

public class searchPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_page);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        TextView business_TitleTv = (TextView)findViewById(R.id.business_Title);
        TextView dealTitleTv = (TextView)findViewById(R.id.dealTitle);
        TextView distTv = (TextView)findViewById(R.id.distTv);
        TextView dealInfoTv = (TextView)findViewById(R.id.dealInfo);
        TextView ratingTv = (TextView)findViewById(R.id.ratingTv);
        TextView dealLink = (TextView)findViewById(R.id.dealLink);
        dealLink.setMovementMethod(LinkMovementMethod.getInstance());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();

        String name = intent.getStringExtra("name");
        final String imageUrl = intent.getStringExtra("imageUrl");
        String dealTitle = intent.getStringExtra("dealTitle");
        String dealInfo = intent.getStringExtra("dealInfo");
        String rating = intent.getStringExtra("rating");

        double userLatitude = Double.parseDouble(intent.getStringExtra("userLatitude"));
        double userLongitude = Double.parseDouble(intent.getStringExtra("userLongitude"));
        double busLatitude = Double.parseDouble(intent.getStringExtra("busLatitude"));
        double busLongitude = Double.parseDouble(intent.getStringExtra("busLongitude"));

        double dist = distance(userLatitude, userLongitude, busLatitude, busLongitude, "M");
        DecimalFormat df2 = new DecimalFormat("#.0");
        distTv.setText("Distance: " + df2.format(dist) + "m");

        String[] spliter = dealInfo.split("<");

        business_TitleTv.setText(name);
        dealTitleTv.setText(dealTitle);
        dealInfoTv.setText(spliter[0]);

//        dealLink.setText(spliter[1]);
        dealLink.setText("YELP");
        ratingTv.setText("Rating: " + rating);

        final WebView business_ImageWv = (WebView)findViewById(R.id.business_Image);

        business_ImageWv.loadUrl(imageUrl);
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {

        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lon1);;
        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lon2);
        float myR = loc1.distanceTo(loc2);
        Log.i("my res:  ", myR+"");
        return myR;
    }


}
