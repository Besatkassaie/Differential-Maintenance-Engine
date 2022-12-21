


import ca.uwaterloo.cs.ver.{Update_Spec, psdIrVerifier_regex}

import java.io.{BufferedWriter, FileWriter}
import java.time.LocalDateTime





object main {


  /**
   * this is an entry point to test the verifier where we work on regular expression representation
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
   // val args = new Array[String](4)

//    args(0)="/Verifier/data/extractPrograms/Updates/DateFormat/dateFormat.prg"
//    args(1)="/Verifier/data/extractPrograms/Updates/DateFormat/dateFormat.sp"
//    args(2)="/Verifier/data/extractPrograms/finalExtractors/all_morethanOneauthor/all_more_than_one_author.prg"
//    args(3)="/Verifier/data/extractPrograms/finalExtractors/all_morethanOneauthor/dateUpdateLog.txt"

    println("arg(0) update program "+ args(0) )
    println("arg(1) update specification "+ args(1) )
    println("arg(2) extraction program "+ args(2) )
    println("arg(3) log file program "+ args(3) )

    val fw = new BufferedWriter(new FileWriter(args(3), true))

   // read the regular formula of update from the file
     val updateformula=Update_Spec.getFormulaFromFile(args(0))
    // read the   replacement string and update variable
    val (updateVar_, replacmentString)=Update_Spec.getUpdateSpec(args(1))
    // read the regular formula for the extractor
     val extractorFormula= Update_Spec.getFormulaFromFile(args(2))
    var start2 = java.lang.System.currentTimeMillis

    var us=new Update_Spec(updateformula,updateVar_,replacmentString)
    var verifier=new psdIrVerifier_regex(us)

    var result=verifier.verifyPseudoIrrelevantUpdates(extractorFormula)
    var stop = java.lang.System.currentTimeMillis

    if (result)
     {println("update is psedu  irrelevant")
       fw.write("\n \n")
       fw.write("update is psedu  irrelevant")
     }
    else {
      fw.write("\n \n")
      fw.write("update is not psedu  irrelevant")

      println("update is not psedu irrelevant :(")
    }
    System.out.print("****************************************************************************\n")
    System.out.println("Time elapsed in total: " + formatTime(start2, stop) + ".")
    System.out.print("\n")

    fw.write("\n \n")
    fw.write("****************************************************************************\n")
    fw.write( LocalDateTime.now() + "Time elapsed to verify update:  "+ args(0) + " and extractor " +args(2)+ " "+formatTime(start2, stop) + ".")
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
  }}