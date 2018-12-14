package in.hedera.reku.swimclock.clock;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.hedera.reku.swimclock.FragListener;
import in.hedera.reku.swimclock.MainActivity;
import in.hedera.reku.swimclock.R;
import in.hedera.reku.swimclock.utils.Chronometer;

/**
 * A simple {@link Fragment} subclass.
 */
public class PaceFragment extends Fragment {
    public static final String TAG = PaceFragment.class.getSimpleName();

    FragListener callback;
    private Chronometer chronometer;

    public PaceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            callback = (MainActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FragListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_pace, container, false);
        chronometer = rootView.findViewById(R.id.timeview);
        callback.sendOpcode("02");
        chronometer.start();
        return rootView;
    }

}
