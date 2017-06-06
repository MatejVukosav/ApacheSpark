
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

    public static final int STATION_ID = 158324;


    @SuppressWarnings("Duplicates")
    public static void main(String[] args) throws Exception {

        String fileName = "ApacheSparkLab/pollutionData/pollutionData" + STATION_ID + ".csv";

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