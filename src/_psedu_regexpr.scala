


import ca.uwaterloo.cs.ver.{Update_Spec, psdIrVerifier_regex}

import java.io.{BufferedWriter, FileWriter}
import java.time.LocalDateTime
import scala.be.ac.ulb.arc.core.{RegularFormula, RegularFormula_context}





object main {


  /**
   * this is an entry point to  verifier where we work on regular expression representation
   * @Author Besat Kassaie
   * args[0]=update's .prg file path
   * arg[1]= specification's  .sp file path
   * arg[2]= extractor .prg file
   * arg[3]=.log file path
   * */

  def main(args: Array[String]):Unit = {


  //  var g=new RegularFormula(".*$ $k$e$y$=$.$⊢{2}(\\p{Upper}|\\p{Lower}|\\d)*$/$(\\p{Upper}|\\p{Lower}|\\d|/)*⊣$.$ $m$d$a$t$e$=$.$\\d$\\d$\\d$\\d$-$\\d$\\d$-$\\d$\\d$.$>$(<$a$u$t$h$o$r$>$(\\p{Lower}| |\\p{Upper}|\\p{Punct}|-)*$<$/$a$u$t$h$o$r$>)*$<$t$i$t$l$e$>$⊢{4}(\\p{Lower}| |\\p{Upper}|\\p{Punct}|\\(|\\)|-)*⊣$<$/$t$i$t$l$e$>$<$p$a$g$e$s$>$(\\d)*$-$(\\d)*$<$/$p$a$g$e$s$>$<$y$e$a$r$>$2$0$1$0$<$/$y$e$a$r$>$.*");
    //var extractor=new RegularFormula(".*$<$>$⊢{4}(e)*⊣$<$/$.*")
    //var sp= "t$2$ds$2$f"
//    val rf_contexted=RegularFormula_context.RegularFormulaToRegularFormulaWithContext(g)
//  val c_g=rf_contexted.rf.regFormulaToVSetAutomaton()
//    println("number of variables : "+c_g.V.size)
//    println("variables : "+c_g.V.toString())

 // find next character and previous character

//    var c1 = '>'
//
//    var min=Char.MinValue
//    System.out.println("The value of min is: " + min)
//
//    System.out.println("The value of c1 is: " + c1)
//
//
//    System.out.println("After incrementing: " + (c1 + 1).toChar)
//
//    System.out.println("After decrementing: " + (c1 - 1).toChar)


//    val updateformula_test=Update_Spec.getFormulaFromFile(args(0))
//    val atm=updateformula_test.regFormulaToVSetAutomaton()
//    val tree=updateformula_test.regFormulaToExpressionTree()
//    var t=new RegularFormula_context(updateformula_test)
   val args = new Array[String](4)

    args(0)="/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/extractPrograms/VLDB_Experiments/VLDBExtractors/Updates/hashTag/hashTag.prg"
    args(1)="/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/extractPrograms/VLDB_Experiments/VLDBExtractors/Updates/hashTag/hashTag.sp"
    args(2)="/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/extractPrograms/VLDB_Experiments/VLDBExtractors/Blog/Q10/action_title_attribute.prg"
    args(3)="/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/extractPrograms/VLDB_Experiments/VLDBExtractors/Blog/Q10/log_action_title_attribute.txt"
////////   var g_3=new RegularFormula("a$(⊢{2}b⊣|⊢{2}y*⊣)$.~;~\\.*$(e$⊢{3}f*$⊢{4}y*⊣⊣|h$⊢{3}i*⊣$j$⊢{4}y*⊣)$(d|k)*")
////    var g_1=new RegularFormula("a$(⊢{2}b⊣|⊢{2}y*⊣)$.~;~\\.*$d$(e$⊢{3}f*$⊢{4}y*⊣⊣|h$⊢{3}i*$j$⊢{4}y*⊣⊣$k)")
////
//    var g_2=new RegularFormula("⊢{2}⊢{1}b⊣⊣|⊢{1}⊢{2}b⊣⊣")
//
//    var g=new RegularFormula("r$.~;~\\.*")
//    RegularFormula_context.RegularFormulaToRegularFormulaWithContext(g_1,-1)

//    var g_1=new RegularFormula(".*$d$h$i$j$y*$k")
//    var g_2=new RegularFormula(".*$d$h$i$j$y*$k")
//    var g_2=new RegularFormula(".*$d$h$i$j$y*$m")
//
//    println("arg(0) update program "+ args(0) )
//    println("arg(1) update specification "+ args(1) )
//    println("arg(2) extraction program "+ args(2) )
//    println("arg(3) log file program "+ args(3) )




    val   fw = new BufferedWriter(new FileWriter(args(3), true))

   // read the regular formula of update from the file
     val updateformula=Update_Spec.getFormulaFromFile(args(0))
    // read the   replacement string and update variable
    val (updateVar_, replacmentString)=Update_Spec.getUpdateSpec(args(1))
    // read the regular formula for the extractor
     val extractorFormula= Update_Spec.getFormulaFromFile(args(2))

    var us=new Update_Spec(updateformula,updateVar_,replacmentString)
    var verifier=new psdIrVerifier_regex(us)
    fw.write( LocalDateTime.now()+"\n")
    fw.write("****************************************************************************\n")
    fw.write(  "Update: "+ args(0) + "\n")
    fw.write(  "Extractor " +args(2)+ "\n")
    fw.write("****************************************************************************\n")
    fw.flush()
    var start = java.lang.System.currentTimeMillis
    var result=verifier.verifyPseudoIrrelevantUpdates(extractorFormula, fw)

    if (result)

     {
       var stop = java.lang.System.currentTimeMillis
       fw.write("update is psedu_irrelevant\n")
       fw.write("****************************************************************************\n")
       fw.write("Time elapsed for verification in total:" + formatTime(start, stop) + ".")
      fw.flush()
     }
    else {
      var stop = java.lang.System.currentTimeMillis
      fw.write("update is not psedu_irrelevant\n")
      fw.write("****************************************************************************\n")
      fw.write("Time elapsed for verification in total:" + formatTime(start, stop) + ".")
      fw.flush()
    }


    fw.close

}

  /** reads the .sp file and retrung (update variable, replacment string) */


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
}