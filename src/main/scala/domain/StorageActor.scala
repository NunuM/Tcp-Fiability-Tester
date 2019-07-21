package domain


import java.io.{File, PrintWriter}
import java.util.UUID

import akka.actor.{Actor, ActorLogging}
import domain.TestEvents._
import scalafx.collections.ObservableBuffer

/**
  * Created by nuno on 29-04-2017.
  */
class StorageActor(tests: ObservableBuffer[TableResultsModel]) extends Actor with ActorLogging {

  var test: Map[UUID, TestResults] = Map()

  implicit val dispatcher = this.context.dispatcher

  override def receive: Receive = {
    case newTest: NewTest => {
      if (!test.contains(newTest.testResults.testProprieties.iD)) {
        test += (newTest.testResults.testProprieties.iD -> newTest.testResults)
        tests += TableResultsModel(newTest.testResults)
      }
    }
    case toUpdate: NewTestRecord => {
      test.get(toUpdate.uUID) match {
        case Some(db) => val elementIndex = tests.indexOf(TableResultsModel(db))
          val res = toUpdate.testRecord :: db.testRecords
          db.testRecords = res
          tests.update(elementIndex, TableResultsModel(db))
        case _ => //
      }

    }
    case fn: FinishTest => {
      test.get(fn.uUID) match {
        case Some(db) => val elementIndex = tests.indexOf(TableResultsModel(db))
          db.end = fn.end
          tests.update(elementIndex, TableResultsModel(db))
        case _ => //
      }
    }
    case sinker: SinkResults => {
      val writer = new PrintWriter(new File(sinker.directory + "/tcp_results.csv"))
      writer.write("id,start,end,node,port,duration,interval,timeout,succeeded,failed,total,availability,mtbf\n")
      test.foreach {
        case (k, v) => {
          writer.write(s"${k.toString},${v.start.getTime},${if (v.end == null) "In Progress" else v.end.getTime},${v.testProprieties.inetSocketAddress.getAddress},${v.testProprieties.inetSocketAddress.getPort},${v.testProprieties.duration.toMillis},${v.testProprieties.interval.toMillis},${v.testProprieties.timeout.duration.toMillis},${v.getSucceeded},${v.getFailed},${v.getSucceeded + v.getFailed},${v.getAvailability},${v.getMTBF}\n")
        }
      }
      writer.close()


      test.foreach {
        case (k, v) => {
          val secondWriter = new PrintWriter(new File(s"result-${k.toString}.csv"))
          secondWriter.write("id,start,status\n")
          v.testRecords.foreach { ele =>
            secondWriter.write(s"${k.toString},${ele.registeredAt.getTime},${if (ele.succeeded == 1l) "UP" else "DOWN"}\n")
          }
          secondWriter.close()
        }
      }

      sender() ! true
    }
  }

}
