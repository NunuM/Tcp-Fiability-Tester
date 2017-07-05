package domain

import java.net.Socket

import akka.actor.{Actor, ActorLogging, ActorRef}
import domain.TestEvents.{ConnectionFailed, ConnectionHelper, ConnectionSucceeded}

import scala.util.{Failure, Success, Try}

/**
  * Created by nuno on 27-04-2017.
  */
class TesterConnectionActor(counter: ActorRef) extends Actor with ActorLogging {


  override def receive: Receive = {
    case connection: ConnectionHelper => {

      Try {
        val sk = new Socket()
        sk.connect(connection.inetSocketAddress, connection.timeout.duration.toMillis.toInt)
        sk
      } match {
        case Success(sk) => {
          if (sk.isConnected) counter ! ConnectionSucceeded(connection.uUID) else counter ! ConnectionFailed(connection.uUID)
          sk.close()
        }
        case Failure(e) => counter ! ConnectionFailed(connection.uUID)
      }
    }
  }
}

