package com.example.mapspot;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import static com.example.mapspot.R.layout.marker_create_fragment;

/**
 * Created by George on 4/12/2013.
 */
public class MarkerDetailsDialogFragment extends DialogFragment {

    ArrayList<String> categoriesMap = new ArrayList<>();

    /**
     * Interface that provides methods retrieving
     * the fragment's input field values.
     */
    public interface MarkerDialogListener {
        void onFinishMarkerDialog(MapMarker newMarker);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        categoriesMap.add("custom location");
        categoriesMap.add("recreation");
        categoriesMap.add("gas station");
        categoriesMap.add("food and drinks");
        categoriesMap.add("supermarket");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(marker_create_fragment, container, false);

        // Retrieve marker information data from arguments
        final LatLng location = getArguments().getParcelable("location");

        final EditText title = (EditText) view.findViewById(R.id.marker_title);
        final EditText description = (EditText) view.findViewById(R.id.marker_description);
        final Spinner category = (Spinner) view.findViewById(R.id.categories_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.categories_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        category.setAdapter(adapter);

        String savedTitle = getArguments().getString("title");
        if (savedTitle != null) {
            String savedDescription = getArguments().getString("description");
            String savedCategory = getArguments().getString("category");

            title.setText(savedTitle);
            description.setText(savedDescription);
            category.setSelection(categoriesMap.indexOf(savedCategory));

            getDialog().setTitle(getResources().getString(R.string.edit_marker_title));
        } else {
            getDialog().setTitle(getResources().getString(R.string.create_marker_title));
        }

        // Set the button listener
        Button button = (Button) view.findViewById(R.id.insert_marker_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MarkerDialogListener activity = (MarkerDialogListener) getActivity();

                MapMarker newMarker = new MapMarker(title.getText().toString(),
                        description.getText().toString(),
                        categoriesMap.get(category.getSelectedItemPosition()),
                        location.latitude,
                        location.longitude);

                activity.onFinishMarkerDialog(newMarker);
                getDialog().dismiss();
                return;
            }
        });

        return view;
    }
}
