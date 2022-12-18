package software.isratech.filetransferos.networking;

import static software.isratech.filetransferos.Constants.DEFAULT_BYTES;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

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
}
