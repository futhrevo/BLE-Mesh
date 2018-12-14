package in.hedera.reku.swimclock.clock;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import in.hedera.reku.swimclock.FragListener;
import in.hedera.reku.swimclock.MainActivity;
import in.hedera.reku.swimclock.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClockFragment extends Fragment {
    public static final String TAG = ClockFragment.class.getSimpleName();
    FragListener callback;

    private FragmentTabHost mTabHost;

    public ClockFragment() {
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
        View rootView = inflater.inflate(R.layout.clock_tablayout,container, false);
        Switch powerSwitch = rootView.findViewById(R.id.power_switch);
        powerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                togglePower(isChecked);
            }
        });
        Switch buzzerSwitch = rootView.findViewById(R.id.buzzer_switch);
        buzzerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleBuzzer(isChecked);
            }
        });
        mTabHost = (FragmentTabHost)rootView.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);
        mTabHost.addTab(mTabHost.newTabSpec("wallFrag").setIndicator("Wall Clock"), WallFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("paceFrag").setIndicator("Pace Clock"), PaceFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("lapFrag").setIndicator("Lap Clock"), LapFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("mlapFrag").setIndicator("Multi Lap"), MLapFragment.class, null);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        callback = null;
        super.onDestroyView();
    }

    public void togglePower(boolean isChecked) {
        if(isChecked) {
            callback.sendOpcode("01");
        }else {
            callback.sendOpcode("08");
        }

    }

    public void toggleBuzzer(boolean isChecked) {
        if(isChecked) {
            callback.sendOpcode("9001");
        }else {
            callback.sendOpcode("0900");
        }
    }
}
