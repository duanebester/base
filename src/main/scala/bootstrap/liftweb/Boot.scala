package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import js.jquery.JQueryArtifacts
import sitemap._
import Loc._
import mapper._

import code.model._
import code.lib._
import net.liftmodules.JQueryModule

import net.liftweb.squerylrecord.SquerylRecord
import org.squeryl.Session
import org.squeryl.adapters.MySQLAdapter
import net.liftweb.squerylrecord.RecordTypeMode.inTransaction
import java.sql.{DriverManager}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor = 
	new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
			     Props.get("db.url") openOr 
			     "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
			     Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }

    // Use Lift's Mapper ORM to populate the database
    // you don't need to use Mapper to use Lift... use
    // any ORM you want
    //Schemifier.schemify(true, Schemifier.infoF _, User)
    
    // Deprecated
    //SquerylRecord.init(() => new MySQLAdapter)
    MySchemaHelper.initSquerylRecordWithMySqlDB

    // Where to search for snippet
    LiftRules.addToPackages("code")
    
    // Define Logged in vs logged out
    val loggedIn = If(() => User.loggedIn_?, () => RedirectResponse("/"))
    val loggedOut = Unless(() => User.loggedIn_?, () => RedirectResponse("/"))
    
    // Menu
    val login = Loc("Login", "login" :: Nil, "Login" ,loggedOut)
    val logout = Loc("Logout", "logout" :: Nil, "Logout" ,loggedIn)
    val signup = Loc("SignUp", "signup" :: Nil, "Sign Up" ,loggedOut)
    val settings = Loc("Settings", "settings" :: Nil, "Settings" ,loggedIn)
    val home = Loc("Home", "home" :: Nil, "Home",loggedIn)

    // Build SiteMap
    def sitemap = SiteMap(
      Menu.i("Base") / "index",// >> User.AddUserMenusAfter, // the simple way to declare a menu
      Menu(login),
      Menu(signup),
      Menu(home),
      //Menu(settings),

      // more complex because this menu allows anything in the
      // /static path to be visible
      Menu(Loc("Static", Link(List("static"), true, "/static/index"), 
	       "Static")),
	       
	  Menu(logout)
	       
    )
	 

    //def sitemapMutators = User.sitemapMutator

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    //LiftRules.setSiteMapFunc(() => sitemapMutators(sitemap))
    LiftRules.setSiteMap(sitemap);

    //Init the jQuery module, see http://liftweb.net/jquery for more information.
    LiftRules.jsArtifacts = JQueryArtifacts
    JQueryModule.InitParam.JQuery=JQueryModule.JQuery182
    JQueryModule.init()

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))    
      
    // Allow resources
    ResourceServer.allow {
      case "css" :: "main.css" :: Nil => true
      case "fonts" :: _ => true
      case "images" :: _ => true
    }

    // Make a transaction span the whole HTTP request
    //S.addAround(DB.buildLoanWrapper)
    S.addAround(new LoanWrapper {
      override def apply[T](f: => T): T =
        {
          inTransaction { f }
        }
    })
  }
}
