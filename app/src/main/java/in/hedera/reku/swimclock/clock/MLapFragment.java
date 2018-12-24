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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.hedera.reku.swimclock.FragListener;
import in.hedera.reku.swimclock.MainActivity;
import in.hedera.reku.swimclock.R;
import in.hedera.reku.swimclock.utils.Constants;
import in.hedera.reku.swimclock.utils.LapSet;

/**
 * A simple {@link Fragment} subclass.
 */
public class MLapFragment extends Fragment {
    public static final String TAG = MLapFragment.class.getSimpleName();

    FragListener callback;
    private Button actionBtn;
    private String ld = "00";

    private TextView timeview;
    private TextView repInfo;

    private List<LapSet> alllaps;
    private LapSet lapSet;
    private int laps;
    private int set;
    private int currentlap;
    private boolean isCountdownTimer = false;

    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;

    Handler handler;


    private LapListAdapter adapter;

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
        handler = new Handler() ;
        View rootView = inflater.inflate(R.layout.fragment_mlap, container, false);

        Button resetBtn = rootView.findViewById(R.id.reset_btn);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetlaps();
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

        timeview = rootView.findViewById(R.id.timeview);
        repInfo = rootView.findViewById(R.id.rep_info);

        adapter = new LapListAdapter(getContext(), R.layout.rep_interval_item, new ArrayList<LapSet>());
        ListView lapsListView = rootView.findViewById(R.id.rep_list);
        lapsListView.setAdapter(adapter);

        ImageButton addRep = rootView.findViewById(R.id.add_rep_btn);
        addRep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.add(new LapSet());
            }
        });
        // Inflate the layout for this fragment
        return rootView;
    }

    private void startLaps() {
        if(adapter.getCount() == 0) {
            Toast.makeText(getActivity(), " No Lap Info Entered", Toast.LENGTH_SHORT).show();
            return;
        }
        alllaps = new ArrayList<LapSet>(adapter.getCount());
        alllaps.addAll(adapter.getLaps());
        for (LapSet newLap : alllaps) {
            callback.sendOpcode("01" + ld + newLap.getLapcount() + newLap.getMillisHex());
        }
        set = 0;
        lapSet = alllaps.get(set);
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

    private void resetlaps() {
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
            repInfo.setText("["+String.valueOf(set+1)+ "] - " + String.valueOf(currentlap + 1));
            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            updateTimeView(UpdateTime);

            if (UpdateTime > lapSet.getTotalMills()) {
                currentlap++;
                Log.d(TAG, String.valueOf(currentlap) + " <- " + String.valueOf(laps));
                resetRunnable();
                if (laps > currentlap) {

                    StartTime = SystemClock.uptimeMillis();
                    repInfo.setText("["+String.valueOf(set+1)+ "] - " + String.valueOf(currentlap));
                    handler.postDelayed(this, 0);
                }else if(set < alllaps.size()-1) {
                    Log.d(TAG, "New set started");
                    set++;
                    lapSet = alllaps.get(set);
                    laps = lapSet.getlapcountInt();
                    currentlap = 0;
                    StartTime = SystemClock.uptimeMillis();
                    handler.postDelayed(this, 0);
                }else {
                    Log.d(TAG, "All sets completed");
                    handler.removeCallbacks(uprunnable);
                    currentlap = 0;
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
            repInfo.setText("["+String.valueOf(set+1)+ "] - " + String.valueOf(currentlap + 1));
            MillisecondTime = StartTime - SystemClock.uptimeMillis();
            UpdateTime =  MillisecondTime;
            updateTimeView(UpdateTime);
            if(UpdateTime < 0) {
                currentlap++;

                Log.d(TAG, String.valueOf(currentlap) + " <- " + String.valueOf(laps));
                resetRunnable();

                if (laps > currentlap) {
                    StartTime = SystemClock.uptimeMillis() + lapSet.getTotalMills();
                    handler.postDelayed(this, 0);
                } else if(set < alllaps.size()-1) {
                    Log.d(TAG, "New set started");
                    set++;
                    lapSet = alllaps.get(set);
                    laps = lapSet.getlapcountInt();
                    currentlap = 0;
                    StartTime = SystemClock.uptimeMillis() + lapSet.getTotalMills();
                    handler.postDelayed(this, 0);
                }else {
                    Log.d(TAG, "All sets completed");
                    handler.removeCallbacks(uprunnable);
                    currentlap = 0;
                    actionBtn.setText(Constants.BTN_START);
                }
            } else {
                handler.postDelayed(this, 0);
            }
        }
    };
}
