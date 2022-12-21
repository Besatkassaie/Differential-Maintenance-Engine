package ca.uwaterloo.cs.psupdate


import ca.uwaterloo.cs.ver.Update_Spec

import java.io.{BufferedWriter, File, FileWriter}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
/**
 * Besat Kassaie
 * @Note: class to apply shift on the extracted relation*/
class ReadOutputFile {

}


object ReadOutputFile {


def applyshift(updateData:mutable.HashMap[String, mutable.HashSet[ArrayBuffer[(Int , Int)]]], U:String,extractedFilePath:String, UVarIndex:Int, shiftedFile:String):ArrayBuffer[String]={
  val start = java.lang.System.currentTimeMillis
  var numberofUpdates=0
  var numberofUpdatesRedords=0
  // Open output file
  val outputFile = new File(shiftedFile)
  // Check if it already exists
  if(outputFile.exists) throw new Exception("File " + shiftedFile + " already exists.")
  val fw = new BufferedWriter(new FileWriter(outputFile, true))
  var newContent=new ArrayBuffer[String]
  import scala.io.Source
  var docname=""
  val docName_Pattern = "(-+)((\\w|\\s|\\d|\\.)+\\.xml)(-+)".r
  val endOfFile_Pattern = "\\*+.".r
  val extractedSpans_Pattern= "\\((\\d+),(\\d+)\\)".r
  val extractedSpans_Line_Pattern= "(\\((\\d+),(\\d+)\\)).+".r
  val source = Source.fromFile(extractedFilePath, "UTF-8")
  val lineIterator = source.getLines
  var line = lineIterator.next()

  //proceed to the first document
  while(lineIterator.hasNext && !line.matches(docName_Pattern.regex)){
    line=lineIterator.next()
  }

  if(!lineIterator.hasNext){
   //means
    println("there is no document with extracted items")
    return newContent

  }
  else {
  while(lineIterator.hasNext) {
      //get file name
      val filename_2=for (m <- docName_Pattern.findFirstMatchIn(line)) yield m.group(2)
      docname=filename_2.get
     var update_relation=updateData.get(docname)
    fw.write(line+"\n")
    fw.flush()
    if(update_relation!=None)
    { println("document is updated "+docname)
      numberofUpdates=numberofUpdates+1
      line=lineIterator.next()

    while(line.matches(extractedSpans_Line_Pattern.regex) || (!line.matches(docName_Pattern.regex) && lineIterator.hasNext)) {
      if (line.matches(extractedSpans_Line_Pattern.regex)) {
        val list=extractedSpans_Pattern.findAllIn(line).toList
        var tempArray=new ArrayBuffer[(Int , Int)]
        for (item <- list){
          val tokens=item.split(",")
          val first=tokens(0).substring(tokens(0).indexOf("(")+1).toInt
          val second=tokens(1).substring(0,tokens(1).indexOf(")")).toInt
          val tuple=(first,second)
          tempArray+=shift(tuple,update_relation.get,U,UVarIndex)
          numberofUpdatesRedords=numberofUpdatesRedords+1

        }


        var numberOftuples=tempArray.size
        var items=line.split(" ")
        var n=items.length
        var m=numberOftuples+1
        var newLine=""
        while(m<items.length){
          newLine=newLine+items(m)
          newLine=newLine+" "
          m=m+1
        }
        newLine=" "+newLine
      //  println(newLine)
        var tempLine=""
        for (m <-tempArray){
          tempLine=tempLine+m.toString()
        }
        newLine=tempLine+newLine
        fw.write(newLine+"\n")
        fw.flush()


      }
      else {
        fw.write(line+"\n")
        fw.flush()

      }
      line=lineIterator.next()
    }
    }
    else{
      var continue=true
      // skip until the next doc
      while (lineIterator.hasNext && continue){
        line=lineIterator.next()
        if (!line.matches(docName_Pattern.regex)){

        fw.write(line+"\n")
        fw.flush()

      }
        else {

          continue=false

        }

      }

    }


  }
  val stop = java.lang.System.currentTimeMillis
  fw.write("\n \n")
  fw.write("****************************************************************************\n")
  fw.write("Time elapsed for shift: " + formatTime(start, stop) + ".")
    fw.write("****************************************************************************\n")
    fw.write("number of updated documents: " + numberofUpdates + ".")
    fw.write("****************************************************************************\n")
    fw.write("number of updated records: " + numberofUpdatesRedords + ".")
   fw.flush()
  fw.close()
  newContent}

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


private def  shift(span:(Int,Int),Update_Relation:mutable.HashSet[ArrayBuffer[(Int , Int)]],U:String, UpdatevarIndex:Int):(Int,Int)={
  var shiftVal=0
  var l=0
  for (row <- Update_Relation){
    //get updatevar
     var(f,s)=row(UpdatevarIndex)
     var varlength=s - f
    // update happens before the input span
    if(f<span._1)
       {
         l=computeLength(U,row)
         shiftVal=shiftVal+varlength-l
       }

  }

    (span._1-shiftVal,span._2-shiftVal)
}


private def computeLength(U:String,tupl:ArrayBuffer[(Int , Int)]): Int ={
  var y=0
  for (t <- tupl){
    y=t._2-t._1+y
  }
   y+U.length
}


  def readResult(file: String):mutable.HashMap[String, mutable.HashSet[ArrayBuffer[(Int , Int)]]]={
    println("File Name: "+file)
    var data_map=new mutable.HashMap[String, mutable.HashSet[ArrayBuffer[(Int , Int)]]]
    import scala.io.Source
    var docname=""
    val docName_Pattern = "(-+)((\\w|\\s|\\d|\\.)+\\.xml)(-+)".r
    val endOfFile_Pattern = "\\*+.".r
    val extractedSpans_Pattern= "\\((\\d+),(\\d+)\\)".r
    val extractedSpans_Line_Pattern= "(\\((\\d+),(\\d+)\\)).+".r
    val source = Source.fromFile(file, "UTF-8")
    val lineIterator = source.getLines
    var line = lineIterator.next()
    //println(line)
    while(!line.matches(endOfFile_Pattern.regex)) {
      if(!line.matches(docName_Pattern.regex))
        throw new Exception( "expect a doc name")
      else{
        //get the first file name
        val filename_2=for (m <- docName_Pattern.findFirstMatchIn(line)) yield m.group(2)
        docname=filename_2.get
      }
      line=lineIterator.next()
      while(line.matches(extractedSpans_Line_Pattern.regex) || (!line.matches(docName_Pattern.regex) && !line.matches(endOfFile_Pattern.regex))) {
        if (line.matches(extractedSpans_Line_Pattern.regex)) {
         val list=extractedSpans_Pattern.findAllIn(line).toList
          var tempArray=new ArrayBuffer[(Int , Int)]
          for (item<-list){
           val tokens=item.split(",")
           val first=tokens(0).substring(tokens(0).indexOf("(")+1).toInt
           val second=tokens(1).substring(0,tokens(1).indexOf(")")).toInt
           val tuple=(first,second)
           tempArray+=tuple
         }
          val elem = data_map.get(docname)

          if (elem == None) {
            var tempset=new  mutable.HashSet[ArrayBuffer[(Int , Int)]]
            tempset+=tempArray
            data_map +=(docname -> tempset )
          }
          else {
            data_map(docname) += tempArray
          }
          tempArray=new ArrayBuffer[(Int , Int)]
        }
        line=lineIterator.next()

      }
    }
     data_map
  }

/** args(0) updateMatchFile path
 * args(1) extractorMatchFile path
 * args(2) extractorShiftedFile path
 * args(3) spec_file path
 * ******/


  def main(args: Array[String]):Unit = {

//     val updateMatchFile="/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/extractPrograms/Updates/DOI_new/output_DOI_OldToModernFormat_JEpe9_small.txt"
//     val extractorMatchFile="/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/extractPrograms/finalExtractors/article_less/output_article_less_At43o_small.txt"
//     val extractorShiftedFile="/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/extractPrograms/finalExtractors/article_less/Output_article_less_At43o_shifted.txt"
//     val spec_file="/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/extractPrograms/Updates/DOI_new/DOI_OldToModernFormat.sp"
    val updateMatchFile=args(0)
    val extractorMatchFile=args(1)
    val extractorShiftedFile= args(2)
    val spec_file=args(3)
    val (updateVar_, replacementString)=Update_Spec.getUpdateSpec(spec_file)
    var update_match=readResult(updateMatchFile)
    var uvar_index= -1
    if(update_match.size==0){
      throw new Exception( "no document has been updated no need to shift")
    }
    else{
      val row=update_match.head._2.head
      var maxlength= -1

      for (r <- row){
        if((r._2-r._1)> maxlength)
          {
            maxlength=r._2-r._1
            uvar_index= row.indexOf(r)
          }
      }
    }
   val r= applyshift(update_match,replacementString,extractorMatchFile,uvar_index,extractorShiftedFile)
  }
}
