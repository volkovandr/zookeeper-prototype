import GroupChecker.{Check, LeaveGroup}
import akka.actor.{Actor, Props}
import com.typesafe.scalalogging.LazyLogging
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.nodes.GroupMember

import scala.collection.JavaConverters._

object GroupChecker {
  def props(zookeeper: CuratorFramework, id: String, groupPath: String) = Props(new GroupChecker(zookeeper, id, groupPath))

  case object Check
  case object LeaveGroup
}

class GroupChecker(zookeeper: CuratorFramework, id: String, groupPath: String) extends Actor with LazyLogging {

  var group: GroupMember = new GroupMember(zookeeper, groupPath, id, Array[Byte]())
  group.start()
  logger.info("Joined the group")
  var members: Set[String] = Set()

  override def receive: Receive = {
    case Check =>
      val newMembers = group.getCurrentMembers.keySet().asScala.toSet
      val added = newMembers.diff(members)
      val removed = members.diff(newMembers)
      if(added.nonEmpty) logger.info(s"Members added: $added")
      if(removed.nonEmpty) logger.info(s"Members removed: $removed")
      members = newMembers
    case LeaveGroup =>
      group.close()
      logger.info("Left the group")
      sender() ! true
    case m => logger.warn(s"Received unknown message: $m")
  }
}
