package software.isratech.filetransferos.view.receive;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import software.isratech.filetransferos.MainActivity;
import software.isratech.filetransferos.R;
import software.isratech.filetransferos.networking.Client;


@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ReceivingFileFragment extends Fragment {

    private static final String ipAddressStr = "ipAddress";
    private static final String portStr = "port";
    private static final String downloadUriStr = "downloadUri";

    private Button homeButton;
    private Button receiveMoreFilesButton;
    private ProgressBar spinner;
    private TextView connectionDetailsTextView;
    private TextView transferStatusTextView;

    /**
     * Ip address to connect to
     */
    private String ipAddress;

    /**
     * Port to connect to
     */
    private int port;

    /**
     * Where the file should be downloaded to
     */
    private Uri downloadUri;

    /**
     * Creates a new fragment
     *
     * @param ipAddress   - the ip address to connect to
     * @param port        - the port to connect to
     * @param downloadUri - where the file should be downloaded to
     * @return a new instance of this fragment
     */
    public static ReceivingFileFragment newInstance(
            @NonNull final String ipAddress,
            final int port,
            @NonNull final Uri downloadUri
    ) {
        ReceivingFileFragment fragment = new ReceivingFileFragment();
        Bundle args = new Bundle();
        args.putString(ipAddressStr, ipAddress);
        args.putInt(portStr, port);
        args.putString(downloadUriStr, downloadUri.toString());
        fragment.setArguments(args);
        return fragment;
    }

    private void initializeUI() {
        this.homeButton = requireView().findViewById(R.id.receivingFileFragmentBackButton);
        this.receiveMoreFilesButton = requireView().findViewById(R.id.receivingFileFragmentSendMoreFilesButton);
        this.connectionDetailsTextView = requireView().findViewById(R.id.receiveFileFragmentIntoText);
        this.transferStatusTextView = requireView().findViewById(R.id.receiveFileFragmentConnectionDetailsText);
        this.spinner = requireView().findViewById(R.id.receiveFileSpinner);
    }

    private void setActionListeners() {
        this.homeButton.setOnClickListener(v -> {
            MainActivity.backToMainMenu();
        });
        this.receiveMoreFilesButton.setOnClickListener(v -> {
            MainActivity.setCurrentFragment(ReceiveFileSettingsFragment.newInstance());
        });
    }

    private void hideButtons() {
        this.homeButton.setVisibility(View.INVISIBLE);
        this.receiveMoreFilesButton.setVisibility(View.INVISIBLE);
    }

    private void showButtons() {
        this.homeButton.setVisibility(View.VISIBLE);
        this.receiveMoreFilesButton.setVisibility(View.VISIBLE);
    }

    private void transferFile() {
        final Client client = new Client();
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        new Thread(() -> {
            try {
                client.connect(ipAddress, port, downloadUri, requireContext(), requireContext().getContentResolver(), connectionDetailsTextView, transferStatusTextView, requireActivity());
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    transferStatusTextView.setText(String.format("Error occurred!%n%s", e.getMessage()));
                    if (e.getMessage() != null && e.getMessage().contains("EPERM (Operation not permitted)")) {
                        transferStatusTextView.setText(
                                String.format(
                                        "%s%n%s%n%s",
                                        transferStatusTextView.getText(),
                                        "Cant write to arbitrary folders in your version of android!",
                                        "Please select a folder matching the file type you are receiving, or manually create a new folder!"
                                )
                        );
                    }
                    transferStatusTextView.setTextColor(Color.RED);
                });
            }
            requireActivity().runOnUiThread(() -> {
                showButtons();
                spinner.setVisibility(View.INVISIBLE);
                requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            });
        }).start();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeUI();
        setActionListeners();
        hideButtons();
        try {
            transferFile();
        } catch (Exception e) {
            // ignored - only happens when fragment dies intentionally
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ipAddress = getArguments().getString(ipAddressStr);
            port = getArguments().getInt(portStr);
            downloadUri = Uri.parse(getArguments().getString(downloadUriStr));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receiving_file, container, false);
    }
}