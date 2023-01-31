package project.football;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;
import java.util.Arrays;

import static org.apache.spark.sql.functions.*;

public class FootballStats {

    public static void main(String[] args){
        new FootballStats().run(args[0], args[1]);
    }

    private void run(String inputFilePath, String outputDir) {

        // Création SparkSession
        SparkSession spark = SparkSession.builder()
                .appName(FootballStats.class.getName())
                .master("local[*]")
                .getOrCreate();

        // Lecture du fichier csv
        Dataset<Row> dataFrame = spark.read()
                .format("csv")
                .option("header", "true")
                .option("charset", "ISO-8859-1")
                .load(inputFilePath);

        // Convertion df en RDD
        JavaRDD<Row> rdd = dataFrame.javaRDD();
        JavaPairRDD<String, Integer> teamRDD = rdd.mapToPair(row -> new Tuple2<>(row.getAs("home_team"), 1));

        // Nombre de buts pour chaque équipe
        JavaPairRDD<String, Integer> teamCountRDD = teamRDD.reduceByKey((x, y) -> x + y).sortByKey();
        //dataFrame.groupBy("team").sum("team").withColumnRenamed("sum(team)", "goals_scored").sort(functions.desc("goals_scored"));


        // -------1-----------
        // Top 10 des équipes qui marque le plus de but
        Dataset<Row> topScorersTeams = dataFrame.groupBy("team").count().sort(desc("count")).limit(10);
        topScorersTeams.write().format("csv").mode("append").option("header", "true").save(outputDir);

        // --------2----------
        // Top 10 des meilleurs buteurs
        Dataset<Row> topScorersPlayers = dataFrame.groupBy("scorer").count().sort(desc("count")).limit(10);
        topScorersPlayers.write().format("csv").mode("append").option("header", "true").save(outputDir);

        // --------3----------
        // Moyenne des buts par match pour chaque équipe
        Dataset<Row> goalsPerMatchForEachTeam = dataFrame.groupBy("date", "home_team", "away_team").count();

        // Calcul de la moyenne des buts
        Dataset<Row> avgGoalsPerMatch = goalsPerMatchForEachTeam.groupBy("home_team").avg("count").withColumnRenamed("avg(count)", "avg_goals_per_match");
        avgGoalsPerMatch = avgGoalsPerMatch.union(goalsPerMatchForEachTeam.groupBy("away_team").avg("count").withColumnRenamed("avg(count)", "avg_goals_per_match"));

        avgGoalsPerMatch.coalesce(1).write().format("csv").mode("append").option("header", "true").save(outputDir);

        // -------4-----------
        //Pourcentage de victoires, de défaites et de matchs nuls pour tous les pays
        Dataset<Row> matchResults = dataFrame
                .groupBy(col("date"), col("home_team"), col("away_team"))
                .agg(sum(col("home_team").equalTo(col("team")).cast("int")).as("home_score"),
                        sum(col("away_team").equalTo(col("team")).cast("int")).as("away_score"));

        Dataset<Row> newDataFrame = matchResults
                .withColumn("result", when(col("home_score").gt(col("away_score")), "home_team_win")
                        .when(col("home_score").lt(col("away_score")), "away_team_win")
                        .otherwise("draw"))
                .groupBy(col("result"))
                .agg(count("result").as("count"))
                .withColumn("percentage", col("count").divide(matchResults.count()).multiply(100));

        newDataFrame.coalesce(1).write().format("csv").mode("append").option("header", "true").save(outputDir);


        //nombre de match au total
        Dataset<Row> matchCount = dataFrame
                .groupBy(col("home_team"), col("away_team"), col("date"))
                .agg(countDistinct(concat(col("home_team"), col("away_team"), col("date"))).as("match_count"));

        long totalMatchCount = matchCount.agg(sum("match_count")).first().getLong(0);
        System.out.println("----------------NOMBRE TOTAL DE MATCH-----------------------------");
        System.out.println(totalMatchCount);


        // --------5----------
        // Nombre de but à la première mi-temps, la deuxième mi-temps et à la prolongation
        Dataset<Row> goalsAnalysis = dataFrame
                .groupBy("team")
                .agg(
                        sum(when(dataFrame.col("minute").$less(45), 1).otherwise(0)).alias("first_half"),
                        sum(when(dataFrame.col("minute").$greater(45).and(dataFrame.col("minute").$less(90)), 1).otherwise(0)).alias("second_half"),
                        sum(when(dataFrame.col("minute").$greater(90), 1).otherwise(0)).alias("extra_time")
                );
        goalsAnalysis.coalesce(1).write().format("csv").mode("append").option("header", "true").save(outputDir);


        // --------6----------
        // Nombre total de buts marqués pour chaque année
        dataFrame.createOrReplaceTempView("goals");

        Dataset<Row> goalsEachYears = spark.sql("SELECT SUBSTRING(date, 0, 4) as year, count(*) as total_goals from goals group by year order by total_goals desc");

        goalsEachYears.orderBy(goalsEachYears.col("total_goals").desc()).show();
        goalsEachYears.coalesce(1).write().format("csv").mode("append").option("header", "true").save(outputDir);


        // --------7----------
        // CR7 SIIIU Analyse but extérieur et domicile
        Dataset<Row> cristianoGoals = dataFrame.filter("scorer = 'Cristiano Ronaldo'");
        long totalGoals = cristianoGoals.count();
        long homeGoals = cristianoGoals.filter("home_team like '%Portugal%'").count();
        long awayGoals = cristianoGoals.filter("away_team like '%Portugal%'").count();

        double homeGoalsPercentage = (double) homeGoals / totalGoals * 100;
        double awayGoalsPercentage = (double) awayGoals / totalGoals * 100;

        System.out.println("Cristiano Ronaldo's goals at home: " + homeGoalsPercentage + "%");
        System.out.println("Cristiano Ronaldo's goals away: " + awayGoalsPercentage + "%");

        Dataset<Row> df = spark.createDataFrame(
                Arrays.asList(
                        RowFactory.create("Cristiano Ronaldo", homeGoalsPercentage, awayGoalsPercentage)
                ),
                new StructType(new StructField[] {
                        new StructField("player", DataTypes.StringType, false, Metadata.empty()),
                        new StructField("home", DataTypes.DoubleType, false, Metadata.empty()),
                        new StructField("away", DataTypes.DoubleType, false, Metadata.empty())
                })
        );

        df.coalesce(1).write().format("csv").mode("append").option("header", "true").save(outputDir);


        // Stop SparkSession
        spark.stop();

    }
}
