package software.isratech.filetransferos.view.send;

import static software.isratech.filetransferos.Constants.DEFAULT_BIND_ADDRESS;
import static software.isratech.filetransferos.Constants.DEFAULT_PORT;
import static software.isratech.filetransferos.utils.AndroidFileAccessUtils.getNameFromContentUri;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import software.isratech.filetransferos.MainActivity;
import software.isratech.filetransferos.R;
import software.isratech.filetransferos.view.MenuScreenFragment;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PickFileFragment extends Fragment {

    private EditText chosenFilePathEditText;
    private Button sendFileButton;
    private Button backButton;
    private SwitchCompat advancedNetworkSettingsSwitch;
    private TextView networkSettingsText;
    private EditText ipAddressEditText;
    private EditText portEditText;

    private Uri uploadFileUri;
    private String fileName;

    public static PickFileFragment newInstance() {
        PickFileFragment fragment = new PickFileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void sendFile() {
        if (uploadFileUri == null) {
            Toast.makeText(getContext(), "No file selected!", Toast.LENGTH_SHORT).show();
            return;
        }
        final String ipAddress = getIpAddress();
        final int port = getPort();
        requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        MainActivity.setCurrentFragment(SendingFileFragment.newInstance(uploadFileUri, fileName, ipAddress, port));
    }

    @NonNull
    private String getIpAddress() {
        if (!advancedNetworkSettingsSwitch.isChecked()) return DEFAULT_BIND_ADDRESS;
        if (ipAddressEditText.getText() != null && !ipAddressEditText.getText().toString().isEmpty()) {
            return ipAddressEditText.getText().toString();
        }
        Toast.makeText(getContext(), "IP Address left blank, using default.", Toast.LENGTH_SHORT).show();
        return DEFAULT_BIND_ADDRESS;
    }

    private int getPort() {
        if (!advancedNetworkSettingsSwitch.isChecked()) return DEFAULT_PORT;
        if (portEditText.getText() != null && !portEditText.getText().toString().isEmpty()) {
            try {
                return Integer.parseInt(portEditText.getText().toString());
            } catch (Exception e) {
                Toast.makeText(getContext(), "Incorrect port entered! Using default.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Port left blank, using default.", Toast.LENGTH_SHORT).show();
        }
        return DEFAULT_PORT;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chosenFilePathEditText = requireView().findViewById(R.id.chooseFilePathInputText);
        chosenFilePathEditText.setOnClickListener(v -> openFile());
        sendFileButton = requireView().findViewById(R.id.sendFileButton);
        sendFileButton.setOnClickListener(v -> sendFile());
        advancedNetworkSettingsSwitch = requireView().findViewById(R.id.sendFileAdvancedSwitch);
        advancedNetworkSettingsSwitch.setOnCheckedChangeListener((a, b) -> toggleAdvancedNetworkSettings());
        networkSettingsText = requireView().findViewById(R.id.advancedNetworkConfigsTextView);
        ipAddressEditText = requireView().findViewById(R.id.bindIpAddressInputText);
        portEditText = requireView().findViewById(R.id.bindPortInputText);
        backButton = requireView().findViewById(R.id.pickFileFragmentBackButton);
        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
            MainActivity.setCurrentFragment(MenuScreenFragment.newInstance());
        });
        toggleAdvancedNetworkSettings(); // hide on start
    }

    public void openFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);
        Intent chooserIntent;
        if (requireActivity().getPackageManager().resolveActivity(sIntent, 0) != null) {
            // it is device with Samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Select file to send!");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent});
        } else {
            chooserIntent = Intent.createChooser(intent, "Select file to send!");
        }
        try {
            startActivityForResult(chooserIntent, 2);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(
            int requestCode,
            int resultCode,
            Intent resultData
    ) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                uploadFileUri = resultData.getData();
                this.fileName = getNameFromContentUri(requireContext().getContentResolver(), uploadFileUri);
                chosenFilePathEditText.setText(this.fileName);
            }
        }
    }

    private void toggleAdvancedNetworkSettings() {
        if (advancedNetworkSettingsSwitch.isChecked()) {
            networkSettingsText.setVisibility(View.VISIBLE);
            ipAddressEditText.setVisibility(View.VISIBLE);
            portEditText.setVisibility(View.VISIBLE);
        } else {
            networkSettingsText.setVisibility(View.INVISIBLE);
            ipAddressEditText.setVisibility(View.INVISIBLE);
            portEditText.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pick_file, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}