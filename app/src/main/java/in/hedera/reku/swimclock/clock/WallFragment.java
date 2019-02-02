package in.hedera.reku.swimclock.clock;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import in.hedera.reku.swimclock.FragListener;
import in.hedera.reku.swimclock.MainActivity;
import in.hedera.reku.swimclock.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class WallFragment extends Fragment {

    public static final String TAG = WallFragment.class.getSimpleName();
    FragListener callback;
    private Calendar mTime;
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    TextView timeview;
    private boolean mRunning = true;
    private static final int TICK_WHAT = 2;

    public WallFragment() {
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
        callback.sendOpcode("03");
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_wall, container, false);
        timeview = rootView.findViewById(R.id.timeview);
        Button sync3btn = rootView.findViewById(R.id.sync3_btn);
        sync3btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.sendOpcode("0003");
            }
        });
        Button sync4btn = rootView.findViewById(R.id.sync4_btn);
        sync4btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.sendOpcode("0004");
            }
        });
        Button rtcBtn = rootView.findViewById(R.id.real_time_btn);
        rtcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.sendOpcode("07");
            }
        });
        mTime = Calendar.getInstance();
        mHandler.sendMessageDelayed(Message.obtain(mHandler,
                TICK_WHAT), 100);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeMessages(TICK_WHAT);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message m) {
            if (mRunning) {
                updateTime();
                sendMessageDelayed(Message.obtain(this , TICK_WHAT),
                        1000);
            }
        }
    };

    private void updateTime() {
        mTime.setTimeInMillis(System.currentTimeMillis());
        timeview.setText(formatter.format(mTime.getTime()));
    }

}
