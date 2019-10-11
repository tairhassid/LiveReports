package liveReports.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import liveReports.livereports.R;
import liveReports.utils.LocationHelper;

public class PostFragment extends Fragment {

    private static final String TAG = "PostFragment";
    private LocationHelper locationHelper;
    private Button button;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationHelper = new LocationHelper(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        button = view.findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationHelper.setLatlng();
                LatLng latLng = locationHelper.getCurrentLatlng();

//                TextView txt = view.findViewById(R.id.location_text);
//                txt.setText(Double.toString(latLng.latitude));
                Log.d(TAG, "onClick: " + Double.toString(latLng.latitude));
            }
        });
        return view;
    }

}
