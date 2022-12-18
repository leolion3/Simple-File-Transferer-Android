package software.isratech.filetransferos;

import android.os.Bundle;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import software.isratech.filetransferos.view.MenuScreenFragment;

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

    public static void setCurrentFragment(@NonNull final Fragment fragment) {
        if (currentActiveFragment == null) {
            fragmentManager.beginTransaction()
                    .add(android.R.id.content, fragment)
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commit();
        }
        currentActiveFragment = fragment;
    }
}