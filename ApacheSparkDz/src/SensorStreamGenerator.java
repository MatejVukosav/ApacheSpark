
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;


public class SensorStreamGenerator {

    private static final int WAIT_PERIOD_IN_MILLISECONDS = 1;
    public static final int PORT = 10002;

    public static void main(String[] args) throws Exception {

        //String fileName = "Sensorscope-monitor";
        String fileName = "Sensorscope-monitor/sensorscope-monitor-100.txt";

        if (args.length != 1) {
            //         System.err.println("Usage: SensorStreamGenerator <input file>");
            //           System.exit(-1);
        } else {
            fileName = args[0];
        }


        System.out.println("Waiting for client connection");

        try (ServerSocket serverSocket = new ServerSocket(PORT);
             Socket clientSocket = serverSocket.accept()) {

            System.out.println("Connection successful");

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),
                    true);


            Stream<String> lines = Files.lines(Paths.get(fileName));
//            Stream<String> lines = Files.list(Paths.get(fileName))
//                    .filter(Files::isRegularFile)
//                    .flatMap(p -> {
//                        try {
//                            return Files.lines(p);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        return null;
//                    });

            lines.forEach(line -> {
                out.println(line);
                try {
                    Thread.sleep(WAIT_PERIOD_IN_MILLISECONDS);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            });

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}