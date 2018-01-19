import java.util.concurrent.TimeUnit

import GroupChecker.{Check, LeaveGroup}
import StateListenerActor.StateListener
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.recipes.locks.InterProcessMutex
import org.apache.curator.retry.ExponentialBackoffRetry

import scala.concurrent.duration._
import scala.io.StdIn.readLine
import akka.pattern.ask

object Main extends App with LazyLogging {
  val conf = ConfigFactory.load()
  val zookeeperConnectionString = conf.getString("zookeeper.connection-string")
  val zookeeperGroupPath = conf.getString("zookeeper.group-path")
  val zookeeperLockPath = conf.getString("zookeeper.lock-path")
  val lockTimeoutSeconds = conf.getInt("zookeeper.lock-timeout-seconds")
  val myId = java.util.UUID.randomUUID().toString
  logger.info(s"My ID is $myId")
  val client = CuratorFrameworkFactory.newClient(zookeeperConnectionString, new ExponentialBackoffRetry(1000, 3))

  val system: ActorSystem = ActorSystem("Main")
  import system.dispatcher
  implicit val timeout: akka.util.Timeout = 60 seconds

  client.getConnectionStateListenable.addListener(StateListener(system))

  client.start()

  val groupActor = system.actorOf(GroupChecker.props(client, myId, zookeeperGroupPath), "GroupChecker")
  val ticker = system.scheduler.schedule(1 second, 100 millisecond, groupActor, Check)

  def shutdown(): Unit = {
    ticker.cancel()
    (groupActor ? LeaveGroup).onComplete(_ => {
      client.close()
      logger.info("Zookeeper connection closed.")
      system.terminate()
    })
  }

  val hook = sys.addShutdownHook(shutdown())

  //So that the log output is not mixed with what we print to the console
  Thread.sleep(2000)

  var doExit = false
  val lockMap = scala.collection.mutable.Map[String, InterProcessMutex]()

  while(!doExit) {
    print("Enter a name for a new lock, or type 'list' to show the list of them, or 'exit' to exit: ")
    val lockName = readLine()
    if(lockName == "list") lockMap.keySet.foreach(println)
    else if(lockName == "exit") doExit = true
    else if(lockName != "") {
      if (lockMap.contains(lockName)) {
        println("This lock already exists. Will release it now")
        lockMap(lockName).release()
        logger.info(s"Released lock $lockName")
        lockMap -= lockName
      }
      else {
        val lock = new InterProcessMutex(client, zookeeperLockPath + "/" + lockName)
        if (lock.acquire(lockTimeoutSeconds, TimeUnit.SECONDS)) {
          logger.info(s"Acquired a lock $lockName")
          lockMap += ((lockName, lock))
        }
        else {
          logger.info(s"Could not acquire a lock $lockName. Already held by someone else?")
        }
      }
    }
  }

  hook.remove()

  shutdown()
}
