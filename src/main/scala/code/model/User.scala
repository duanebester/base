package code.model

import net.liftweb.record.{MetaRecord, Record}
import net.liftweb.record.field.{LongField, LongTypedField, StringField, OptionalEmailField} 
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.util.Helpers
import net.liftweb.http.SessionVar
import net.liftweb.http.S
import net.liftweb.common.Loggable
import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.common.Full
import org.squeryl.Session
import org.squeryl.Query
import org.squeryl.annotations.Column
import code.lib._


class User private () extends Record[User] with MyPassword[User] with KeyedRecord[Long]{
  def meta = User
  
  @Column(name="id")
  val idField = new LongField(this, 1)
  
  val email = new OptionalEmailField(this, 48) {
      override def validations = valMinLen(1, "Email is required") _ :: super.validations
      override def setFilter = trim _ :: super.setFilter
  }
  
  val firstName = new StringField(this, 32) {
      override def validations = valMinLen(1, "Firstname is required") _ :: super.validations
      override def setFilter = trim _ :: super.setFilter
  }
  
  val lastName = new StringField(this, 32) {
      override def validations = valMinLen(1, "Lastname is required") _ :: super.validations
      override def setFilter = trim _ :: super.setFilter
  }
  val userName = new StringField(this, 32) {
      override def validations = valMinLen(3, "Username is required") _ :: super.validations
      override def setFilter = trim _ :: super.setFilter
  }
}

object User extends User with MetaRecord[User] with BaseUser[User] {
  
    /**
     * Find user by email.
     */
    def findUserByEmail(email: String): Box[User] = {
      Session.currentSession.setLogger(msg => logger.info(msg))
      val query = from(MySchema.users)(users => where(users.email.value === Some(email)) select (users))
      val result = if (query.isEmpty) Empty else Full(query.single)
      //logger.info("*** validated: " + result.open_!.validated.value)
      result
    }
    /**
     * Add user.
     */
    def addUser(user: User, password: String) {
      val salt = User.generateSalt(password)
      val hashedPassword = User.hashPassword(salt, password);
      user.password_pw(hashedPassword).password_slt(salt);
      MySchema.users.insert(user);
    }
    def loggedIn_? : Boolean = currentUser.isDefined
}

trait MyPassword[T <: MyPassword[T]] extends Record[T] with Loggable {
    self: T =>

    lazy val password_pw = new MyPassword(this, 32)
    lazy val password_slt = new StringField(this, 16)

    class MyPassword(obj: T, size: Int) extends StringField(obj, size) {
      def match_?(toTest: String): Boolean = {
        val salt = password_slt.get
        val hsh = Helpers.hash(toTest + salt)
        val pwd = this.get
        logger.info("password "+hsh)

        return (pwd == hsh)
      }
    }
  }

trait BaseUser[User] extends Loggable {

  //type UserType = ModelType
  
    def logUserIn(who: User) {
      logger.info("removing "+curUser)
      curUser.remove()
      curUser(Full(who))
    }

    def logoutCurrentUser = {
      curUser.remove()
    }

    private object curUser extends SessionVar[Box[User]](Empty)

    def currentUser: Box[User] = curUser.is

    /**
     * Generate salt of specified length
     * @param string to generate salt for
     */
    //    def generateSalt(length: Int): String = {
    def generateSalt(data: String): String = {
      Helpers.randomString(128 - data.length())
    }

    /**
     * Hash password using user supplied password and generate salt
     * @param salt
     * @param password The string to hash
     */
    def hashPassword(salt: String, password: String): String = {
      Helpers.hash(password + salt)
    }
}