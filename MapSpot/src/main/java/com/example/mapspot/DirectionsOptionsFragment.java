package com.example.mapspot;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

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
    private static final String STARTTYPE = "starting point type";
    private static final String STARTTEXT = "starting point text";
    private static final String ENDTYPE = "destination type";
    private static final String ENDTEXT = "destination text";
    private static final String TRANSPORTTYPE = "transport type";
    private static final String STARTMARKERID = "starting point marker id";
    private static final String ENDMARKERID = "destination marker id";
    private static final String APPTAG = "MapSpot";

    private int referencedSpinnerID;
    private int startType;
    private String startText = "";
    private int endType;
    private String endText = "";
    private int transportType;
    private long startMarkerID;
    private long endMarkerID;
    private boolean programmaticStartSpinnerChange = false;
    private boolean programmaticEndSpinnerChange = false;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * Presets some values to be used when this fragment view is initialized.
     * This needs only be called when a specific EditText is preset with a MapSpot.
     *
     * @param startSelection     The starting point type.
     * @param startText          The starting point EditText value.
     * @param endSelection       The destination type.
     * @param endText            The destination EditText value.
     * @param transportSelection The transport type.
     * @param startMarkerID      The MapSpot marker database ID for the starting point, if any.
     * @param endMarkerID        The MapSpot marker database ID for the destination, if any.
     * @return A new instance of fragment DirectionsOptionsFragment.
     */
    public static DirectionsOptionsFragment newInstance(int startSelection, String startText, int endSelection, String endText, int transportSelection, long startMarkerID, long endMarkerID) {
        DirectionsOptionsFragment fragment = new DirectionsOptionsFragment();
        Bundle args = new Bundle();
        args.putInt(STARTTYPE, startSelection);
        args.putString(STARTTEXT, startText);
        args.putInt(ENDTYPE, endSelection);
        args.putString(ENDTEXT, endText);
        args.putInt(TRANSPORTTYPE, transportSelection);
        args.putLong(STARTMARKERID, startMarkerID);
        args.putLong(ENDMARKERID, endMarkerID);
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
            startType = getArguments().getInt(STARTTYPE);
            startText = getArguments().getString(STARTTEXT);
            endType = getArguments().getInt(ENDTYPE);
            endText = getArguments().getString(ENDTEXT);
            transportType = getArguments().getInt(TRANSPORTTYPE);
            startMarkerID = getArguments().getLong(STARTMARKERID);
            endMarkerID = getArguments().getLong(ENDMARKERID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_directions_options, container, false);

        getDialog().setTitle(getResources().getString(R.string.directions_title));

        EditText startingDetails = (EditText) view.findViewById(R.id.starting_point_details);
        EditText destinationDetails = (EditText) view.findViewById(R.id.destination_details);
        startingDetails.setText(startText);
        destinationDetails.setText(endText);

        Spinner startingPoint = (Spinner) view.findViewById(R.id.starting_point_spinner);
        Spinner destination = (Spinner) view.findViewById(R.id.destination_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.destinations_array,
                android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        startingPoint.setAdapter(adapter);
        destination.setAdapter(adapter);

        startingPoint.setSelection(startType);
        destination.setSelection(endType);
        programmaticStartSpinnerChange = true;
        programmaticEndSpinnerChange = true;

        RadioGroup transportGroup = (RadioGroup) view.findViewById(R.id.transportGroup);
        if (transportType != 0) {
            transportGroup.check(transportType);
        }

        // Attach listeners
        startingPoint.setOnItemSelectedListener(this);
        destination.setOnItemSelectedListener(this);

        Button button = (Button) view.findViewById(R.id.start_navigation_button);
        button.setOnClickListener(this);

        return view;
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
        boolean programmaticChange = false;

        EditText editText = null;
        switch (spinnerID) {
            case R.id.starting_point_spinner:
                editText = (EditText) getView().findViewById(R.id.starting_point_details);
                programmaticChange = programmaticStartSpinnerChange;
                startType = position;
                break;
            case R.id.destination_spinner:
                editText = (EditText) getView().findViewById(R.id.destination_details);
                programmaticChange = programmaticEndSpinnerChange;
                endType = position;
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
                    editText.setText("");
                    editText.setVisibility(View.GONE);
                    break;
                case 2: // MapSpot
                    editText.setEnabled(false);
                    editText.setVisibility(View.VISIBLE);
                    if (mListener != null && !programmaticChange) {
                        RadioGroup radioGroup = (RadioGroup) getView().findViewById(R.id.transportGroup);
                        transportType = radioGroup.getCheckedRadioButtonId();
                        referencedSpinnerID = spinnerID;
                        mListener.onFragmentInteraction();
                    }
                    break;
            }
        }

        switch (spinnerID) {
            case R.id.starting_point_spinner:
                programmaticStartSpinnerChange = false;
                break;
            case R.id.destination_spinner:
                programmaticEndSpinnerChange = false;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.start_navigation_button) {
            Spinner startSpinner = (Spinner) getView().findViewById(R.id.starting_point_spinner);
            int startSelection = startSpinner.getSelectedItemPosition();
            EditText startEditText = (EditText) getView().findViewById(R.id.starting_point_details);
            String startText = startEditText.getText().toString();

            Spinner endSpinner = (Spinner) getView().findViewById(R.id.destination_spinner);
            int endSelection = endSpinner.getSelectedItemPosition();
            EditText endEditText = (EditText) getView().findViewById(R.id.destination_details);
            String endText = endEditText.getText().toString();

            RadioGroup transportGroup = (RadioGroup) getView().findViewById(R.id.transportGroup);
            int transportSelection = transportGroup.getCheckedRadioButtonId();

            mListener.onFragmentFinish(startSelection, startText, endSelection, endText, transportSelection, startMarkerID, endMarkerID);
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

        public void onFragmentFinish(int startSelection, String startText, int endSelection, String endText, int transportSelection, long startMarkerID, long endMarkerID);
    }

    public int getStartType() {
        return startType;
    }

    public String getStartText() {
        return startText;
    }

    public int getEndType() {
        return endType;
    }

    public String getEndText() {
        return endText;
    }

    public int getTransportType() {
        return transportType;
    }

    public int getReferencedSpinnerID() {
        return referencedSpinnerID;
    }

    public long getStartMarkerID() {
        return startMarkerID;
    }

    public long getEndMarkerID() {
        return endMarkerID;
    }
}
