package code 
package snippet 

import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import net.liftweb.http._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.provider.HTTPCookie
import code.model._

class HelloWorld {
  lazy val date: Box[Date] = DependencyFactory.inject[Date] // inject the date

  // replace the contents of the element with id "time" with the date
  def howdy = {
    //"#io *+" #> ("Last login: " + date.map(_.toString).get) 
    val d = date.map(_.toString).get
    "#io *+" #> <p>Last login: {d}</p>
  }
  

  /*
   lazy val date: Date = DependencyFactory.time.vend // create the date via factory

   def howdy = "#time *" #> date.toString
   */
}

class UserMain extends Loggable {
  private val cookieName = "sl_cookie"
  private var name: String = ""
  private var password: String = ""

  // capture from whence the user came so we can send them back
  private val whence = S.referer openOr "/"

  /**
   * Associate behavior with each HTML element
   */
  //def splash = "type=submit" #> SHtml.onSubmitUnit(process)

  /**
   *  Process the form
   *
  private def process() = {
    OutfitId(Full(1L))
    S.redirectTo("login")
  }
  * 
  */
  
  def login = {
    
    for {
      cookie1 <- S.findCookie(cookieName)
      value <- cookie1.value
    } {
      val d = value
      if (!d.isEmpty()) {
        name = d
      }
    }
    
      "#email" #> SHtml.text(name, name = _) &
      "#password" #> SHtml.password("", password = _) &
      "type=submit" #> SHtml.onSubmitUnit(authenticateUser)
  }
  
  private def authenticateUser() = {
    val redirect = for (
      usera <- User.findUserByEmail(name).?~("User not found")
    ) yield {
      usera.password_pw.match_?(password) match {
        case true => {
          logger.debug("Authenticate: true")

          User.logUserIn(User)
          
          /*
          if (save) {
            val ck = HTTPCookie(cookieName, name).setMaxAge(3600).setPath("/")
            S.addCookie(ck)
          } 
          else {
            S.deleteCookie(cookieName)
          }
          */
          
          Full("/home")
        }
        case _ => {
          password = ""
          Failure("Failed to authenticate user")
        }
      }
   }

    redirect.flatMap(f=>f) match {
      case Full(rd)  => S.redirectTo(rd)
      case Failure(x,_,_) => S.error(x);
      case _ => S.error("Error in login, check user name");
    }
  
  }
  
  def logout = {
    User.logoutCurrentUser
    S.redirectTo("index")
  }
}

class SignUp extends Loggable {
  
  private var username = ""
  private var firstname = ""
  private var lastname = ""
  private var email = ""
  private var password = ""
  private var password_c = ""
    
  def addUser = {
   /* logger.info("In addUser") 
    User.get match {
      
      case Full(user) => {*/
        "#username" #> SHtml.text(username, username = _) &
        "#firstname" #> SHtml.text(firstname, firstname = _) &
        "#lastname" #> SHtml.text(lastname, lastname = _) &
        "#email" #> SHtml.email(email, (f:String) => email = f.trim()) &
        "#password" #> SHtml.password("", password = _) &
        "#password_c" #> SHtml.password("", password_c = _) &
        "type=submit" #> SHtml.ajaxSubmit("Submit", processAdd) //andThen SHtml.makeFormsAjax
      /*}
      case _ => {
        S.error("User not full user");
        S.redirectTo("/")
      }
    }*/
  }
  
  def processAdd(): JsCmd = {

    //User.get match {
      //case Full(u) => {
        val user = User.createRecord.
          firstName(firstname).
          lastName(lastname).
          userName(username).
          email(email);

        if (password.equals(password_c)) {
          val salt = User.generateSalt(password);
          val hashedPassword = User.hashPassword(salt, password);
          user.password_pw(hashedPassword).password_slt(salt);

          val urlStr = S.hostAndPath

          user.validate match {
            case Nil => {
              MySchema.users.insert(user);
              //S.notice("User added, email submitted")
              logger.debug("User added")
            }
            case xs => {
              S.error(xs);
            }
          }
        } 
        else {
          password = "";
          password_c = ""
          S.error("Passwords don't match")
          SetValById("password", "")
          SetValById("password_c", "")

       // }
     // }
     /*
      case _ => {
        logger.warn("Oops, no outfit"); //TODO: Handle error
      }
      */
    }
  }
  
  
}

