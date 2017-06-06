import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Created by Vuki on 5.6.2017..
 */
public class AirPollution {

    private static final String OUTPUT_DATA = "ApacheSparkLab/pollutionData-all.csv";
    private static final String CSV_DELIMITER = ",";

    /**
     * Cilj zadatka je ucitati sve datoteke s mjerenjima senzorskih postaja i kao izlaz dobiti jednu izlaznu datoteku
     * sa svim ocitanjima.
     * Ova datoteka ce biti koristena u 3. zadatku kao ulaz generatora toka senzorskih podataka.
     *
     * @param args
     */
    public static void main(String[] args) {

        AirPollution airPollution = new AirPollution();
        airPollution.parseInput("ApacheSparkLab/pollutionData");

    }

    private void parseInput(String folderName) {
        Stream<PollutionReading> data = null;
        try {
            data = Files.list(Paths.get(folderName))
                    .filter(Files::isRegularFile)
                    .flatMap(p -> {
                        try {
                            return Files.lines(p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).filter(PollutionReading::canParse)
                    .map(PollutionReading::new)
                    .sorted(Comparator.comparing(pollutionReading -> pollutionReading.timestamp));

        } catch (IOException e) {
            e.printStackTrace();
        }

        writeToFile(data);


    }

    @SuppressWarnings("Duplicates")
    private void writeToFile(Stream<PollutionReading> pollutionReadingStream) {

        if (pollutionReadingStream == null) {
            return;
        }
        try {
            FileWriter fileWriter = new FileWriter(OUTPUT_DATA);
            pollutionReadingStream.forEach(data -> {
                try {
                    fileWriter.append(data.toString());
                    fileWriter.append(System.lineSeparator());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * ozone,particullate_matter,carbon_monoxide,sulfure_dioxide,nitrogen_dioxide,longitude,latitude,timestamp
     * 101,94,49,44,87,10.104986076057457,56.23172069428216,2014-08-01 00:05:00
     */
    static class PollutionReading {

        int ozone;
        int particullateMatter;
        int carbonMonoxide;
        int sulfure_dioxide;
        int nitrogen_dioxide;
        double longitude;
        double latitude;
        String timestamp;

        public PollutionReading(String line) {
            String[] data = line.split(",");
            this.ozone = Integer.parseInt(data[0]);
            this.particullateMatter = Integer.parseInt(data[1]);
            this.carbonMonoxide = Integer.parseInt(data[2]);
            this.sulfure_dioxide = Integer.parseInt(data[3]);
            this.nitrogen_dioxide = Integer.parseInt(data[4]);
            this.longitude = Double.parseDouble(data[5]);
            this.latitude = Double.parseDouble(data[6]);
            this.timestamp = data[7];
        }

        public static boolean canParse(String line) {
            try {
                String[] data = line.split(",");
                int ozone = Integer.parseInt(data[0]);
                int particullateMatter = Integer.parseInt(data[1]);
                int carbonMonoxide = Integer.parseInt(data[2]);
                int sulfure_dioxide = Integer.parseInt(data[3]);
                int nitrogen_dioxide = Integer.parseInt(data[4]);
                double longitude = Double.parseDouble(data[5]);
                double latitude = Double.parseDouble(data[6]);
                String timestamp = data[7];
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return ozone + CSV_DELIMITER
                    + particullateMatter + CSV_DELIMITER
                    + carbonMonoxide + CSV_DELIMITER
                    + sulfure_dioxide + CSV_DELIMITER
                    + nitrogen_dioxide + CSV_DELIMITER
                    + longitude + CSV_DELIMITER
                    + latitude + CSV_DELIMITER
                    + timestamp;
        }
    }


}
