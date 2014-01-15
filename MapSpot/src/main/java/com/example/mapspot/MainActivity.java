package com.example.mapspot;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.OpenGraphAction;
import com.facebook.model.OpenGraphObject;
import com.facebook.widget.FacebookDialog;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends FragmentActivity implements GoogleMap.OnMapClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerClickListener,
        DirectionsOptionsFragment.OnFragmentInteractionListener,
        MarkerDetailsDialogFragment.MarkerDialogListener {
    // Global constants
    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static String APPTAG = "MapSpot";
    private GoogleMap map;
    private LocationClient locationClient;
    private LocationClientHandler locationHandler;
    private DatabaseHandler db;
    private Menu actionMenu;
    private boolean isMarkerMenuOn = false;
    private HashMap<String, Long> markerMap = new HashMap<>();
    private Marker selectedMarker;
    private UiLifecycleHelper uiHelper;
    private boolean selectDirectionsMarkerState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getActionBar().setDisplayShowTitleEnabled(false);

        // Start up database connection
        db = new DatabaseHandler(this);
        db.open();

        // TODO: check for google play services
        // Get a handle to the Map Fragment
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        // TODO: ASyncTask
        List<MapMarker> markers = db.getAllMarkers();
        for (MapMarker marker : markers) {
            addMarkerToMap(marker);
        }

        // Set onclick events for the info balloons
        map.setOnInfoWindowClickListener(this);

        /*
         * Create a location handler class instance and assign it to
         * our location client.
         */
        locationHandler = new LocationClientHandler(this, map);
        locationClient = locationHandler.getLocationClient();

        // Facebook handler
        uiHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                onSessionStateChanged(session, state, exception);
            }
        });
        uiHelper.onCreate(savedInstanceState);
    }

    /**
     * Adds a Google Maps marker on the map. The title and description of the marker
     * make the info window text, and the category of the marker decides its color.
     * Also adds the new Google Maps marker's unique map id to a HashMap, associating
     * it with the marker's database id.
     *
     * @param mapMarker A MapSpot MapMarker object containing the marker details.
     * @return The newly created Google Maps Marker object.
     */
    private Marker addMarkerToMap(MapMarker mapMarker) {
        Marker marker;
        switch (mapMarker.getCategory()) {
            case "custom location":
                marker = map.addMarker(new MarkerOptions()
                        .position(mapMarker.getPosition())
                        .title(mapMarker.getTitle())
                        .snippet(mapMarker.getDescription())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                break;
            case "recreation":
                marker = map.addMarker(new MarkerOptions()
                        .position(mapMarker.getPosition())
                        .title(mapMarker.getTitle())
                        .snippet(mapMarker.getDescription())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                break;
            case "gas station":
                marker = map.addMarker(new MarkerOptions()
                        .position(mapMarker.getPosition())
                        .title(mapMarker.getTitle())
                        .snippet(mapMarker.getDescription())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                break;
            case "food and drinks":
                marker = map.addMarker(new MarkerOptions()
                        .position(mapMarker.getPosition())
                        .title(mapMarker.getTitle())
                        .snippet(mapMarker.getDescription())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                break;
            case "supermarket":
                marker = map.addMarker(new MarkerOptions()
                        .position(mapMarker.getPosition())
                        .title(mapMarker.getTitle())
                        .snippet(mapMarker.getDescription())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                break;
            default:
                marker = map.addMarker(new MarkerOptions()
                        .position(mapMarker.getPosition())
                        .title(mapMarker.getTitle())
                        .snippet(mapMarker.getDescription()));
                break;
        }

        markerMap.put(marker.getId(), mapMarker.getDbID());

        return marker;
    }

    /**
     * Edits an existing Google Maps marker's details and redraws the marker.
     * It does NOT actually edit a marker, since that is not possible, but
     * recreates it using the new parameters.
     *
     * @param mapMarker A MapSpot MapMarker object containing the marker details.
     * @param marker    A Google Maps Marker object describing the map to be edited.
     * @return The new handle to the marker. The old marker's behaviour is undefined.
     */
    private Marker editMarkerOnMap(MapMarker mapMarker, Marker marker) {
        markerMap.remove(marker.getId());
        marker.remove();

        marker = addMarkerToMap(mapMarker);

        return marker;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        locationClient.connect();
        db.open();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        locationClient.disconnect();
        db.close();
        super.onStop();
    }

    @Override
    protected void onPause() {
        // Disconnect the client
        locationClient.disconnect();
        db.close();
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Connect the client.
        locationClient.connect();
        db.open();
        uiHelper.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        actionMenu = menu;
        return true;
    }

    /**
     * On click listener action function for the new point button
     *
     * @param item The button clicked.
     */
    public void addNewMarker(MenuItem item) {
        map.setOnMapClickListener(this);
    }

    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK:
                    /*
                     * Try the request again
                     */
                        break;
                }
        }

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("Activity", String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("Activity", "Success!");
            }
        });
    }

    private void onSessionStateChanged(Session session, SessionState state, Exception exception) {
        Log.d(APPTAG, "RESPONSE");
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (isMarkerMenuOn) {
            // This was called from an info window dismissal.
            // Change action menu to original and do nothing else.
            removeMarkerMenu();
        } else {
            map.setOnMapClickListener(null);

            MarkerDetailsDialogFragment dialogFragment = new MarkerDetailsDialogFragment();

            Bundle bundle = new Bundle();
            bundle.putParcelable("location", latLng);
            dialogFragment.setArguments(bundle);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            dialogFragment.show(ft, "dialog");
        }
    }

    @Override
    public void onFinishMarkerDialog(MapMarker mapMarker) {
        // TODO: ASyncTask
        if (selectedMarker == null) {
            // Marker is first created
            MapMarker newMarker = db.createMarker(mapMarker.getTitle(), mapMarker.getDescription(), mapMarker.getCategory(), mapMarker.getPosition().latitude, mapMarker.getPosition().longitude);
            addMarkerToMap(newMarker);
        } else {
            // Marker is edited, not created
            db.editMarker(mapMarker.getTitle(), mapMarker.getDescription(), mapMarker.getCategory(), markerMap.get(selectedMarker.getId()));
            MapMarker editedMarker = db.getMarkerByID(markerMap.get(selectedMarker.getId()));
            editMarkerOnMap(editedMarker, selectedMarker);
            removeMarkerMenu();
        }
        Toast.makeText(this, getResources().getText(R.string.successful_save), Toast.LENGTH_SHORT);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        selectedMarker = marker;

        actionMenu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.marker_actions, actionMenu);

        // Turn on map click listener for info window cancel dismissal
        map.setOnMapClickListener(this);
        isMarkerMenuOn = true;
    }

    /**
     * Deletes the selected marker from the database, and removes it from the map.
     *
     * @param item The button clicked.
     */
    public void deleteMarker(MenuItem item) {
        if (selectedMarker != null) {
            int result = db.deleteMarker(markerMap.get(selectedMarker.getId()));
            if (result > 0) {
                selectedMarker.remove();
                Toast.makeText(this, getResources().getText(R.string.successful_delete), Toast.LENGTH_SHORT);
            }
            selectedMarker = null;
        }
        removeMarkerMenu();
    }

    /**
     * Displays a dialog for the user to edit the selected marker.
     *
     * @param item The button clicked.
     */
    public void editMarker(MenuItem item) {
        MapMarker mapMarker = db.getMarkerByID(markerMap.get(selectedMarker.getId()));

        MarkerDetailsDialogFragment dialogFragment = new MarkerDetailsDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable("location", selectedMarker.getPosition());
        bundle.putString("title", mapMarker.getTitle());
        bundle.putString("description", mapMarker.getDescription());
        bundle.putString("category", mapMarker.getCategory());
        dialogFragment.setArguments(bundle);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        dialogFragment.show(ft, "dialog");
    }

    /**
     * Shows a new activity with a menu about directions options.
     *
     * @param item The button clicked.
     */
    public void getDirections(MenuItem item) {
        Location currentLocation = locationClient.getLastLocation();
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        DirectionsOptionsFragment dialogFragment = DirectionsOptionsFragment.newInstance(latLng);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        dialogFragment.show(ft, "dialog");

        removeMarkerMenu();
    }

    /**
     * Shares the selected marker to Facebook using Facebook Share screen.
     *
     * @param item The button clicked.
     */
    public void shareMarker(MenuItem item) {
        MapMarker mapMarker = db.getMarkerByID(markerMap.get(selectedMarker.getId()));

        OpenGraphObject meal = OpenGraphObject.Factory.createForPost("video.movie");
        meal.setProperty("title", "Buffalo Tacos");
        meal.setProperty("image", "http://ia.media-imdb.com/images/M/MV5BMzU0NDY0NDEzNV5BMl5BanBnXkFtZTgwOTIxNDU1MDE@._V1_SX640_SY720_.jpg");
        meal.setProperty("url", "http://www.imdb.com/title/tt1170358/?ref_=hm_cht_t1");
        meal.setProperty("description", "Leaner than beef and great flavor.");

        OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
        action.setProperty("objects/video.movie", meal);

        FacebookDialog shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(this, action, "video.watches", "objects/video.movie")
                .build();
        uiHelper.trackPendingDialogCall(shareDialog.present());

        removeMarkerMenu();
    }

    /**
     * Clears the action bar menu from any existing menus
     * and replaces it with the main one. Also programmatically
     * deselects any selected Google Maps markers.
     */
    private void removeMarkerMenu() {
        actionMenu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, actionMenu);
        isMarkerMenuOn = false;
        map.setOnMapClickListener(null);
        selectedMarker = null;
        return;
    }

    /**
     * Clears the map of any visible markers.
     *
     * @param item The button clicked.
     */
    public void clearMap(MenuItem item) {
        map.clear();
    }

    /**
     * Shows all existing database markers on map.
     *
     * @param item The button clicked.
     */
    public void showMarkers(MenuItem item) {
        // TODO: ASyncTask
        List<MapMarker> markers = db.getAllMarkers();
        for (MapMarker marker : markers) {
            addMarkerToMap(marker);
        }
    }

    @Override
    public void onFragmentInteraction() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment dialog = getFragmentManager().findFragmentByTag("dialog");
        ft.hide(dialog);

        Toast.makeText(this, getResources().getString(R.string.select_marker), Toast.LENGTH_LONG).show();

        selectDirectionsMarkerState = true;
    }

    @Override
    public void onFragmentFinish(String startSelection, String startText, String endSelection, String endText, int transportSelection) {
        DialogFragment dialog = (DialogFragment) getFragmentManager().findFragmentByTag("dialog");
        if (dialog != null) {
            dialog.dismiss();
        }


    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (selectDirectionsMarkerState) {
            MapMarker mapMarker = db.getMarkerByID(markerMap.get(marker.getId()));

            // Send the marker title to fragment
            DirectionsOptionsFragment dialog = (DirectionsOptionsFragment) getFragmentManager().findFragmentByTag("dialog");
            if (dialog != null) {
                dialog.setPointText(mapMarker.getTitle());
                dialog.show(getFragmentManager().beginTransaction(), "dialog");
            }

            selectDirectionsMarkerState = false;
            return false;
        } else {
            // Do default behaviour
            return true;
        }
    }
}
