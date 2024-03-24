package software.isratech.filetransferos;

import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        fragmentManager = getSupportFragmentManager();
        final Fragment menuScreenFragment = MenuScreenFragment.newInstance();
        setCurrentFragment(menuScreenFragment, true);
    }

    public static void backToPreviousMenu() {
        for (int i = 0; i < fragmentManager.getBackStackEntryCount() - 1; ++i) {
            fragmentManager.popBackStack();
        }
    }

    public static void backToMainMenu() {
        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
            fragmentManager.popBackStack();
        }
    }

    public static void setCurrentFragment(@NonNull final Fragment fragment, final boolean replaceActive) {
        final String fragmentName = getFragmentName(fragment);
        System.out.println(currentActiveFragment);
        if (currentActiveFragment == null) {
            fragmentManager.beginTransaction()
                    .add(android.R.id.content, fragment)
                    .commit();
        } else {
            if (replaceActive) {
                fragmentManager.popBackStack();
                fragmentManager.beginTransaction()
                        .replace(android.R.id.content, fragment)
                        .commit();
            }
            else {
                fragmentManager.beginTransaction()
                        .remove(currentActiveFragment)
                        .replace(android.R.id.content, fragment)
                        .addToBackStack(fragmentName)
                        .commit();
            }
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
        if (backPressedAction(getApplicationContext())) return;
        super.onBackPressed();
    }

    public static boolean backPressedAction(@NonNull final Context applicationContext) {
        if (currentActiveFragment instanceof SendingFileFragment || currentActiveFragment instanceof ReceivingFileFragment) {
            Toast.makeText(applicationContext, "Back button is disabled during file transfers!", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            return true;
        }
        return false;
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