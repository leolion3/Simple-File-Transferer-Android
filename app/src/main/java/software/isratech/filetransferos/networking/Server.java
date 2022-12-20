package software.isratech.filetransferos.networking;

import static software.isratech.filetransferos.Constants.DEFAULT_BYTES;
import static software.isratech.filetransferos.networking.Communication.receiveMessage;
import static software.isratech.filetransferos.networking.Communication.sendMessage;
import static software.isratech.filetransferos.utils.AndroidFileAccessUtils.getFileSizeFromUri;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.NoSuchAlgorithmException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import software.isratech.filetransferos.utils.AndroidFileAccessUtils;

/**
 * Implements sending files to a remote client.
 */
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Server {

    /**
     * Android application context
     */
    private Context context;

    /**
     * Starts a listener which serves files to remote clients
     *
     * @param host - the address to host on
     * @param port - the port to listen on
     * @param uri  - the file's uri
     */
    public void serve(
            @NonNull final String host,
            final int port,
            @NonNull final Uri uri,
            @NonNull final ContentResolver contentResolver,
            @NonNull final TextView networkSettingsEditText,
            @NonNull final ProgressBar spinner
    ) throws IOException, IllegalArgumentException, NoSuchAlgorithmException {
        try (final ServerSocket serverSocket = new ServerSocket()) {
            String data = "";
            data += "Starting server...";
            networkSettingsEditText.setText(data);
            final SocketAddress socketAddress = new InetSocketAddress(host, port);
            serverSocket.bind(socketAddress);
            data += String.format("%nServer bound and listening on %s:%s...%nWaiting for client connection...", host, port);
            networkSettingsEditText.setText(data);
            final Socket clientSocket = getClientSocket(serverSocket);
            data += String.format("%nAccepted connection from %s", clientSocket.getRemoteSocketAddress().toString());
            networkSettingsEditText.setText(data);
            handleClient(clientSocket, uri, contentResolver, networkSettingsEditText, spinner);
        }
    }

    /**
     * Replies to client pings until a client initiates an actual connection.
     *
     * @param serverSocket - the server socket.
     * @return the client's socket.
     */
    private Socket getClientSocket(@NonNull final ServerSocket serverSocket) throws IOException {
        Socket clientSocket = serverSocket.accept();
        while (!receiveMessage(new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))).equalsIgnoreCase("init")) {
            sendMessage(new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream())), "REPLY");
            clientSocket = serverSocket.accept();
        }
        return clientSocket;
    }

    /**
     * Handles a connected client by sending them the selected file.
     *
     * @param clientSocket - the client's socket.
     * @param uri          - the file's uri.
     */
    private void handleClient(
            @NonNull final Socket clientSocket,
            @NonNull final Uri uri,
            @NonNull final ContentResolver contentResolver,
            @NonNull final TextView networkSettingsEditText,
            @NonNull final ProgressBar spinner
    ) throws IOException, NoSuchAlgorithmException {
        String data = networkSettingsEditText.getText().toString();
        final InputStream socketInputStream = clientSocket.getInputStream();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(socketInputStream));
        final OutputStream socketOutputStream = clientSocket.getOutputStream();
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(socketOutputStream), true);
        data += "\nExchanging file info with client...";
        networkSettingsEditText.setText(data);
        final long existingFileSize = handleInitialCommunication(reader, writer, uri, contentResolver);
        data += "\nSending file to client...";
        networkSettingsEditText.setText(data);
        handleFileTransfer(socketOutputStream, uri, contentResolver, existingFileSize);
        data += "\nSending client file hash...";
        networkSettingsEditText.setText(data);
        checkFileHashes(reader, writer, uri, contentResolver);
        data += "\nFile transfer complete.";
        networkSettingsEditText.setTextColor(Color.GREEN);
        networkSettingsEditText.setText(data);
        spinner.setVisibility(View.INVISIBLE);
    }

    /**
     * Handles initial communication with a client for exchanging file info.
     *
     * @param reader          - the reader used for receiving messages.
     * @param writer          - the writer used for sending messages.
     * @param uri             - the uri of the file.
     * @param contentResolver - the system content resolver.
     * @return the size of the file on the client's system.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private long handleInitialCommunication(
            @NonNull final BufferedReader reader,
            @NonNull final PrintWriter writer,
            @NonNull final Uri uri,
            @NonNull final ContentResolver contentResolver
    ) throws IOException {
        sendMessage(writer, AndroidFileAccessUtils.getNameFromContentUri(contentResolver, uri));
        System.out.println(receiveMessage(reader));
        final long fileSize = getFileSizeFromUri(contentResolver, uri);
        sendMessage(writer, Long.toString(fileSize));
        final String response = receiveMessage(reader);
        long existingSize = 0L;
        System.out.println(response);
        if (!"NONEXISTANT".equalsIgnoreCase(response)) {
            existingSize = Long.parseLong(response.split("SIZE:")[1]);
        }
        sendMessage(writer, "Ready for transfer!");
        System.out.println(receiveMessage(reader));
        return existingSize;
    }

    /**
     * Reads the first n bytes of a file that already exists on a client's system and then sends
     * the remaining bytes to the client.
     *
     * @param socketOutputStream - the client socket's output stream
     * @param uri                - the uri to the file
     * @param contentResolver    - the system's content resolver
     * @param currentFileSize    - the size of the file currently on the client's system
     */
    private void handleFileTransfer(
            @NonNull final OutputStream socketOutputStream,
            @NonNull final Uri uri,
            @NonNull final ContentResolver contentResolver,
            final long currentFileSize
    ) throws IOException {
        final InputStream fileInputStream = readExistingData(uri, contentResolver, currentFileSize);
        long readSize = currentFileSize;
        final long fileSize = getFileSizeFromUri(contentResolver, uri);
        while (readSize < fileSize) {
            int currentReadSize = getReadThreshold(fileSize - readSize);
            final byte[] buffer = new byte[currentReadSize];
            final int returnCode = fileInputStream.read(buffer);
            if (returnCode == -1) break;
            socketOutputStream.write(buffer);
            readSize += returnCode;
            socketOutputStream.flush();
        }
    }

    /**
     * Checks if a file is existing on the client's receiving size.
     * If it exists, attempts to read the first n bytes that the client already has.
     *
     * @param uri             - the file's uri
     * @param contentResolver - the system's content resolver
     * @param currentFileSize - the size of the file on the client's system
     * @return an inputStream of the file, pre-read to the location of the client's data size
     */
    private InputStream readExistingData(
            @NonNull final Uri uri,
            @NonNull final ContentResolver contentResolver,
            final long currentFileSize
    ) throws IOException {
        final InputStream fileInputStream = contentResolver.openInputStream(uri);
        if (currentFileSize == 0L) return fileInputStream;
        long readSize = 0L;
        while (readSize != currentFileSize) {
            int currentReadSize = getReadThreshold(currentFileSize - readSize);
            final byte[] buffer = new byte[currentReadSize];
            final int returnCode = fileInputStream.read(buffer);
            if (returnCode == -1) break;
            readSize += returnCode;
        }
        return fileInputStream;
    }

    /**
     * Computes the sent file's hash and sends it to the client who received the file.
     *
     * @param reader          - the reader used for receiving messages from the client.
     * @param writer          - the writer for sending messages to the client.
     * @param uri             - the file's uri.
     * @param contentResolver - the file's content resolver.
     */
    private void checkFileHashes(
            @NonNull final BufferedReader reader,
            @NonNull final PrintWriter writer,
            @NonNull final Uri uri,
            @NonNull final ContentResolver contentResolver
    ) throws IOException, NoSuchAlgorithmException {
        receiveMessage(reader);
        sendMessage(writer, Hashing.getSHA256FileHash(uri, contentResolver));
    }

    /**
     * Computes how many bytes of a file should be read next.
     *
     * @return how many bytes to read next.
     */
    private int getReadThreshold(final long leftFileSize) {
        return Math.toIntExact(Math.min(leftFileSize, DEFAULT_BYTES));
    }
}
