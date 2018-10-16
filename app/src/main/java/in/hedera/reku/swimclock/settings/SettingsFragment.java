package in.hedera.reku.swimclock.settings;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

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
        addPreferencesFromResource(R.xml.app_preferences);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
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
