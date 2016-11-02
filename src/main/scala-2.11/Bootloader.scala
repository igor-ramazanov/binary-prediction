import java.nio.file.{Files, Paths}

import scala.collection.JavaConversions._
import scala.collection.mutable

object Bootloader {
  type Binaries = List[Char]
  type FrequencyTable = mutable.Map[Binaries, Int]

  val ngrammSizes = Array(3, 4, 5)
  val frequencyTables: Array[FrequencyTable] = ngrammSizes.map(ngrammSize => mutable.Map[Binaries, Int]())

  //size of slice for accuracy calculating
  val metricSliceSize = 100

  def main(args: Array[String]): Unit = {
    /*val random = new Random()
    val randoms = (for (i <- 1 to 10000)
      yield (math.abs(random.nextInt()) % 2).toString).flatten.mkString("")
    Files.write(Paths.get("test.txt"), List(randoms))
    System.exit(0)*/

    val trainingSetFilePath = args(0)
    val plotFilePath = args(1)
    val predictionsFilePath = args(2)

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
    val frequencySum = zeroEndedNgrammCount + oneEndedNgrammCount

    val zeroEndedNgrammProbability = if (frequencySum != 0) zeroEndedNgrammCount / frequencySum else 0
    val oneEndedNgrammProbability = if (frequencySum != 0) oneEndedNgrammCount / frequencySum else 0

    Array('1' -> oneEndedNgrammProbability, '0' -> zeroEndedNgrammProbability)
  }

  /**
    * Update each frequency table
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
    * Calculate prediction for the slice
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
