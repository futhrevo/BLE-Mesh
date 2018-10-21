package in.hedera.reku.swimclock.home;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.silabs.bluetooth_mesh.BluetoothMesh;
import com.silabs.bluetooth_mesh.DeviceInfo;
import com.silabs.bluetooth_mesh.GroupInfo;

import java.util.ArrayList;

import in.hedera.reku.swimclock.R;
import in.hedera.reku.swimclock.utils.Constants;

public class GroupListAdapter  extends RecyclerView.Adapter<GroupListAdapter.GroupListViewHolder> {
    public static final String TAG = GroupListAdapter.class.getSimpleName();
    private ArrayList<GroupInfo> groupsInfo;
    private final LayoutInflater inflater;
    private Context context;
    private HomeFragment fragment;

    class GroupListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView grpName;
        private TextView grpCount;
        private LinearLayout grpItems;
        private TextView grpOptions;

        private GroupListViewHolder(View itemView) {
            super(itemView);
            this.grpName = itemView.findViewById(R.id.group_name_item);
            this.grpCount = itemView.findViewById(R.id.group_count_item);
            this.grpItems = itemView.findViewById(R.id.group_child_items);
            grpOptions = itemView.findViewById(R.id.group_options);
            grpItems.setVisibility(View.GONE);
            int intMaxNoOfChild = 0;
            for (int index = 0; index < groupsInfo.size(); index++) {
                int intMaxSizeTemp = groupsInfo.get(index).devicesList().size();
                if (intMaxSizeTemp > intMaxNoOfChild) intMaxNoOfChild = intMaxSizeTemp;
            }
            if(Constants.mock) {
                intMaxNoOfChild = 5;
            }
            for (int indexView = 0; indexView < intMaxNoOfChild; indexView++) {
                TextView textView = new TextView(context);
                textView.setId(indexView);
                textView.setPadding(0, 20, 0, 20);
                textView.setGravity(Gravity.CENTER);
                textView.setBackground(ContextCompat.getDrawable(context, R.drawable.background_sub_module_text));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                textView.setOnClickListener(this);
                grpItems.addView(textView, layoutParams);
            }
            grpName.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.d(TAG, "Clicked on group");
            if (view.getId() == R.id.group_name_item) {
                if(grpItems.getVisibility() == View.VISIBLE) {
                    grpItems.setVisibility(View.GONE);
                } else {
                    grpItems.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public GroupListAdapter(Context context, HomeFragment homeFragment) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.fragment = homeFragment;
    }

    @NonNull
    @Override
    public GroupListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        Log.d(TAG, "onCreateViewHolder");
        return new GroupListViewHolder(this.inflater.inflate(R.layout.list_group_item, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupListViewHolder holder, final int position) {
        if(groupsInfo != null) {
            GroupInfo groupInfo = groupsInfo.get(position);
            ArrayList<DeviceInfo> devicesList = groupInfo.devicesList();
            if(Constants.mock) {
                for (int  i = 0; i < 5; i++) {
                    DeviceInfo deviceInfo = new DeviceInfo("Test" + String.valueOf(i), BluetoothMesh.randomGenerator(16));
                    devicesList.add(deviceInfo);
                }
            }

            holder.grpName.setText(groupInfo.name());
            holder.grpCount.setText(String.valueOf(groupInfo.devicesList().size()));
            int noOfChildTextViews = holder.grpItems.getChildCount();
            int noOfChild = devicesList.size();
            if (noOfChild < noOfChildTextViews) {
                for (int index = noOfChild; index < noOfChildTextViews; index++) {
                    TextView currentTextView = (TextView) holder.grpItems.getChildAt(index);
                    currentTextView.setVisibility(View.GONE);
                }
            }
            for (int textViewIndex = 0; textViewIndex < noOfChild; textViewIndex++) {
                TextView currentTextView = (TextView) holder.grpItems.getChildAt(textViewIndex);
                currentTextView.setText(devicesList.get(textViewIndex).name());
            }
            holder.grpOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(context, holder.grpOptions);

                    //inflating menu from xml resource
                    popup.inflate(R.menu.group_options);

                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.menu_group_action:

                                    return true;
                            }
                            return false;
                        }
                    });

                    //displaying the popup
                    popup.show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if(groupsInfo != null) {
            return groupsInfo.size();
        }
        return 0;
    }

    void setGroups(ArrayList<GroupInfo> groups) {
        groupsInfo = groups;
        notifyDataSetChanged();
    }

    public GroupInfo getItemAtPosition(int position) {
        return groupsInfo.get(position);
    }
}
