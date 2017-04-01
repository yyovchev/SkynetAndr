package org.sliven.skynet.arrmarker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnCameraMoveListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        OnMapReadyCallback{

    Bitmap icons;

    private GoogleMap mMap;
    Hashtable<Integer, TwInfo> locs
            = new Hashtable<Integer, TwInfo>();

    public WebSocketClient mWebSocketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        icons = BitmapFactory.decodeResource(getResources(), R.drawable.spriteicons);

        connectWebSocket();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.i("GoogleMap","bearing : "+Float.toString(mMap.getCameraPosition().bearing));
    }

    @Override
    public void onCameraMove() {
        Log.i("GoogleMap","MOVE");
    }

    @Override
    public void onMapLongClick(LatLng point) {
        Log.i("GoogleMap","clicked");
    }

    private void connectWebSocket() {
        URI uri;
        try {
//            uri = new URI("ws://192.168.43.136:8001"); phone wifi
//            uri = new URI("ws://192.168.0.104:8001"); home wifi
            uri = new URI("ws://192.168.1.8:8001");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri, new Draft_17()) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getIcaos(message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
        Log.i("Websocket","connected");
    }

    public void getIcaos (String jsonArr) throws JSONException {
        locs.clear();

        JSONArray jArray = new JSONArray(jsonArr);
        for (int i=0; i < jArray.length(); i++)
        {
            try {
                JSONObject oneObject = jArray.getJSONObject(i);
                // Pulling items from the array

                int ICAO = oneObject.getInt("icao");
                double lat = oneObject.getDouble("lat");
                double lon = oneObject.getDouble("lon");
                int heading = oneObject.getInt("heading");
                int alt = oneObject.getInt("alt");
                String callsign = oneObject.getString("callsign");
                int speed = oneObject.getInt("speed");
                int vr_speed =oneObject.getInt("vr_speed");


                locs.put(ICAO,new TwInfo(new LatLng(lat, lon), heading, alt, callsign, speed, vr_speed));

            } catch (JSONException e) {
                // Oops
            }
        }
        printMarker();
    }

    public void printMarker (){
        mMap.clear();

        Set<Integer> keys = locs.keySet();
        for(Integer key: keys){

            double b_col  =  locs.get(key).heading/11.25;
            int col = (int)b_col;
            Bitmap icon = Bitmap.createBitmap(icons,col * 20 ,0,20,20);

            mMap.addMarker(new MarkerOptions()
                    .position(locs.get(key).latLng)
                    .title(Integer.toHexString(key).toUpperCase()+"\n"+locs.get(key).callsign)
                    .icon(BitmapDescriptorFactory
                    //        .fromBitmap(icon)
                            .fromResource(R.drawable.planeiconmini)
                    )
                    .rotation(locs.get(key).heading))
                    .showInfoWindow();
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.i("Marker","Clicked");
        return false;
    }
}
