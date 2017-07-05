package domain

import java.sql.Timestamp

import akka.actor.{Actor, ActorLogging, ActorRef}
import domain.TestEvents._

/**
  * Created by nuno on 27-04-2017.
  */
class CounterActor(storage: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {
    case cS: ConnectionSucceeded => {
      storage ! NewTestRecord(cS.uUID, TestRecord(new Timestamp(System.currentTimeMillis()), 1l, 0l))
    }

    case cF: ConnectionFailed => {
      storage ! NewTestRecord(cF.uUID, TestRecord(new Timestamp(System.currentTimeMillis()), 0l, 1l))
    }
  }

}
