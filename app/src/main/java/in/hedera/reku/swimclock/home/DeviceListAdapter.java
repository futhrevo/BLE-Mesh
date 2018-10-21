package in.hedera.reku.swimclock.home;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
    public static final String TAG = DeviceListAdapter.class.getSimpleName();

    class DeviceListViewHolder extends RecyclerView.ViewHolder {

        private TextView diName;
        private TextView diAddr;
        private TextView diOptions;

        private DeviceListViewHolder(View itemView) {
            super(itemView);
            diName = itemView.findViewById(R.id.group_device_item);
            diAddr = itemView.findViewById(R.id.mesh_address);
            diOptions = itemView.findViewById(R.id.textViewOptions);
        }
    }

    private final LayoutInflater inflater;
    private ArrayList<DeviceInfo> devicesInfo;
    private Context context;
    private HomeFragment fragment;

    public DeviceListAdapter(Context context, HomeFragment homeFragment) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.fragment = homeFragment;
    }

    @NonNull
    @Override
    public DeviceListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = inflater.inflate(R.layout.list_device_item, parent, false);
        return new DeviceListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final DeviceListViewHolder holder, final int position) {
        if (devicesInfo != null) {
            DeviceInfo deviceInfo = devicesInfo.get(position);
            holder.diName.setText(deviceInfo.name());
            holder.diAddr.setText(String.valueOf(deviceInfo.meshAddress()));
            holder.diOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(context, holder.diOptions);

                    //inflating menu from xml resource
                    popup.inflate(R.menu.device_options);

                    // get device info
                    final DeviceInfo deviceInfo = getItemAtPosition(position);

                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.menu_add_remove:
                                    fragment.addRemoveGroup(deviceInfo);
                                    return true;

                                case R.id.menu_factory_reset:
                                    fragment.factoryReset(deviceInfo);
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
