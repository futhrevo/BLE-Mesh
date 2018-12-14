package in.hedera.reku.swimclock.clock;


import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import in.hedera.reku.swimclock.FragListener;
import in.hedera.reku.swimclock.MainActivity;
import in.hedera.reku.swimclock.R;
import in.hedera.reku.swimclock.utils.BtnState;
import in.hedera.reku.swimclock.utils.Chronometer;
import in.hedera.reku.swimclock.utils.Constants;
import in.hedera.reku.swimclock.utils.LapSet;

/**
 * A simple {@link Fragment} subclass.
 */
public class LapFragment extends Fragment {
    public static final String TAG = LapFragment.class.getSimpleName();

    FragListener callback;
    private BtnState btnState = BtnState.START;
    private LapSet lapSet;
    private Button actionBtn;
    private String ld = "00";

    private EditText etRepCount;
    private EditText etIntMin;
    private EditText etIntSec;
    private EditText etIntms;
    private int laps;
    private int currentlap;
    private long timeWhenPaused = 0;

    private Chronometer chronometer;

    public LapFragment() {
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
        lapSet = new LapSet();
        View rootView = inflater.inflate(R.layout.fragment_lap, container, false);
        Button resetBtn = rootView.findViewById(R.id.reset_btn);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetLaps();
            }
        });
        Button rtcBtn = rootView.findViewById(R.id.real_time_btn);
        rtcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.sendOpcode("07");
            }
        });

        CheckBox lapDirection = rootView.findViewById(R.id.checkBox);
        lapDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((CheckBox) v).isChecked()) {
                    ld = "01";
                } else {
                    ld = "00";
                }
            }
        });
        actionBtn = rootView.findViewById(R.id.time_pause);
        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button)v;
                String label = b.getText().toString();
                switch (label) {
                    case Constants.BTN_START:
                        startLaps();
                        break;
                    case Constants.BTN_PAUSE:
                        pauseLaps();
                        break;
                    case Constants.BTN_RESUME:
                        resumeLaps();
                        break;
                }
            }
        });
        etRepCount = rootView.findViewById(R.id.et_rep_count);
        etIntMin = rootView.findViewById(R.id.et_int_min);
        etIntSec = rootView.findViewById(R.id.et_int_sec);
        etIntms = rootView.findViewById(R.id.et_int_ms);

        ImageButton lapClearBtn = rootView.findViewById(R.id.set_clear);
        lapClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etRepCount.setText("");
                etIntMin.setText("");
                etIntSec.setText("");
                etIntms.setText("");
            }
        });
        chronometer = rootView.findViewById(R.id.timeview);
        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if(chronometer.getTimeElapsed() > lapSet.getTotalMills()) {
                    chronometer.stop();
                    // make this as runnable with delay
                    restartCounter();

                }
            }
        });
        return rootView;
    }

    private void startLaps() {
        lapSet.setLapcount(Integer.parseInt("0" + etRepCount.getText().toString()));
        lapSet.setMinute(Integer.parseInt("0" + etIntMin.getText().toString()));
        lapSet.setSecond(Integer.parseInt("0" + etIntSec.getText().toString()));
        lapSet.setMilliseconds(Integer.parseInt("0" + etIntms.getText().toString()));
        callback.sendOpcode("04" + ld + lapSet.getLapcount() + lapSet.getMillisHex());
        laps = lapSet.getlapcountInt();
        currentlap = 0;
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        actionBtn.setText(Constants.BTN_PAUSE);
    }

    private void pauseLaps() {
        callback.sendOpcode("0501");
        timeWhenPaused = chronometer.getBase() - SystemClock.elapsedRealtime();
        chronometer.stop();
        actionBtn.setText(Constants.BTN_RESUME);
    }

    private void resumeLaps() {
        callback.sendOpcode("0500");
        chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
        chronometer.start();
        actionBtn.setText(Constants.BTN_PAUSE);
    }

    private void resetLaps() {
        callback.sendOpcode("06");
        actionBtn.setText(Constants.BTN_START);
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
    }

    public void restartCounter() {
            currentlap++;
            Log.d(TAG, String.valueOf(currentlap));
            if(laps >= currentlap) {
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
            }
        }

        public Runnable uprunnable = new Runnable() {
            @Override
            public void run() {

            }
        };
}
