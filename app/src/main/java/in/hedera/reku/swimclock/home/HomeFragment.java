package in.hedera.reku.swimclock.home;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.silabs.bluetooth_mesh.NetworkInfo;

import in.hedera.reku.swimclock.FragListener;
import in.hedera.reku.swimclock.MainActivity;
import in.hedera.reku.swimclock.R;


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
    private ExpandableListView expandableListView;
    private GroupExpandableListAdapter groupExpandableListAdapter;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        progressBar = view.findViewById(R.id.progress_bar_nw);
        emptyLayout = view.findViewById(R.id.empty_nw_layout);
        networkLayout = view.findViewById(R.id.network_layout);
        expandableListView = view.findViewById(R.id.expandable_group_list);
        emptyNetworkLayout = view.findViewById(R.id.group_list_emptyview);
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
        callback.readNetInfo();
        return view;
    }

    @Override
    public void onDetach() {
        callback = null;
        super.onDetach();
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
            case R.id.menu_delete_nw:
                Log.d(TAG, "delete network");
                callback.deleteNetwork();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void showAddNetworkDialog(Context c) {
        final EditText taskEditText = new EditText(c);
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Create new Network")
                .setMessage("Name for network")
                .setView(taskEditText)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String task = String.valueOf(taskEditText.getText());
                        startNetworkCreation(task);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
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
            if(deviceCount == 0) {
                emptyNetworkLayout.setVisibility(View.VISIBLE);
                expandableListView.setVisibility(View.GONE);
            } else {
                emptyNetworkLayout.setVisibility(View.GONE);
                expandableListView.setVisibility(View.VISIBLE);
                groupExpandableListAdapter = new GroupExpandableListAdapter(getContext(), networkInfo.groupsInfo());
                expandableListView.setAdapter(groupExpandableListAdapter);
            }
        }
    }
}
