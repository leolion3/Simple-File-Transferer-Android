package software.isratech.filetransferos.networking;

import static software.isratech.filetransferos.Constants.DEFAULT_BYTES;
import static software.isratech.filetransferos.Constants.DEFAULT_LOOPBACK_ADDRESS;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Handles basic communication between two tcp clients.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Communication {

    /**
     * Write some text to a printWriter.
     *
     * @param writer  - the writer to write the text to.
     * @param message - the message to write.
     */
    public static void sendMessage(@NonNull final PrintWriter writer, @NonNull final String message) {
        writer.println(message);
    }

    /**
     * Receive a message from an input stream.
     *
     * @param bufferedReader - the buffered reader to read the message from.
     * @return the read message.
     */
    @NonNull
    public static String receiveMessage(@NonNull final BufferedReader bufferedReader) throws IOException {
        return bufferedReader.readLine();
    }

    /**
     * Receive a single long value from an input stream.
     *
     * @param bufferedReader - the buffered reader to read the long from.
     * @return a single long.
     */
    @NonNull
    public static Long receiveLong(@NonNull final BufferedReader bufferedReader) throws IOException {
        return Long.parseLong(receiveMessage(bufferedReader));
    }

    /**
     * Receive a file from the remote
     *
     * @param is                - the socket's input stream to read data from
     * @param fileInfoQuadruple - a quadruple containing file name, size, file exists and existing file size
     * @param exportFilePath    - the path where the file should be exported
     * @return the file after it was received
     */
    @NonNull
    public static File receiveFile(
            @NonNull final InputStream is,
            @NotNull final Client.Quadruple<String, Long, Boolean, Long> fileInfoQuadruple,
            @NonNull final String exportFilePath
    ) throws IOException {
        try (
                final BufferedOutputStream bufferedWriter = new BufferedOutputStream(
                        new FileOutputStream(exportFilePath + fileInfoQuadruple.getFirst(), fileInfoQuadruple.getThird())
                )
        ) {
            long receivedLength = fileInfoQuadruple.getFourth();
            while (receivedLength < fileInfoQuadruple.getSecond()) {
                int code;
                byte[] buffer = new byte[Math.toIntExact(getBufferSize(fileInfoQuadruple.getSecond(), receivedLength))];
                code = is.read(buffer);
                bufferedWriter.write(buffer, 0, code);
                receivedLength += code;
            }
            bufferedWriter.flush();
            return new File(exportFilePath + fileInfoQuadruple.getFirst());
        }
    }

    /**
     * Determines how many bytes of data should be read from the socket.
     * The standard transfer speed shall be 2 MB/cycle.
     *
     * @param fileSize - the size of the file to receive
     * @param readSize - the amount of bytes already read
     * @return how many bytes should be read in the next cycle
     */
    private static long getBufferSize(final long fileSize, final long readSize) {
        if (fileSize < DEFAULT_BYTES) {
            if (readSize > 0L) {
                return Math.max(fileSize - readSize, 0L);
            }
            return fileSize;
        }
        return Math.min(fileSize - readSize, DEFAULT_BYTES);
    }

    public static String getIpAddress() {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    /**
     * Ping all addresses in subnet to find a server listening for connections
     *
     * @param port          - the port to ping on
     * @param networkStatus - text view describing what the ping service is doing
     * @param stopScanning  - used to stop scanning for hosts
     * @return a list of all available servers and their mac addresses
     */
    public static List<String> getAvailableServers(
            final int port,
            final AtomicBoolean stopScanning,
            final TextView networkStatus,
            final FragmentActivity activity
    ) {
        final List<String> result = new ArrayList<>();
        final String hostAddress = getIpAddress();
        if (DEFAULT_LOOPBACK_ADDRESS.equalsIgnoreCase(hostAddress)) return result;
        final String[] hostAddressSplit = Objects.requireNonNull(hostAddress).split("\\.");
        if (hostAddressSplit.length < 3) throw new IllegalArgumentException("Cannot find subnet!");
        final String subnet = String.format(
                "%s.%s.%s.",
                hostAddressSplit[0],
                hostAddressSplit[1],
                hostAddressSplit[2]
        );
        for (int i = 100; i < 256; i++) {
            if (stopScanning.get()) break;
            final String hostName = subnet + i;
            System.out.println("Attempting scan of " + hostName + ":" + port);
            try (final Socket socket = new Socket()) {
                activity.runOnUiThread(() -> {
                    networkStatus.setText(String.format("Pinging %s...", hostName));
                });
                final SocketAddress socketAddress = new InetSocketAddress(hostName, port);
                socket.connect(socketAddress, 250);
                if (pingServer(socket)) {
                    result.add(hostName);
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity.getApplicationContext(), "Found " + hostName, Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                // ignored
            }
        }
        return result;
    }

    /**
     * Sends a ping to a socket
     *
     * @param socket - the connected socket
     * @return true if the server responds with REPLY, else false
     */
    private static boolean pingServer(final Socket socket) throws IOException {
        final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        sendMessage(printWriter, "PING");
        return "REPLY".equalsIgnoreCase(receiveMessage(bufferedReader));
    }
}
