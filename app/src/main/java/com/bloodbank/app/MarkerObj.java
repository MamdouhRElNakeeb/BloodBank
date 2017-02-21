package com.bloodbank.app;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Nakeeb PC on 8/2/2016.
 */
public class MarkerObj implements ClusterItem {

    private String id;
    private String name;
    private String address;
    private String latitude;
    private String longitude;

    private String phone;


    public MarkerObj() {
    }

    public MarkerObj(String id, String name, String address, String latitude, String longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the title
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the title to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the snippet
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the snippet to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the position
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the position to set
     */
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the position
     */
    public String getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the position to set
     */
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    /**
     * @return the position
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the position to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }


    @Override
    public LatLng getPosition() {
        return new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
    }
}