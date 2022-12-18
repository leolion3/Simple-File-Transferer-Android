package software.isratech.filetransferos.view;

import static software.isratech.filetransferos.utils.AndroidFileAccessUtils.getFileSizeFromUri;
import static software.isratech.filetransferos.utils.AndroidFileAccessUtils.getHumanReadableFileSize;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

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
                String.format("File name: %s%nFile size: %s", fileName,
                        getHumanReadableFileSize(getFileSizeFromUri(contentResolver, uri)))
        );
        networkTextView = requireView().findViewById(R.id.sendFileFragmentConnectionDetailsText);
        backButton = requireView().findViewById(R.id.sendingFileFragmentBackButton);
        sendAgainButton = requireView().findViewById(R.id.SendingFileFragmentSendMoreFilesButton);
        backButton.setVisibility(View.INVISIBLE);
        sendAgainButton.setVisibility(View.INVISIBLE);
        final Server server = new Server(getContext());
        serverThread = new Thread(() -> {
            try {
                server.serve(ipAddress, port, uri, contentResolver, networkTextView, spinner);
                requireActivity().runOnUiThread(() -> {
                    backButton.setVisibility(View.VISIBLE);
                    sendAgainButton.setVisibility(View.VISIBLE);
                });
            } catch (IOException | NoSuchAlgorithmException e) {
                // ignored
            }
        });
        backButton.setOnClickListener(v ->
        {
            MainActivity.setCurrentFragment(MenuScreenFragment.newInstance());
            requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        });
        sendAgainButton.setOnClickListener(v ->
        {
            MainActivity.setCurrentFragment(PickFileFragment.newInstance());
            requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        serverThread.start();
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