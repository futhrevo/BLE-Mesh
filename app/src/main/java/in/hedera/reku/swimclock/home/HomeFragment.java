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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.silabs.bluetooth_mesh.BluetoothMesh;
import com.silabs.bluetooth_mesh.DeviceInfo;
import com.silabs.bluetooth_mesh.GroupInfo;
import com.silabs.bluetooth_mesh.NetworkInfo;

import java.util.ArrayList;

import in.hedera.reku.swimclock.FragListener;
import in.hedera.reku.swimclock.MainActivity;
import in.hedera.reku.swimclock.R;
import in.hedera.reku.swimclock.utils.Constants;

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
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.toggleButton);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (networkInfo == null || networkInfo.devicesInfo().isEmpty()) {
                    showEmptyDevicesLayout();
                    return;
                }
                if(checkedId == R.id.toggle_devices) {
                    // The toggle is disabled, show devices
                    groupsRecyclerView.setVisibility(View.GONE);
                    devicesRecyclerView.setVisibility(View.VISIBLE);
                }else {
                    // The toggle is enabled, show groups
                    groupsRecyclerView.setVisibility(View.VISIBLE);
                    devicesRecyclerView.setVisibility(View.GONE);
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
        groupsAdapter = new GroupListAdapter(getContext(), HomeFragment.this);
        groupsRecyclerView.setAdapter(groupsAdapter);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        groupsRecyclerView.addOnItemTouchListener(
//                new RecyclerTouchListener(getContext(), groupsRecyclerView, new ClickListener() {
//                    @Override
//                    public void onClick(View view, int position) {
//                        Log.d(TAG, "Group clicked at " + position + " do on off");
//                        callback.onOffSet(null);
//                    }
//
//                    @Override
//                    public void onLongClick(View view, int position) {
//                        Log.d(TAG, "group long clicked at  " + position);
//                    }
//                }));
    }

    private void setUpDeviceRecycler() {
        devicesAdapter = new DeviceListAdapter(getContext(), HomeFragment.this);
        devicesRecyclerView.setAdapter(devicesAdapter);
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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

    private void showEmptyDevicesLayout() {
        emptyNetworkLayout.setVisibility(View.VISIBLE);
        devicesRecyclerView.setVisibility(View.GONE);
        groupsRecyclerView.setVisibility(View.GONE);
    }
    public void showNetInfo(NetworkInfo networkInfo) {
        this.networkInfo = networkInfo;
        if(Constants.mock) {
            GroupInfo groupInfo = networkInfo.groupById(0);
            for (int  i = 0; i < 5; i++) {
                DeviceInfo deviceInfo = new DeviceInfo("Test" + String.valueOf(i), BluetoothMesh.randomGenerator(16));
                deviceInfo.addToNetwork(networkInfo, i);
                networkInfo.addDevice(deviceInfo);
            }
        }
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
                showEmptyDevicesLayout();
            } else {
                emptyNetworkLayout.setVisibility(View.GONE);
                devicesRecyclerView.setVisibility(View.VISIBLE);
                devicesAdapter.setDevices(networkInfo.devicesInfo());
                groupsAdapter.setGroups(networkInfo.groupsInfo());
            }
        }
    }

    public void deviceAction(DeviceInfo deviceInfo) {
        Log.i(TAG, "User wants to on or off device");
        callback.onOffSet(deviceInfo, null);
    }

    public void addRemoveGroup(DeviceInfo deviceInfo) {
        Log.d(HomeFragment.TAG, "add/ remove group");
        callback.addRemoveGroup(deviceInfo, null);
    }

    public void factoryReset(DeviceInfo deviceInfo) {
        Log.d(HomeFragment.TAG, "Factory reset device");
        callback.factoryReset(deviceInfo);
    }

    public void groupAction(GroupInfo groupInfo) {
        callback.onOffSet(null, groupInfo);
    }

    public void listGroupDevices(GroupInfo groupInfo) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Devices List");
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.list_group_devices, null);
        alertDialog.setView(convertView);
        ListView lv = (ListView) convertView.findViewById(R.id.list_group_devices);
        ArrayList<DeviceInfo> devicesList = groupInfo.devicesList();
        if (Constants.mock) {
            for (int i = 0; i < 20; i++) {
                DeviceInfo deviceInfo = new DeviceInfo("Test" + String.valueOf(i), BluetoothMesh.randomGenerator(16));
                devicesList.add(deviceInfo);
            }
        }
        String[] devices = new String[devicesList.size()];
        int index = 0;
        for(DeviceInfo dvInfo: devicesList) {
            devices[index] = dvInfo.name();
            index++;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,devices);
        lv.setAdapter(adapter);
        alertDialog.setNegativeButton("Close", null);
        alertDialog.show();
    }
}
