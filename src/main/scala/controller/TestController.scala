package controller

import java.net.{InetAddress, InetSocketAddress}
import java.util.UUID

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import domain.TestEvents.{SinkResults, TableResultsModel, TestProprieties}
import domain.TestSupervisorActor
import util.AppConfig

import scala.concurrent.duration.{Duration, _}
import scala.util.{Failure, Success}
import scalafx.application.Platform
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer

/**
  * Created by nuno on 28-04-2017.
  */
class TestController {

  val system = ActorSystem(AppConfig.appName)

  import system.dispatcher

  val resultsMembers = new ObservableBuffer[TableResultsModel]()

  val totalObserver = new StringProperty(this, "Total", resultsMembers.count(_.end.value != "In Progress").toString)
  val activeObserver = new StringProperty(this, "Active", resultsMembers.count(_.end.value == "In Progress").toString)

  resultsMembers.onChange((x, y) => {
    Platform.runLater {
      activeObserver.value = x.count(_.end.value == "In Progress").toString
      totalObserver.value = x.count(_.end.value != "In Progress").toString
    }
  })

  val supervisor: ActorRef = system.actorOf(Props(new TestSupervisorActor(resultsMembers)), s"tester-supervisor")


  def getHostAddress(node: String): InetAddress = {
    InetAddress.getByName(node)
  }

  def submitJob(inetAddress: InetAddress,
                port: Int,
                duration: Duration,
                interval: Duration,
                timeout: Duration): Unit = {


    supervisor ! TestProprieties(UUID.randomUUID(), new InetSocketAddress(inetAddress, port), duration, interval, Timeout(timeout._1, timeout._2))

  }


  def writeTo(directory: String, call: (Boolean) => Unit): Unit = {

    val res = (supervisor.ask(SinkResults(directory))(30 seconds)).mapTo[Boolean]

    res.onComplete {
      case Success(e) => call(e)
      case Failure(exception) => call(false)
    }
  }


  def shutdown = {
    val ft = system.terminate()

    ft.onComplete {
      case Success(e) => System.exit(0)
      case Failure(e) => {
        println(e)
        System.exit(1)
      }
    }

  }

  def getResults: ObservableBuffer[TableResultsModel] = resultsMembers

}
