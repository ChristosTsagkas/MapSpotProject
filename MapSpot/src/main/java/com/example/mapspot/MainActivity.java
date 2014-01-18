package com.example.mapspot;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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

    private DirectionsOptionsFragment onHoldDialog;

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
        DirectionsOptionsFragment dialogFragment;
        if (selectedMarker != null) {
            MapMarker mapMarker = db.getMarkerByID(markerMap.get(selectedMarker.getId()));

            dialogFragment = DirectionsOptionsFragment.newInstance(
                    0,                      // Default value for start type
                    "",                     // Default value for start text
                    2,                      // MapSpot for end type
                    mapMarker.getTitle(),   // MapSpot title for end text
                    0,                      // Default value for transport type
                    0,                      // Default value for start MapSpot database marker id
                    mapMarker.getDbID());   // MapSpot database marker id
        } else {
            dialogFragment = new DirectionsOptionsFragment();
        }

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
        onHoldDialog = (DirectionsOptionsFragment) getFragmentManager().findFragmentByTag("dialog");
        onHoldDialog.dismiss();

        map.setOnMarkerClickListener(this);
        Toast.makeText(this, getResources().getString(R.string.select_marker), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFragmentFinish(int startSelection, String startText, int endSelection, String endText, int transportSelection, long startMarkerID, long endMarkerID) {
        DialogFragment dialog = (DialogFragment) getFragmentManager().findFragmentByTag("dialog");
        if (dialog != null) {
            dialog.dismiss();
        }

        String params = "?sensor=true";

        switch (startSelection) {
            case 0:
                params += "&origin=" + Uri.encode(startText);
                break;
            case 1:
                Location currentLocation = locationClient.getLastLocation();
                params += "&origin=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude();
                break;
            case 2:
                MapMarker marker = db.getMarkerByID(startMarkerID);
                params += "&origin=" + marker.getPosition().latitude + "," + marker.getPosition().longitude;
                break;
        }

        switch (endSelection) {
            case 0:
                params += "&destination=" + Uri.encode(endText);
                break;
            case 1:
                Location currentLocation = locationClient.getLastLocation();
                params += "&destination=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude();
                break;
            case 2:
                MapMarker marker = db.getMarkerByID(endMarkerID);
                params += "&destination=" + marker.getPosition().latitude + "," + marker.getPosition().longitude;
                break;
        }

        switch (transportSelection) {
            case R.id.pedestrian:
                params += "&mode=walking";
                break;
            case R.id.bicycle:
                params += "&mode=bicycling";
                break;
            case R.id.car:
                params += "&mode=driving";
                break;
        }

        new DownloadAsyncTask().execute(params);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Disable the listener
        map.setOnMarkerClickListener(null);

        MapMarker mapMarker = db.getMarkerByID(markerMap.get(marker.getId()));

        // Send the marker title to fragment
        if (onHoldDialog != null) {
            String text = mapMarker.getTitle();
            DirectionsOptionsFragment newFragment = null;

            switch (onHoldDialog.getReferencedSpinnerID()) {
                case R.id.starting_point_spinner:
                    newFragment = DirectionsOptionsFragment.newInstance(
                            onHoldDialog.getStartType(),
                            text,
                            onHoldDialog.getEndType(),
                            onHoldDialog.getEndText(),
                            onHoldDialog.getTransportType(),
                            mapMarker.getDbID(),
                            onHoldDialog.getEndMarkerID());
                    break;
                case R.id.destination_spinner:
                    newFragment = DirectionsOptionsFragment.newInstance(
                            onHoldDialog.getStartType(),
                            onHoldDialog.getStartText(),
                            onHoldDialog.getEndType(),
                            text,
                            onHoldDialog.getTransportType(),
                            onHoldDialog.getStartMarkerID(),
                            mapMarker.getDbID());
                    break;
            }

            if (newFragment != null) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();

                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                newFragment.show(ft, "dialog");
            }
        }
        return false;
    }

    /**
     * A method to download json data from url
     *
     * @param strUrl A working URL string.
     * @return The data returned from the URL parsing.
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadAsyncTask extends AsyncTask<String, Void, String> {
        private static final String PREFIXURL = "http://maps.googleapis.com/maps/api/directions/json";

        @Override
        protected String doInBackground(String... params) {
            Log.d(APPTAG, "Requesting directions from: " + PREFIXURL + params[0]);
            String data = null;
            try {
                data = downloadUrl(PREFIXURL + params[0]);
            } catch (IOException e) {
                Log.d(APPTAG, e.toString());
            }

            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.color(Color.MAGENTA);
            }

            if (lineOptions == null) {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.directions_error), Toast.LENGTH_LONG);
            } else {
                // Drawing polyline in the Google Map for the i-th route
                map.addPolyline(lineOptions);
                Toast.makeText(MainActivity.this, getResources().getString(R.string.navigation_start), Toast.LENGTH_SHORT);
            }
        }
    }
}
