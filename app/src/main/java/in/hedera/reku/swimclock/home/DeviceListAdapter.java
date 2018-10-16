package in.hedera.reku.swimclock.home;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.silabs.bluetooth_mesh.DeviceInfo;
import java.util.ArrayList;

import in.hedera.reku.swimclock.R;

/**
 * Created by rakeshkalyankar on 02/10/18. Contact k.rakeshlal@gmail.com for
 * licence
 */
public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceListViewHolder> {

    class DeviceListViewHolder extends RecyclerView.ViewHolder {

        private TextView diName;
        private TextView diAddr;

        private DeviceListViewHolder(View itemView) {
            super(itemView);
            diName = itemView.findViewById(R.id.group_device_item);
            diAddr = itemView.findViewById(R.id.mesh_address);
        }
    }

    private final LayoutInflater inflater;
    private ArrayList<DeviceInfo> devicesInfo;

    public DeviceListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public DeviceListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = inflater.inflate(R.layout.list_device_item, parent, false);
        return new DeviceListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceListViewHolder holder, int position) {
        if (devicesInfo != null) {
            DeviceInfo deviceInfo = devicesInfo.get(position);
            holder.diName.setText(deviceInfo.name());
            holder.diAddr.setText(String.valueOf(deviceInfo.meshAddress()));
        }
    }

    void setDevices(ArrayList<DeviceInfo> devices) {
        devicesInfo = devices;
        notifyDataSetChanged();
    }

    public DeviceInfo getItemAtPosition(int position) {
        return devicesInfo.get(position);
    }

    @Override
    public int getItemCount() {
        if (devicesInfo != null)
            return devicesInfo.size();
        return 0;
    }
}
