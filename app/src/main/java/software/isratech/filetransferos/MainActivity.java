package software.isratech.filetransferos;

import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

import software.isratech.filetransferos.view.MenuScreenFragment;
import software.isratech.filetransferos.view.receive.ReceiveFileSettingsFragment;
import software.isratech.filetransferos.view.receive.ReceivingFileFragment;
import software.isratech.filetransferos.view.send.PickFileFragment;
import software.isratech.filetransferos.view.send.SendingFileFragment;

public class MainActivity extends AppCompatActivity {

    @Nullable
    static Fragment currentActiveFragment;

    static FragmentManager fragmentManager;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        fragmentManager = getSupportFragmentManager();
        final Fragment menuScreenFragment = MenuScreenFragment.newInstance();
        setCurrentFragment(menuScreenFragment);
    }

    public static void backToMainMenu() {
        for(int i = 0; i < fragmentManager.getBackStackEntryCount() - 1; ++i) {
            fragmentManager.popBackStack();
        }
    }

    public static void setCurrentFragment(@NonNull final Fragment fragment) {
        final String fragmentName = getFragmentName(fragment);
        if (currentActiveFragment == null) {
            fragmentManager.beginTransaction()
                    .add(android.R.id.content, fragment)
                    .addToBackStack(fragmentName)
                    .commit();
        } else if (fragmentManager.getBackStackEntryCount() > 1 && checkFragmentAlreadyOnStack(fragmentManager, fragmentName)) {
            fragmentManager.popBackStack();
        } else {
            fragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack(fragmentName)
                    .commit();
        }
        currentActiveFragment = fragment;
    }

    private static boolean checkFragmentAlreadyOnStack(@NonNull final FragmentManager fragmentManager, @NonNull final String fragmentName) {
        final List<String> fragments = new ArrayList<>();
        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
            fragments.add(fragmentManager.getBackStackEntryAt(i).getName());
        }
        return fragments.contains(fragmentName);
    }

    @Override
    public void onBackPressed() {
        if (currentActiveFragment instanceof SendingFileFragment || currentActiveFragment instanceof ReceivingFileFragment) {
            Toast.makeText(getApplicationContext(), "Back button is disabled during file transfers!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            return;
        }
        super.onBackPressed();
    }

    private static String getFragmentName(Fragment fragment) {
        if (fragment instanceof MenuScreenFragment) {
            return "MenuScreen";
        } else if (fragment instanceof ReceiveFileSettingsFragment) {
            return "ReceiveFileSettings";
        } else if (fragment instanceof PickFileFragment) {
            return "SendingFileSettings";
        } else if (fragment instanceof ReceivingFileFragment) {
            return "ReceivingFileFragment";
        } else if (fragment instanceof SendingFileFragment) {
            return "SendingFileFragment";
        }
        return "Unknown";
    }
}