
import Main.formatTime

import scala.collection.immutable.{HashSet => SVars}
import ca.uwaterloo.cs.ver.{Update_Spec, pseudoIrrelevanceVerifier}
import be.ac.ulb.arc.core._

import scala.{+:, Int => SVar}
import scala.{+:, Int => SVar}
import scala.be.ac.ulb.arc.core.{RegularFormula, ReplacementSpecification}

object main_ {

  var m=ReplacementSpecification
  def main(args: Array[String]):Unit = {

    val spannerOpt = CoreSpannerFileReader.getCoreSpanner("/Users/besat/Desktop/SystemTExperimentPapers/Verifier/data/testPseudIrrel/date.csp")
    val Extractor = CoreSpannerFileReader.getCoreSpanner("/Users/besat/Desktop/SystemTExperimentPapers/Verifier/data/testPseudIrrel/action.csp")
    val test_operationcls=CoreSpannerFileReader.getCoreSpanner("/Users/besat/Desktop/SystemTExperimentPapers/Verifier/data/testPseudIrrel/findopertioncloseequvalencyPath/operationClose.csp")
    //val test_operationcls=CoreSpannerFileReader.getCoreSpanner("/Users/besat/Desktop/SystemTExperimentPapers/Verifier/data/testPseudIrrel/findopertioncloseequvalencyPath/operationClose_small.csp")

    //test_operationcls.get.automaton.toRegexGraph().to_eVSet_old(test_operationcls.get.automaton.toRegexGraph())

    test_operationcls.get.automaton.toRegexGraph().to_eVSet()
  //test_operationcls.get.automaton.Π()(0).to_eVSet().Π()(3).toDot() error here that we generated a wrong path from operation closed function !
    test_operationcls.get.automaton.Π()(0).to_eVSet()
    val s= ReplacementSpecification(SVars[SVar](1,2,3,4),"test$2sdfd$3sdf$4sfs",1)
//    val diamond=s.Diamond_U()


    val verifier=new pseudoIrrelevanceVerifier(s)
    //
    val start = java.lang.System.currentTimeMillis
    //
    //
    val result_sudo_Irrelevant=verifier.verifyPseudoIrrelevantUpdates(Extractor.get.automaton,spannerOpt.get.automaton)
    //

    if (result_sudo_Irrelevant)
      {
        println("it's psudo Irrelevant")
      }
    else
      {
        println("it is not psudo Irrelevant ")
      }
    var stop = java.lang.System.currentTimeMillis
    System.out.print("\n \n")
    System.out.print("****************************************************************************\n")
    System.out.println("Time elapsed for pseudo Irrelevant verification: " + formatTime(start, stop) + ".")
//    val s1= ReplacementSpecification(SVars[SVar](1,2,3,4),"test$2sdfd$3$4",1)
//    val diamond1=s1.Diamond_U()

    //    val verifier=new pseudoIrrelevanceVerifier(ReplacementSpecification(SVars[SVar](1,2,3,4),"",1))
//
//    val start = java.lang.System.currentTimeMillis
//
//
//    val result_conflict=verifier.conflict(spannerOpt.get.automaton)
//
//    var stop = java.lang.System.currentTimeMillis
//    System.out.print("\n \n")
//    System.out.print("****************************************************************************\n")
//    System.out.println("Time elapsed for conflict function: " + formatTime(start, stop) + ".")
//    if (result_conflict==None) {
//      println("no conflict")
//    }
//      else
//        {
//          println("has conflict")
//
//        }
//
//    val result_ambig=verifier.ambig(spannerOpt.get.automaton)
//
//    stop = java.lang.System.currentTimeMillis
//    System.out.print("\n \n")
//    System.out.print("****************************************************************************\n")
//    System.out.println("Time elapsed for ambigius function: " + formatTime(start, stop) + ".")
//    if (result_ambig==null){
//      println("not ambig")
//    }
//    else
//      {
//        println("ambig")
//      }
//
//
//    val result_Conflicts=verifier.conflicts(Extractor.get.automaton,spannerOpt.get.automaton)
//    stop = java.lang.System.currentTimeMillis
//    System.out.print("\n \n")
//    System.out.print("****************************************************************************\n")
//    System.out.println("Time elapsed for conflicts function: " + formatTime(start, stop) + ".")
//
//    if (result_Conflicts==null){
//      println("not conflics")
//    }
//    else
//    {
//      println("conflicts")
//    }


//    val pruned_result=  result.prune(result.δ,result.Q,result.q0,result.qf)
//
//    val conflict_automata=new VSetAutomaton(pruned_result._2,result.q0,result.qf,result.V,pruned_result._1)

    println("End")
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
}
