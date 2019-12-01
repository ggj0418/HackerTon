package com.example.ggj04.hackerton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentHostCallback;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    public String seenValue1;

    static int counter = 0;

    private static final String TAG = "imagesearchexample";
    public static final int LOAD_SUCCESS = 101;

    private String SEARCH_URL = "http://203.250.148.89:7579/Mobius/DoctorDrone/sensor1/index1/latest";
    private String REQUEST_URL = SEARCH_URL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        //textView = (TextView) findViewById(R.id.textView);

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

        final Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getJsonClass th1 = new getJsonClass();
                th1.start();
            }
        });
    }

    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {

        final OverlayImage image = OverlayImage.fromResource(R.drawable.ic_fire);

        final Marker sejongMarker = new Marker();
        sejongMarker.setPosition(new LatLng(37.550504, 127.073845));
        //sejongMarker.setCaptionColor(color);
        // seenValue2 ="test";
        //sejongMarker.setCaptionText(seenValue1);

        sejongMarker.setOnClickListener(new Overlay.OnClickListener() {
            @Override
            public boolean onClick(@NonNull Overlay overlay) {

                if (Integer.parseInt(seenValue1) > 5) {
                    sejongMarker.setIcon(image);
                    return true;
                } else
                    sejongMarker.setIcon(Marker.DEFAULT_ICON);
                return true;
            }
        });

        CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(new LatLng(37.550504, 127.073845), 15);

        sejongMarker.setMap(naverMap);
        naverMap.moveCamera(cameraUpdate);
        naverMap.setMinZoom(5.0);
        naverMap.setMaxZoom(18.0);
    }

    private final MapActivity.MyHandler mHandler = new MapActivity.MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<MapActivity> weakReference;

        public MyHandler(MapActivity mapactivity) {
            weakReference = new WeakReference<MapActivity>(mapactivity);
        }

        @Override
        public void handleMessage(Message msg) {

            MapActivity mapactivity = weakReference.get();

            if (mapactivity != null) {

                switch (msg.what) {

                    case LOAD_SUCCESS:
                        //mapactivity.progressDialog.dismiss();

                        String jsonString = (String) msg.obj;

                        //mapactivity.textView.setText(jsonString);
                        mapactivity.seenValue1 = jsonString;

                        break;
                }
            }
        }
    }

    class getJsonClass extends Thread {

        @Override
        public void run() {
            String result;

            String preResult;

            try {

                Log.d(TAG, REQUEST_URL);
                URL url = new URL(REQUEST_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(10000);
                httpURLConnection.setConnectTimeout(10000);
                //httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestProperty("X-M2M-RI", "12345");
                // conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+json; ty=4");
                httpURLConnection.setRequestProperty("X-M2M-Origin", "Superman");
                httpURLConnection.connect();


                int responseStatusCode = httpURLConnection.getResponseCode();

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();
                httpURLConnection.disconnect();

                preResult = sb.toString().trim();

                String[] splitArray = preResult.split(":");
                String[] finalArray = splitArray[2].split("\\}");

                result = finalArray[0];

            } catch (Exception e) {
                result = e.toString();
            }
            Message message = mHandler.obtainMessage(LOAD_SUCCESS, result);
            mHandler.sendMessage(message);
        }
    }

}
