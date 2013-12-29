package com.example.mapspot;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import static com.example.mapspot.R.layout.marker_actions_fragment;

/**
 * Created by Mike on 28/12/2013.
 */
public class MarkerActionsDialogFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(marker_actions_fragment, container, false);
        getDialog().setTitle(getResources().getString(R.string.marker_options_title));

        Button deleteButton = (Button) view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return view;
    }
}
