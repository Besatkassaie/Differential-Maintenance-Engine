package ca.uwaterloo.cs.util



import java.io.{BufferedWriter, File, FileWriter}
import java.time.LocalDateTime
import scala.be.ac.ulb.arc.core.RegularFormula
import scala.reflect.io.Path
import scala.util.Random

class ProgramManagement {




}

/**
 * call main of this class to convert a .prg to its .csp
 * args[0]=single
 * arg[1]=.prg file path
 * arg[2]=.csp folder path
 * arg[3]= .log file path
 * */

object ProgramManagement {


  def main(args: Array[String]):Unit = {

    val start = java.lang.System.currentTimeMillis
    val fw = new BufferedWriter(new FileWriter(args(2), true))

    val programpath = args(0)
    val outputdirectory = args(1)
    val outfile = programToCsp(programpath, outputdirectory)
    //val outfile = programToCsp(args(1), args(2))

    val stop = java.lang.System.currentTimeMillis
    fw.write("\n \n")
    fw.write("****************************************************************************\n")
    fw.write( LocalDateTime.now() + " Time elapsed to generate "+ outfile + " "+ formatTime(start, stop) + ".")
    fw.write("****************************************************************************\n")
    fw.close

    //val outfile=programToAqls(programpath,outputdirectory)
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
/**
 * This function get a program with only one extractor written as expression and
 * output the .csp file and return the file name */
  def programToCsp(program_file:String, outputDir:String):String={

    // extract  program file name and create aqls file name
    var split_from=program_file.lastIndexOf("/")
    var split_to=program_file.lastIndexOf(".")
    var filename=program_file.substring(split_from+1,split_to)
    var path_str=program_file.substring(0,split_from+1)
    var csp_file=path_str+filename
    var new_lines  = List[String]()

    import java.nio.charset.StandardCharsets
    import java.nio.file.Files
    import java.nio.file.Paths

    val lines = Files.readAllLines(Paths.get(program_file), StandardCharsets.UTF_8)
    val expr_str=lines.get(0)
    val path = exprToCSP(expr_str,filename+"_",outputDir)

      if(path == None) throw new Exception("could not create csp for extractor in program: "+program_file)

    path


}
  /**
   * @Note This function read a extraction program and convert it to  .aqls
   * file which can be compiled and run by morciano engine
   *
   * Input program file format is similar to aqls format:
   * E1= expression 1
   * E2= expression2
   * -
   * E3=E1 ⋈ E2
   *
   * We just replace E with its csp file path
   *
   * */

 def programToAqls(program_file:String, outputDir:String):Boolean={

   // extract  program file name and create aqls file name
   var split_from=program_file.lastIndexOf("/")
   var split_to=program_file.lastIndexOf(".")
   var filename=program_file.substring(split_from+1,split_to)
   var path_str=program_file.substring(0,split_from+1)
   var aqls_file=path_str+filename+"_converted"+".aqls"
   var new_lines  = List[String]()
   import scala.io.Source

   val source = Source.fromFile(program_file, "UTF-8")
   val lineIterator = source.getLines

   var line = lineIterator.next()

   // Acquire the spanners
   while(line != "-") {
     val name=line.substring(0,line.indexOf("=")).trim
     val expr_str= line.substring(line.indexOf("=")+1,line.length)
   // val automatonPattern(name, expr_str) = line

     val path = exprToCSP(expr_str,filename+"_",outputDir)


     if(path == None) throw new Exception("could not create csp for extractor: " + name + "in program: "+program_file)

       new_lines=new_lines:+(name+" = "+path)

     line = lineIterator.next()
   }
  // retrieve rest of the file
   while(lineIterator.hasNext){

     new_lines=new_lines:+(line)
     line = lineIterator.next()
   }
   new_lines=new_lines:+(line)

   //write out the file to a .aqls
writeFile(aqls_file,new_lines)

   false


  }

/**
 * convert a regular expression string to a vset and write it in the csp file in the given directory and return the file path*/
   def exprToCSP(expr:String, input_file:String,output_directory:String):String={
    var str=new RegularFormula(expr).regFormulaToVSetAutomaton().toString_()

     // generate output filename
     var new_path=randomFileName(Path.string2path(output_directory),input_file,".csp").get.toString()
    println(new_path)
     import java.nio.charset.StandardCharsets
     import java.nio.file.{Files, Paths}

     Files.write(Paths.get(new_path), str.getBytes(StandardCharsets.UTF_8))

     new_path
   }

/**
 * write a list into a file
 * */

def writeFile(filename: String, lines: List[String]): Unit = {

  val file = new File(filename)
  val bw = new BufferedWriter(new FileWriter(file))
  for (line <- lines) {
    bw.write(line)
    bw.newLine()
  }
  bw.close()
}



  /**
   * Get a available random file name in the specified directory
   * @param dir The director
   * @param prefix File prefix
   * @param suffix File suffix (extension)
   * @param nameSize Size of generated filename. Note: 5 chars is about 60^5 = 777 millions of possible names
   * @return A file Path, or None if can't found anyone
   */
  def randomFileName(dir: Path, prefix: String = "", suffix: String = "", maxTries: Int = 10, nameSize: Int = 5): Option[Path] = {
    val alphabet = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9') ++ ("_")

    //0.- Expensive calculation!
    def generateName = (1 to nameSize).map(_ => alphabet(Random.nextInt(alphabet.size))).mkString

    //1.- Iterator
    val paths = for(_ <- (1 to maxTries).iterator) yield dir/(prefix + generateName + suffix)

    //2.- Return the first non existent file (path)
    paths.find(!_.exists)
  }






}