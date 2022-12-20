package software.isratech.filetransferos.view.receive;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import software.isratech.filetransferos.R;


@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ReceivingFileFragment extends Fragment {

    private static final String ipAddressStr = "ipAddress";
    private static final String portStr = "port";
    private static final String downloadUriStr = "downloadUri";

    /**
     * Ip address to connect to
     */
    private String ipAddress;

    /**
     * Port to connect to
     */
    private int port;

    /** Where the file should be downloaded to */
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
        args.putString(downloadUriStr, downloadUriStr.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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