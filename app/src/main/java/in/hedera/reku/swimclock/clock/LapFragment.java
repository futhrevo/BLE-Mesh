package in.hedera.reku.swimclock.clock;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.TextView;

import in.hedera.reku.swimclock.FragListener;
import in.hedera.reku.swimclock.MainActivity;
import in.hedera.reku.swimclock.R;
import in.hedera.reku.swimclock.utils.BtnState;
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
    private TextView timeview;
    private TextView repInfo;
    private int laps;
    private int currentlap;
    private boolean isCountdownTimer = false;

    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;

    Handler handler;

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
        lapSet = new LapSet();
        handler = new Handler() ;
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
                if (((CheckBox) v).isChecked()) {
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
                Button b = (Button) v;
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
        timeview = rootView.findViewById(R.id.timeview);
        repInfo = rootView.findViewById(R.id.rep_info);
        return rootView;
    }

    private void uploadData() {
        lapSet.setLapcount(Integer.parseInt("0" + etRepCount.getText().toString()));
        lapSet.setMinute(Integer.parseInt("0" + etIntMin.getText().toString()));
        lapSet.setSecond(Integer.parseInt("0" + etIntSec.getText().toString()));
        lapSet.setMilliseconds(Integer.parseInt("0" + etIntms.getText().toString()));
        callback.sendOpcode("04" + ld + lapSet.getLapcount() + lapSet.getMillisHex());
    }

    private void begin() {
        laps = lapSet.getlapcountInt();
        currentlap = 0;
        isCountdownTimer = ld.equals("00");
        callback.sendOpcode("0902");
        if(isCountdownTimer) {
            StartTime = SystemClock.uptimeMillis() + lapSet.getTotalMills();
            handler.postDelayed(downRunnable, 0);
        } else {
            StartTime = SystemClock.uptimeMillis();
            handler.postDelayed(uprunnable, 0);
        }
    }
    private void startLaps() {
        uploadData();
        begin();
        actionBtn.setText(Constants.BTN_PAUSE);
    }

    private void pauseLaps() {
        callback.sendOpcode("0501");
        TimeBuff += MillisecondTime;

        handler.removeCallbacks(uprunnable);
        handler.removeCallbacks(downRunnable);

        actionBtn.setText(Constants.BTN_RESUME);
    }

    private void resumeLaps() {
        callback.sendOpcode("0500");
        if(isCountdownTimer) {
            StartTime = SystemClock.uptimeMillis() + MillisecondTime;
            handler.postDelayed(downRunnable, 0);
        } else {
            StartTime = SystemClock.uptimeMillis();
            handler.postDelayed(uprunnable, 0);
        }
        actionBtn.setText(Constants.BTN_PAUSE);
    }

    private void resetLaps() {
        callback.sendOpcode("0903");
        callback.sendOpcode("06");
        actionBtn.setText(Constants.BTN_START);
        handler.removeCallbacks(uprunnable);
        handler.removeCallbacks(downRunnable);
        resetRunnable();
        repInfo.setText("");
    }

    private void resetRunnable() {
        MillisecondTime = 0L ;
        StartTime = 0L ;
        TimeBuff = 0L ;
        UpdateTime = 0L ;
        timeview.setText("00:00.00");
    }

    public void updateTimeView(long millis) {
        int Seconds = (int) (millis / 1000);

        int Minutes = Seconds / 60;

        Seconds = Seconds % 60;

        int MilliSeconds = (int) (millis % 100);

        timeview.setText("" + String.format("%02d", Minutes) + ":"
                + String.format("%02d", Seconds) + ":"
                + String.format("%02d", MilliSeconds));
    }

    public Runnable uprunnable = new Runnable() {
        @Override
        public void run() {
            repInfo.setText("[1] - " + String.valueOf(currentlap + 1));
            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            updateTimeView(UpdateTime);

            if (UpdateTime > lapSet.getTotalMills()) {
                currentlap++;
                Log.d(TAG, String.valueOf(currentlap) + " <- " + String.valueOf(laps));
                if (laps > currentlap) {
                    resetRunnable();
                    StartTime = SystemClock.uptimeMillis();
                    repInfo.setText("[1] - " + String.valueOf(currentlap));
                    handler.postDelayed(this, 0);
                } else {
                    handler.removeCallbacks(uprunnable);
                    currentlap = 0;
//                    repInfo.setText("");
                    actionBtn.setText(Constants.BTN_START);
                }
            }else {
                handler.postDelayed(this, 0);
            }

        }
    };

    public Runnable downRunnable = new Runnable() {
        @Override
        public void run() {
            repInfo.setText("[1] - " + String.valueOf(currentlap + 1));
            MillisecondTime = StartTime - SystemClock.uptimeMillis();
            UpdateTime =  MillisecondTime;
            updateTimeView(UpdateTime);
            if(UpdateTime < 0) {
                currentlap++;

                Log.d(TAG, String.valueOf(currentlap) + " <- " + String.valueOf(laps));
                resetRunnable();
                StartTime = SystemClock.uptimeMillis() + lapSet.getTotalMills();
                if (laps > currentlap) {

                    handler.postDelayed(this, 0);
                } else {
                    handler.removeCallbacks(uprunnable);
                    currentlap = 0;
//                    repInfo.setText("");
                    actionBtn.setText(Constants.BTN_START);
                }
            } else {
                handler.postDelayed(this, 0);
            }
        }
    };
}
