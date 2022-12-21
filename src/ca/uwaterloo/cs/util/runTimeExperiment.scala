package ca.uwaterloo.cs.util



import java.io._
import scala.be.ac.ulb.arc.core.{ExpressionTree, RegularFormula, RegularFormula_context, ReplacementSpecification}

//import be.ac.ulb.arc.core.{AQLCoreFragmentSpecificationReader, CoreSpannerFileReader, CoreSpannerGenerator}
//import be.ac.ulb.arc.core.{AQLCoreFragmentSpecificationReader}
//import be.ac.ulb.arc.core.{CoreSpannerGenerator}
//import be.ac.ulb.arc.core.{CoreSpannerFileReader}
import scala.{+:, Int => SVar}
import scala.collection.immutable.{HashSet => SVars}

import be.ac.ulb.arc.core._


//import be.ac.ulb.arc.runtime.{ClassicalImplementation, OutputWriter}

import be.ac.ulb.arc.runtime._

import scala.collection.mutable.{HashSet => VSRelation, ArrayBuffer}
import scala.io._
import be.ac.ulb.arc.runtime.{StringPointerCollection => VSTuple}

class runTimeExperiment {

}

/**
 * Created by Besat  on 2022/10/27.
 */

object runTimeExperiment {









  val defaultOutputTuplesFileName = "tuples"
  val defaultOutputSpannerFileName = "spanner.csp"

  def main(args: Array[String]):Unit = {




    val coreSpannerFilePattern = ".+\\.csp"
    val AQLSpecificationFilePattern = ".+\\.aqls"
    val illegalCharPattern = ".*[^a-zA-Z0-9_./-].*"






//
//    // added by Besat for DBLP
//    val args = new Array[String](6)
//    args(0) = "-d"
//    args(1) = "/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/DBLP/benchmark"
//    //args(1) =  "/u5/bkassaie/verifier/dblp/data/benchmark"
//    args(2) = "-o"
//    args(3) = "/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/extractPrograms/Updates/DOI_new/OutPut_DOI_OldToModernFormat_G9NTR.txt"
//    args(4) ="/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/extractPrograms/Updates/DOI_new/DOI_OldToModernFormat_G9NTR.csp"
//    val stat_file_name="/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/extractPrograms/Updates/DOI_new/stat.csv"
    val stat_file_name=args(5)
    try {

      if(args.size == 0) throw new MissingArgumentException("Not enough arguments.")
      // If the input file is a core spanner file

        System.out.println(args.last)
        // Get the input spanner

        val spannerOpt = CoreSpannerFileReader.getCoreSpanner(args(4))
        //Besat added
        //  val spannerOpt_title = CoreSpannerFileReader.getCoreSpanner(test).get


        if(spannerOpt == None) throw new IllegalArgumentException("The input spanner is not valid.")
        val spanner = spannerOpt.get


        // look for the input documents folder
        val docsI = args.indexOf("-d")

        if(docsI == -1) throw new MissingArgumentException("Missing argument: -d")
        val docsFolder = args(docsI + 1)

        val docsFolderFile = new File(docsFolder)
        if(!docsFolderFile.exists) throw new IllegalArgumentException("The folder" + docsFolder + " does not exist.")
        if(!docsFolderFile.isDirectory) throw new IllegalArgumentException(docsFolder + " is not a directory.")

        var outputFileName = defaultOutputTuplesFileName

        // Look for possible output filename
        val outI = args.indexOf("-o")
        if(outI != -1) {

          val fileName = args(outI + 1)
          if(fileName.matches(illegalCharPattern)) throw new IllegalArgumentException("The name " + fileName + " is not a valid filename.")
          outputFileName = fileName
        }

        // Open output file
        println("creating outputfile")
        val statFile=new File(stat_file_name)
        val outputFile = new File(outputFileName)
        // Check if it already exists
        if(outputFile.exists) throw new Exception("File " + outputFileName + " already exists.")
        val fw = new BufferedWriter(new FileWriter(outputFile, true))


        if(statFile.exists) throw new Exception("File " + stat_file_name + " already exists.")
        val fw_stat = new BufferedWriter(new FileWriter(statFile, true))
        // Get input files
        val inputFiles = docsFolderFile.listFiles.filter(_.isFile)

        // Check for additional parameters
        var consolidateV = -1
        val consolidateI = args.indexOf("-C")
        if(consolidateI != -1) {

          consolidateV = args(consolidateI + 1).toInt
        }



        var inputFilessorted= inputFiles.sortWith(sortByLength)



        // Acquire the strings
        var docs = Map[String, String]()
        for(f <- inputFilessorted) {

          val source = Source.fromFile(f, "ISO-8859-1")
          val d = try source.getLines.mkString + '\0' finally source.close

          if (d.isInstanceOf[String])
            docs += ((f.getName, d))
        }

        // Evaluate the spanner on the strings
        val start = java.lang.System.currentTimeMillis
        fw_stat.write("filename,size(byte),time(millsec)\n")
        for(f <- inputFilessorted) {{
          val k=f.getName
          val d=docs(k)
          //Besat this is only happening in apple OS an extra file is created
          val temp=k.mkString("")
          println(temp)
          if(temp!=".DS_Store"){

            // Evaluate the spanner on the document and write
            // resulting tuples to output
            val starteval = java.lang.System.currentTimeMillis
            val tsOpt = spanner.evaluate(d)
            val stopeval = java.lang.System.currentTimeMillis
            val time=stopeval-starteval
            fw_stat.write(f.getName+","+f.length().toString+","+time+"\n")

         }
        }}
        fw_stat.flush()
        fw_stat.close()


    }
    catch {

      case e:Exception => {

        println("ERROR: " + e.getMessage)
        e.printStackTrace
      }
    }


  }



  def sortByLength(f1: File, f2: File) = {
    //println("comparing %s and %s".format(f1, f2))
    f1.length < f2.length
  }

class MissingArgumentException(s:String) extends Exception(s)

}

