package in.hedera.reku.swimclock.clock;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import in.hedera.reku.swimclock.FragListener;
import in.hedera.reku.swimclock.MainActivity;
import in.hedera.reku.swimclock.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MLapFragment extends Fragment {
    public static final String TAG = MLapFragment.class.getSimpleName();

    FragListener callback;

    public MLapFragment() {
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
        callback.sendOpcode("02");

        View rootView = inflater.inflate(R.layout.fragment_mlap, container, false);

        Button resetBtn = rootView.findViewById(R.id.reset_btn);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.sendOpcode("06");
            }
        });
        Button rtcBtn = rootView.findViewById(R.id.real_time_btn);
        rtcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.sendOpcode("07");
            }
        });
        // Inflate the layout for this fragment
        return rootView;
    }

}
