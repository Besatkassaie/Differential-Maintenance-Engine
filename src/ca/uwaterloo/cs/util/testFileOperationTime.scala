package ca.uwaterloo.cs.util


import java.io._
import scala.be.ac.ulb.arc.core.{ExpressionTree, RegularFormula, RegularFormula_context, ReplacementSpecification}
import scala.collection.mutable

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

class testFileOperationTime {


}

/**
 * Created by Besat
 */
object testFileOperationTime {

  val defaultOutputTuplesFileName = "tuples"
  val defaultOutputSpannerFileName = "spanner.csp"

  def main(args: Array[String]):Unit = {

    //example 1
    //var t=new RegularFormula("r$.*$⊢{1}(⊢{2}(c|f)⊣|⊢{2}(a*)⊣)⊣$.*$⊢{6}((e*)|D$t)⊣").regFormulaToVSetAutomaton();
    //example 2
    //var t=new RegularFormula("(.*$-)*$⊢{1}⊢{2}\\p{Lower}⊣$-$⊢{3}\\p{Upper}⊣⊣$(-$.*)*").regFormulaToVSetAutomaton();
    //example 3
    // var t=new RegularFormula("(.*$-)*$⊢{1}⊢{2}\\p{Lower}⊣$-$⊢{3}\\p{Upper}⊣⊣$(\\$$.*)*").regFormulaToVSetAutomaton();
    //example 4 empty string language
    //var t=new RegularFormula("(.*$-)*$(w$⊢{1}⊢{2}p⊣$-$⊢{3}P⊣⊣|⊢{1}⊢{2}e⊣$-$⊢{3}r⊣⊣|y$⊢{1}⊢{2}q⊣$-$⊢{3}r⊣⊣$x|⊢{1}⊢{2}q⊣$⊢{3}r⊣⊣)$(t$.*)*")
    //var t=new RegularFormula(".*$<$a$u$t$h$o$r$>$C$h$u$n$g$u$a$n$g$ $L$u$<$/$a$u$t$h$o$r$>$<$a$u$t$h$o$r$>$\\p{Upper}$(\\p{Lower})*$ *$\\p{Upper}$(\\p{Lower})*$<$/$a$u$t$h$o$r$>$(<$a$u$t$h$o$r$>$\\p{Upper}$(\\p{Lower})*$ *$\\p{Upper}$(\\p{Lower})*$<$/$a$u$t$h$o$r$>)*$<$t$i$t$l$e$>$⊢{4}(\\p{Lower}|\\p{Upper}|\\d)$(\\p{Lower}|\\p{Upper}|\\d|\\p{Punct}| )*⊣$<$/$t$i$t$l$e$>$<$p$a$g$e$s$>$(\\d)*$-$(\\d)*$<$/$p$a$g$e$s$>$.*").regFormulaToVSetAutomaton()

    // var list=ExpressionTree.normalize(t.regFormulaToExpressionTree().root,"1")

    // var te=RegularFormula_context.RegularFormulaToRegularFormulaWithContext(t,2)
    //var t= new RegularFormula("⊢{1}⊢{2}r⊣$-⊣").regFormulaToExpressionTree()

    //  t.printTree()



    val coreSpannerFilePattern = ".+\\.csp"
    val AQLSpecificationFilePattern = ".+\\.aqls"
    val illegalCharPattern = ".*[^a-zA-Z0-9_./-].*"





    //added by Besat
    //    val args = new Array[String](5)
    //    args(0) = "-d"
    //   args(1) = "src/test/scala/be.ac.ulb.arc/benchmark/excerpt"
    //   args(2) = "-o"
    //   args(3) = "output_newone.txt"
    //   args(4) = "/Users/besat/Desktop/SystemTExperimentPapers/experimentDesing/amorciano_project_sbt/arc/src/test/scala/be.ac.ulb.arc/benchmark/attribute.csp"


    //added by Besat for DBLP
    //    val args = new Array[String](5)
    //    args(0) = "-d"
    //    args(1) = "/Users/besat/Desktop/SystemTExperimentPapers/Verifier/data/DBLP/benchmark"
    //    args(2) = "-o"
    //    args(3) = "/Users/besat/Desktop/SystemTExperimentPapers/Verifier/data/extractPrograms/Updates/DOI_new/output_DBLP_DOI_New.txt"
    //    args(4) ="/Users/besat/Desktop/SystemTExperimentPapers/Verifier/data/extractPrograms/Updates/DOI_new/DOI_OldToModernFormat_0QhSQ.csp"



    //
    // added by Besat for DBLP
//    val args = new Array[String](5)
//    args(0) = "-d"
//    args(1) = "/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/DBLP/benchmark"
//    //args(1) =  "/u5/bkassaie/verifier/dblp/data/benchmark"
//    args(2) = "-o"
//    args(3) = "/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/extractPrograms/Updates/DOI_new/output_DOI_OldToModernFormat_G9NTR.txt"
//    args(4) ="/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/extractPrograms/Updates/DOI_new/DOI_OldToModernFormat_G9NTR.csp"




    try {

      if(args.size == 0) throw new MissingArgumentException("Not enough arguments.")
      // If the input file is a core spanner file
      if(args.last.matches(coreSpannerFilePattern)) {
        System.out.println(args.last)
        // Get the input spanner
        if(!(new File(args.last)).exists) throw new IllegalArgumentException("The file" + args.last + " does not exist.")
        println("getting the core spanner")

        val spannerOpt = CoreSpannerFileReader.getCoreSpanner(args.last)
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
        val outputFile = new File(outputFileName)
        // Check if it already exists
        if(outputFile.exists) throw new Exception("File " + outputFileName + " already exists.")
        val fw = new BufferedWriter(new FileWriter(outputFile, true))

        // Get input files
        val inputFiles = docsFolderFile.listFiles.filter(_.isFile)

        // Check for additional parameters
        var consolidateV = -1
        val consolidateI = args.indexOf("-C")
        if(consolidateI != -1) {

          consolidateV = args(consolidateI + 1).toInt
        }

        var blockV, blockDist, blockCount = -1
        val blockPattern = "\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)".r
        val blockI = args.indexOf("-b")
        if(blockI != -1) {

          val blockPattern(v, d, c) = args(blockI + 1)
          blockV = v.toInt
          blockDist = d.toInt
          blockCount = c.toInt
        }

        var lengthConstraint = -1
        val lengthI = args.indexOf("-l")
        if(lengthI != -1) {

          lengthConstraint = args(lengthI + 1).toInt
        }

        var inputFilessorted= inputFiles.sortWith(sortByLength)



        // Acquire the strings
        val start = java.lang.System.currentTimeMillis
        for(f <- inputFilessorted) {

          val source = Source.fromFile(f, "ISO-8859-1")
          val d = try source.getLines.mkString + '\0' finally source.close

        }

        val stop = java.lang.System.currentTimeMillis
        fw.write("\n \n")
        fw.write("****************************************************************************\n")
        fw.write("Time elapsed only to open/read/close files" + formatTime(start, stop) + ".")
        fw.close
      }
      else if(args.last.matches(AQLSpecificationFilePattern)) {
        val start = java.lang.System.currentTimeMillis
        // Get the specification
        if (!(new File(args.last)).exists) throw new IllegalArgumentException("The file" + args.last + " does not exist.")
        // Check if the user requested specializing the interpreter operations
        val specI = args.indexOf("-s")
        val specOpt = AQLCoreFragmentSpecificationReader.getSpecification(args.last, if(specI != -1) true else false)
        if (specOpt == None) throw new IllegalArgumentException("The input specification is not valid.")
        val spec = specOpt.get

        val compileI = args.indexOf("-c")

        // Execute the AQL specification
        if (compileI == -1) {

          // look for the input documents folder
          val docsI = args.indexOf("-d")
          if (docsI == -1) throw new MissingArgumentException("Missing argument: -d")
          val docsFolder = args(docsI + 1)
          val docsFolderFile = new File(docsFolder)

          if (!docsFolderFile.exists) throw new IllegalArgumentException("The folder" + docsFolder + " does not exist.")
          if (!docsFolderFile.isDirectory) throw new IllegalArgumentException(docsFolder + " is not a directory.")

          var outputFileName = defaultOutputTuplesFileName

          // Look for possible output filename
          val outI = args.indexOf("-o")
          if (outI != -1) {

            val fileName = args(outI + 1)
            if (fileName.matches(illegalCharPattern)) throw new IllegalArgumentException("The name " + fileName + " is not a valid filename.")
            outputFileName = fileName
          }

          // Open output file
          val outputFile = new File(outputFileName)
          // Check if it already exists
          if (outputFile.exists) throw new Exception("File " + outputFileName + " already exists.")
          val fw = new BufferedWriter(new FileWriter(outputFile, true))

          // Get input files
          val inputFiles = docsFolderFile.listFiles.filter(_.isFile)



          // Check for additional parameters
          var consolidateV = -1
          val consolidateI = args.indexOf("-C")
          if(consolidateI != -1) {

            consolidateV = args(consolidateI + 1).toInt
          }

          var blockV, blockDist, blockCount = -1
          val blockPattern = "\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)".r
          val blockI = args.indexOf("-b")
          if(blockI != -1) {

            val blockPattern(v, d, c) = args(blockI + 1)
            blockV = v.toInt
            blockDist = d.toInt
            blockCount = c.toInt
          }

          var lengthConstraint = -1
          val lengthI = args.indexOf("-l")
          if(lengthI != -1) {

            lengthConstraint = args(lengthI + 1).toInt
          }



          // Acquire the strings
          var docs = Map[String, String]()
          for(f <- inputFiles) {

            val source = Source.fromFile(f, "ISO-8859-1")
            val d = try source.getLines.mkString + '\0' finally source.close

            if (d.isInstanceOf[String])
              docs += ((f.getName, d))
          }

          val start = java.lang.System.currentTimeMillis
          // Evaluate the specification on the input files
          for((k,d) <- docs) {

            // Evaluate the spanner on the document and write
            // resulting tuples to output
            //Besat this is only happening in apple OS an extra file
            val temp=k.mkString("")
            if(temp!=".DS_Store"){
              val tsOpt = spec.evaluate(d)
              if(tsOpt != None) {

                var t:VSRelation[VSTuple] = tsOpt.get
                var l:List[VSTuple] =null

                if (consolidateV != -1)
                  t = ClassicalImplementation.Ωo(t, consolidateV)

                if (blockV != -1 && blockDist != -1 && blockCount != -1)
                  l = ClassicalImplementation.β(t, blockV, blockDist, blockCount)

                fw.write("-----------------------------" + k + "-----------------------------\n\n")
                if(l != null)
                  OutputWriter.writeOutput(d, l, fw, lengthConstraint)
                else
                  OutputWriter.writeOutput(d, t, fw, lengthConstraint)
                fw.write("\n\n\n")
                fw.flush
              }
            }
          }
          val stop = java.lang.System.currentTimeMillis
          fw.write("\n \n")
          fw.write("****************************************************************************\n")
          fw.write("Time elapsed: " + formatTime(start, stop) + ".")
          fw.close
        }
        else {

          var outputFileName = defaultOutputSpannerFileName

          // Look for possible output filename
          val outI = args.indexOf("-o")
          if (outI != -1) {

            val fileName = args(outI + 1)
            if (fileName.matches(illegalCharPattern)) throw new IllegalArgumentException("The name " + fileName + " is not a valid filename.")
            outputFileName = fileName + ".csp"
          }

          // Open output file
          val outputFile = new File(outputFileName)
          // Check if it already exists
          if (outputFile.exists) throw new Exception("File " + outputFileName + " already exists.")

          val spanner = CoreSpannerGenerator.generate(spec)

          import java.nio.charset.StandardCharsets
          import java.nio.file.{Files, Paths}

          Files.write(Paths.get(outputFileName), spanner.toString.getBytes(StandardCharsets.UTF_8))
          val stop = java.lang.System.currentTimeMillis
          println("Time elapsed for compiling aql to core spanner: " + formatTime(start, stop) + ".")

        }
      }
      else throw new IllegalArgumentException("Invalid input file: " + args.last)
    }
    catch {

      case e:Exception => {

        println("ERROR: " + e.getMessage)
        e.printStackTrace
      }
    }


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

  def sortByLength(f1: File, f2: File) = {
    //println("comparing %s and %s".format(f1, f2))
    f1.length < f2.length
  }
}

class MissingArgumentException(s:String) extends Exception(s)



