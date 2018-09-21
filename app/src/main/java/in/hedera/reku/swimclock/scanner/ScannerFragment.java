package in.hedera.reku.swimclock.scanner;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;

import java.util.List;

import in.hedera.reku.swimclock.Constants;
import in.hedera.reku.swimclock.R;
import in.hedera.reku.swimclock.store.NPDevice.NPDevice;

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

    public ScannerFragment() {
        // Required empty public constructor
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
        RecyclerView recyclerView = view.findViewById(R.id.deviceList);
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
        } else {
            item.setTitle("SCAN");
        }
    }

    @Override
    public void candidateBleDevice(final BluetoothDevice device, byte[] scan_record, final int rssi) {
        NPDevice npDevice = new NPDevice(device, rssi);
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
            bleScanner.startScanning(this, Constants.SCAN_TIMEOUT);
        } else {
            bleScanner.stopScanning();
        }

    }
}
