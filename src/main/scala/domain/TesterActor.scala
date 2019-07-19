package domain

import java.sql.Timestamp

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import domain.TestEvents._

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Created by nuno on 27-04-2017.
  */
class TesterActor(storageActor: ActorRef) extends Actor with ActorLogging {

  import context.dispatcher

  private val nowTime: () => Timestamp = () => new Timestamp(System.currentTimeMillis())

  override def receive: Receive = {
    case toTest: TestProprieties => {

      Future {

        val start = nowTime()

        val testResults = TestResults(start, null, toTest, List())

        storageActor ! NewTest(testResults)

        val counter: ActorRef = context.actorOf(Props(new CounterActor(storageActor)))

        val helpers: ActorRef = context.actorOf(Props(new TesterConnectionActor(counter)))

        log.info(s"Starting test on ${toTest.inetSocketAddress.getAddress} with port ${toTest.inetSocketAddress.getPort}.")

        val cancellable = context.system.scheduler.schedule(
          Duration.Zero,
          FiniteDuration(toTest.interval._1, toTest.interval._2),
          helpers,
          ConnectionHelper(toTest.iD, toTest.inetSocketAddress, toTest.timeout)
        )

        Thread sleep toTest.duration.toMillis


        storageActor ! FinishTest(toTest.iD, nowTime())

        cancellable cancel()

        log.info(s"Finished test on ${toTest.inetSocketAddress.getAddress} with port ${toTest.inetSocketAddress.getPort}.")

      }


    }
    case _ => log.info("Unknown message")
  }
}
