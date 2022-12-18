package software.isratech.filetransferos.networking;

import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class ClientTest {

    @Test
    public void testClientConnect() throws IOException, NoSuchAlgorithmException {
        final Client client = new Client();
        client.connect("127.0.0.1", 5050, "../");
    }
}
