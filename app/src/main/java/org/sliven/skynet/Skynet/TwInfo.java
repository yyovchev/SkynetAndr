package org.sliven.skynet.arrmarker;

import com.google.android.gms.maps.model.LatLng;

/**
 * 
 */

public class TwInfo {
    public LatLng latLng;
    public int heading;
    public String callsign;
    public int speed;
    public int vr_speed;
    public int alt;


    TwInfo(LatLng l, int heading, int alt, String callsign, int speed, int vr_speed){
        this.latLng = l;
        this.heading = heading;
        this.callsign = callsign;
        this.speed = speed;
        this.vr_speed = vr_speed;
        this.alt = alt;
    }
}
