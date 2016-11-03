import java.nio.file.{Files, Paths}

import scala.collection.JavaConversions._
import scala.collection.mutable

object Bootloader {
  type Binaries = List[Char]
  type FrequencyTable = mutable.Map[Binaries, Int]
  lazy val frequencyTables: Array[FrequencyTable] = ngrammSizes.map(ngrammSize => mutable.Map[Binaries, Int]())
  var trainingSetFilePath: String = _
  var plotFilePath: String = _
  var predictionsFilePath: String = _
  var ngrammSizes: Array[Int] = _
  //size of slice for accuracy calculating
  var metricSliceSize: Int = _

  def main(args: Array[String]): Unit = {
    parseArgs(args)

    val trainingBinaries: Binaries = List(Files.readAllLines(Paths.get(trainingSetFilePath)).flatten: _*)
    val predictions = trainingBinaries.indices.map(i => {
      updateFrequencyTables(trainingBinaries.take(i + 1))
      predict(trainingBinaries.take(i + 1))
    }).toList

    val plotPoints = "y,x" +: {
      for (i <- (metricSliceSize - 1).until(trainingBinaries.length))
        yield "%.2f,%d".format(
          calcPredictionAccuracy(trainingBinaries, predictions, i, metricSliceSize),
          i
        )
    }

    Files.write(Paths.get(plotFilePath), plotPoints)
    Files.write(Paths.get(predictionsFilePath), predictions.mkString("") :: Nil)
  }

  def parseArgs(args: Array[String]): Unit = {
    if (args.length > 0 && args(0) == "--help") {
      println("--training-set\t\t\tpath to input file with training data, default = test.txt")
      println("--plot\t\t\t\t\tpath to output file with plot data, default = plot.txt")
      println("--predictions\t\t\tpath to output file with predicted data, default = predictions.txt")
      println("--window-size\t\t\tsize of window for accuracy calculating, default = 100")
      println("--ngramms-sizes\t\t\tsize of ngramm comma separated without spaces, default = 3,4,5")
      System.exit(0)
    }

    def getArg(argName: String): Option[String] = {
      val indexOfArgName = args.indexOf(argName)
      if (indexOfArgName != -1) {
        Some(args(indexOfArgName + 1))
      } else {
        None
      }
    }

    trainingSetFilePath = getArg("--training-set").getOrElse("test.txt")
    plotFilePath = getArg("--plot").getOrElse("plot.txt")
    predictionsFilePath = getArg("--predictions").getOrElse("predictions.txt")
    ngrammSizes = getArg("--ngramms-sizes")
      .map(string =>
        string.split(',').map(_.toInt).toList.toArray)
      .getOrElse(Array(3, 4, 5))
    metricSliceSize = getArg("--training-set").map(_.toInt).getOrElse(100)
  }

  /**
    * Predict character '0' or '1'
    *
    * @param binaries characters according that prediction is done
    * @return predicted character
    */
  def predict(binaries: Binaries): Char = {
    ngrammSizes.zipWithIndex.flatMap {
      case (ngrammSize, i) =>
        val prefix = binaries.takeRight(ngrammSize - 1)
        calcCharsProbability(prefix, frequencyTables(i))
    }.groupBy {
      case (char, probability) => char
    }.mapValues(probabilities => probabilities.map {
      case (char, probability) => probability
    }
      .sum).maxBy {
      case (char, probability) => probability
    }._1
  }

  /**
    * Calculate probability of '0' and '1' chars by frequency table
    *
    * @param prefix         ngramm prefix
    * @param frequencyTable tables of ngramm frequencies
    * @return array of char to probability pairs
    */
  def calcCharsProbability(prefix: Binaries,
                           frequencyTable: FrequencyTable): Array[(Char, Double)] = {
    val zeroEndedNgrammCount = frequencyTable.getOrElse(prefix :+ '0', 0)
    val oneEndedNgrammCount = frequencyTable.getOrElse(prefix :+ '1', 0)
    val totalCount = zeroEndedNgrammCount + oneEndedNgrammCount

    val zeroEndedNgrammProbability = if (totalCount != 0) zeroEndedNgrammCount / totalCount else 0
    val oneEndedNgrammProbability = if (totalCount != 0) oneEndedNgrammCount / totalCount else 0

    Array('1' -> oneEndedNgrammProbability, '0' -> zeroEndedNgrammProbability)
  }

  /**
    * Update each frequency table
    *
    * @param binaries characters according that updates is done
    */

  def updateFrequencyTables(binaries: Binaries): Unit = {
    ngrammSizes.zipWithIndex.foreach {
      case (ngrammSize, i) =>
        val ngramm = binaries.takeRight(ngrammSize)
        frequencyTables(i).put(ngramm, frequencyTables(i).getOrElse(ngramm, 0))
    }
  }

  /**
    * Calculate prediction accuracy for the slice
    *
    * @param index     index of the last element of the calculating slice
    * @param sliceSize slice size
    * @return percent accuracy for the slice
    */
  def calcPredictionAccuracy(binaries: Binaries,
                             predictions: Binaries,
                             index: Int,
                             sliceSize: Int): Double = {
    val labelsToPredictions = binaries.slice(index - sliceSize + 1, index - sliceSize + 1 + sliceSize)
      .zip(predictions.slice(index - sliceSize + 1, index - sliceSize + 1 + sliceSize))
    val totalCount = labelsToPredictions.length
    val countOfRightPredictions = labelsToPredictions.count {
      case (label, prediction) => label == prediction
    }

    countOfRightPredictions.toDouble / totalCount.toDouble * 100
  }
}
