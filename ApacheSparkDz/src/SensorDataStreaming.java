import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.NoSuchElementException;

/**
 * Koristi spark-streaming
 * <p>
 * <p>
 * Created by Vuki on 5.6.2017..
 */
public class SensorDataStreaming {


    public static void main(String[] args) {

        SparkConf conf = new SparkConf().setAppName(SensorDataStreaming.class.getName());
        //set the master if not already se
        t through the command line
        try {
            conf.get("spark.master");
        } catch (NoSuchElementException e) {
            //spark streaming application requires at least 2 threads
            conf.setMaster("local[2]");
        }

        JavaStreamingContext context = new JavaStreamingContext(conf, Durations.seconds(5));
        context.checkpoint("./checkpoint");

        JavaDStream<String> lines = context.socketTextStream("localhost", SensorStreamGenerator.PORT);

        final int WINDOW_DURATION = 60;
        final int SLIDE_DURATION = 10;

        JavaPairDStream<Integer, Float> result = lines
                .filter(SensorScope.SensorscopeReading::isParseable)
                .map(SensorScope.SensorscopeReading::new)
                .mapToPair(record -> new Tuple2<>(record.stationId, record.solarPanelCurrent))
                .reduceByKeyAndWindow(Math::max, Durations.seconds(WINDOW_DURATION), Durations.seconds(SLIDE_DURATION));

        result.dstream().saveAsTextFiles("rezultati/rez", "");

        context.start();
        try {
            context.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
