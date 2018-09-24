package in.hedera.reku.swimclock.scanner;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import in.hedera.reku.swimclock.R;
import in.hedera.reku.swimclock.store.NPDevice.NPDevice;

public class NPDeviceAdapter extends RecyclerView.Adapter<NPDeviceAdapter.NPDeviceViewHolder> {

    class NPDeviceViewHolder extends RecyclerView.ViewHolder {
        private TextView bdname;
        private TextView bdaddr;
        private TextView bdrssi;
        private TextView bdadv;

        private NPDeviceViewHolder(View itemView) {
            super(itemView);
            bdname = itemView.findViewById(R.id.bdname);
            bdrssi = itemView.findViewById(R.id.bdrssi);
            bdaddr = itemView.findViewById(R.id.bdaddr);
            bdadv = itemView.findViewById(R.id.bdadv);
        }
    }

    private final LayoutInflater inflater;
    private List<NPDevice> npDevices; // cached copy

    NPDeviceAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public NPDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.scan_list_item, parent, false);
        return new NPDeviceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NPDeviceViewHolder holder, int position) {
        if(npDevices != null) {
            NPDevice current = npDevices.get(position);
            holder.bdaddr.setText(current.getMac());
            holder.bdname.setText(current.getName());
            holder.bdrssi.setText(current.getRssi() + " dBm");
            holder.bdadv.setText(current.getAdv());
        } else {
            holder.bdname.setText("NO DEVICES FOUND");
        }
    }

    void setNpDevices(List<NPDevice> devices) {
        npDevices = devices;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(npDevices != null) return npDevices.size();
        return 0;
    }
}
