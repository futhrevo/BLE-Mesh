package in.hedera.reku.swimclock.home;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.silabs.bluetooth_mesh.DeviceInfo;
import java.util.ArrayList;

import in.hedera.reku.swimclock.R;

public class DeviceListAdapter extends Adapter<DeviceListAdapter.DeviceListViewHolder> {
    private ArrayList<DeviceInfo> devicesInfo;
    private final LayoutInflater inflater;

    class DeviceListViewHolder extends ViewHolder {
        private TextView diAddr;
        private TextView diName;

        private DeviceListViewHolder(View itemView) {
            super(itemView);
            this.diName = (TextView) itemView.findViewById(R.id.group_device_item);
            this.diAddr = (TextView) itemView.findViewById(R.id.mesh_address);
        }
    }

    public DeviceListAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    public DeviceListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new DeviceListViewHolder(this.inflater.inflate(R.layout.list_device_item, parent, false));
    }

    public void onBindViewHolder(@NonNull DeviceListViewHolder holder, int position) {
        if (this.devicesInfo != null) {
            DeviceInfo deviceInfo = (DeviceInfo) this.devicesInfo.get(position);
            holder.diName.setText(deviceInfo.name());
            holder.diAddr.setText(String.valueOf(deviceInfo.meshAddress()));
        }
    }

    void setDevices(ArrayList<DeviceInfo> devices) {
        this.devicesInfo = devices;
        notifyDataSetChanged();
    }

    public DeviceInfo getItemAtPosition(int position) {
        return (DeviceInfo) this.devicesInfo.get(position);
    }

    public int getItemCount() {
        if (this.devicesInfo != null) {
            return this.devicesInfo.size();
        }
        return 0;
    }
}
