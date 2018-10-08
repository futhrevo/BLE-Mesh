package in.hedera.reku.swimclock.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.silabs.bluetooth_mesh.DeviceInfo;
import com.silabs.bluetooth_mesh.GroupInfo;

import java.util.ArrayList;

import in.hedera.reku.swimclock.R;

/**
 * Created by rakeshkalyankar on 24/09/18.
 * Contact k.rakeshlal@gmail.com for licence
 */
public class GroupExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<GroupInfo> groupInfos;

    public GroupExpandableListAdapter(Context context, ArrayList<GroupInfo> groupInfos) {
        this.context = context;
        this.groupInfos = groupInfos;
    }

    @Override
    public int getGroupCount() {
        return groupInfos.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groupInfos.get(groupPosition).devicesList().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupInfos.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groupInfos.get(groupPosition).devicesList().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupInfo groupInfo = (GroupInfo) getGroup(groupPosition);
        if(convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
        }
        TextView groupTitle = convertView.findViewById(R.id.group_title);
        groupTitle.setText(groupInfo.name());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        DeviceInfo deviceInfo = (DeviceInfo) getChild(groupPosition, childPosition);
        if(convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_device_item, null);
        }
        TextView deviceTitleView = convertView.findViewById(R.id.group_device_item);
        deviceTitleView.setText(deviceInfo.name());
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
