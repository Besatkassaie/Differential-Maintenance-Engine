package ca.uwaterloo.cs.psupdate

import ca.uwaterloo.cs.psupdate.ReadOutputFile.readResult
import ca.uwaterloo.cs.ver.Update_Spec

import java.nio.file.{Files, Paths, StandardCopyOption}
import java.io.{BufferedWriter, File, FileWriter, IOException}
import java.util.Scanner
import scala.collection.mutable

object ApplyUpdates {
  /**
   * This function apply the updates represented by update relation to the
   * original corpus snapshot n and generate
   * n+1 snapshot
   * @param updateRelation path
   * @param originalCorpus_path
   * @param updateSpecFile path
   * @param updatedCorpus_Path
   * */

  def applyUpdatesToCorpus(updateRelation:String,originalCorpus_path:String,updateSpecFile:String, updatedCorpus_Path: String ):Boolean={

    val FileList=getListOfFiles(originalCorpus_path)
    val (updateVar_, replacementString)=Update_Spec.getUpdateSpec(updateSpecFile)
    // a hashmap from file to updated spans
    var updatedFiles=readResult(updateRelation)
    if(updatedFiles.size==0){
      // no document has been updated only copy the files without any changes
      FileList.foreach{f =>
        val src_file = f.toString()

        //val tgt_file = tgt_path + "/" + getFileNameWithTS(f.getName) // If you want to rename files in target
        val tgt_file = updatedCorpus_Path + "/" + f.getName // If you do not want to rename files in target

        copyRenameFile(src_file,tgt_file)
        //moveRenameFile(src_file,tgt_file) - Try this if you want to delete source file
        println("File Copied : " + tgt_file)
      }

    }
    else{
      // go through files if they are updated apply update else just copy
      FileList.foreach{f =>
        val src_file = f.toString()

        val tgt_file = updatedCorpus_Path + "/" + f.getName // If you do not want to rename files in target

        if(updatedFiles.contains(f.getAbsoluteFile.getName))
        {
          //apply update then copy
          var content=readFileContent(f)
          var span_groups=updatedFiles(f.getAbsoluteFile.getName)
          // we know that these regions are not overlapping since update is well defined
          var updateRegions=mutable.HashSet[(Int,Int)]()

          for(s_item <- span_groups){
            var i=0
            var updatevar_offsets=(content.length,0)
              while(i<s_item.length) {
                 if( s_item(i)._1< updatevar_offsets._1 && s_item(i)._2 > updatevar_offsets._2)
                   updatevar_offsets=s_item(i)
                i=i+1
              }
            updateRegions+=updatevar_offsets



          }

          var new_content= applyUpdateToContent(content,replacementString,updateRegions )
          createNewFile(tgt_file,new_content)
        }
        else {
        copyRenameFile(src_file,tgt_file)
        }

      }


    }

    true
  }



  def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  // Function to copy file from source to target - Retain the source file
  def copyRenameFile(source: String, destination: String): Unit = {

    val path = Files.copy(
      Paths.get(source),
      Paths.get(destination),
      StandardCopyOption.REPLACE_EXISTING
      //StandardCopyOption.ATOMIC_MOVE
    )
  }




  def applyUpdateToContent(content:String, UpdateSpecificationString:String, updateRegions:mutable.HashSet[(Int,Int)]): String ={
    var newContent=""
    var updateRegionsList= updateRegions.toList
    var updateRegionsList_Sorted=updateRegionsList.sortWith(_._1<_._1)
    var index=0
    while(index<updateRegionsList_Sorted.length){
      if (index==0)
      {
        newContent=newContent+content.substring(0,updateRegionsList_Sorted(index)._1)+UpdateSpecificationString

      }
      else if (index==updateRegionsList_Sorted.length-1)
        {
          newContent=newContent+content.substring(updateRegionsList_Sorted(index-1)._2, updateRegionsList_Sorted(index)._1)+UpdateSpecificationString+content.substring(updateRegionsList_Sorted(index)._2)
        }
      else {
        newContent= newContent+content.substring(updateRegionsList_Sorted(index-1)._2, updateRegionsList_Sorted(index)._1)+UpdateSpecificationString

      }
     index=index+1

      }
    newContent
    }




  def readFileContent(file:File): String ={
    import java.io.FileNotFoundException
    var content=""
    try {

      val myReader = new Scanner(file)
      while ( {
        myReader.hasNextLine
      }) {
        val data = myReader.nextLine
        content=content+data+"\n"
      }
      myReader.close
    } catch {
      case e: FileNotFoundException =>
        System.out.println("An error occurred.")
        e.printStackTrace()
    }
    content
  }
  def createNewFile(fileName:String, çontent:String): Unit ={
    val file = new File(fileName)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(çontent)
    bw.close()
  }





  def main(args: Array[String]):Unit = {

    val updaterelation="/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/extractPrograms/Updates/DateFormat/outputdateFormat_4l2Fp.txt"
    val originalCorpus_path="/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/DBLP/benchmark"
    val updatedCorpus_Path ="/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/DBLP/benchmark_updated"
     val updateSpecFile="/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/extractPrograms/Updates/DateFormat/dateFormat.sp"
     applyUpdatesToCorpus( updaterelation,originalCorpus_path,updateSpecFile,updatedCorpus_Path)

  }


}
