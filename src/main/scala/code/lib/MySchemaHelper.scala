package code.lib

import net.liftweb._
import common._
import org.squeryl.Schema
import org.squeryl.Session
import squerylrecord.SquerylRecord
import squerylrecord.RecordTypeMode._
import java.sql.{DriverManager,Connection}
import org.squeryl.internals.DatabaseAdapter;
import org.squeryl.adapters.H2Adapter
import org.squeryl.adapters.MySQLAdapter
import org.squeryl.adapters.PostgreSqlAdapter
import net.liftweb.util.{ Props }
import net.liftweb.db.StandardDBVendor
// db connection pool provider
import com.jolbox.bonecp.BoneCP
import com.jolbox.bonecp.BoneCPConfig

/*
 *  Helper object that initiates the db connection and creates the schema 
 *  Do not use this (createShema) approach in production  
 */
object MySchemaHelper extends Loggable {

  private var usingH2Driver=false
  
  def initSquerylRecordWithMySqlDB {
    initSquerylRecord(new MyMySqlDBSettings)
  }

  private def initSquerylRecord(db: DBSettings) {
    logger.debug("initSquerylRecord with DBSettings: driver="+db.dbDriver+" url="+db.dbUrl+" user="+db.dbUser+" pw="+db.dbPass)
    SquerylRecord.initWithSquerylSession {
      Class.forName(db.dbDriver)
      val session = Session.create(PoolProvider.getPoolConnection(db), db.dbAdapter)
       // return session
      session
    }
  }
  
  trait DBSettings {
    val dbAdapter: DatabaseAdapter;
    val dbDriver: String = ""
    val dbUrl: String = ""
    val dbUser: String = ""
    val dbPass: String = ""
  }

  class MyMySqlDBSettings extends DBSettings with Loggable {
    override val dbAdapter = new MySQLAdapter;
    override val dbDriver = Props.get("mysql.db.driver").openOr("org.h2.Driver")
    override val dbUrl = Props.get("mysql.db.url").openOr("jdbc:h2:database/testXYZDB;FILE_LOCK=NO") //drop to h2 on harddrive
    override val dbUser = Props.get("mysql.db.user").openOr("test")
    override val dbPass = Props.get("mysql.db.password").openOr("")
    logger.debug("MyMySqlDBSettings: seting adapter=MySQLAdapter driver="+dbDriver+" url="+dbUrl+" user="+dbUser+" pw="+dbPass)
  }

  /* database connection pooling provider - we are using BoneCP */
  object PoolProvider extends Loggable {

    
    def getPoolConnection(db: DBSettings) : Connection = {
      if(pool==null){
        pool=initPool(db)
      }
      pool.getConnection
    }      
    
    private var pool:BoneCP=null
    private def initPool(db: DBSettings):BoneCP = {
      // create a new configuration object	
      lazy val config = new BoneCPConfig
      try {
        // load the DB driver class
        Class.forName(db.dbDriver)
        // set the JDBC url
        config.setJdbcUrl(db.dbUrl)
        // set the username
        config.setUsername(db.dbUser)
        // set the password
        config.setPassword(db.dbPass)
        // setup the connection pool
        //pool = Full(new BoneCP(config))
        logger.info("BoneCP connection pool is now initialized.")
        new BoneCP(config)
      } catch {
        case e: Exception => {
          logger.error("BoneCP - FAILED to initialize connection pool: "+e)
          throw new java.lang.Exception("BoneCP - FAILED to initialize connection pool."+e.printStackTrace)
        } 
      }
    }
 }

}