package in.hedera.reku.swimclock.settings;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SeekBarPreference;
import android.util.Log;

import java.util.Objects;

import in.hedera.reku.swimclock.MainActivity;
import in.hedera.reku.swimclock.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        addPreferencesFromResource(R.xml.app_preferences);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SeekBarPreference seekBar = (SeekBarPreference) findPreference(getString(R.string.pref_scan_timeout));
        if(seekBar != null) {
            seekBar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if(o instanceof Integer) {
                        Log.d(TAG, "value is " + String.valueOf(o));
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(getString(R.string.pref_scan_timeout), (Integer) o);
                        editor.apply();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        Log.d(TAG, preference.getKey());
        if(preference.getKey().equals(getString(R.string.pref_delete_network))) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
            builder.setMessage("Are you sure ? ").setTitle("Delete Network");
            builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    ((MainActivity) getActivity()).deleteNetwork();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        }
        return super.onPreferenceTreeClick(preference);
    }
}
