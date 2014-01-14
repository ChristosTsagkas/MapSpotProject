package com.example.mapspot;

import android.app.Activity;
import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
public class DirectionsOptionsFragment extends DialogFragment implements OnItemSelectedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private final static String APPTAG = "MSpot";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DirectionsOptionsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DirectionsOptionsFragment newInstance(String param1, String param2) {
        DirectionsOptionsFragment fragment = new DirectionsOptionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
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
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.destinations_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        startingPoint.setAdapter(adapter);
        destination.setAdapter(adapter);

        // Attach listeners
        startingPoint.setOnItemSelectedListener(this);
        destination.setOnItemSelectedListener(this);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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

        Spinner spinner = null;
        EditText editText = null;
        switch (spinnerID) {
            case R.id.starting_point_spinner:
                spinner = (Spinner) view.findViewById(R.id.starting_point_spinner);
                editText = (EditText) view.findViewById(R.id.starting_point_details);
                break;
            case R.id.destination_spinner:
                spinner = (Spinner) view.findViewById(R.id.destination_spinner);
                editText = (EditText) view.findViewById(R.id.destination_details);
                break;
        }

        if (spinner != null) {
            switch (position) {
                case 0: // Custom
                    editText.setEnabled(true);
                    break;
                case 1: // Current
                    editText.setEnabled(false);
                    break;
                case 2: // MapSpot
                    editText.setEnabled(false);
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
