package code.lib

import org.squeryl.Schema
import code.model._
import net.liftweb.squerylrecord.RecordTypeMode._
import org.squeryl.ForeignKeyDeclaration

trait SchemaWrapper {
  def doTransaction[A <: Any](code: => A): A = {
    transaction[A](code)
  }
}

object MySchema extends Schema with SchemaWrapper {
  val users = table[User] { "users" }
}