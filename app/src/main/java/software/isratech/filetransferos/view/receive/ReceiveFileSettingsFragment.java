package software.isratech.filetransferos.view.receive;

import static software.isratech.filetransferos.Constants.DEFAULT_LOOPBACK_ADDRESS;
import static software.isratech.filetransferos.Constants.DEFAULT_PORT;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import software.isratech.filetransferos.MainActivity;
import software.isratech.filetransferos.R;
import software.isratech.filetransferos.networking.Communication;
import software.isratech.filetransferos.view.MenuScreenFragment;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ReceiveFileSettingsFragment extends Fragment implements NetworkRecyclerViewAdapter.ItemClickListener {

    private Button backButton;
    private SwitchCompat modeSwitch;
    private Button scanButton;
    private Button stopScanButton;
    private TextView informationTextView;
    private EditText downloadLocationEditText;
    private EditText ipAddressInputText;
    private EditText portInputText;
    private Button connectButton;
    private RecyclerView recyclerView;
    private TextView recyclerConnectionInfoText;
    private Uri selectedDownloadUri;
    private NetworkRecyclerViewAdapter recyclerViewAdapter;
    private final AtomicBoolean stopScanning = new AtomicBoolean(false);

    public static ReceiveFileSettingsFragment newInstance() {
        ReceiveFileSettingsFragment fragment = new ReceiveFileSettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void resetRecyclerViewAdapter() {
        recyclerViewAdapter.clearDataset();
        recyclerView.setVisibility(View.INVISIBLE);
    }

    private void setRecyclerViewAdapter(@NonNull final List<String> entries) {
        recyclerViewAdapter.replaceData(entries);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void disableSwitch() {
        modeSwitch.setClickable(false);
        modeSwitch.setText("Disabled");
    }

    private void enableSwitch() {
        modeSwitch.setClickable(true);
        modeSwitch.setText("Auto");
    }

    private void setScanButton() {
        disableSwitch();
        backButton.setVisibility(View.INVISIBLE);
        scanButton.setVisibility(View.INVISIBLE);
        stopScanButton.setVisibility(View.VISIBLE);
        recyclerConnectionInfoText.setTextColor(Color.GRAY);
        recyclerConnectionInfoText.setVisibility(View.VISIBLE);
        stopScanning.set(false);
        informationTextView.setText("Scanning for hosts...");
        resetRecyclerViewAdapter();
        recyclerConnectionInfoText.setVisibility(View.VISIBLE);
        new Thread(() -> {
            final List<String> availableServers = Communication.getAvailableServers(DEFAULT_PORT, stopScanning, recyclerConnectionInfoText, requireActivity());
            requireActivity().runOnUiThread(() -> {
                setStopScanButton();
                if (availableServers.size() == 0) {
                    recyclerConnectionInfoText.setText("No servers found!");
                    recyclerConnectionInfoText.setTextColor(Color.RED);
                    return;
                }
                setRecyclerViewAdapter(availableServers);
                recyclerConnectionInfoText.setVisibility(View.INVISIBLE);
            });
        }).start();
    }

    private void setStopScanButton() {
        enableSwitch();
        backButton.setVisibility(View.VISIBLE);
        informationTextView.setText("Scan for hosts?");
        stopScanButton.setVisibility(View.INVISIBLE);
        scanButton.setVisibility(View.VISIBLE);
        stopScanning.set(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        backButton = requireView().findViewById(R.id.receiveFileSettingsFragmentBackButton);
        modeSwitch = requireView().findViewById(R.id.receiveModeSwitch);
        scanButton = requireView().findViewById(R.id.receiveFileScanButton);
        stopScanButton = requireView().findViewById(R.id.stopScanButton);
        downloadLocationEditText = requireView().findViewById(R.id.selectedDownloadPath);
        informationTextView = requireView().findViewById(R.id.receiveFileInfoText);
        ipAddressInputText = requireView().findViewById(R.id.receiveFileIpAddress);
        portInputText = requireView().findViewById(R.id.receiveFilePort);
        connectButton = requireView().findViewById(R.id.receiveFileConnectButton);
        recyclerConnectionInfoText = requireView().findViewById(R.id.receiveFileErrorText);
        backButton.setOnClickListener(v -> MainActivity.setCurrentFragment(MenuScreenFragment.newInstance()));
        modeSwitch.setOnClickListener(v -> toggleManualMode());
        scanButton.setOnClickListener(v -> setScanButton());
        stopScanButton.setOnClickListener(v -> setStopScanButton());
        connectButton.setOnClickListener(v -> setConnectButton());
        downloadLocationEditText.setOnClickListener(v -> openFilePath());
        recyclerView = requireView().findViewById(R.id.receiveFileRecyclerView);
        recyclerViewAdapter = new NetworkRecyclerViewAdapter(requireContext(), new ArrayList<>());
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(recyclerViewAdapter);
        setup();
    }

    private void setConnectButton() {
        String ipAddress = DEFAULT_LOOPBACK_ADDRESS;
        if (ipAddressInputText.getText() == null || ipAddressInputText.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "IP Address left empty! Using default!", Toast.LENGTH_SHORT).show();
        } else {
            ipAddress = ipAddressInputText.getText().toString();
        }
        int port = DEFAULT_PORT;
        if (portInputText.getText() == null || portInputText.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Port left blank! Using default!", Toast.LENGTH_SHORT).show();
        } else {
            try {
                Integer.parseInt(portInputText.getText().toString());
            } catch (Exception e) {
                Toast.makeText(getContext(), "Illegal port entered! Using default!", Toast.LENGTH_SHORT).show();
            }
        }
        execute(ipAddress,port,this.selectedDownloadUri);
    }

    @Override
    public void onItemClick(@NonNull final View view, int position) {
        if(position != RecyclerView.NO_POSITION){
            final String ipAddress = recyclerViewAdapter.getItem(position);
            execute(ipAddress, DEFAULT_PORT, this.selectedDownloadUri);
        }
        else {
            Toast.makeText(getContext(), "No item selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void execute(@NonNull final String ipAddress, final int port, @NonNull final Uri uri) {
        requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        MainActivity.setCurrentFragment(ReceivingFileFragment.newInstance(ipAddress,port,uri));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void setup() {
        recyclerView.setVisibility(View.INVISIBLE);
        recyclerConnectionInfoText.setVisibility(View.INVISIBLE);
        scanButton.setVisibility(View.INVISIBLE);
        stopScanButton.setVisibility(View.INVISIBLE);
        ipAddressInputText.setVisibility(View.INVISIBLE);
        portInputText.setVisibility(View.INVISIBLE);
        connectButton.setVisibility(View.INVISIBLE);
        modeSwitch.setText("Disabled");
        informationTextView.setText("Please select download location.");
        informationTextView.setTextColor(Color.RED);
        modeSwitch.setClickable(false);
    }

    private void toggleManualMode() {
        if (!modeSwitch.isChecked()) {
            recyclerView.setVisibility(View.VISIBLE);
            scanButton.setVisibility(View.VISIBLE);
            ipAddressInputText.setVisibility(View.INVISIBLE);
            portInputText.setVisibility(View.INVISIBLE);
            connectButton.setVisibility(View.INVISIBLE);
            modeSwitch.setText("Auto");
            informationTextView.setText("Scan for hosts?");
        } else {
            recyclerView.setVisibility(View.INVISIBLE);
            recyclerConnectionInfoText.setVisibility(View.INVISIBLE);
            scanButton.setVisibility(View.INVISIBLE);
            ipAddressInputText.setVisibility(View.VISIBLE);
            portInputText.setVisibility(View.VISIBLE);
            connectButton.setVisibility(View.VISIBLE);
            modeSwitch.setText("Manual");
            informationTextView.setText("Using Manual Mode");
        }
    }

    private void afterFolderPicked() {
        downloadLocationEditText.setVisibility(View.INVISIBLE);
        modeSwitch.setText("Auto");
        modeSwitch.setClickable(true);
        informationTextView.setTextColor(Color.GRAY);
        toggleManualMode();
    }

    public void openFilePath() {
        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        final Intent chooserIntent = Intent.createChooser(intent, "Select download path!");
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
            final Intent resultData
    ) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                selectedDownloadUri = resultData.getData();
                afterFolderPicked();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_receive_file_settings, container, false);
    }
}