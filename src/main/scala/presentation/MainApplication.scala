package presentation

import java.net.{InetAddress, InetSocketAddress}
import java.util.UUID

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout
import domain.TestEvents.TestProprieties
import domain.TestSupervisorActor
import util.AppConfig

import scala.concurrent.duration._
import scala.io.StdIn
import scala.util.Try

/**
  * Created by nuno on 27-04-2017.
  */
object MainApplication extends App {

  val system = ActorSystem(AppConfig.appName)

  val menu = "\t\tTCP CONNECTION TESTER\n" +
    "1 - New Test\n" +
    "2 - Exit"


  var option = 0

  do {
    println(menu)
    option = StdIn.readInt()

    option match {
      case 1 => {

        Try {

          println("\nInsert node:")
          val node = StdIn.readLine()

          println("\nInsert port:")
          val port = StdIn.readInt()

          println("\nInsert test duration:")
          val duration = Duration(StdIn.readLine())

          println("\nInsert test interval:")
          val interval = Duration(StdIn.readLine())

          println("\nInsert test timeout:")
          val timeout = Duration(StdIn.readLine())

          val inetAddress = InetAddress.getByName(node)

          val supervisor: ActorRef = system.actorOf(Props[TestSupervisorActor], s"tester-$node-supervisor")

          supervisor ! TestProprieties(UUID.randomUUID(), new InetSocketAddress(inetAddress, port), duration, interval, Timeout(timeout._1, timeout._2))


        }.recover {
          case e => println("Node is not valid " + e.getMessage)
        }

      }
      case 0 =>
    }

  } while (option != 0)

  system.terminate()
}
