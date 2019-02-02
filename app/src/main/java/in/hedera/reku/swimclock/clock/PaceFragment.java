package in.hedera.reku.swimclock.clock;


import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import in.hedera.reku.swimclock.FragListener;
import in.hedera.reku.swimclock.MainActivity;
import in.hedera.reku.swimclock.R;
import in.hedera.reku.swimclock.utils.Chronometer;
import in.hedera.reku.swimclock.utils.Constants;

/**
 * A simple {@link Fragment} subclass.
 */
public class PaceFragment extends Fragment {
    public static final String TAG = PaceFragment.class.getSimpleName();

    FragListener callback;
    private Button actionBtn;
    private Chronometer chronometer;
    private long timeWhenStopped = 0;

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
        Button resetBtn = rootView.findViewById(R.id.reset_btn);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPace();
            }
        });
        Button rtcBtn = rootView.findViewById(R.id.real_time_btn);
        rtcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.sendOpcode("07");
            }
        });

        actionBtn = rootView.findViewById(R.id.time_pause);
        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                String label = b.getText().toString();
                switch (label) {
                    case Constants.BTN_START:
                        startPace();
                        break;
                    case Constants.BTN_PAUSE:
                        pausePace();
                        break;
                    case Constants.BTN_RESUME:
                        resumePace();
                        break;
                }
            }
        });

        chronometer = rootView.findViewById(R.id.timeview);
        return rootView;
    }

    private void startPace() {
        callback.sendOpcode("02");
        chronometer.start();
        actionBtn.setText(Constants.BTN_PAUSE);
    }

    private void pausePace() {
        callback.sendOpcode("0501");
        actionBtn.setText(Constants.BTN_RESUME);
        timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();
        chronometer.stop();
    }

    private void resumePace() {
        callback.sendOpcode("0500");
        actionBtn.setText(Constants.BTN_PAUSE);
        chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        chronometer.start();
    }

    private void resetPace() {
        callback.sendOpcode("06");
        actionBtn.setText(Constants.BTN_START);
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime()); timeWhenStopped = 0;
    }

}
