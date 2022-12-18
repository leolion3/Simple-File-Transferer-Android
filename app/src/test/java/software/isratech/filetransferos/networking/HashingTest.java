package software.isratech.filetransferos.networking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import static software.isratech.filetransferos.networking.Hashing.getSHA256FileHash;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class HashingTest {

    @Test
    public void testHashFile() throws IOException, NoSuchAlgorithmException, InterruptedException {
        final String filePath = "../README.md";
        final File file = new File(filePath);
        Runtime r = Runtime.getRuntime();
        Process p = r.exec("sha256sum ../README.md");
        p.waitFor();
        final BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        final List<String> output = new ArrayList<>();
        while ((line = b.readLine()) != null) {
            output.add(line);
        }
        b.close();
        assertFalse(output.isEmpty());
        assertEquals(output.get(0).split(" ")[0], getSHA256FileHash(file));
    }
}
