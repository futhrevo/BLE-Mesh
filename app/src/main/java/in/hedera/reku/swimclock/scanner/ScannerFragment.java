package in.hedera.reku.swimclock.scanner;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.silabs.bluetooth_mesh.BluetoothMesh;

import java.util.ArrayList;
import java.util.List;

import in.hedera.reku.swimclock.FragListener;
import in.hedera.reku.swimclock.MainActivity;
import in.hedera.reku.swimclock.R;
import in.hedera.reku.swimclock.store.NPDevice.NPDevice;
import in.hedera.reku.swimclock.utils.Constants;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScannerFragment extends Fragment implements ScannerInterface {
    public static final String TAG = ScannerFragment.class.getSimpleName();
    private static final long SCAN_TIMEOUT = 5000;

    private boolean ble_scanning = false;
    private Handler handler = new Handler();

    private BleScanner bleScanner;
    private Toast toast;
    private Menu menu;
    private NPDViewModel npdViewModel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    FragListener callback;

    public ScannerFragment() {
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
    public void onDetach() {
        callback = null;
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        npdViewModel = ViewModelProviders.of(this).get(NPDViewModel.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);
        recyclerView = view.findViewById(R.id.deviceList);
        emptyView = view.findViewById(R.id.empty_scan_list);
        progressBar = view.findViewById(R.id.progress_bar_scan);
        final NPDeviceAdapter adapter = new NPDeviceAdapter(getContext());
        recyclerView.setAdapter(adapter);
        bleScanner = new BleScanner(getContext());
        recyclerView.setLayoutManager( new LinearLayoutManager(getContext()));
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (ble_scanning) {
//                    setScanState(false);
//                    bleScanner.stopScanning();
//                }
//                ScanDevice device = scanListAdapter.getDevice(position);
//                if (toast != null) {
//                    toast.cancel();
//                }
//            }
//        });
        npdViewModel.getAllNPdevices().observe(this, new Observer<List<NPDevice>>() {
            @Override
            public void onChanged(@Nullable List<NPDevice> npDevices) {
                adapter.setNpDevices(npDevices);
                toggleEmptyLayout(!ble_scanning && npDevices.isEmpty());
            }
        });
        Log.d(TAG, "oncreateview");
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        inflater.inflate(R.menu.scan_options, menu);
        return;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan_menu:
                toggleScanning();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void setScanState(boolean b) {
        ble_scanning = b;
        MenuItem item = menu.findItem(R.id.scan_menu);
        if (b) {
            item.setTitle("STOP");
            progressBar.setVisibility(View.VISIBLE);
        } else {
            item.setTitle("SCAN");
            progressBar.setVisibility(View.GONE);
        }
    }

    private void toggleEmptyLayout(boolean empty) {
        if(empty) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
    @Override
    public void scanResult(ScanResult result) {
        NPDevice npDevice = new NPDevice(result.getDevice(), result.getRssi());
        npdViewModel.insert(npDevice);
    }

    @Override
    public void scanningStarted() {
        setScanState(true);
    }

    @Override
    public void scanningStopped() {
        if (toast != null) {
            toast.cancel();
        }
        setScanState(false);
    }

    private void simpleToast(String msg, int duration) {
        toast = Toast.makeText(getContext(), msg, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void toggleScanning() {
        if(!bleScanner.isScanning()){
            npdViewModel.deleteAll();
            simpleToast(Constants.SCANNING, 2000);
            List<ScanFilter> filters = new ArrayList<>();
            ParcelUuid meshServ = ParcelUuid.fromString(BluetoothMesh.meshUnprovisionedService.toString());
            ScanFilter filter = new ScanFilter.Builder().setServiceUuid(meshServ).build();
            // TODO: Enable this line when testing with mesh devices
//          filters.add(filter);
            bleScanner.startScanning(this, Constants.SCAN_TIMEOUT, filters);
        } else {
            bleScanner.stopScanning();
        }

    }
}
