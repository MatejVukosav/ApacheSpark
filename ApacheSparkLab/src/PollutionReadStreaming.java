import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.NoSuchElementException;

/**
 * Created by Vuki on 6.6.2017..
 */
public class PollutionReadStreaming {

    private static final int WINDOW_DURATION = 45;
    private static final int SLIDE_DURATION = 15;


    public static void main(String[] args) {

        Logger.getLogger("org").setLevel(Level.ERROR);
        Logger.getLogger("akka").setLevel(Level.ERROR);

        PollutionReadStreaming pollutionReadStreaming = new PollutionReadStreaming();

        JavaStreamingContext context = pollutionReadStreaming.getContext();
        JavaDStream<String> lines = pollutionReadStreaming.parseInput(context);
        pollutionReadStreaming.calculateOzonPerStationId(lines);
        pollutionReadStreaming.run(context);
    }

    private void run(JavaStreamingContext context) {
        context.start();

        try {
            context.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void calculateOzonPerStationId(JavaDStream<String> lines) {
        JavaPairDStream<String, Integer> ozonePerStation = lines.filter(AirPollution.PollutionReading::canParse)
                .map(AirPollution.PollutionReading::new)
                .mapToPair(pollutionReading -> new Tuple2<>(pollutionReading.latitude + "" + pollutionReading.longitude, pollutionReading.ozone))
                .reduceByKeyAndWindow(Math::min, Durations.seconds(WINDOW_DURATION), Durations.seconds(SLIDE_DURATION));

        ozonePerStation.dstream().saveAsTextFiles("ApacheSparkLab/ozonePerStation", "");
    }

    private JavaStreamingContext getContext() {
        SparkConf conf = new SparkConf().setAppName(PollutionReadStreaming.class.getName());
        //set the master if not already set through the command line
        try {
            conf.get("spark.master");
        } catch (NoSuchElementException e) {
            //spark streaming application requires at least 2 threads
            conf.setMaster("local[2]");
        }

        return new JavaStreamingContext(conf, Durations.seconds(3));
    }

    private JavaDStream<String> parseInput(JavaStreamingContext context) {
        return context.socketTextStream("localhost", SensorStreamGenerator.PORT);
    }
}
