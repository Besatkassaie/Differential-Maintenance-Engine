package ca.uwaterloo.cs.VldbExperiments

import java.io.{File, PrintWriter}
import scala.util.Random

class SampleGen {

}

object SampleGen {



  def main(args: Array[String]):Unit = {
    val sampleRate=args(0).toDouble
    val docsFolder = args(1)
    val outputFolderPath = args(2)
    val outputFolder = new File(outputFolderPath)
    if (!outputFolder.exists()) outputFolder.mkdir()

    val docsFolderFile = new File(docsFolder)
    if(!docsFolderFile.exists) throw new IllegalArgumentException("The folder" + docsFolder + " does not exist.")
    if(!docsFolderFile.isDirectory) throw new IllegalArgumentException(docsFolder + " is not a directory.")
    var inputFiles = docsFolderFile.listFiles.filter(_.isFile)
    val fileToRemove = new File(docsFolder+"/.DS_Store")
    inputFiles = inputFiles.filterNot(_ == fileToRemove)

    val shuffled = Random.shuffle(inputFiles.toList)
    val numElements = shuffled.size
    val first5Percent = shuffled.take((numElements * sampleRate).toInt)
    var remainingList = shuffled.drop((numElements * sampleRate).toInt)
    val second5Percent = remainingList.take((numElements * sampleRate).toInt)
     remainingList = remainingList.drop((numElements * sampleRate).toInt)
    val third5Percent = remainingList.take((numElements * sampleRate).toInt)
     remainingList = remainingList.drop((numElements * sampleRate).toInt)
    val forth5Percent = remainingList.take((numElements * sampleRate).toInt)
     remainingList = remainingList.drop((numElements * sampleRate).toInt)
    val fifth5Percent = remainingList.take((numElements * sampleRate).toInt)


//write to files
    val first5PercentFile = new File(outputFolder, "first"+sampleRate.toString+"Percent.txt")
    val second5PercentFile = new File(outputFolder, "second"+sampleRate.toString+"Percent.txt")
    val third5PercentFile = new File(outputFolder, "third"+sampleRate.toString+"Percent.txt")
    val forth5PercentFile = new File(outputFolder, "forth"+sampleRate.toString+"Percent.txt")
    val fifth5PercentFile = new File(outputFolder, "fifth"+sampleRate.toString+"Percent.txt")

    val firstWriter = new PrintWriter(first5PercentFile)
    first5Percent.foreach(file => firstWriter.println(file.getName))
    firstWriter.close()

    val secondWriter = new PrintWriter(second5PercentFile)
    second5Percent.foreach(file => secondWriter.println(file.getName))
    secondWriter.close()

    val thirdWriter = new PrintWriter(third5PercentFile)
    third5Percent.foreach(file => thirdWriter.println(file.getName))
    thirdWriter.close()

    val forthWriter = new PrintWriter(forth5PercentFile)
    forth5Percent.foreach(file => forthWriter.println(file.getName))
    forthWriter.close()

    val fifthWriter = new PrintWriter(fifth5PercentFile)
    fifth5Percent.foreach(file => fifthWriter.println(file.getName))
    fifthWriter.close()


  }

}
