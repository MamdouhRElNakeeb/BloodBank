package com.bloodbank.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.provider.Settings;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mamdouhelnakeeb on 2/20/17.
 */


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener,
        LocationListener,
        PlaceSelectionListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        ClusterManager.OnClusterClickListener<MarkerObj>,
        ClusterManager.OnClusterInfoWindowClickListener<MarkerObj>,
        ClusterManager.OnClusterItemClickListener<MarkerObj>,
        ClusterManager.OnClusterItemInfoWindowClickListener<MarkerObj>{

    private static final String TAG = MapActivity.class.getSimpleName();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;

    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    LocationRequest mLocationRequest;

    private final int[] MAP_TYPES = { GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE };
    private int curMapTypeIndex = 1;

    Context context = this;
    private GoogleMap hospitalsMap;

    private ClusterManager<MarkerObj> mClusterManager;

    CoordinatorLayout mainCoordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        Toolbar toolbar= (Toolbar) findViewById(R.id.mytoolBar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Retrieve the PlaceAutocompleteFragment.
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(this);
        autocompleteFragment.setHasOptionsMenu(true);

        mainCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //move map camera
        hospitalsMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        hospitalsMap.animateCamera(CameraUpdateFactory.zoomTo(12f));
        syncHospitals(location.getLatitude(), location.getLongitude());

        if (mCurrentLocation != null){
            LatLng latLng1 = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            //move map camera
            hospitalsMap.moveCamera(CameraUpdateFactory.newLatLng(latLng1));
            hospitalsMap.animateCamera(CameraUpdateFactory.zoomTo(12f));
        }

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onPlaceSelected(Place place) {

        //Place current location
        LatLng latLng = place.getLatLng();

        //move map camera
        hospitalsMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        hospitalsMap.animateCamera(CameraUpdateFactory.zoomTo(12f));
        syncHospitals(place.getLatLng().latitude, place.getLatLng().longitude);

    }

    @Override
    public void onError(Status status) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        showLocationSettings(marker);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        hospitalsMap = googleMap;
        hospitalsMap.setMapType(MAP_TYPES[curMapTypeIndex]);
        initListeners();
        enableMyLocation();
        mClusterManager = new ClusterManager<MarkerObj>(this, hospitalsMap);
        mClusterManager.setRenderer(new MarkerRenderer());
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);
        hospitalsMap.setOnCameraIdleListener(mClusterManager);

        mClusterManager.cluster();

    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private class MarkerRenderer extends DefaultClusterRenderer<MarkerObj> {

        private final IconGenerator mIconGenerator = new CachedIconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new CachedIconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final TextView mClusterTextView;
        private final int mDimension;


        public MarkerRenderer() {
            super(getApplicationContext(), hospitalsMap, mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_hospitals, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);
            mClusterTextView = (TextView) multiProfile.findViewById(R.id.amu_text);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);

            mIconGenerator.setContentView(mImageView);

        }

        @Override
        protected void onBeforeClusterItemRendered(MarkerObj item, MarkerOptions markerOptions) {
            mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.hospital_marker))).title(item.getName());
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<MarkerObj> cluster, MarkerOptions markerOptions) {

            mClusterIconGenerator.setBackground(getResources().getDrawable(R.drawable.cluster_icon));
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<MarkerObj> cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }

    @Override
    public boolean onClusterClick(Cluster<MarkerObj> cluster) {
        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            hospitalsMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<MarkerObj> cluster) {

    }

    @Override
    public boolean onClusterItemClick(MarkerObj markerObj) {
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(MarkerObj markerObj) {

    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
            buildGoogleApiClient();
            hospitalsMap.setMyLocationEnabled(true);
        } else if (hospitalsMap != null) {
            // Access to the location has been granted to the app.
            buildGoogleApiClient();
            hospitalsMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void initListeners() {
        hospitalsMap.setOnMarkerClickListener(this);
        hospitalsMap.setOnInfoWindowClickListener(this);
        hospitalsMap.setTrafficEnabled(true);
        hospitalsMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void removeListeners() {
        if( hospitalsMap != null ) {
            hospitalsMap.setOnMarkerClickListener(null);
            hospitalsMap.setOnMapLongClickListener(null);
            hospitalsMap.setOnInfoWindowClickListener(null);
            hospitalsMap.setOnMapClickListener(null);
        }
    }


    private void syncHospitals(double latitude, double longitude){

        StringBuilder stringBuilder = new StringBuilder(AppConfig.URL_HOSPITALS)
        .append("location=").append(latitude).append(",").append(longitude)
        .append("&radius=").append(AppConfig.RADIUS)
        .append("&types=hospital")
        .append("&sensor=true")
        .append("&key=").append(AppConfig.MAPS_API_KEY);

        Log.d("strReq", stringBuilder.toString());

        JsonObjectRequest strReq = new JsonObjectRequest(stringBuilder.toString(), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Hospitals Response: " + response.toString());

                String id, place_id, placeName = null, reference, icon, vicinity = null;
                double latitude, longitude;

                try {
                    //hospitalsMap.clear();

                    // Extract JSON array from the response
                    JSONArray arr = response.getJSONArray("results");
                    System.out.println(arr.length());
                    // If no of array elements is not zero
                    if(arr.length() != 0){
                        // Loop through each array element, get JSON object
                        for (int i = 0; i < arr.length(); i++) {
                            // Get JSON object
                            JSONObject place = arr.getJSONObject(i);

                            id = place.getString("id");
                            place_id = place.getString("place_id");
                            if (!place.isNull("name")) {
                                placeName = place.getString("name");
                            }
                            if (!place.isNull("vicinity")) {
                                vicinity = place.getString("vicinity");
                            }
                            latitude = place.getJSONObject("geometry").getJSONObject("location")
                                    .getDouble("lat");
                            longitude = place.getJSONObject("geometry").getJSONObject("location")
                                    .getDouble("lng");
                            reference = place.getString("reference");
                            icon = place.getString("icon");

                            MarkerOptions markerOptions = new MarkerOptions();
                            LatLng latLng = new LatLng(latitude, longitude);
                            markerOptions.position(latLng);
                            markerOptions.title(placeName + " : " + vicinity);

                            //hospitalsMap.addMarker(markerOptions);
                            mClusterManager.addItem(new MarkerObj(id, placeName, vicinity, String.valueOf(latitude), String.valueOf(longitude)));


                        }
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Connection Error: " + error.getMessage());
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }

    private void showLocationSettings(Marker marker) {
        Snackbar snackbar = Snackbar
                .make(mainCoordinatorLayout, marker.getTitle(),
                        Snackbar.LENGTH_LONG)
                .setAction("set", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        snackbar.setActionTextColor(Color.RED);
        snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView
                .findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);

        snackbar.show();
    }

    private void toggleTraffic() {
        hospitalsMap.setTrafficEnabled(!hospitalsMap.isTrafficEnabled());
    }

    private void cycleMapType() {
        if (curMapTypeIndex < MAP_TYPES.length - 1 ) {
            curMapTypeIndex++;
        } else {
            curMapTypeIndex = 0;
        }

        hospitalsMap.setMapType(MAP_TYPES[curMapTypeIndex]);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear: {
                hospitalsMap.clear();
                return true;
            }

            case R.id.action_traffic: {
                toggleTraffic();
                return true;
            }
            case R.id.action_cycle_map_type: {
                cycleMapType();
                return true;
            }
            case R.id.refresh: {
                syncHospitals(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeListeners();
    }

    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

}
