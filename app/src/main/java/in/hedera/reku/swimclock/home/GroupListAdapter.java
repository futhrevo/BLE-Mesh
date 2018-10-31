package in.hedera.reku.swimclock.home;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.silabs.bluetooth_mesh.BluetoothMesh;
import com.silabs.bluetooth_mesh.DeviceInfo;
import com.silabs.bluetooth_mesh.GroupInfo;

import java.util.ArrayList;

import in.hedera.reku.swimclock.R;
import in.hedera.reku.swimclock.utils.Constants;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.GroupListViewHolder> {
    public static final String TAG = GroupListAdapter.class.getSimpleName();
    private ArrayList<GroupInfo> groupsInfo;
    private final LayoutInflater inflater;
    private Context context;
    private HomeFragment fragment;

    class GroupListViewHolder extends RecyclerView.ViewHolder {
        private TextView grpName;
        private TextView grpCount;
        private TextView grpOptions;

        private GroupListViewHolder(View itemView) {
            super(itemView);
            this.grpName = itemView.findViewById(R.id.group_name_item);
            this.grpCount = itemView.findViewById(R.id.group_count_item);
            grpOptions = itemView.findViewById(R.id.group_options);
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
        return new GroupListViewHolder(this.inflater.inflate(R.layout.list_group_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupListViewHolder holder, final int position) {
        if (groupsInfo != null) {
            final GroupInfo groupInfo = groupsInfo.get(position);
            ArrayList<DeviceInfo> devicesList = groupInfo.devicesList();
            if (Constants.mock) {
                for (int i = 0; i < 5; i++) {
                    DeviceInfo deviceInfo = new DeviceInfo("Test" + String.valueOf(i), BluetoothMesh.randomGenerator(16));
                    devicesList.add(deviceInfo);
                }
            }

            holder.grpName.setText(groupInfo.name());
            holder.grpCount.setText(String.valueOf(groupInfo.devicesList().size()));
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
                                    fragment.groupAction(groupInfo);
                                    return true;

                                case R.id.menu_group_list_devices:
                                    fragment.listGroupDevices(groupInfo);
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
        if (groupsInfo != null) {
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
