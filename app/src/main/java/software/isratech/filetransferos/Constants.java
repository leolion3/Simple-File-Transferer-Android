package software.isratech.filetransferos;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Contains some constants
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

    /**
     * By default, the server listens on all interfaces.
     */
    public static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";

    /**
     * By default, use port 5050
     */
    public static final int DEFAULT_PORT = 5050;

    /**
     * Default amount of bytes to read.
     */
    public static final int DEFAULT_BYTES = 2000000;

    /**
     * Google DNS
     * Used for looking up client IPv4 Address
     */
    public static final String GOOGLE_DNS = "8.8.8.8";

    /**
     * Default loopback address
     */
    public static final String DEFAULT_LOOPBACK_ADDRESS = "127.0.0.1";
}
