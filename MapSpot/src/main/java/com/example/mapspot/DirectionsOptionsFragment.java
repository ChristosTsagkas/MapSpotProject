package com.example.mapspot;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.google.android.gms.maps.model.LatLng;

import static android.widget.AdapterView.OnItemSelectedListener;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DirectionsOptionsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DirectionsOptionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DirectionsOptionsFragment extends DialogFragment implements OnItemSelectedListener,
        View.OnClickListener {
    private static final String LOCATION = "location";
    private static final String APPTAG = "MapSpot";
    private EditText referencedEditText = null;

    private LatLng currentLocation;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param currentLocation The current location of the user.
     * @return A new instance of fragment DirectionsOptionsFragment.
     */
    public static DirectionsOptionsFragment newInstance(LatLng currentLocation) {
        DirectionsOptionsFragment fragment = new DirectionsOptionsFragment();
        Bundle args = new Bundle();
        args.putParcelable(LOCATION, currentLocation);
        fragment.setArguments(args);
        return fragment;
    }

    public DirectionsOptionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentLocation = getArguments().getParcelable(LOCATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_directions_options, container, false);

        getDialog().setTitle(getResources().getString(R.string.directions_title));

        final Spinner startingPoint = (Spinner) view.findViewById(R.id.starting_point_spinner);
        final Spinner destination = (Spinner) view.findViewById(R.id.destination_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.destinations_array,
                android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        startingPoint.setAdapter(adapter);
        destination.setAdapter(adapter);

        // Attach listeners
        startingPoint.setOnItemSelectedListener(this);
        destination.setOnItemSelectedListener(this);

        Button button = (Button) view.findViewById(R.id.start_navigation_button);
        button.setOnClickListener(this);

        return view;
    }

    public void setPointText(String text) {
        if (referencedEditText != null) {
            referencedEditText.setText(text);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int spinnerID = parent.getId();

        EditText editText = null;
        switch (spinnerID) {
            case R.id.starting_point_spinner:
                editText = (EditText) getView().findViewById(R.id.starting_point_details);
                break;
            case R.id.destination_spinner:
                editText = (EditText) getView().findViewById(R.id.destination_details);
                break;
        }

        if (editText != null) {
            switch (position) {
                case 0: // Custom
                    editText.setEnabled(true);
                    editText.setVisibility(View.VISIBLE);
                    break;
                case 1: // Current
                    editText.setEnabled(false);
                    editText.setVisibility(View.GONE);
                    break;
                case 2: // MapSpot
                    editText.setEnabled(false);
                    editText.setText("");
                    editText.setVisibility(View.VISIBLE);
                    if (mListener != null) {
                        mListener.onFragmentInteraction();
                        referencedEditText = editText;
                    } else {
                        Log.d(APPTAG, "null listener");
                    }
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.start_navigation_button) {
            Spinner startSpinner = (Spinner) view.findViewById(R.id.starting_point_spinner);
            String startSelection = startSpinner.getSelectedItem().toString();
            EditText startEditText = (EditText) view.findViewById(R.id.starting_point_details);
            String startText = startEditText.getText().toString();

            Spinner endSpinner = (Spinner) view.findViewById(R.id.destination_spinner);
            String endSelection = endSpinner.getSelectedItem().toString();
            EditText endEditText = (EditText) view.findViewById(R.id.destination_details);
            String endText = endEditText.getText().toString();

            RadioGroup transportGroup = (RadioGroup) view.findViewById(R.id.transportGroup);
            int transportSelection = transportGroup.getCheckedRadioButtonId();

            mListener.onFragmentFinish(startSelection, startText, endSelection, endText, transportSelection);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction();

        public void onFragmentFinish(String startSelection, String startText, String endSelection, String endText, int transportSelection);
    }

}
