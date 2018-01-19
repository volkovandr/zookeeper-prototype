import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.scalalogging.LazyLogging
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.state.{ConnectionState, ConnectionStateListener}

object StateListenerActor {
  object StateListener {
    def apply(system: ActorSystem): StateListener = {
      new StateListener(system.actorOf(StateListenerActor.props))
    }
  }

  class StateListener(actor: ActorRef) extends ConnectionStateListener with LazyLogging {
    override def stateChanged(client: CuratorFramework, newState: ConnectionState): Unit = {
      actor ! s"Zookeeper connection changed to $newState"
    }
  }

  def props = Props(new StateListenerActor)
}

class StateListenerActor extends Actor with LazyLogging {
  override def receive: Receive = {
    case message: String => logger.info(message)
  }
}


