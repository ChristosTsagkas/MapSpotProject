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

import java.util.HashMap;
import java.util.Map;

import static com.example.mapspot.R.layout.marker_create_fragment;

/**
 * Created by George on 4/12/2013.
 */
public class MarkerDetailsDialogFragment extends DialogFragment {

    private HashMap<Integer, String> categoriesMap = new HashMap<>();

    /**
     * Interface that provides methods retrieving
     * the fragment's input field values.
     */
    public interface MarkerDialogListener {
        void onFinishMarkerDialog(Map<String, String> details);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categoriesMap.put(0, "custom location");
        categoriesMap.put(1, "recreation");
        categoriesMap.put(2, "gas station");
        categoriesMap.put(3, "food and drinks");
        categoriesMap.put(4, "supermarket");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(marker_create_fragment, container, false);
        getDialog().setTitle(getResources().getString(R.string.create_marker_title));

        final EditText title = (EditText) view.findViewById(R.id.marker_title);
        final EditText description = (EditText) view.findViewById(R.id.marker_description);
        final Spinner category = (Spinner) view.findViewById(R.id.categories_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.categories_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        category.setAdapter(adapter);

        // Set the button listener
        Button button = (Button) view.findViewById(R.id.insert_marker_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MarkerDialogListener activity = (MarkerDialogListener) getActivity();

                Map<String, String> details = new HashMap<>();
                details.put("title", title.getText().toString());
                details.put("description", description.getText().toString());
                details.put("category", categoriesMap.get(category.getSelectedItemPosition()));

                activity.onFinishMarkerDialog(details);
                getDialog().dismiss();
                return;
            }
        });

        return view;
    }
}
