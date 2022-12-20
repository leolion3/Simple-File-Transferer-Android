package software.isratech.filetransferos.networking;

import static software.isratech.filetransferos.Constants.DEFAULT_LOOPBACK_ADDRESS;
import static software.isratech.filetransferos.networking.Communication.getIpAddress;
import static software.isratech.filetransferos.networking.Communication.receiveFile;
import static software.isratech.filetransferos.networking.Communication.receiveLong;
import static software.isratech.filetransferos.networking.Communication.receiveMessage;
import static software.isratech.filetransferos.networking.Communication.sendMessage;

import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
            @NonNull final String exportFilePath
    ) throws IOException, NoSuchAlgorithmException {
        final SocketAddress socketAddress = new InetSocketAddress(remoteHost, remotePort);
        try (final Socket socket = new Socket()) {
            socket.connect(socketAddress);
            final InputStream socketInputStream = socket.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(socketInputStream));
            final PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            final Quadruple<String, Long, Boolean, Long> fileInfoQuadruple = handleInitialCommunication(reader, writer, exportFilePath);
            final File receivedFile = receiveFile(socketInputStream, fileInfoQuadruple, exportFilePath);
            compareFileHashes(reader, writer, receivedFile);
        }
    }

    /**
     * Handles initial client-server communication and returns info about the file that will be received.
     *
     * @param reader - the input reader to read incoming messages from.
     * @param writer - the writer to send messages to the remote with.
     * @return a quadruple containing file name, file size, file exists on own system and the existing file size.
     */
    @NonNull
    private Quadruple<String, Long, Boolean, Long> handleInitialCommunication(
            @NonNull final BufferedReader reader,
            @NonNull final PrintWriter writer,
            @NonNull final String exportFilePath
    ) throws IOException {
        long existingFileSize = 0L;
        sendMessage(writer, "init");
        final String fileName = receiveMessage(reader);
        sendMessage(writer, "Received Name");
        final long fileSize = receiveLong(reader);
        final Path filePath = Paths.get(exportFilePath + fileName);
        final AtomicBoolean fileExists = new AtomicBoolean(false);
        if (Files.exists(filePath)) {
            existingFileSize = Files.size(filePath);
            sendMessage(writer, String.format("SIZE:%s", existingFileSize));
            fileExists.set(true);
        } else {
            sendMessage(writer, "NONEXISTANT");
        }
        receiveMessage(reader);
        sendMessage(writer, "Beginning files transfer...");
        return new Quadruple<>(fileName, fileSize, fileExists.get(), existingFileSize);
    }

    /**
     * Checks if the hash of the received file matches that of the sent file.
     * If they do not match prompts the user to delete the file.
     *
     * @param receivedFileHash - hash of the received file.
     * @param fileHash         - the actual file hash.
     * @param file             - the received file.
     */
    private void verifyHashesMatch(
            @NonNull final String receivedFileHash,
            @NonNull final String fileHash,
            @NonNull final File file
    ) {
        if (!receivedFileHash.equalsIgnoreCase(fileHash)) {
            System.out.println("file hashes mismatch");
            // todo
        } else {
            //Toast.makeText(this.context, "File received!", Toast.LENGTH_SHORT).show();
            // todo return to main menu
            System.out.println("File hashes match");
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
            @NonNull final File file
    ) throws IOException, NoSuchAlgorithmException {
        sendMessage(writer, "GIVE_ME_HASH");
        final String receivedFileHash = receiveMessage(reader);
        final String fileHash = Hashing.getSHA256FileHash(file);
        verifyHashesMatch(receivedFileHash, fileHash, file);
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
