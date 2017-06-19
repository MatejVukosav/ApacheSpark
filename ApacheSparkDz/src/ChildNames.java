import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Cilj zadatka je obaviti analizu podataka o ucestalosti imena novorodencadi u Sjedinjenim Americkim Drzavama po
 * godinama i drzavama.
 * <p>
 * <p>
 * Potreban apache-spark
 * <p>
 * Created by Vuki on 4.6.2017..
 */
public class ChildNames {


    public static void main(String[] args) {


        System.setProperty("hadoop.home.dir", "C:\\Program Files\\WinUtils");

        JavaSparkContext context = getContext();
        parseInput(context, args);
    }

    private static JavaSparkContext getContext() {
        SparkConf conf = new SparkConf().setAppName("ChildNames");
        //set the master if not already set through the command line
        try {
            conf.get("spark.master");
        } catch (NoSuchElementException e) {
            conf.setMaster("local");
        }
        return new JavaSparkContext(conf);
    }

    private static void parseInput(JavaSparkContext context, String[] args) {

        String path = "StateNames.csv";

        //create an RDD from text file lines. Resilient Distributed Dataset
        JavaRDD<String> lines = context.textFile(path);

        JavaRDD<USBabyNameRecord> dataLineJavaRDD =
                lines.filter(USBabyNameRecord::canParse)
                        .map(USBabyNameRecord::new);


        //getMostUnPopularGirlsName(dataLineJavaRDD);
        //getTop10MaleNames(dataLineJavaRDD);
        //getStateWithMaxBornChilds(dataLineJavaRDD, 1946);
        //getFemaleBirthsCountThroughYears(dataLineJavaRDD);
        //getFemaleNameThroughYears(dataLineJavaRDD, "Mary");
        //getSumOfBornChildern(dataLineJavaRDD);
        getSumOfDistinctNames(dataLineJavaRDD);
    }

    private static void getSumOfDistinctNames(JavaRDD<USBabyNameRecord> dataLineJavaRDD) {
        long count = dataLineJavaRDD
                .map(usBabyNameRecord -> usBabyNameRecord.name)
                .distinct()
                .count();
        System.out.println("Distinct names sum is: " + count);
    }

    private static void getFemaleNameThroughYears(JavaRDD<USBabyNameRecord> dataLineJavaRDD, String name) {


        JavaPairRDD<Integer, Long> femaleBirthsCountThroughYears = getFemaleBirthsCountThroughYears(dataLineJavaRDD);

        System.out.println("Female name " + name + " through years:");
        JavaPairRDD<Integer, Long> maryBirthsCountThroughYears = dataLineJavaRDD.filter(usBabyNameRecord -> usBabyNameRecord.gender == 'F')
                .filter(usBabyNameRecord -> usBabyNameRecord.name.equals(name))
                .mapToPair(usBabyNameRecord -> new Tuple2<>(usBabyNameRecord.year, usBabyNameRecord.count))
                //reduciraj po kljucu i zbroji vrijednosti
                .reduceByKey((x, y) -> x + y)
                .sortByKey();


        JavaPairRDD<Integer, Double> result = maryBirthsCountThroughYears.join(femaleBirthsCountThroughYears)
                .mapToPair(record -> new Tuple2<>(record._1, ((double) record._2._1 / record._2._2) * 100))
                .sortByKey();
        result
                .foreach(record -> {
                    System.out.println(record._1 + " " + record._2);
                });

        try {
            FileWriter fw = new FileWriter("./out5.txt");
            result.collect().forEach(record -> {
                String line = record._1 + "," + record._2 + "\n";
                try {
                    fw.write(line);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static JavaPairRDD<Integer, Long> getFemaleBirthsCountThroughYears(JavaRDD<USBabyNameRecord> dataLineJavaRDD) {

        System.out.println("Female born childrens through years:");
        JavaPairRDD<Integer, Long> pairs = dataLineJavaRDD.filter(usBabyNameRecord -> usBabyNameRecord.gender == 'F')
                .mapToPair(usBabyNameRecord -> new Tuple2<>(usBabyNameRecord.year, usBabyNameRecord.count))
                //reduciraj po kljucu i zbroji vrijednosti
                .reduceByKey((x, y) -> x + y)
                .sortByKey();


        pairs.foreach(record -> {
            System.out.println(record._1 + " " + record._2);
        });

        return pairs;

    }

    private static void getSumOfBornChildern(JavaRDD<USBabyNameRecord> dataLineJavaRDD) {

        Tuple2<Integer, Long> result = dataLineJavaRDD
                .mapToPair(usBabyNameRecord -> new Tuple2<>(0, usBabyNameRecord.count))
                .reduceByKey((x, y) -> x + y)
                .first();


        long count = result._2;

        System.out.println("Sum of born children of all times is: " + count);
    }

    private static void getStateWithMaxBornChilds(JavaRDD<USBabyNameRecord> dataLineJavaRDD, int year) {


        Tuple2<Long, String> first = dataLineJavaRDD
                .filter(usBabyNameRecord -> usBabyNameRecord.year == year)
                .mapToPair(record -> new Tuple2<>(record.state, record.count))
                //reduciraj po drzavi
                .reduceByKey((x, y) -> x + y)
                .mapToPair(Tuple2::swap)
                .sortByKey(false)
                .first();


        System.out.println("Most childrens born in " + year + " are from " + first._2 + ".");
    }

    private static void getTop10MaleNames(JavaRDD<USBabyNameRecord> dataLineJavaRDD) {

        System.out.println("Most popular male names are: ");

        dataLineJavaRDD.filter(usBabyNameRecord -> usBabyNameRecord.gender == 'M')
                .mapToPair(record -> new Tuple2<>(record.name, record.count))
                .reduceByKey((x, y) -> x + y)
                .mapToPair(Tuple2::swap)
                .sortByKey(false)
                .take(10)
                .forEach(record -> {
                    System.out.println(record._2 + " " + record._1);
                });
    }

    private static void getMostUnPopularGirlsName(JavaRDD<USBabyNameRecord> dataLineJavaRDD) {

        JavaPairRDD<Long, String> stringLongJavaPairRDD = dataLineJavaRDD
                .filter(USBabyNameRecord -> USBabyNameRecord.gender == 'F')
                .mapToPair(record -> new Tuple2<>(record.name, record.count))
                .reduceByKey((integer, integer2) -> integer + integer2)
                .mapToPair(Tuple2::swap)
                .sortByKey(true);

        System.out.println("Most unpopular girl name is: " + stringLongJavaPairRDD.first()._2);
    }


    static class USBabyNameRecord implements Serializable {
        long id;
        String name;
        int year;
        char gender; //F, M
        String state; //two letters
        long count;

        public USBabyNameRecord(String line) {
            String[] data = line.split(",");

            this.id = Long.parseLong(data[0]);
            this.name = data[1];
            this.year = Integer.parseInt(data[2]);
            this.gender = data[3].charAt(0);
            this.state = data[4];
            this.count = Long.parseLong(data[5]);
        }

        public static boolean canParse(String line) {
            String[] data = line.split(",");

            try {
                long id = Long.parseLong(data[0]);
                String name = data[1];
                int year = Integer.parseInt(data[2]);
                char gender = data[3].charAt(0);
                String state = data[4];
                long count = Long.parseLong(data[5]);
            } catch (Exception e) {
                return false;
            }

            return true;
        }
    }


}
