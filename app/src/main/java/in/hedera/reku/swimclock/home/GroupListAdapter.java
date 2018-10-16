package in.hedera.reku.swimclock.home;

import android.content.Context;
import android.service.autofill.TextValueSanitizer;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.silabs.bluetooth_mesh.DeviceInfo;
import com.silabs.bluetooth_mesh.GroupInfo;

import java.util.ArrayList;

import in.hedera.reku.swimclock.R;

public class GroupListAdapter  extends RecyclerView.Adapter<GroupListAdapter.GroupListViewHolder> {

    private ArrayList<GroupInfo> groupsInfo;
    private final LayoutInflater inflater;

    class GroupListViewHolder extends RecyclerView.ViewHolder {
        private TextView grpName;
        private TextView grpCount;

        private GroupListViewHolder(View itemView) {
            super(itemView);
            this.grpName = itemView.findViewById(R.id.group_name_item);
            this.grpCount = itemView.findViewById(R.id.group_count_item);
        }
    }

    public GroupListAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public GroupListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new GroupListViewHolder(this.inflater.inflate(R.layout.list_group_item, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupListViewHolder holder, int position) {
        if(groupsInfo != null) {
            GroupInfo groupInfo = groupsInfo.get(position);
            holder.grpName.setText(groupInfo.name());
            holder.grpCount.setText(String.valueOf(groupInfo.devicesList().size()));
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
        this.groupsInfo = groups;
        notifyDataSetChanged();
    }

    public GroupInfo getItemAtPosition(int position) {
        return groupsInfo.get(position);
    }
}
