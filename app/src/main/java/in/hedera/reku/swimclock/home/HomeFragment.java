package in.hedera.reku.swimclock.home;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.silabs.bluetooth_mesh.DeviceInfo;
import com.silabs.bluetooth_mesh.NetworkInfo;

import in.hedera.reku.swimclock.FragListener;
import in.hedera.reku.swimclock.MainActivity;
import in.hedera.reku.swimclock.R;
import in.hedera.reku.swimclock.scanner.ClickListener;
import in.hedera.reku.swimclock.utils.RecyclerTouchListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    public static final String TAG = HomeFragment.class.getSimpleName();
    FragListener callback;

    private Menu menu;
    private ProgressBar progressBar;
    private LinearLayout emptyLayout;
    private LinearLayout networkLayout;
    private DeviceListAdapter devicesAdapter;
    private GroupListAdapter groupsAdapter;
    private RecyclerView devicesRecyclerView;
    private RecyclerView groupsRecyclerView;
    private LinearLayout emptyNetworkLayout;
    final Handler handler = new Handler();
    private NetworkInfo networkInfo;
    private DeviceListAdapter adapter;

    public HomeFragment() {
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "returned to home");
        callback.readNetInfo(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        progressBar = view.findViewById(R.id.progress_bar_nw);
        emptyLayout = view.findViewById(R.id.empty_nw_layout);
        networkLayout = view.findViewById(R.id.network_layout);
        // expandableListView = view.findViewById(R.id.expandable_group_list);
        emptyNetworkLayout = view.findViewById(R.id.group_list_emptyview);
        devicesRecyclerView = (RecyclerView) view.findViewById(R.id.home_devices_list);
        groupsRecyclerView = view.findViewById(R.id.home_group_list);
        setUpDeviceRecycler();
        setupGroupsRecycler();
        ToggleButton toggle = (ToggleButton) view.findViewById(R.id.toggleButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (networkInfo != null) {
                    if (isChecked) {
                        // The toggle is enabled, show groups
                        groupsRecyclerView.setVisibility(View.VISIBLE);
                        devicesRecyclerView.setVisibility(View.GONE);
                    } else {
                        // The toggle is disabled, show devices
                        groupsRecyclerView.setVisibility(View.GONE);
                        devicesRecyclerView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        Button button = view.findViewById(R.id.create_nw_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddNetworkDialog(getContext());
            }
        });
        Button provisonScanButton = view.findViewById(R.id.scan_provision_button);
        provisonScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.gotoScannerScreen();
            }
        });
        callback.readNetInfo(this);
        return view;
    }

    private void setupGroupsRecycler() {
        groupsAdapter = new GroupListAdapter(getContext());
        groupsRecyclerView.setAdapter(groupsAdapter);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupsRecyclerView.addOnItemTouchListener(
                new RecyclerTouchListener(getContext(), groupsRecyclerView, new ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        Log.d(TAG, "Group clicked at " + position + " do on off");
                        callback.onOffSet(null);
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                        Log.d(TAG, "group long clicked at  " + position);
                    }
                }));
    }

    private void setUpDeviceRecycler() {
        devicesAdapter = new DeviceListAdapter(getContext());
        devicesRecyclerView.setAdapter(devicesAdapter);
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        devicesRecyclerView.addOnItemTouchListener(
                new RecyclerTouchListener(getContext(), this.devicesRecyclerView, new ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        DeviceInfo deviceInfo = HomeFragment.this.devicesAdapter.getItemAtPosition(position);
                        String str = HomeFragment.TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("item clicked at ");
                        stringBuilder.append(deviceInfo.name());
                        Log.d(str, stringBuilder.toString());
                        Log.i(TAG, "User wants to on or off device");
                        callback.onOffSet(deviceInfo);
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                        Log.d(HomeFragment.TAG, "item long clicked, add/ remove group");
                        DeviceInfo deviceInfo = devicesAdapter.getItemAtPosition(position);
                        callback.addRemoveGroup(deviceInfo);
                    }
                }));
    }

    @Override
    public void onDestroyView() {
        callback = null;
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        inflater.inflate(R.menu.home, menu);
        return;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.connect_nw:
            Log.d(TAG, "User wants to connect Network");
            callback.connectNetwork();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }

    }

    private void showAddNetworkDialog(Context c) {
        final EditText taskEditText = new EditText(c);
        AlertDialog dialog = new AlertDialog.Builder(c).setTitle("Create new Network").setMessage("Name for network")
                .setView(taskEditText).setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String task = String.valueOf(taskEditText.getText());
                        startNetworkCreation(task);
                    }
                }).setNegativeButton("Cancel", null).create();
        dialog.show();
    }

    private void startNetworkCreation(String nw) {
        showProgressBar(true);
        Log.d(TAG, "create a network with name : " + nw);
        callback.createNetwork(nw);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showProgressBar(false);
            }
        }, 5000);
    }

    public void onNetworkCreated(NetworkInfo defaultNetwork) {
        showProgressBar(false);
        showNetInfo(defaultNetwork);
    }

    private void showProgressBar(boolean show) {
        if (callback == null)
            return;
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            callback.disableUIinteraction(true);
        } else {
            progressBar.setVisibility(View.GONE);
            callback.disableUIinteraction(false);
        }
    }

    private void showEmptyLayout(boolean empty) {
        if (empty) {
            emptyLayout.setVisibility(View.VISIBLE);
            networkLayout.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            networkLayout.setVisibility(View.VISIBLE);
        }
    }

    public void showNetInfo(NetworkInfo networkInfo) {
        this.networkInfo = networkInfo;
        if (networkInfo == null) {
            showEmptyLayout(true);
        } else {
            showEmptyLayout(false);
            int deviceCount = networkInfo.devicesInfo().size();
            TextView networkTitle = networkLayout.findViewById(R.id.network_title);
            networkTitle.setText(networkInfo.name());
            TextView groupCount = networkLayout.findViewById(R.id.device_count);

            groupCount.setText(String.valueOf(deviceCount));
            if (deviceCount == 0) {
                emptyNetworkLayout.setVisibility(View.VISIBLE);
                devicesRecyclerView.setVisibility(View.GONE);
                groupsRecyclerView.setVisibility(View.GONE);
            } else {
                emptyNetworkLayout.setVisibility(View.GONE);
                devicesRecyclerView.setVisibility(View.VISIBLE);
                devicesAdapter.setDevices(networkInfo.devicesInfo());
                groupsAdapter.setGroups(networkInfo.groupsInfo());
            }
        }
    }
}
