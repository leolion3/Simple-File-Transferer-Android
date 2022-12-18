package software.isratech.filetransferos.networking;

/**
 * Tests the server's networking portion
 */
public class ServerTest {

//    @Test
//    public void testServer() throws IOException, NoSuchAlgorithmException {
//        final Server server = new Server();
//        server.serve(DEFAULT_BIND_ADDRESS, DEFAULT_PORT, "../README.md");
//    }
//
//    @Test
//    public void testClientAndServer() throws IOException, NoSuchAlgorithmException, InterruptedException {
//        final Server server = new Server();
//        final Client client = new Client();
//        new Thread(() -> {
//            try {
//                server.serve(DEFAULT_BIND_ADDRESS, DEFAULT_PORT, "../README.md");
//            } catch (IOException | NoSuchAlgorithmException e) {
//                e.printStackTrace();
//                throw new IllegalArgumentException();
//            }
//        }).start();
//        Thread.sleep(2000);
//        client.connect("127.0.0.1", 5050, "../");
//    }
}
