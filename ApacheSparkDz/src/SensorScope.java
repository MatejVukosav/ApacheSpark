import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Cilj zadatka je ucitati podatke iz nekoliko tekstualnih datoteka, filtrirati ih i sortirati te zapisati na lokalni disk.
 * Ulazne teksualne datoteke sadrze senzorska ocitanja koja su prikupljena projektom SensorScope.
 * Svaka datoteka sadrzi ocitanja s jedne mjerne postaje pa je cilj zadatka dobiti jednu izlazno sortiranu datoteku
 * s ocitanjima sa svih senzorskih postaja.
 * <p>
 * Uspjesnim rjesenjem ovog zadatka steci cete sljedeca stanja:
 * - osnove rada s kolekcijskim tokovima iz Java 8 Streams APIja
 * - jednostavna predobrada ulaznih podataka
 */
public class SensorScope {


    public static void main(String[] args) {

        SensorScope sensorScope = new SensorScope();
        sensorScope.run("Sensorscope-monitor");
    }

    private void run(String folderName) {
        parseInputData(folderName);
    }


    private void parseInputData(String folderName) {

        Stream<SensorscopeReading> sensorscopeReadingStream = null;
        try {
            sensorscopeReadingStream = Files.list(Paths.get(folderName))
                    .filter(Files::isRegularFile)
                    .flatMap(p -> {
                        try {
                            return Files.lines(p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    })
                    .filter(SensorscopeReading::isParseable)
                    .map(SensorscopeReading::new)
                    .sorted(Comparator.comparingInt(o -> o.timeSinceTheEpoch));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (sensorscopeReadingStream == null) {
            System.out.println("Stream is null!");
            return;
        }

        try {
            FileWriter fileWriter = new FileWriter("result.txt");
            sensorscopeReadingStream.forEach(data -> {
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

    public static class SensorscopeReading {
        /**
         * Column definitions:
         * <p>
         * 1. Station ID
         * 2. Year
         * 3. Month
         * 4. Day
         * 5. Hour
         * 6. Minute
         * 7. Second
         * 8. Time since the epoch [s]
         * 9. Sequence Number
         * 10. Config Sampling Time [s]
         * 11. Data Sampling Time [s]
         * 12. Radio Duty Cycle [%]
         * 13. Radio Transmission Power [dBm]
         * 14. Radio Transmission Frequency [MHz]
         * 15. Primary Buffer Voltage [V]
         * 16. Secondary Buffer Voltage [V]
         * 17. Solar Panel Current [mA]
         * 18. Global Current [mA]
         * 19. Energy Source
         */
        int stationId;
        int year;
        int month;
        int day;
        int hour;
        int minute;
        int second;
        int timeSinceTheEpoch;
        int sequenceNumber;
        float configSamplingTime;
        float dataSamplingTime;
        float radioDutyCycle;
        float radioTransmissionPower;
        float radioTransmissionFrequency;
        float primaryBufferVoltage;
        float secondaryBufferVoltage;
        float solarPanelCurrent;
        float globalCurrent;
        float energySource;

        public SensorscopeReading(String line) {
            String[] data = line.split(" ");
            this.stationId = Integer.parseInt(data[0]);
            this.year = Integer.parseInt(data[1]);
            this.month = Integer.parseInt(data[2]);
            this.day = Integer.parseInt(data[3]);
            this.hour = Integer.parseInt(data[4]);
            this.minute = Integer.parseInt(data[5]);
            this.second = Integer.parseInt(data[6]);
            this.timeSinceTheEpoch = Integer.parseInt(data[7]);
            this.sequenceNumber = Integer.parseInt(data[8]);
            this.configSamplingTime = Float.parseFloat(data[9]);
            this.dataSamplingTime = Float.parseFloat(data[10]);
            this.radioDutyCycle = Float.parseFloat(data[11]);
            this.radioTransmissionPower = Float.parseFloat(data[12]);
            this.radioTransmissionFrequency = Float.parseFloat(data[13]);
            this.primaryBufferVoltage = Float.parseFloat(data[14]);
            this.secondaryBufferVoltage = Float.parseFloat(data[15]);
            this.solarPanelCurrent = Float.parseFloat(data[16]);
            this.globalCurrent = Float.parseFloat(data[17]);
            this.energySource = Float.parseFloat(data[18]);
        }

        public static boolean isParseable(String line) {
            try {
                String[] data = line.split(" ");
                long stationId = Integer.parseInt(data[0]);
                int year = Integer.parseInt(data[1]);
                int month = Integer.parseInt(data[2]);
                int day = Integer.parseInt(data[3]);
                int hour = Integer.parseInt(data[4]);
                int minute = Integer.parseInt(data[5]);
                int second = Integer.parseInt(data[6]);
                int timeSinceTheEpoch = Integer.parseInt(data[7]);
                int sequenceNumber = Integer.parseInt(data[8]);
                float configSamplingTime = Float.parseFloat(data[9]);
                float dataSamplingTime = Float.parseFloat(data[10]);
                float radioDutyCycle = Float.parseFloat(data[11]);
                float radioTransmissionPower = Float.parseFloat(data[12]);
                float radioTransmissionFrequency = Float.parseFloat(data[13]);
                float primaryBufferVoltage = Float.parseFloat(data[14]);
                float secondaryBufferVoltage = Float.parseFloat(data[15]);
                float solarPanelCurrent = Float.parseFloat(data[16]);
                float globalCurrent = Float.parseFloat(data[17]);
                float energySource = Float.parseFloat(data[18]);
            } catch (Exception e) {
                return false;
            }
            return true;
        }


        @Override
        public String toString() {
            String separator = ",";
            return
                    stationId + separator
                            + year + separator
                            + month + separator
                            + day + separator
                            + hour + separator
                            + minute + separator
                            + second + separator
                            + timeSinceTheEpoch + separator
                            + sequenceNumber + separator
                            + configSamplingTime + separator
                            + dataSamplingTime + separator
                            + radioDutyCycle + separator
                            + radioTransmissionPower + separator
                            + radioTransmissionFrequency + separator
                            + primaryBufferVoltage + separator
                            + secondaryBufferVoltage + separator
                            + solarPanelCurrent + separator
                            + globalCurrent + separator
                            + energySource;
        }
    }
}