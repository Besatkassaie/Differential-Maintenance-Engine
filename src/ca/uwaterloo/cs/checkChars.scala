package ca.uwaterloo.cs

import be.ac.ulb.arc.core.VSetAutomaton

class checkChars {

}



object checkChars {




  def main(args: Array[String]):Unit = {

    printcharseq()
   var newline=System.lineSeparator().toCharArray.head.toInt
    var c1 = '.'
    check(' ')
    check(',')
  //  check('.')
    check(';')
    var min1=Char.MinValue.toInt
    var max1=Char.MaxValue.toInt

    var min=Char.MinValue


  }

  def check(c1:Char)= {
  {

    System.out.println("The value of c1 is: " + c1)

    System.out.println("The code of c1 is: " + c1.toInt)
    System.out.println("After incrementing: " + (c1 + 1).toChar)

    System.out.println("After decrementing: " + (c1 - 1).toChar)

  }


  }
  def printcharseq(): Unit ={
    for (i <- 0 to 128)
      System.out.println(i+ " The value : " + i.toChar )
  }
}
