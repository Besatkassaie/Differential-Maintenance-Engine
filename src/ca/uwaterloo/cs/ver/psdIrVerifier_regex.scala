package ca.uwaterloo.cs.ver

import be.ac.ulb.arc.core.{CoreSpannerFileReader, RegXTransition, State, VSetAutomaton}
import dk.brics.automaton.RegExp

import scala.{Int => SVar}
import scala.collection.immutable.{HashSet => SVars}
import scala.collection.immutable.{HashMap => AllenSet}
import java.io.File
import scala.util.control.Breaks.breakable
import scala.be.ac.ulb.arc.core.{RegularFormula, RegularFormula_context}
/**
 * this calss is verifying  Pseudo-Irrelevance of an update w.r.t  an extractor
 * Both of its input is represented as regular formulas
 *
 * */
class psdIrVerifier_regex (val us:Update_Spec) extends verifier_regex  {


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
    //val base_path="/Users/besat/Desktop/SystemTExperimentPapers/Verifier/data/Allen_Interval/"
    val localDir: String = System.getProperty("user.home")
     println("path: "+localDir)
    val base_path="/u5/bkassaie/verifier/Verifier/data/Allen_Interval"
    val files = new File(base_path).listFiles()
    var allen_rel_list=new AllenSet[String,VSetAutomaton]
    for (fl<-files)
    {
      //loads csp file into spanner and changes the names of X and Y
      // for 0meet1 and 0equal1 we have 2 disjunctions so we load each disjunct and the union them

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

  private def conflict(g: VSetAutomaton):Option[VSetAutomaton]={
    System.out.print("\n \n")
    println("conflict function starts ")
    val Max_var= g.V.max

    val X= Max_var+1   //SVar
    val Y= Max_var+2 //SVar

    val g_renamed_x=g.rename(us.updateVar.toInt,X)

    val g_renamed_x_projected=g_renamed_x.π(SVars[SVar](X))

    val g_renamed_y=g.rename(us.updateVar.toInt,Y)
    val g_renamed_y_projected=g_renamed_y.π(SVars[SVar](Y))

    val alen_rel_list=load_allen_relationships(X,Y)
   // var Γ_X_Y:VSetAutomaton=alen_rel_list.get("0Overlaps1.csp").get // is the disjunction of the fifth through the twelfth basic relationships
    var result:Option[VSetAutomaton]=None
    for ((k,v) <- alen_rel_list)
    {
      k match {
        case a if !List("0meets1_1.csp","0meets1_2.csp","0precedes1.csp","1metby0_2.csp","1metby0_1.csp","1precedeby0.csp","0equal1_1.csp","0equal1_2.csp").contains(a)
        => { //System.out.print("\n \n")
          //System.out.print("allen "+a)
        //  System.out.print("\n \n")
       //   var start = java.lang.System.currentTimeMillis
          result=       g_renamed_x_projected.⋈(alen_rel_list.get(a).get).get.⋈(g_renamed_y_projected)
       //   var stop = java.lang.System.currentTimeMillis
         // System.out.print("****************************************************************************\n")
       //   System.out.println("Time elapsed for join: " + formatTime(start, stop) + ".")
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
   * an update automaton g is well defined if it has two properties:
   * 1- is not ambiguous 2- doesn't have any conflict
   * @param g
   * @return
   */
    /*
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
*/

    def isWellDefined():Boolean={
    val g=this.us.update_regularFormula.regFormulaToVSetAutomaton();

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



  private def ambig(g: VSetAutomaton):Option[VSetAutomaton]= {

    var res:Option[VSetAutomaton]= None
    val Z_list=g.V- us.updateVar.toInt

    val Max_var = g.V.max

    val X = Max_var + 1 //SVar
    val Y = Max_var + 2 //SVar

    val alen_rel_list = load_allen_relationships(X, Y)
   // var Γ_X_Y: VSetAutomaton = alen_rel_list.get("0Overlaps1.csp").get // is the disjunction of the fifth through the twelfth basic relationships

    for (z <- Z_list ) {

      val g_renamed_x = g.rename(z, X)

      val g_renamed_x_projected = g_renamed_x.π(SVars[SVar](X, us.updateVar.toInt))

      val g_renamed_y = g.rename(z, Y)
      val g_renamed_y_projected = g_renamed_y.π(SVars[SVar](Y, us.updateVar.toInt))

      for ((k, v) <- alen_rel_list) {
        k match {
          case a if !List("0equal1_1.csp","0equal1_2.csp").contains(a) =>
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

/**
 * @note A  well-defined update spanner specified by g, is durable if spans
 *      marked by the update variable and spans marked by
 *      context variables by its peer spanner C_UVar(g)(g) are disjoint.
 * */
 def durable():Boolean= {
   val rf= this.us.update_regularFormula
   val updatevar=this.us.updateVar
   // convert g to C(g) means mark context
   val rf_contexted=RegularFormula_context.RegularFormulaToRegularFormulaWithContext(rf,updatevar.toInt)
   println(rf_contexted.rf.regularFormula_str)
   //val g=rf.regFormulaToVSetAutomaton();
   val c_g=rf_contexted.rf.regFormulaToVSetAutomaton()
   var result: Option[VSetAutomaton] = None
   println("number of variables : "+c_g.V.size)
    println("variables : "+c_g.V.toString())
  //get context variables from c_g
   // we know that the formula is update formula which contains an update variable + context variables

  //keep only context variables
   var cvariables=c_g.V
   cvariables-=updatevar.toInt

  val Z_list = cvariables
  //if context variables are empty set then return none as being durable by definition
  if (Z_list.size==0) return true

  val Max_var = c_g.V.max

  val X = Max_var + 1 //SVar
  val Y = Max_var + 2 //SVar

  val c_g_renamed_x = c_g.rename(us.updateVar.toInt, X)

  val c_g_renamed_x_projected = c_g_renamed_x.π(SVars[SVar](X))

   val alen_rel_list=load_allen_relationships(X,Y)
  // is the disjunction of the fifth through the 13th basic relationships
  var allen_list_to_check=List("0Overlaps1.csp","0during1.csp","0finishes1.csp","0starts1.csp","1finishedby0.csp","1Overlapby0.csp",
     "1startby0.csp","1contans0.csp","0equal1_1.csp","0equal1_2.csp")

   var res:Option[VSetAutomaton]= None

   for (z <- Z_list ) {


     val c_g_renamed_y = c_g.rename(z, Y)
     val c_g_renamed_y_projected = c_g_renamed_y.π(SVars[SVar](Y))
     //Γ(XtY)
     for (a <-allen_list_to_check) {
       println("Checking allen: "+a)
       println("variable: "+z)
       var param1=c_g_renamed_x_projected.⋈(alen_rel_list.get(a).get)
       if(param1!=None)
        res = param1.get.⋈(c_g_renamed_y_projected)
       if (res != None) {
         println("not durable!")
         return false
       }

     }

   }
  true
}


/**
 * @note This fuction verify dependency of E on the current update formula and its  nabla
 * I did not implement this in a genral form to be able to optimize knowing that the update varibale
 * encloses all other vars and we do not need to go through all the vars in update formula
 * @return true if E depends on update formula or nabla, false otherwise
 */
def depends(E:RegularFormula):Boolean={
  println("starting depends function..... ")

  val rf_update= this.us.update_regularFormula
  val updatevar=this.us.updateVar
  val update_vset=rf_update.regFormulaToVSetAutomaton();
  val nabla_vset=    this.us.nabla()


  // convert E to C(E) means mark context
  val E_contexted=RegularFormula_context.RegularFormulaToRegularFormulaWithContext(E)
  val E_contexted_vset=E_contexted.rf.regFormulaToVSetAutomaton()
  println("the contexted extractor" +E_contexted.rf.regularFormula_str)
  println("number of variables in extractor: "+ E_contexted_vset.V.size)
  println(E_contexted_vset.V.toString())
  var result: Option[VSetAutomaton] = None

  //get context variables from E_contexted_vset
  val Z_list=E_contexted_vset.V

  val X = (E_contexted_vset.V.max).max(updatevar.toInt) + 1
  val Y = X + 1

  val update_vset_renamed_y = update_vset.rename(us.updateVar.toInt, Y)
  val update_vset_renamed_y_projected = update_vset_renamed_y.π(SVars[SVar](Y))

  val nabla_vset_renamed_y = nabla_vset.rename(us.updateVar.toInt, Y)
  val nabla_vset_renamed_y_projected = nabla_vset_renamed_y.π(SVars[SVar](Y))


  val alen_rel_list=load_allen_relationships(X,Y)
  // is the disjunction of the fifth through the 13th basic relationships
  var allen_list_to_check=List("0Overlaps1.csp","0during1.csp","0finishes1.csp","0starts1.csp","1finishedby0.csp","1Overlapby0.csp",
    "1startby0.csp","1contans0.csp","0equal1_1.csp","0equal1_2.csp")

  var res:Option[VSetAutomaton]= None

  for (z <- Z_list ) {


    val E_contexted_vset_rename_x = E_contexted_vset.rename(z, X)
    val E_contexted_vset_rename_x_projected = E_contexted_vset_rename_x.π(SVars[SVar](X))

    for (a <-allen_list_to_check) {
       println("Checking allen:"+a)
      println("variable: "+z)
      var param1=E_contexted_vset_rename_x_projected.⋈(alen_rel_list.get(a).get)
      if(param1!=None) {
        res = param1.get.⋈(update_vset_renamed_y_projected)

      if (res != None) {
        println("dependent ! resulted from update formula")
        return true
      }
        // verify the nabla
      else {
          res = param1.get.⋈(nabla_vset_renamed_y_projected)
        if (res != None) {
          println("dependent ! resulted from nabla")
          return true
        }

      }
      }
      else {
         println("joining E with allen "+a+" is none ")
      }
    }

  }
  false
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

  def verifyPseudoIrrelevantUpdates(E:RegularFormula):Boolean=
  {
    var start = java.lang.System.currentTimeMillis
    var start2 = java.lang.System.currentTimeMillis

    //check preconditions
    System.out.print("\n")
    println("checking precondition: Whether update is WellDefined ")
    if (!this.isWellDefined())
    {
      //raise proper exception
      println("g is not well defined")
      return  false;
    }
    // check durability for the update spanner

    println("update is well defined!")
    var stop = java.lang.System.currentTimeMillis
    System.out.print("****************************************************************************\n")
    System.out.println("Time elapsed for well defined function: " + formatTime(start, stop) + ".")
    System.out.print("\n")
    println("checking precondition: Whether update is durable ")
    start = java.lang.System.currentTimeMillis
    if(!this.durable())
      {
        println("g is not durable")
        return false
      }
    println("g is  durable")
    stop = java.lang.System.currentTimeMillis
    System.out.print("****************************************************************************\n")
    System.out.println("Time elapsed for durable defined function: " + formatTime(start2, stop) + ".")

    System.out.print("****************************************************************************\n")
    //check pseudo relevancy
    System.out.print("\n")
    println("Verify Pseudo-irrelevancy of Update Spanner")
    println("check dependency of extractor on update and nabla.....")
    if(depends(E))
    {stop = java.lang.System.currentTimeMillis
    System.out.print("\n \n")
    System.out.print("****************************************************************************\n")
    System.out.println("Time elapsed for the whole psedu irrelavancy check is: " + formatTime(start2, stop) + ".")
     false


    }
    else {
      println("The update is pseudo relevant! Horaay")
      stop = java.lang.System.currentTimeMillis
      System.out.print("****************************************************************************\n")
      System.out.println("Time elapsed for the whole psedu irrelavancy check is: " + formatTime(start, stop) + ".")


       true
    }

  }






/**
 * @Note read the file containg regular formulas and verify each one
 * regular formula arein a line
 * */
/**
 * program are save in .prg files
 * program file format is similar to aqls format:
 * E1= expression 1
 * E2= expression2
 * -
 * E3=E1 ⋈ E2
 * */
def verifyProgram(file:String):Boolean={

  import scala.io.Source

  val source = Source.fromFile(file, "UTF-8")
  val lineIterator = source.getLines
  var start = java.lang.System.currentTimeMillis
  var stop = java.lang.System.currentTimeMillis

  for (line <- lineIterator){

      if(!verifyPseudoIrrelevantUpdates(new RegularFormula(line)))
        {
          println("The update is NOT  pseudo relevant! ")
          stop = java.lang.System.currentTimeMillis
          System.out.print("****************************************************************************\n")
          System.out.println("Time elapsed for the whole psedu irrelavancy check of the program is: " + formatTime(start, stop) + ".")

          return false}

}
  println("The update is pseudo relevant! ")
  stop = java.lang.System.currentTimeMillis
  System.out.print("****************************************************************************\n")
  System.out.println("Time elapsed for the whole psedu irrelavancy check of the program is: " + formatTime(start, stop) + ".")
 true

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
