package ca.uwaterloo.cs.ver

import be.ac.ulb.arc.core._
import dk.brics.automaton.RegExp

import java.io.File
import scala.be.ac.ulb.arc.core.ReplacementSpecification
import scala.{+:, Int => SVar}
import scala.collection.immutable.{HashSet => SVars}

import scala.collection.immutable.{HashMap => AllenSet}

import util.control.Breaks._

class pseudoIrrelevanceVerifier (val rs:ReplacementSpecification) extends verifier {

  /***
   * returns list of files in a folder
   * @param dir
   * @return
   */
  def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  /***
   * loads Allen Intervals
   * @return
   */
  def load_allen_relationships(X:SVar,Y:SVar):AllenSet[String,VSetAutomaton]= {
    val base_path="/Users/besat/Desktop/SystemTExperimentPapers/Verifier/data/Allen_Interval/"
    val files = new File(base_path).listFiles()
    var allen_rel_list=new AllenSet[String,VSetAutomaton]
    for (fl<-files)
      {
        //loads csp file into spanner and changes the names of X and Y

        if (fl.getName.startsWith("0"))
        allen_rel_list+=(fl.getName-> CoreSpannerFileReader.getCoreSpanner(fl.getAbsolutePath).get.rename(0,X).get.rename(1,Y).get.automaton)
        else allen_rel_list+= (fl.getName-> CoreSpannerFileReader.getCoreSpanner(fl.getAbsolutePath).get.rename(1,X).get.rename(0,Y).get.automaton)
        //allen_rel_list+= (fl.getName+"_Y", CoreSpannerFileReader.getCoreSpanner(fl.getPath+"/"+fl.getName).get.rename(0,Y).get.rename(1,X))

      }
    allen_rel_list
  }

  /***
   * Conflict
   * Given an update spanner defined by g  If conflicts(g) =∅,
   * then g satisfies the first condition i.e. if the spans for UVar(g) are not equal, they must not overlap
   * @param g
   * @return
   */

  def conflict(g: VSetAutomaton):Option[VSetAutomaton]={

    //TODO: have to change this to random number generator to produce X and Y and make sure they are not part of g variables

    val Max_var= g.V.max

    val X= Max_var+1   //SVar
    val Y= Max_var+2 //SVar

    val g_renamed_x=g.rename(rs.UpdateVar,X)

    val g_renamed_x_projected=g_renamed_x.π(SVars[SVar](X))

    val g_renamed_y=g.rename(rs.UpdateVar,Y)
    val g_renamed_y_projected=g_renamed_y.π(SVars[SVar](Y))

    val alen_rel_list=load_allen_relationships(X,Y)

    var result:Option[VSetAutomaton]=None
        for ((k,v) <- alen_rel_list)
          {
            k match {
              case a if !List("0meets1.csp","0precedes1.csp","1metby0.csp","1precedeby0.csp","0equal1.csp").contains(a)
              => {result= g_renamed_x_projected.⋈(alen_rel_list.get(a).get).get.⋈(g_renamed_y_projected)
                if (result!=None) {
                  println("Found overlap  in update conflict detection function!")
                  return result
                }
              }
              case _=>
            }
            //println("Verifying continues in update conflict detection function!")
          }

    result

  }

  /***
   *
   * @param g
   * @param Z
   * @return
   */

  def ambig(g: VSetAutomaton):Option[VSetAutomaton]= {

    var res:Option[VSetAutomaton]= None
    val Z_list=rs.V- rs.UpdateVar

    val Max_var = g.V.max

    val X = Max_var + 1 //SVar
    val Y = Max_var + 2 //SVar

    val alen_rel_list = load_allen_relationships(X, Y)
    var Γ_X_Y: VSetAutomaton = alen_rel_list.get("0Overlaps1.csp").get // is the disjunction of the fifth through the twelfth basic relationships





    for (z <- Z_list ) {

      val g_renamed_x = g.rename(z, X)

      val g_renamed_x_projected = g_renamed_x.π(SVars[SVar](X, rs.UpdateVar))

      val g_renamed_y = g.rename(z, Y)
      val g_renamed_y_projected = g_renamed_y.π(SVars[SVar](Y, rs.UpdateVar))

      for ((k, v) <- alen_rel_list) {
        k match {
          case a if !List("0equal1.csp").contains(a) =>
            {
              res= g_renamed_x_projected.⋈(alen_rel_list.get(a).get).get.⋈(g_renamed_y_projected)
              if (res!=None) {
                println("Found overlap  in update ambiguity detection function!")
                return res
              }
            } //if k is not amongst the first 4 allen relation
          case _ =>
        }
      }

     //println("Verifying ambiguity continues!")

    }
    res
  }

  /***
   * If for all variables in  SVars(E) conflicts(g,E,Z) = empty update and extractor spanners
are disjoint.
   * @param E
   * @param g
   * @return
   */
  def conflicts(E:VSetAutomaton,g:VSetAutomaton):Option[VSetAutomaton]={


    if (E.V.intersect(g.V).size>0 )
      {
        return null
      }
    //X,Y /∈ SVars(g) ∪ SVars(E)

    val Max_var = (g.V++E.V).max

    val X = Max_var + 1 //SVar
    val Y = Max_var + 2 //SVar

    //πX(ρUVar(g)→X(g))
    val g_renamed_x = g.rename(rs.UpdateVar, X)
    val g_renamed_x_projected = g_renamed_x.π(SVars[SVar](X))

    var res:Option[VSetAutomaton]= None  //the return variable which keeps the unions


    val alen_rel_list=load_allen_relationships(X,Y)


    var allen_list_to_check=List("0Overlaps1.csp","0during1.csp","0finishes1.csp","0starts1.csp","1finishedby0.csp","1Overlapby0.csp","1startby0.csp","1contans0.csp")

    for (z <- E.V ) {
      //println("Verifying ambiguity continues!")

      //πY(ρZ→Y(E))
      val E_renamed_y = E.rename(z, Y)
      val E_renamed_y_projected = E_renamed_y.π(SVars[SVar](Y))
      //Γ(XtY)
      for (a <-allen_list_to_check) {
        //println("Checking allen:"+a)
        res = g_renamed_x_projected.⋈(alen_rel_list.get(a).get).get.⋈(E_renamed_y_projected)
        if (res != None) {
          println("Found overlap  in update/extractor conflicts detection function!")
          return res
        }

      }

    }
    res
  }

  /***
   * an update automaton g is well defined if it has two properties: 1- is not ambiguous 2- doesn't have any conflict
   * @param g
   * @return
   */
  def isWellDefined(g:VSetAutomaton):Boolean={

    val result_conflict=this.conflict(g)


    if (result_conflict==None )
      {
        val result_ambig=this.ambig(g)
        if (result_ambig==None)

        return true
        else
          return  false
      }
    else
      {
        return false
      }

  }

  /***
   * an update Repl(g,U) is disjoint from extractor E if g is disjoint from E, and nabla(g,U) is disjoint from E
   * @param E
   * @param g
   * @return
   */
  def isDisjoint(E:VSetAutomaton,g:VSetAutomaton):Boolean={

    val result_conflicts= conflicts(E,g)

    if (result_conflicts!=None)
      return false
    else
      {
        val result_nabla_conflicts=conflicts(E,g.create_post_update_automaton(g,this.rs))
        if (result_nabla_conflicts!=None)
          return false
        else
          return true
      }

  }

  /***
   * Input:extraction automatonE,update expressionRepl(g,U)Output:Boolean Value
   * Precondition:g is well-defined,
   *              Repl(g,A) disjoint from E,
   *              SVars(E) ∩ SVars(g) =∅
   * @param E
   * @param g
   * @return
   */
  def verifyPseudoIrrelevantUpdates(E:VSetAutomaton,g:VSetAutomaton):Boolean=
  {
    var start = java.lang.System.currentTimeMillis

    //check preconditions
    if (!this.isWellDefined(g))
      {
        //raise proper exception
        println("g is not well defined")
        return  false;
      }

    println("update is welldefined!")
    var stop = java.lang.System.currentTimeMillis
    System.out.print("\n \n")
    System.out.print("****************************************************************************\n")
    System.out.println("Time elapsed for welldefined function: " + formatTime(start, stop) + ".")
    start = java.lang.System.currentTimeMillis

    if (!this.isDisjoint(E,g))
      {
        //raise proper exception
        println("E and g are not disjoint")
        return  false;
      }
    println("E and g are disjoint")
    stop = java.lang.System.currentTimeMillis
    System.out.print("\n \n")
    System.out.print("****************************************************************************\n")
    System.out.println("Time elapsed for disjoint function: " + formatTime(start, stop) + ".")

    if (!E.V.intersect(g.V).isEmpty)
      {
        //raise proper exception
        println("E and g shouldn't have common variables")
        return  false;
      }
    println("E and g do not have common variables")

    var C:VSetAutomaton= null
    var C_prime:VSetAutomaton= null
    var g_prime:VSetAutomaton= null
    var E_i_prime:VSetAutomaton= null


    //for every complete simple path in E
    var Pi_E=E.Π()
    for (p <- Pi_E) {
    {
      //build cross product Ei and g


      start = java.lang.System.currentTimeMillis

// g_prime represents part of g that updates documents extracted by p
      g_prime=(g.⋈(p.to_eVSet())).get.π(g.V)

      stop = java.lang.System.currentTimeMillis
      System.out.print("\n")
      System.out.print("****************************************************************************\n")
      System.out.println("Time elapsed to limit g to the part that has been active for p: " + formatTime(start, stop) + ".")
      start = java.lang.System.currentTimeMillis

      E_i_prime=g_prime.create_post_update_automaton(g_prime,this.rs).⋈(p.to_eVSet()).get.π(E.V)
      stop = java.lang.System.currentTimeMillis
      System.out.print("\n")
      System.out.print("****************************************************************************\n")
      System.out.println("Time elapsed to limit p to the part that has been active for g_prime: " + formatTime(start, stop) + ".")

      // val CΠ=C.Π()
      val PΠ=p

     val E_i_primeΠ=E_i_prime.Π()
//     val number_path_Cprime=C_primeΠ.size
      val number_path_E_i_primeΠ=E_i_primeΠ.size

      // var i=0
    //  while (i<number_path_C)
   //    {
        var current_path_ok=true
         val P_transitions=PΠ.get_transitions_inorder(PΠ)

        var j=0
      breakable  {
        while(j<number_path_E_i_primeΠ){
        val E_i_prime_path = E_i_primeΠ(j)
      //   current_path_ok=true

        val E_i_prime_path_transitions=E_i_prime_path.get_transitions_inorder(E_i_prime_path)

        //conditions to check to make sure they have similar structure
        if (PΠ.δ.size== E_i_prime_path.δ.size)
          {

          var k=0
         // val f=C_path.δ.size
           val f=PΠ.δ.size

          breakable {
            while (k < f) {

              //check the variables are the same

              val P_path_current_transition=P_transitions(k).asInstanceOf[RegXTransition[State]]

              val E_i_prime_path_current_transition=E_i_prime_path_transitions(k).asInstanceOf[RegXTransition[State]]

              if (E_i_prime_path_current_transition.V==P_path_current_transition.V) {

                val ak = new RegExp(P_path_current_transition.Regex).toAutomaton() //getting the regex for each transition
                val ak_prime = new RegExp(E_i_prime_path_current_transition.Regex).toAutomaton()

                var ak_prime_ak= ak_prime.intersection(ak.complement())
                if (!ak_prime_ak.isEmpty) {
                //   current_path_ok=false
                 //  break()
                  println("not psedu irrelevant....")
                  return false
                }
              }
else{          println("different variable pofile is found ")
                println("not psedu irrelevant....")
                return false
              }

              k=k+1
            }

          }
        }
        else {
          println("different number of transitions")
          println("not psedo irrelevant")

          return false
        }
   //    if(current_path_ok)
   //      break()
      j=j+1
        }
      }
  //   if(!current_path_ok)
     //    {
    //         println("the update is not pseudo relevant")
   ///          return false
    //     }

       //  i=i+1

      //}
//      else
//      {
//        //raise error!
//        println("C or Cprime paths have a length of more than 1 which is wrong!")
//        return  false
//      }
    }
    }
    println("The update is pseudo relevant! Horaay")
    return true

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
