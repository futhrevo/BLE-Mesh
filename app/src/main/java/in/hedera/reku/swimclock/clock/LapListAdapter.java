package in.hedera.reku.swimclock.clock;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import in.hedera.reku.swimclock.R;
import in.hedera.reku.swimclock.utils.LapSet;

/**
 * Created by rakeshkalyankar on 15/12/18.
 */
public class LapListAdapter extends ArrayAdapter<LapSet> {
    public static final String TAG = LapListAdapter.class.getSimpleName();

    private List<LapSet> items;
    private int layoutResourceId;
    private Context context;

    public LapListAdapter(Context context, int resource, List<LapSet> objects) {
        super(context, resource, objects);
        this.layoutResourceId = resource;
        this.context = context;
        this.items = objects;
    }

    @Nullable
    @Override
    public LapSet getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);

        LapSetHolder holder = new LapSetHolder();
        holder.lapSet = items.get(position);

        holder.lapNo = row.findViewById(R.id.tv_lap_no);
        holder.lapNo.setText("[" + (position + 1) + "]");
        holder.lapClearBtn = row.findViewById(R.id.set_clear);
        holder.lapClearBtn.setTag(holder.lapSet);
        holder.lapClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LapSet toremove = (LapSet) v.getTag();
                items.remove(toremove);
                notifyDataSetChanged();
            }
        });

        holder.etRepCount = row.findViewById(R.id.et_rep_count);
        holder.etIntMin = row.findViewById(R.id.et_int_min);
        holder.etIntSec = row.findViewById(R.id.et_int_sec);
        holder.etIntms = row.findViewById(R.id.et_int_ms);

        holder.etRepCount.setText(String.valueOf(holder.lapSet.getlapcountInt()));
        holder.etIntMin.setText(String.valueOf(holder.lapSet.getMinute()));
        holder.etIntSec.setText(String.valueOf(holder.lapSet.getSecond()));
        holder.etIntms.setText(String.valueOf(holder.lapSet.getMilliseconds()));

        setRepCountChangeListener(holder);
        setIntMinChangeListener(holder);
        setIntSecChangeListener(holder);
        setIntmsChangeListener(holder);

        return row;
    }

    public List<LapSet> getLaps() {
        return items;
    }
    public static class LapSetHolder {
        LapSet lapSet;
        TextView lapNo;
         TextView etRepCount;
        TextView etIntMin;
        TextView etIntSec;
        TextView etIntms;
        ImageButton lapClearBtn;
    }

    private void setRepCountChangeListener(final LapSetHolder holder) {
        holder.etRepCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                holder.lapSet.setLapcount(s.toString().equals("") ? 0 : Integer.parseInt(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setIntMinChangeListener(final LapSetHolder holder) {
        holder.etIntMin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                holder.lapSet.setMinute(s.toString().equals("") ? 0 : Integer.parseInt(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private void setIntSecChangeListener(final LapSetHolder holder) {
        holder.etIntSec.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                holder.lapSet.setSecond(s.toString().equals("") ? 0 : Integer.parseInt(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setIntmsChangeListener(final LapSetHolder holder) {
        holder.etIntms.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                holder.lapSet.setMilliseconds(s.toString().equals("") ? 0 : Integer.parseInt(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
