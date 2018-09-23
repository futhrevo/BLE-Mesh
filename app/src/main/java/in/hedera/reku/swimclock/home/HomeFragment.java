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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

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
    final Handler handler = new Handler();

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
        View view =  inflater.inflate(R.layout.fragment_home, container, false);
        progressBar = view.findViewById(R.id.progress_bar_nw);
        Button button = view.findViewById(R.id.create_nw_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddNetworkDialog(getContext());
            }
        });
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
        Log.d(TAG, "create a network with name : " + nw );
        callback.createNetwork(nw);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showProgressBar(false);
            }
        }, 5000);
    }

    public void onNetworkCreated() {
        showProgressBar(false);
    }

    private void showProgressBar(boolean show) {
        if(show) {
            progressBar.setVisibility(View.VISIBLE);
            try{
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.e(TAG, "Unable to update activity flags");
            }

        } else {
            progressBar.setVisibility(View.GONE);
            try{
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.e(TAG, "Unable to update activity flags");
            }
        }
    }
}
