package in.hedera.reku.swimclock.clock;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.hedera.reku.swimclock.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClockFragment extends Fragment {
    private FragmentTabHost mTabHost;

    public ClockFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.clock_tablayout,container, false);
        mTabHost = (FragmentTabHost)rootView.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);
        mTabHost.addTab(mTabHost.newTabSpec("wallFrag").setIndicator("Wall Clock"), WallFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("paceFrag").setIndicator("Pace Clock"), PaceFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("lapFrag").setIndicator("Lap Clock"), LapFragment.class, null);
        return rootView;
    }

}
