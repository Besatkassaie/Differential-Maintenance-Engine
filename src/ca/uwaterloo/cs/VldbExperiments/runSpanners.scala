package ca.uwaterloo.cs.VldbExperiments

import be.ac.ulb.arc.core.CoreSpanner
import ca.uwaterloo.cs.util.MissingArgumentException

import scala.io.Source
import java.io._
import be.ac.ulb.arc.core._
import be.ac.ulb.arc.runtime._

import scala.collection.mutable.{ArrayBuffer, HashSet => VSRelation}
import be.ac.ulb.arc.runtime.{StringPointerCollection => VSTuple}

import scala.::
import scala.collection.mutable
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


class runSpanners {

}

object runSpanners {
  val timeout = 4500.seconds // specify the timeout in seconds
  def main(args: Array[String]): Unit = {
    var sampleInfoMap = Map[String, List[String]]()
 // look for the folder contaning the text files havin sample file names
 val sampleInfo = args.indexOf("-s")
    if(sampleInfo != -1) {

      val sampleInfoFolder = args(sampleInfo + 1)


      val folder = new File(sampleInfoFolder)
      if (folder.exists() && folder.isDirectory) {
        val files = folder.listFiles().filter(_.isFile).filter(_.getName.endsWith(".txt"))
        for (file <- files) {
         // println(s"Reading contents of ${file.getName}")
          val source = scala.io.Source.fromFile(file)
          val lines = try source.getLines().toList finally source.close()
          sampleInfoMap+=(file.getName ->lines)
          //println(lines)
        }
      } else {
        println(s"The specified folder path '$sampleInfoFolder' does not exist or is not a directory.")
      }

    }

    // look for the input documents folder
    val docsI = args.indexOf("-d")

    if(docsI == -1) throw new MissingArgumentException("Missing argument: -d")
    val docsFolder = args(docsI + 1)

    val docsFolderFile = new File(docsFolder)
    if(!docsFolderFile.exists) throw new IllegalArgumentException("The folder" + docsFolder + " does not exist.")
    if(!docsFolderFile.isDirectory) throw new IllegalArgumentException(docsFolder + " is not a directory.")


    // Look for possible output folder
    val outI = args.indexOf("-o")
    if(outI == -1) {
      throw new Exception("Output folder argument: -o is missing")
    }

      val outputFolderPath = args(outI + 1)
      val outputFolder = new File(outputFolderPath)
      if (!outputFolder.exists()) throw new Exception("Output folder "+outputFolderPath+" does not exist  exists.")

    val coreSpannerPath = args.last

    // Get the input spanner
    val coresp=new File(coreSpannerPath)
    val spannerFileName=coresp.getName
    if (!(new File(coreSpannerPath)).exists) throw new IllegalArgumentException("The file" + args.last + " does not exist.")


    val spannerOpt = CoreSpannerFileReader.getCoreSpanner(args.last)
    //Besat added
    //  val spannerOpt_title = CoreSpannerFileReader.getCoreSpanner(test).get


    if (spannerOpt == None) throw new IllegalArgumentException("The input spanner is not valid.")
    val spanner = spannerOpt.get

    // go through the map and get the sample files
    for ((k,v)<-sampleInfoMap) {

      evaluateWithTimeout(k,spannerFileName,outputFolder,docsFolderFile,v,spanner,timeout)



    }


  }
    def formatTime(diff:Long):String = {


      var x = diff / 1000
      val seconds = x % 60
      x /= 60
      val minutes = x % 60
      x /= 60
      val hours = x % 24

      val res = "" + hours + "h" + minutes + "m" + seconds + "s"

      res
    }
    def formatTime(start:Long, stop:Long):String = {

      val diff = stop - start
      var x = diff / 1000
      val seconds = x % 60
      x /= 60
      val minutes = x % 60
      x /= 60
      val hours = x % 24

      val res = "" + hours + "h" + minutes + "m" + seconds + "s"

      res
    }

    def evaluateWithTimeout(k:String, spannerFileName:String, outputFolder:File,docsFolderFile:File, v:List[String], spanner:CoreSpanner, timeout:Duration):Boolean= {


      val outputFileName = "output_" + k + "_" + spannerFileName + ".txt"
      val outputFile = new File(outputFolder, outputFileName)
      // Check if it already exists
      if (outputFile.exists) throw new Exception("File " + outputFileName + " already exists.")
      val fw = new BufferedWriter(new FileWriter(outputFile, true))

      println(" output_file is created")

      var inputFilesSet = mutable.HashSet[File]()


     for(fname <-v){

       inputFilesSet+= new File(docsFolderFile.getAbsolutePath+"/"+fname)

     }
      println(" after creating the list of file")
      val inputFiles= inputFilesSet.toArray

      //val inputFiles = docsFolderFile.listFiles().filter(file => v.contains(file.getName))
      println(" number of files in memory: "+inputFiles.length)

      // Acquire the strings
      var docs = Map[String, String]()
      for (f <- inputFiles) {
        // System.out.println("reading file content: "+ f)
        val source = Source.fromFile(f, "ISO-8859-1")
        // Besat added \n here to be able to read newlines as characters
        val d = try source.getLines.mkString(System.lineSeparator()) + '\0' finally source.close

        if (d.isInstanceOf[String])
          docs += ((f.getName, d))
      }


      var diff: Long = 0

      println("files are in memory ....")

      val future = Future {
      for (f <- inputFiles) {
        {
          val k = f.getName
          val d = docs(k)
          //Besat this is only happening in apple OS an extra file is created
          val temp = k.mkString("")
          // println(temp)

          // Evaluate the spanner on the document and write
          // resulting tuples to output
          val start = java.lang.System.currentTimeMillis

          val tsOpt = spanner.evaluate(d)


          val stop = java.lang.System.currentTimeMillis
          diff = diff + (stop - start)
          if (tsOpt != None) {

            var t: VSRelation[VSTuple] = tsOpt.asInstanceOf[Option[VSRelation[VSTuple]]].get
            var l: List[VSTuple] = null


            fw.write("-----------------------------" + k + "-----------------------------\n\n")


            OutputWriter.writeOutput(d, t, fw, -1)

            fw.write("\n\n\n")
            fw.flush
          }


        }
      }




      // stop = java.lang.System.currentTimeMillis
      fw.write("\n \n")
      fw.write("****************************************************************************\n")
      fw.write("Time elapsed: " + formatTime(diff) + ".")

      // fw.write("Time elapsed: " + formatTime(start, stop) + ".")
      fw.close

    }
      try {
        Some(Await.result(future, timeout))

     true
      } catch {
        case ex: TimeoutException =>
          fw.write("------------------------timeout occured-----" + timeout + "-----------------------------\n\n")
          fw.flush
          fw.close

          false
      }

    }





}




