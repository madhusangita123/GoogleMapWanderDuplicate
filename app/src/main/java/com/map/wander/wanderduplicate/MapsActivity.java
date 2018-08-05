package com.map.wander.wanderduplicate;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;

import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, OnStreetViewPanoramaReadyCallback {


    private final int Rquest_Location_Access =1;
    private final int zoom = 20;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mapFragment).commit();
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        setMapLongClick(googleMap);
        setPOIClick(googleMap);
        setMapStyle(googleMap);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Rquest_Location_Access);
            }else
                setMyCurrentLocation();
        }else{
            setMyCurrentLocation();
        }

        /**
         * Uncomment the below method stub to see the street view panorama of a POI of a city which has Street View Coverage
         */
        setInfoWindowClickToPanorama(googleMap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_options, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.normal_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.hybrid_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.satellite_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.terrain_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            case R.id.none_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                return true;
            default:
               return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case Rquest_Location_Access:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    setMyCurrentLocation();
                }else{
                    LatLng home = new LatLng(12.953527, 77.706843);
                    mMap.addMarker(new MarkerOptions().position(home).title("My Home"));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(home));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home, zoom));
                    /*GroundOverlayOptions homeOverlay = new GroundOverlayOptions()
                            .image(BitmapDescriptorFactory.fromResource(R.drawable.android))
                            .position(home,100);
                    mMap.addGroundOverlay(homeOverlay);*/
                }
                return;
        }
    }

    private void setMyCurrentLocation(){
        if(mMap != null){
            try {
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener(){
                    @Override
                    public void onMyLocationChange(Location location) {
                        LatLng home = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(home).title("It's Me!"));
                        /*GroundOverlayOptions homeOverlay = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.android))
                                .position(home,100);
                        mMap.addGroundOverlay(homeOverlay);*/
                    }
                });
            }catch (SecurityException se){
                se.printStackTrace();
            }
        }
    }

    private void setMapLongClick(final GoogleMap googleMap){
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                String snippet = String.format(Locale.getDefault(),"Lat: %1$.5f, Long: %2$.5f",latLng.latitude,latLng.longitude);
                googleMap.addMarker(new MarkerOptions().position(latLng)
                        .title(getString(R.string.dropped_pin))
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                );
            }
        });
    }

    private void setPOIClick(final GoogleMap googleMap){
        googleMap.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest pointOfInterest) {
                Marker poiMarker = googleMap.addMarker(new MarkerOptions().position(pointOfInterest.latLng).title(pointOfInterest.name));
                poiMarker.showInfoWindow();
                poiMarker.setTag("POI");
            }
        });

    }

    private void setMapStyle(GoogleMap googleMap){
        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MapsActivity.this, R.raw.mapstyles_json));
            if (!success)
                Log.e(getClass().getName(), "Style parsing failed.");
        }catch (Resources.NotFoundException re){
            Log.e(getClass().getName(), "Can't find style. Error: ", re);
        }
        LatLng home = new LatLng(12.953527, 77.706843);
        mMap.addMarker(new MarkerOptions().position(home).title("My Home"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(home));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home, zoom));
    }

    private void setInfoWindowClickToPanorama(GoogleMap googleMap){

        /*LatLng mountainView = new LatLng(37.386051, -122.083855);
        mMap.addMarker(new MarkerOptions().position(mountainView).title("Marker in mountainView"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mountainView));*/

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(marker.getTag() == "POI"){
                    StreetViewPanoramaOptions options = new StreetViewPanoramaOptions().position(marker.getPosition());
                    SupportStreetViewPanoramaFragment streetViewPanoramaFragment = SupportStreetViewPanoramaFragment.newInstance(options);
                    streetViewPanoramaFragment.getStreetViewPanoramaAsync(MapsActivity.this);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,streetViewPanoramaFragment)
                    .addToBackStack(getString(R.string.street_view_fragment_tag)).commit();
                }
            }
        });
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
        streetViewPanorama.setOnStreetViewPanoramaChangeListener(new StreetViewPanorama.OnStreetViewPanoramaChangeListener() {
            @Override
            public void onStreetViewPanoramaChange(StreetViewPanoramaLocation streetViewPanoramaLocation) {
                if(streetViewPanoramaLocation==null || streetViewPanoramaLocation.links==null){
                    getSupportFragmentManager().popBackStack(getString(R.string.street_view_fragment_tag), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    Toast.makeText(MapsActivity.this,getString(R.string.no_street_coverage),Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
