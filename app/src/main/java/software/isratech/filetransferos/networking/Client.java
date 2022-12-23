package software.isratech.filetransferos.networking;

import static software.isratech.filetransferos.networking.Communication.receiveFile;
import static software.isratech.filetransferos.networking.Communication.receiveLong;
import static software.isratech.filetransferos.networking.Communication.receiveMessage;
import static software.isratech.filetransferos.networking.Communication.sendMessage;
import static software.isratech.filetransferos.utils.AndroidFileAccessUtils.getExportPathFromDocumentTreeUri;
import static software.isratech.filetransferos.utils.AndroidFileAccessUtils.getFileSizeFromFileUri;
import static software.isratech.filetransferos.utils.AndroidFileAccessUtils.getHumanReadableFileSize;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Handles receiving files from a remote.
 */
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Client {

    /**
     * Connect to remote and receive a file
     *
     * @param remoteHost - ip address of the remote host
     * @param remotePort - the port of the remote host
     */
    public void connect(
            @NonNull final String remoteHost,
            final int remotePort,
            @NonNull final Uri exportFilePath,
            @NonNull final Context context,
            @NonNull final ContentResolver contentResolver,
            @NonNull final TextView connectionStatusTextView,
            @NonNull final TextView transferStatusTextView,
            @NonNull final Activity fragment
            ) throws IOException, NoSuchAlgorithmException {
        final SocketAddress socketAddress = new InetSocketAddress(remoteHost, remotePort);
        try (final Socket socket = new Socket()) {
            String connectionStatusText = String.format("Connecting to %s:%s...", remoteHost, remotePort);
            connectionStatusTextView.setText(connectionStatusText);
            socket.connect(socketAddress);
            connectionStatusText += "\nConnection successful!";
            connectionStatusTextView.setText(connectionStatusText);
            final InputStream socketInputStream = socket.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(socketInputStream));
            final PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            final Quadruple<Uri, Long, Boolean, Long> fileInfoQuadruple = handleInitialCommunication(reader, writer, exportFilePath, context, contentResolver, transferStatusTextView);
            final Uri receivedFile = receiveFile(socketInputStream, fileInfoQuadruple, transferStatusTextView, fragment);
            transferStatusTextView.setText(String.format("%s%n%s", transferStatusTextView.getText().toString(), "Computing hashes..."));
            compareFileHashes(reader, writer, getReceivedFileUri(receivedFile), contentResolver, transferStatusTextView);
            transferStatusTextView.setText(String.format("%s%n%s", transferStatusTextView.getText().toString(), "Transfer complete."));
        }
    }

    /** Get the content uri of the received file
     * @param receivedFile - received file uri
     * @return a content url for the received file */
    private Uri getReceivedFileUri(@NonNull final Uri receivedFile) {
        return Uri.fromFile(new File(receivedFile.toString()));
    }

    /**
     * Handles initial client-server communication and returns info about the file that will be received.
     *
     * @param reader - the input reader to read incoming messages from.
     * @param writer - the writer to send messages to the remote with.
     * @return a quadruple containing file name, file size, file exists on own system and the existing file size.
     */
    @NonNull
    private Quadruple<Uri, Long, Boolean, Long> handleInitialCommunication(
            @NonNull final BufferedReader reader,
            @NonNull final PrintWriter writer,
            @NonNull final Uri exportFilePath,
            @NonNull final Context context,
            @NonNull final ContentResolver contentResolver,
            @NonNull final TextView transferStatusTextView
    ) throws IOException {
        long existingFileSize = 0L;
        sendMessage(writer, "init");
        String transferStatusText = "Retrieving file info...";
        transferStatusTextView.setText(transferStatusText);
        final String fileName = receiveMessage(reader);
        sendMessage(writer, "Received Name");
        final long fileSize = receiveLong(reader);
        transferStatusText += String.format("%nFile name: %s%nFile size: %s", fileName, getHumanReadableFileSize(fileSize));
        transferStatusTextView.setText(transferStatusText);
        final Uri actualExportUri = getExportPathFromDocumentTreeUri(exportFilePath, fileName);
        final AtomicBoolean fileExists = new AtomicBoolean(false);
        final Uri existingFileUri = getExistingFileUri(context, exportFilePath, fileName);
        try (final InputStream ignored = contentResolver.openInputStream(existingFileUri)) {
            existingFileSize = getFileSizeFromFileUri(contentResolver, getReceivedFileUri(actualExportUri));
            sendMessage(writer, String.format("SIZE:%s", existingFileSize));
            fileExists.set(true);
        }
        catch (Exception e) {
            sendMessage(writer, "NONEXISTANT");
        }
        receiveMessage(reader);
        sendMessage(writer, "Beginning files transfer...");
        return new Quadruple<>(actualExportUri, fileSize, fileExists.get(), existingFileSize);
    }

    private Uri getExistingFileUri(
            @NonNull final Context context,
            @NonNull final Uri treeUri,
            @NonNull final String fileName
    ) {
        final DocumentFile documentFile = DocumentFile.fromTreeUri(context, treeUri);
        return Objects.requireNonNull(documentFile).getUri().buildUpon().appendPath(fileName).build();
    }

    /**
     * Checks if the hash of the received file matches that of the sent file.
     *
     * @param receivedFileHash - hash of the received file.
     * @param fileHash         - the actual file hash.
     */
    private void verifyHashesMatch(
            @NonNull final String receivedFileHash,
            @NonNull final String fileHash,
            @NonNull final TextView infoTextView
    ) {
        if (!receivedFileHash.equalsIgnoreCase(fileHash)) {
            infoTextView.setText(String.format("%s%n%s", infoTextView.getText().toString(), "Hashes mismatch!\nPlease delete file!"));
            infoTextView.setTextColor(Color.RED);
        } else {
            infoTextView.setText(String.format("%s%n%s", infoTextView.getText().toString(), "File hashes match!"));
            infoTextView.setTextColor(Color.GREEN);
        }
    }

    /**
     * Receives the hash of the file that was sent from the remote and compares it to the received
     * file's hash.
     *
     * @param reader - the reader to receive data from.
     * @param writer - the writer to send messages to the remote.
     * @param file   - the received file.
     */
    private void compareFileHashes(
            @NonNull final BufferedReader reader,
            @NonNull final PrintWriter writer,
            @NonNull final Uri file,
            @NonNull final ContentResolver contentResolver,
            @NonNull final TextView infoTextView
    ) throws IOException, NoSuchAlgorithmException {
        sendMessage(writer, "GIVE_ME_HASH");
        final String receivedFileHash = receiveMessage(reader);
        final String fileHash = Hashing.getSHA256FileHash(file, contentResolver);
        verifyHashesMatch(receivedFileHash, fileHash, infoTextView);
    }

    /**
     * Implements a quadruple, which contains 4 different data types.
     */
    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PUBLIC)
    static class Quadruple<X, Y, Z, W> {
        private X first;
        private Y second;
        private Z third;
        private W fourth;
    }
}
