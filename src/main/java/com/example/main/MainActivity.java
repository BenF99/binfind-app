package com.example.mapboxtest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;




import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.matching.v5.MapboxMapMatching;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
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

// Classes needed to handle location permissions
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// Classes needed to add the location engine
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import java.lang.ref.WeakReference;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.api.directions.v5.DirectionsCriteria.ANNOTATION_SPEED;
import static com.mapbox.api.directions.v5.DirectionsCriteria.OVERVIEW_FULL;
import static com.mapbox.api.directions.v5.DirectionsCriteria.PROFILE_WALKING;
import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;


@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener, View.OnClickListener, MapboxMap.OnMapClickListener, MapboxMap.OnMarkerClickListener ,MapboxMap.OnInfoWindowClickListener{

    private MapView mapView;
    private MapboxMap mapboxMap;
    // Variables needed to handle location permissions
    private PermissionsManager permissionsManager;
    private FloatingActionButton currentLoc;
    private static final String TAG = "MainActivity";

    private final String GENERAL = "general_waste";
    private final String DOG = "dog_waste";
    private final String RECYCLING = "recycling";


    // Variables needed to add the location engine
    private LocationComponent locationComponent;
    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private MainActivityLocationCallback callback = new MainActivityLocationCallback(this);

    FloatingActionButton btn_add, menu_button;
    ExtendedFloatingActionButton btn_general_waste, btn_recycling, btn_dog_waste;
    ExtendedFloatingActionButton profile_button, leaderboard_button, info_button, help_button, about_button;
    Switch generalfilter, recyclingfilter,dogwastefilter;
    Float translationY = 100f;
    Boolean isMenuOpen = false;
    Boolean isMenuOpen2 = false;
    boolean addbins=false;
    boolean submitbutton=false;
    OvershootInterpolator interpolator = new OvershootInterpolator();
    //ImageView text_general_waste, text_recycling, text_dog_waste;

    private Marker destinationmarker,removemarker1;
    private Bitmap icon,bitmapgeneralwaste,bitmaprecycling,bitmapdogwaste,check,bit;
    public static Bitmap  userinputbitmap;
    private Button btn_submit;
    private String typesofbins="";
    private com.mapbox.mapboxsdk.annotations.Icon icongeneralwaste,iconrecycling,icondogwaste;
    private Point destination,origin;
    private DatabaseReference mDatabase;
    private Button startNav;

    private String uniqueID;
    private MapboxDirections client;

    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private DirectionsRoute currentRoute;
    AlertDialog alertDialog;
    boolean remove=false;
    public static final String EXTRA_LAT = "";
    public static final String EXTRA_LNG = "com.example.application.example.EXTRA_LNG";
    boolean enteraddimage=false;
    public static ArrayList<Double> latarraylist;
    public static ArrayList<Double> lngarraylist;
    public static ArrayList<Bitmap> bitmaplist;

    public static ArrayList<Data> allmarkers;
    public static ArrayList<String> nameofmarkers;
    public static double inputimagelat;
    public static double inputimagelong;
    public Uri imaguri;
    private NavigationMapRoute navigationMapRoute;
    public static final String IMAGESTORAGE="image/";

    boolean runfirstime=true;
    View v;
    String longitude="" ,latitude="",title="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gets unique hardware ID

        uniqueID = Settings.Secure.getString(MainActivity.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true); // Solves uncaught exception in FireBase runloop

        // Mapbox access token is configured here.

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        mDatabase = FirebaseDatabase.getInstance().getReference("binLocations");


        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        currentLoc = findViewById(R.id.getLoc);
        currentLoc.setOnClickListener(this);
        startNav = findViewById(R.id.startNav);
        startNav.setOnClickListener(this);

        latarraylist = new ArrayList<Double>();
        lngarraylist = new ArrayList<Double>();
        bitmaplist = new ArrayList<Bitmap>();
        allmarkers = new ArrayList<Data>();
        nameofmarkers = new ArrayList<String>();

        add_icon_bitmap();
        initFabMenu();
        initFabMenu2();

        //nav_button = findViewById(R.id.nav_button);
        //nav_button.setOnClickListener(this);

        profile_button = (ExtendedFloatingActionButton) findViewById(R.id.profile_button);
        leaderboard_button = (ExtendedFloatingActionButton) findViewById(R.id.leaderboard_button);
        info_button = (ExtendedFloatingActionButton) findViewById(R.id.info_button);
        help_button = (ExtendedFloatingActionButton) findViewById(R.id.help_button);
        about_button = (ExtendedFloatingActionButton) findViewById(R.id.about_button);

//        recyclingfilter = (Switch) findViewById(R.id.recyclingfilter);
//        dogwastefilter = (Switch) findViewById(R.id.dogwastefilter);


//        recyclingfilter.setOnClickListener(this);
//        dogwastefilter.setOnClickListener(this);
        profile_button.setOnClickListener(this);
        leaderboard_button.setOnClickListener(this);
        info_button.setOnClickListener(this);
        help_button.setOnClickListener(this);
        about_button.setOnClickListener(this);

    }

    /** Ben - 18/02/20
     * Listener for Marker interaction (Markers added by the user)
     */

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        destinationmarker = marker;
        if (remove == false) {

        /*marker.getPosition().getLongitude();
        marker.getPosition().getLongitude();*/
//            Toast.makeText(this, "MARKER SELECTED", Toast.LENGTH_SHORT).show();
//            setLine(marker.getPosition().getLongitude(), marker.getPosition().getLatitude());
//
            // Location lastKnownLocation = mapboxMap.getLocationComponent().getLastKnownLocation();

            return false;

        } else {
            removemarker1=destinationmarker;
            removemarker();
        }

        return true;
    }





    /**
     * Ben - 28/2/20
     * Executes getRoute with parameters generated from onClick markers or features
     */

    public void setLine(Double lng, Double lat) {
        Location lastKnownLocation = mapboxMap.getLocationComponent().getLastKnownLocation();
        Point origin = Point.fromLngLat(lastKnownLocation.getLongitude(), lastKnownLocation.getLatitude());
        Point destination = Point.fromLngLat(lng, lat);
        getRoute(origin, destination);

    }



    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        /** Ben - 18/02/20
         * Listener for Symbol interaction (GeoJSON Markers)
         */

        PointF screenPoint = mapboxMap.getProjection().toScreenLocation(point);
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, "leeds_l","york_l","shepparton_l");
        if (!features.isEmpty()) {
            // Log.i(TAG, "Point: " + point.getLatitude() + "," + point.getLongitude()); TESTING VARIALBE - Ben (18/2/20)
            // Feature selectedFeature = features.get(0);
            // String title = selectedFeature.getStringProperty("title");

            Toast.makeText(MainActivity.this, "SYMBOL SELECTED", Toast.LENGTH_SHORT).show();
        }

        if ( addbins==true) {
            addbins(point);
        }
        return false;
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.addOnMapClickListener(this);
        // Default mapbox style
        mapboxMap.setInfoWindowAdapter(new MapboxMap.InfoWindowAdapter() {
            @Nullable
            @Override
            public View getInfoWindow(@NonNull Marker marker) {
                longitude = Double.toString(marker.getPosition().getLongitude());
                latitude= Double.toString(marker.getPosition().getLatitude());
                check = marker.getIcon().getBitmap();
                if (check==bitmapgeneralwaste){
                    title="generalwaste bins";
                }
                if (check==bitmapdogwaste){
                    title="dogwaste bins ";
                }
                if (check==bitmaprecycling){
                    title="recycling bins ";
                }

                v=getLayoutInflater().inflate(R.layout.info_window,null);
                TextView types_of_bins=v.findViewById(R.id.types_of_bins);
                TextView Lat=v.findViewById(R.id.lat);
                TextView Lng=v.findViewById(R.id.Lng);
                TextView distance=v.findViewById(R.id.distance);
                ImageView imageview=v.findViewById(R.id.imageView);


                Double lat_ = destinationmarker.getPosition().getLatitude();
                Double long_ = destinationmarker.getPosition().getLongitude();


                if (enteraddimage==false) {
                }
                else{
                    userinputbitmap = checkarraylist(lat_, long_);
                    if (userinputbitmap != null) {
                        imageview.setImageBitmap(userinputbitmap);
                    }
                }

                types_of_bins.setText(title);
                Lat.setText("lat:"+latitude);
                Lng.setText("lng:"+longitude);

                float[] results = new float[1];
                Location.distanceBetween(destinationmarker.getPosition().getLatitude(), destinationmarker.getPosition().getLongitude(),
                        locationComponent.getLastKnownLocation().getLatitude(), locationComponent.getLastKnownLocation().getLongitude(),results);
                double distanceoutput=results[0];
                distance.setText("distance:"+distanceoutput);
                return v;
            }
        });
        mapboxMap.setStyle(Style.MAPBOX_STREETS,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        addDestinationIconSymbolLayer(style);
                        enableLocationComponent(style);
                        retrieveData();

                        initSource(style);
                        initLayers(style);

                        /**
                         * Ben - 6/2/20
                         * Calling populate map to create layer and source id for specified dataset
                         */


                        populateMap(style, GENERAL, "leeds_s", "leeds_l", "asset://street_bins_leeds.geojson"); // York
                        populateMap(style, DOG, "york_s", "york_l", "asset://dog_and_litter_york.geojson");
                        populateMap(style, GENERAL, "shepparton_s", "shepparton_l", "asset://australia_greater_shepparton.geojson");





                    }
                });


        mapboxMap.setMaxZoomPreference(17); // Ben - 6/2/20 | Setting max zoom
        mapboxMap.setOnMarkerClickListener(this);
        mapboxMap.setOnInfoWindowClickListener(this);




    }


    @Override
    public boolean onInfoWindowClick(@NonNull Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        View mView =getLayoutInflater().inflate(R.layout.button_menu,null);
        Button removebins= mView.findViewById(R.id.removebins);
        Button addimage= mView.findViewById(R.id.addimage);

        builder.setView(mView);

        alertDialog= builder.create();
        alertDialog.show();

        return true;
    }
    /**
     * Ben - 25/02/20 (Initializes source and layer for route to bin)
     */

    private void initLayers(@NonNull Style loadedMapStyle) {
        LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID);

// Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(5f),
                lineColor(Color.parseColor("#009688"))
        );
        loadedMapStyle.addLayer(routeLayer);

    }

    private void initSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(ROUTE_SOURCE_ID,
                FeatureCollection.fromFeatures(new Feature[] {})));

    }

    /**
     * Ben - 28/2/20
     * Creates a line path for two points and displays them on the map
     */


    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
// You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

// Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }


    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

    /**
     * Ben - 6/2/20
     * Method to create source and layer for GeoJSON data and populate map with markers.
     */

    public void populateMap(@NonNull Style loadedMapStyle, String type, String source_id, String layer_id, String asset_id) {

        // features[*].properties.BinType

        try {
            GeoJsonSource source = new GeoJsonSource(source_id, new URI(asset_id));

            loadedMapStyle.addSource(source);

            Bitmap icon;

            switch (type){
                case GENERAL:
                    icon = bitmapgeneralwaste;
                    break;
                case DOG:
                    icon = bitmapdogwaste;
                    break;
                case RECYCLING:
                    icon = bitmaprecycling;
                    break;
                default:
                    icon = bitmapgeneralwaste;
                    break;
            }


            loadedMapStyle.addImage(layer_id + " marker", icon);

            SymbolLayer symbolLayer = new SymbolLayer(layer_id, source_id);

            symbolLayer.setProperties(
                    iconImage(layer_id + " marker"),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true),
                    iconOffset(new Float[]{0f, -9f}) // Set bottom of marker to location
            );

            loadedMapStyle.addLayer(symbolLayer);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getLoc:
                Location lastKnownLocation = mapboxMap.getLocationComponent().getLastKnownLocation();

                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())) // Sets the new camera position
                        .zoom(15) // Sets the zoom
                        .bearing(0) // Rotate the camera
                        .tilt(0) // Set the camera tilt
                        .build();
                // Creates a CameraPosition from the builder

                // Animate Camera

                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2000);

                break;

            case R.id.btn_add:
                btn_submit.setVisibility(View.INVISIBLE);
                if (isMenuOpen) {
                    closeMenu();
                }
                else {
                    // Enables exapandable FAB
                    btn_general_waste.setEnabled(true);
                    btn_dog_waste.setEnabled(true);
                    btn_recycling.setEnabled(true);
                    openMenu();
                }
                break;
            case R.id.menu_button:
                btn_submit.setVisibility(View.INVISIBLE);
                if(isMenuOpen2){
                    closeMenu2();
                }
                else{
                    btn_submit.setVisibility(View.INVISIBLE);
                    profile_button.setEnabled(true);
                    leaderboard_button.setEnabled(true);
                    info_button.setEnabled(true);
                    help_button.setEnabled(true);
                    about_button.setEnabled(true);
                    openMenu2();
                }
                break;

            case R.id.btn_general_waste:
                locationComponent.setLocationComponentEnabled(false);
                closeMenu();
                typesofbins=GENERAL;
                startbutton(false);
                addbins(null);
                btn_submit.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_recycling:
                locationComponent.setLocationComponentEnabled(false);
                closeMenu();
                typesofbins=RECYCLING;
                startbutton(false);
                addbins(null);
                btn_submit.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_dog_waste:
                locationComponent.setLocationComponentEnabled(false);
                closeMenu();
                typesofbins=DOG;
                startbutton(false);
                addbins(null);
                btn_submit.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_submit:
                locationComponent.setLocationComponentEnabled(true);
                startbutton(true);
                break;

//<<<<<<< HEAD
            case R.id.profile_button:
                Intent gotoProfile = new Intent(this, ProfilePage.class);
                startActivity(gotoProfile);
                break;
            case R.id.leaderboard_button:
                Intent gotoLeaderboard = new Intent(this, LeaderboardPage.class);
                startActivity(gotoLeaderboard);
                break;
            case R.id.info_button:
                Intent gotoInformation = new Intent(this, InformationPage.class);
                startActivity(gotoInformation);
                break;
            case R.id.help_button:
                Intent gotoHelp = new Intent(this, HelpPage.class);
                startActivity(gotoHelp);
                break;
            case R.id.about_button:
                Intent gotoAbout = new Intent(this, AboutPage.class);
                startActivity(gotoAbout);
                break;



            case R.id.directions:
                Toast.makeText(this, "MARKER SELECTED", Toast.LENGTH_SHORT).show();
                setLine(destinationmarker.getPosition().getLongitude(), destinationmarker.getPosition().getLatitude());
//                Marker originalmarker = locationComponent.getLastKnownLocation();
//                getRoute(destinationmarker,originalmarker);
                alertDialog.cancel();
                destinationmarker.hideInfoWindow();
                startNav.setVisibility(mapView.VISIBLE);
                break;
            case R.id.removebins:
                removemarker1=destinationmarker;
                removemarker();
                alertDialog.cancel();
                break;


            case R.id.addimage:
                enteraddimage=true;
                userinputimage();
                alertDialog.cancel();
                destinationmarker.hideInfoWindow();
                break;


            case R.id.startNav:

                // CAUSES ERRORS - STILL TESTING...


                Log.i(TAG, "ROUTE " + currentRoute.toString());

                NavigationLauncherOptions options = NavigationLauncherOptions.builder()

                        .directionsRoute(currentRoute)
                        .shouldSimulateRoute(false)
                        .build();
                NavigationLauncher.startNavigation(MainActivity.this, options);
//>>>>>>> origin/master
                break;
            default:btn_submit.setVisibility(View.INVISIBLE);
        }
    }


    /** Ben - 18/2/20
     * Backend - Retrieving data from FireBase Realtime Database (Cloud Store) and adding markers
     */

    private void retrieveData() {

        FirebaseDatabase.getInstance().getReference().child("binLocations")
                .addValueEventListener(new ValueEventListener() {
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            Data data = snapshot.getValue(Data.class);

                            Icon icon_;
                            switch (data.getTypeOfBin()){
                                case GENERAL:
                                    icon_ = icongeneralwaste;
                                    break;
                                case DOG:
                                    icon_ = icondogwaste;
                                    break;
                                case RECYCLING:
                                    icon_ = iconrecycling;
                                    break;
                                default:
                                    icon_ = icongeneralwaste;
                                    break;
                            }
                            if (runfirstime==true){
                                mapboxMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(data.getLatitude(), data.getLongitude()))
                                        .icon(icon_));
                            }


                            allmarkers.add(data);
                            nameofmarkers.add(snapshot.getKey());
                        }
                        runfirstime=false;
                    }

                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }


    /**
     * Expandable FAB Methods
     */



    private void add_icon_bitmap() {
        bitmapgeneralwaste = BitmapFactory.decodeResource(getResources(), R.drawable.general_waste);
        icongeneralwaste= IconFactory.recreate("generalwaste", bitmapgeneralwaste);

        bitmaprecycling= BitmapFactory.decodeResource(getResources(),R.drawable.recycling);
        iconrecycling= IconFactory.recreate("recycling", bitmaprecycling);

        bitmapdogwaste = BitmapFactory.decodeResource(getResources(), R.drawable.dog_waste);
        icondogwaste= IconFactory.recreate("dogwaste", bitmapdogwaste);
    }

    private void initFabMenu(){

        btn_submit = findViewById(R.id.btn_submit);
        btn_add = findViewById(R.id.btn_add);
        btn_general_waste = findViewById(R.id.btn_general_waste);
        btn_recycling = findViewById(R.id.btn_recycling);
        btn_dog_waste = findViewById(R.id.btn_dog_waste);

        /*text_general_waste = findViewById(R.id.general_text);
        //text_recycling = findViewById(R.id.recycling_text);
        text_dog_waste = findViewById(R.id.dog_text);*/

        btn_general_waste.setAlpha(0f);
        btn_recycling.setAlpha(0f);
        btn_dog_waste.setAlpha(0f);

        /*text_general_waste.setAlpha(0f);
        text_recycling.setAlpha(0f);
        text_dog_waste.setAlpha(0f);*/

        btn_general_waste.setTranslationY(translationY);
        btn_recycling.setTranslationY(translationY);
        btn_dog_waste.setTranslationY(translationY);

        /*text_general_waste.setTranslationY(translationY);
        text_recycling.setTranslationY(translationY);
        text_dog_waste.setTranslationY(translationY);*/

        btn_general_waste.setOnClickListener(this);
        btn_recycling.setOnClickListener(this);
        btn_dog_waste.setOnClickListener(this);

        /*text_general_waste.setOnClickListener(this);
        text_recycling.setOnClickListener(this);
        text_dog_waste.setOnClickListener(this);*/

    }

    private void initFabMenu2(){

        menu_button = findViewById(R.id.menu_button);
        profile_button = findViewById(R.id.profile_button);
        leaderboard_button = findViewById(R.id.leaderboard_button);
        info_button = findViewById(R.id.info_button);
        help_button = findViewById(R.id.help_button);
        about_button = findViewById(R.id.about_button);

        profile_button.setOnClickListener(this);
        leaderboard_button.setOnClickListener(this);
        info_button.setOnClickListener(this);
        help_button.setOnClickListener(this);
        about_button.setOnClickListener(this);

        profile_button.setAlpha(0f);
        leaderboard_button.setAlpha(0f);
        info_button.setAlpha(0f);
        help_button.setAlpha(0f);
        about_button.setAlpha(0f);

        profile_button.setTranslationY(translationY);
        leaderboard_button.setTranslationY(translationY);
        info_button.setTranslationY(translationY);
        help_button.setTranslationY(translationY);
        about_button.setTranslationY(translationY);

    }

    private void openMenu(){
        isMenuOpen = !isMenuOpen;

        btn_add.animate().setInterpolator(interpolator).rotation(45f).setDuration(300).start();

        btn_general_waste.animate().translationY(0).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        btn_general_waste.setEnabled(true);
        btn_general_waste.setVisibility(mapView.VISIBLE);
        btn_recycling.animate().translationY(0).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        btn_recycling.setEnabled(true);
        btn_recycling.setVisibility(mapView.VISIBLE);
        btn_dog_waste.animate().translationY(0).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        btn_dog_waste.setEnabled(true);
        btn_dog_waste.setVisibility(mapView.VISIBLE);

        /*text_general_waste.animate().translationY(0).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        text_general_waste.setEnabled(true);
        text_recycling.animate().translationY(0).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        text_recycling.setEnabled(true);
        text_dog_waste.animate().translationY(0).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        text_dog_waste.setEnabled(true);*/

        if(addbins==true){
            mapboxMap.removeMarker(destinationmarker);
            startbutton(false);
        }
    }

    private void openMenu2(){
        isMenuOpen2 =!isMenuOpen2;

        menu_button.animate().setInterpolator(interpolator).rotation(90f).setDuration(300).start();

        profile_button.animate().translationY(0).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        profile_button.setEnabled(true);
        profile_button.setVisibility(mapView.VISIBLE);
        leaderboard_button.animate().translationY(0).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        leaderboard_button.setEnabled(true);
        leaderboard_button.setVisibility(mapView.VISIBLE);
        info_button.animate().translationY(0).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        info_button.setEnabled(true);
        info_button.setVisibility(mapView.VISIBLE);
        help_button.animate().translationY(0).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        help_button.setEnabled(true);
        help_button.setVisibility(mapView.VISIBLE);
        about_button.animate().translationY(0).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        about_button.setEnabled(true);
        about_button.setVisibility(mapView.VISIBLE);

    }

    private void closeMenu(){
        isMenuOpen = !isMenuOpen;

        btn_add.animate().setInterpolator(interpolator).rotation(0f).setDuration(300).start();

        btn_general_waste.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        btn_general_waste.setEnabled(false);
        btn_general_waste.setVisibility(mapView.GONE);
        btn_recycling.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        btn_recycling.setEnabled(false);
        btn_recycling.setVisibility(mapView.GONE);
        btn_dog_waste.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        btn_dog_waste.setEnabled(false);
        btn_dog_waste.setVisibility(mapView.GONE);

        /*text_general_waste.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        text_general_waste.setEnabled(false);
        text_recycling.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        text_recycling.setEnabled(false);
        text_dog_waste.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        text_dog_waste.setEnabled(false);*/
    }

    private void closeMenu2(){
        isMenuOpen2 = !isMenuOpen2;

        menu_button.animate().setInterpolator(interpolator).rotation(0f).setDuration(300).start();

        profile_button.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        profile_button.setEnabled(false);
        profile_button.setVisibility(mapView.GONE);
        leaderboard_button.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        leaderboard_button.setEnabled(false);
        leaderboard_button.setVisibility(mapView.GONE);
        info_button.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        info_button.setEnabled(false);
        info_button.setVisibility(mapView.GONE);
        help_button.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        help_button.setEnabled(false);
        help_button.setVisibility(mapView.GONE);
        about_button.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        about_button.setEnabled(false);
        about_button.setVisibility(mapView.GONE);
    }

    private void startbutton(Boolean Submitbutton) {
        Button marks = this.findViewById(R.id.btn_submit);
        if (Submitbutton==true){
            submitserver(destinationmarker);
            marks.setText("let's go");
            marks.setEnabled(false);
            marks.setTextColor(Color.BLACK);
            addbins=!addbins;
            destinationmarker=null;
            submitbutton=true;
            icon=null;
            typesofbins=null;
            Toast.makeText(this,"Bin Submitted", Toast.LENGTH_LONG).show();
            btn_submit.setVisibility(View.INVISIBLE);
        }
        /*else if( addbins==true)
        {
            marks.setText("");
            marks.setEnabled(false);
            marks.setTextColor(Color.WHITE);
            addbins=!addbins;
        }*/
        else
        {
            marks.setText("Add Bin");
            marks.setEnabled(true);
            marks.setTextColor(Color.BLACK);
            addbins=!addbins;
        }
    }

    private void addbins(LatLng point) {
        if (destinationmarker != null) {
            mapboxMap.removeMarker(destinationmarker);
        }

        LocationComponent locationComponent = mapboxMap.getLocationComponent();
        if(point==null){
            destinationmarker = mapboxMap.addMarker(new MarkerOptions().position(new LatLng(locationComponent.getLastKnownLocation())).title(typesofbins));
        }else{
            destinationmarker = mapboxMap.addMarker(new MarkerOptions().position(new LatLng(point)).title(typesofbins));
        }
        if(typesofbins==GENERAL){
            destinationmarker.setIcon(icongeneralwaste);
        }else if(typesofbins==RECYCLING){
            destinationmarker.setIcon(iconrecycling);
        }else if(typesofbins==DOG){
            destinationmarker.setIcon(icondogwaste);
        }
    }

    /** Ben - 17/2/20
     * Adding data to FireBase Realtime Database (Cloud Store)
     */
    private void submitserver(Marker destinationmarker) {


        double lat_ = destinationmarker.getPosition().getLatitude();
        double long_ = destinationmarker.getPosition().getLongitude();

        Data data = new Data(lat_,long_,uniqueID,typesofbins);
        mDatabase.push().setValue(data);

        allmarkers.clear();
        nameofmarkers.clear();


    }

    private void removemarker() {
        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setMessage(" remove the bins?").setCancelable(false)
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast toast=Toast. makeText(getApplicationContext(),"cancelled",Toast. LENGTH_SHORT);
                        toast.show();
                    }
                })
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String databaseremoveclassname=null;
                        String userid=null;
                        String output="";
                        for(int x=0;x<allmarkers.size();x++){
                            Data marker = allmarkers.get(x);
                            Double lat1 = removemarker1.getPosition().getLatitude();
                            Double long1 = removemarker1.getPosition().getLongitude();
                            Double lat2 = marker.getLatitude();
                            Double long2 = marker.getLongitude();
                            Double lat3 = lat1-lat2;
                            Double long3 = long1-long2;

                            if(long3==0.0){
                                if(lat3==0.0){
                                    databaseremoveclassname=nameofmarkers.get(x);
                                    userid=allmarkers.get(x).getUserId();

                                }
                            }
                        }
                        if(databaseremoveclassname!=null){
                            if(uniqueID.equals(userid)==true){
                                mDatabase.child(databaseremoveclassname).setValue(null);
                                mapboxMap.removeMarker(removemarker1);
                                output="you removed the marked successfully";
                            }else{
                                output="sorry";
                                outputremovebinsfail();
                            }
                        }
                        boolean test = uniqueID.equals(userid);




                        Toast toast=Toast. makeText(getApplicationContext(),output,Toast. LENGTH_SHORT);
                        toast.show();

                    }
                })
        ;
        AlertDialog alertDialog= builder.create();
        alertDialog.show();



    }

    private void outputremovebinsfail(){
        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setMessage("sorry, fail the remove the marker from server. you can only remove the marker you created").setCancelable(false)
                .setNegativeButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast toast=Toast. makeText(getApplicationContext(),"cancelled",Toast. LENGTH_SHORT);
                        toast.show();
                    }
                });
        AlertDialog alertDialog= builder.create();
        alertDialog.show();
    }

    /**
     * Initialize the Maps SDK's LocationComponent
     */
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            locationComponent = mapboxMap.getLocationComponent();

            // Set the LocationComponent activation options
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();

            // Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            if (mapboxMap.getStyle() != null) {
                enableLocationComponent(mapboxMap.getStyle());
            }
        } else {
            Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
            finish();
        }
    }



    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    private static class MainActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<MainActivity> activityWeakReference;

        MainActivityLocationCallback(MainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         **/

        @Override
        public void onSuccess(LocationEngineResult result) {
            MainActivity activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }

                // Create a Toast which displays the new location's coordinates
                /*Toast.makeText(activity, String.format(activity.getString(R.string.new_location),
                        String.valueOf(result.getLastLocation().getLatitude()), String.valueOf(result.getLastLocation().getLongitude())),
                        Toast.LENGTH_SHORT).show();*/

                // Pass the new location to the Maps SDK's LocationComponent
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can not be captured
         **/

        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d("LocationChangeActivity", exception.getLocalizedMessage());
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Required Methods
     **/

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevent leaks
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();

    }

    public static Bitmap checkarraylist(double lat, double lng ){

        if(latarraylist==null || lngarraylist==null|| bitmaplist==null){
            return null;
        }

        for(int x=0; x<latarraylist.size();x++){
            double getlat= latarraylist.get(x);
            double getlng= lngarraylist.get(x);
            if(getlat==lat && getlng==lng){
                Bitmap bitmap =bitmaplist.get(x);
                return bitmap;
            }
        }
        return null;
    }

    private  void userinputimage(){
        Intent intent = new Intent(this, uploadimagepage.class);
//        inputimagelat = destinationmarker.getPosition().getLatitude();
//        inputimagelong = destinationmarker.getPosition().getLongitude();
//        intent.putExtra(EXTRA_LAT,inputimagelat);
//        intent.putExtra(EXTRA_LNG,inputimagelong);
        startActivity(intent);

    }



    //testing for uploading bitmap to firebase
    //dont remove
    public  static void uploadimagedatabase(){
        String databaseremoveclassname=null;
        String userid=null;
        int num=0;
        for(int x=0;x<allmarkers.size();x++){
            Data marker = allmarkers.get(x);
            Double lat1 = inputimagelat;
            Double long1 = inputimagelong;
            Double lat2 = marker.getLatitude();
            Double long2 = marker.getLongitude();
            Double lat3 = lat1-lat2;
            Double long3 = long1-long2;
            if(long3==0.0){
                if(lat3==0.0){
                    databaseremoveclassname=nameofmarkers.get(x);
                    num=x;
                    break;
                }
            }
        }
        if(databaseremoveclassname!=null){
            Data data =allmarkers.get(num);
//            data.setBitmap(userinputbitmap);
//
//            StorageReference ref = mstorage.child(IMAGESTORAGE+System.currentTimeMillis() +"."+userinputbitmap);
//            mDatabase.child(databaseremoveclassname).child("URL").setValue(ref.getDownloadUrl().toString());


        }
    }

//
//    public void filters(String typesofbins, boolean checking) {
//        for (int x=1;x<allmarkers.size();x++){
//            if(allmarkers.get(x).getTypeOfBin()==typesofbins){
//                if(checking==false){
//
//                }
//            }
//
//        }
//    }










}
