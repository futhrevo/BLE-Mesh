package in.hedera.reku.swimclock.clock;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.hedera.reku.swimclock.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LapFragment extends Fragment {


    public LapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.lapclockview,container, false);
    }

}
