package software.isratech.filetransferos.view.send;

import static software.isratech.filetransferos.networking.Communication.getIpAddress;
import static software.isratech.filetransferos.utils.AndroidFileAccessUtils.getFileSizeFromUri;
import static software.isratech.filetransferos.utils.AndroidFileAccessUtils.getHumanReadableFileSize;

import android.content.ContentResolver;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.net.ServerSocket;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import software.isratech.filetransferos.MainActivity;
import software.isratech.filetransferos.R;
import software.isratech.filetransferos.networking.Server;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SendingFileFragment extends Fragment {

    private static final String uriStr = "uri";
    private static final String fileNameStr = "fileName";
    private static final String ipAddressStr = "ipAddress";
    private static final String portStr = "port";

    private Uri uri;
    private String fileName;
    private String ipAddress;
    private int port;

    private Button backButton;
    private Button sendAgainButton;
    private TextView fileInfoTextView;
    private TextView networkTextView;
    private ProgressBar spinner;
    private Thread serverThread;
    private ServerSocket serverSocket;

    public static SendingFileFragment newInstance(
            final Uri uri,
            final String fileName,
            final String ipAddress,
            final int port
    ) {
        final SendingFileFragment fragment = new SendingFileFragment();
        final Bundle args = new Bundle();
        args.putString(uriStr, uri.toString());
        args.putString(fileNameStr, fileName);
        args.putString(ipAddressStr, ipAddress);
        args.putInt(portStr, port);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ContentResolver contentResolver = requireActivity().getContentResolver();
        spinner = requireView().findViewById(R.id.sendFileSpinner);
        fileInfoTextView = requireView().findViewById(R.id.sendFileFragmentFileInfoText);
        fileInfoTextView.setText(
                String.format(
                        "File name: %s%nFile size: %s%nYour IP Address is: %s",
                        fileName,
                        getHumanReadableFileSize(getFileSizeFromUri(contentResolver, uri)),
                        getIpAddress()
                )
        );
        networkTextView = requireView().findViewById(R.id.sendFileFragmentConnectionDetailsText);
        backButton = requireView().findViewById(R.id.sendingFileFragmentBackButton);
        sendAgainButton = requireView().findViewById(R.id.SendingFileFragmentSendMoreFilesButton);
        backButton.setVisibility(View.INVISIBLE);
        sendAgainButton.setVisibility(View.INVISIBLE);
        final Server server = new Server(getContext());
        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket();
                requireActivity().runOnUiThread(() -> requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));
                server.serve(serverSocket, ipAddress, port, uri, contentResolver, networkTextView);
                requireActivity().runOnUiThread(() -> {
                    backButton.setVisibility(View.VISIBLE);
                    sendAgainButton.setVisibility(View.VISIBLE);
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    final String exc = networkTextView.getText() + "\nError occurred! Please try again!";
                    networkTextView.setText(exc);
                    networkTextView.setTextColor(Color.RED);
                    backButton.setVisibility(View.VISIBLE);
                    sendAgainButton.setVisibility(View.VISIBLE);
                });
            }
            requireActivity().runOnUiThread(() -> {
                spinner.setVisibility(View.INVISIBLE);
                try {
                    requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } catch (Exception f) {
                    // ignored
                }
            });
        });
        backButton.setOnClickListener(v ->
                MainActivity.backToMainMenu());
        sendAgainButton.setOnClickListener(v ->
                MainActivity.backToPreviousMenu());
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            serverSocket.close();
            serverThread.join();
        } catch (Exception e) {
            // ignored
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            serverThread.start();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Application closed during file transfer. Try again!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.uri = Uri.parse(getArguments().getString(uriStr));
            this.fileName = getArguments().getString(fileNameStr);
            this.ipAddress = getArguments().getString(ipAddressStr);
            this.port = getArguments().getInt(portStr);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sending_file, container, false);
    }
}