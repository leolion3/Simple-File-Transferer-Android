package software.isratech.filetransferos.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import software.isratech.filetransferos.MainActivity;
import software.isratech.filetransferos.R;
import software.isratech.filetransferos.view.receive.ReceiveFileSettingsFragment;
import software.isratech.filetransferos.view.send.PickFileFragment;


@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class MenuScreenFragment extends Fragment {

    /**
     * Creates a new instance of the menu screen fragment
     */
    public static MenuScreenFragment newInstance() {
        MenuScreenFragment fragment = new MenuScreenFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Button sendFileButton = requireView().findViewById(R.id.menuScreenSendFileButton);
        final Button receiveFileButton = requireView().findViewById(R.id.menuScreenReceiveFileButton);
        sendFileButton.setOnClickListener(v -> {
            MainActivity.setCurrentFragment(PickFileFragment.newInstance());
        });
        receiveFileButton.setOnClickListener(v -> {
            MainActivity.setCurrentFragment(ReceiveFileSettingsFragment.newInstance());
        });
    }

    /**
     * On create view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu_screen, container, false);
    }

    /**
     * On create
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}