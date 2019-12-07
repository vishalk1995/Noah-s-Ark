package com.mobilecomputing.noahsark.ui.dashboard;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolLongClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mobilecomputing.noahsark.MainActivity;
import com.mobilecomputing.noahsark.NavigationActivity;
import com.mobilecomputing.noahsark.R;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class DashboardFragment extends Fragment implements MapboxMap.OnMapClickListener{

    private DashboardViewModel dashboardViewModel;
    private MapView mapView;
    private NavigationActivity activity;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private MapboxMap mapboxMap;


    private ImageView hoveringMarker;
    private Button selectLocationButton;
    private Layer droppedMarkerLayer;
    private CameraPosition camPos;
    private static final String DROPPED_MARKER_LAYER_ID = "location_marker";

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }
    private void requestPermission() {

        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "Permission Granted", Toast.LENGTH_SHORT).show();

                    // main logic
                } else {
                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermission();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (NavigationActivity) context;
    }
    private void initDroppedMarker(@NonNull Style loadedMapStyle) {
        // Add the marker image to map
        loadedMapStyle.addImage("dropped-icon-image", Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.safe_place), 100, 100, false));
        loadedMapStyle.addSource(new GeoJsonSource("dropped-marker-source-id"));
        loadedMapStyle.addLayer(new SymbolLayer(DROPPED_MARKER_LAYER_ID,
                "dropped-marker-source-id").withProperties(
                iconImage("dropped-icon-image"),
                visibility(NONE),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        ));
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel.class);
        final View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                Log.d("MAPBOX", "map initialized");
                DashboardFragment.this.mapboxMap = mapboxMap;
                mapboxMap.setStyle(Style.TRAFFIC_DAY, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull final Style style) {
                        // get permission if not present
                        if(!checkPermission()){
                            requestPermission();
                        }else{
                            enableLocationComponent(style);
                            addDangerZoneLayer(style);

                            addSafeZoneLayer(style);

                            if(camPos != null){
                                DashboardFragment.this.mapboxMap.setCameraPosition(camPos);
                            } else {
                                LocationComponent locationComponent = DashboardFragment.this.mapboxMap.getLocationComponent();
                                Location last_loc = locationComponent.getLastKnownLocation();
                                double latti = last_loc.getLatitude();
                                double longi = last_loc.getLongitude();

                                Log.d("LATLONG", latti+", "+longi);

                                CameraPosition position = new CameraPosition.Builder()
                                        .target(new LatLng(latti, longi))
                                        .zoom(15) // Sets the zoom
                                        .build(); // Creates a CameraPosition from the builder

                                DashboardFragment.this.mapboxMap.animateCamera(CameraUpdateFactory
                                        .newCameraPosition(position), 4000);

                                camPos = DashboardFragment.this.mapboxMap.getCameraPosition();
                                dashboardViewModel.savePositiion(camPos);
                            }



                            //set safe zone mark logic
                            final Button addSafeLocButton = view.findViewById(R.id.select_location_button);
                            addSafeLocButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    // Switch the button appearance back to select a location.
                                    addSafeLocButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                                    addSafeLocButton.setText("Select Location");

                                    // initialize marker
                                    hoveringMarker = new ImageView(getActivity());
                                    hoveringMarker.setImageResource(R.drawable.mapbox_marker_icon_default);
                                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
                                    hoveringMarker.setLayoutParams(params);
                                    mapView.addView(hoveringMarker);

                                    // Initialize, but don't show, a SymbolLayer for the marker icon which will represent a selected location.
                                    //initDroppedMarker(style);
                                    // Button for user to drop marker or to pick marker back up.
                                    selectLocationButton = getView().findViewById(R.id.select_location_button);
                                    selectLocationButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (hoveringMarker.getVisibility() == View.VISIBLE) {

                                                // Use the map target's coordinates to make a reverse geocoding search
                                                final LatLng mapTargetLatLng = mapboxMap.getCameraPosition().target;

                                                // Hide the hovering red hovering ImageView marker
                                                hoveringMarker.setVisibility(View.INVISIBLE);

                                                // Transform the appearance of the button to become the cancel button
                                                selectLocationButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                                                selectLocationButton.setText("Cancel Select");

                                                // Show the SymbolLayer icon to represent the selected map location
                                                if (style.getLayer(DROPPED_MARKER_LAYER_ID) != null) {
                                                    GeoJsonSource source = style.getSourceAs("dropped-marker-source-id");
                                                    if (source != null) {
                                                        source.setGeoJson(Point.fromLngLat(mapTargetLatLng.getLongitude(), mapTargetLatLng.getLatitude()));
                                                        // save to database

                                                    }
                                                    droppedMarkerLayer = style.getLayer(DROPPED_MARKER_LAYER_ID);
                                                    if (droppedMarkerLayer != null) {
                                                        droppedMarkerLayer.setProperties(visibility(VISIBLE));
                                                    }
                                                }

                                                // Use the map camera target's coordinates to make a reverse geocoding search
                                                // reverseGeocode(Point.fromLngLat(mapTargetLatLng.getLongitude(), mapTargetLatLng.getLatitude()));

                                            } else {

                                                // Switch the button appearance back to select a location.
                                                selectLocationButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                                                selectLocationButton.setText("Select Location");

                                                // Show the red hovering ImageView marker
                                                hoveringMarker.setVisibility(View.VISIBLE);

                                                // Hide the selected location SymbolLayer
                                                droppedMarkerLayer = style.getLayer(DROPPED_MARKER_LAYER_ID);
                                                if (droppedMarkerLayer != null) {
                                                    droppedMarkerLayer.setProperties(visibility(NONE));
                                                }
                                            }
                                        }
                                    });
                                }

                            });
                        }
                    }
                });
            }
        });


        return view;
    }

    private void addSafeZoneLayer(Style style){
        initDroppedMarker(style);
        List<String> dummySafeZones = new ArrayList<String>();
        dummySafeZones.add("33.409460#-111.917743#Murietta Open Space");
//        dummySafeZones.add("33.416458#-111.932106#SDFC Field");
        dummySafeZones.add("33.423703#-111.922631#passport office");
        dummySafeZones.add("33.419710#-111.913364#Creamery park");
        // create symbol manager object
        SymbolManager symbolManager = new SymbolManager(mapView, mapboxMap, style);
        // add click listeners if desired
        symbolManager.addClickListener(new OnSymbolClickListener() {
            @Override
            public void onAnnotationClick(Symbol symbol) {
                Log.d("CLICK", "symbol clicked");
                Toast.makeText(getActivity(), "Long press to start navigation", Toast.LENGTH_SHORT).show();
            }
        });
        symbolManager.addLongClickListener(new OnSymbolLongClickListener() {
            @Override
            public void onAnnotationLongClick(Symbol symbol) {
                LatLng loc = symbol.getLatLng();
                String pos1 = String.valueOf(loc.getLatitude());
                String pos2 = String.valueOf(loc.getLongitude());
                Log.d("CLICK", pos1+pos2);
                startNavigation(pos1, pos2);
            }
        });
        for(int i=0; i<dummySafeZones.size(); i++){

            String latlong = dummySafeZones.get(i);
            Log.d("MARKER", "adding marker at "+latlong);
            double latti = Double.parseDouble(latlong.split("#")[0]);
            double longi = Double.parseDouble(latlong.split("#")[1]);
            String title = latlong.split("#")[2];

            // set non-data-driven properties, such as:
            Symbol symbol = symbolManager.create(new SymbolOptions()
                    .withLatLng(new LatLng(latti, longi))
                    .withIconImage("dropped-icon-image")
                    .withIconSize(1.0f));

            symbolManager.setIconAllowOverlap(true);
        }
    }

    private void startNavigation(String latti, String longi){
        Uri gmmIntentUri = Uri.parse("google.navigation:q="+latti+","+longi);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
    private void addDangerZoneLayer(Style style) {
        // sample way to add danger zone
        // TODO: load danger zones from database and display dynamically
        List<List<Point>> POINTS = new ArrayList<>();
        List<Point> OUTER_POINTS = new ArrayList<>();
        GeoJsonSource unsafeAreasSource = null;
        OUTER_POINTS.add(Point.fromLngLat( -111.937152,33.418034));
        OUTER_POINTS.add(Point.fromLngLat( -111.942839, 33.416299));
        OUTER_POINTS.add(Point.fromLngLat(-111.943622, 33.411866 ));
        OUTER_POINTS.add(Point.fromLngLat(-111.938075, 33.408552 ));
        OUTER_POINTS.add(Point.fromLngLat(-111.926531, 33.407495 ));
        OUTER_POINTS.add(Point.fromLngLat(-111.920941, 33.412967 ));
        OUTER_POINTS.add(Point.fromLngLat(-111.926544, 33.418350 ));
        POINTS.add(OUTER_POINTS);
        unsafeAreasSource = new GeoJsonSource("urban-areas", Polygon.fromLngLats(POINTS));
        style.addSource(unsafeAreasSource);
        FillLayer urbanArea = new FillLayer("urban-areas-fill", "urban-areas");
        urbanArea.setProperties(
                fillColor(Color.parseColor("#ff0000")),
                fillOpacity(0.4f)
        );
        style.addLayerBelow(urbanArea, "water");
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(Style style) {
        LocationComponent locationComponent = this.mapboxMap.getLocationComponent();
        locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(getActivity(), style).build());
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING_COMPASS);
        locationComponent.setRenderMode(RenderMode.COMPASS);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        this.camPos = mapboxMap.getCameraPosition();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        PointF pointf = mapboxMap.getProjection().toScreenLocation(point);
        RectF rectF = new RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10);
        Toast.makeText(getActivity(), "Click on map", Toast.LENGTH_SHORT).show();
        return true;
    }

}
