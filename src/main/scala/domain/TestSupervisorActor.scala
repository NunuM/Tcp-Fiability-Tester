package domain

import akka.actor.{Actor, ActorLogging, Props}
import domain.TestEvents.{SinkResults, TableResultsModel, TestProprieties}
import scalafx.collections.ObservableBuffer
/**
  * Created by nuno on 27-04-2017.
  */
class TestSupervisorActor(tests: ObservableBuffer[TableResultsModel])
  extends Actor
    with ActorLogging {


  private val storage = context.actorOf(Props(new StorageActor(tests)))
  private val testers = context.actorOf(Props(new TesterActor(storage)))

  override def receive: Receive = {
    case toTest: TestProprieties => testers forward toTest
    case sinker: SinkResults => storage forward sinker
    case _ => log.info("Unknown message")
  }

}
