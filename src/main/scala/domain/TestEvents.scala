package domain

import java.net.InetSocketAddress
import java.sql.Timestamp
import java.util.UUID

import akka.util.Timeout

import scala.concurrent.duration.Duration
import scalafx.beans.property.{ObjectProperty, StringProperty}

/**
  * Created by nuno on 27-04-2017.
  */
object TestEvents {

  /**
    *
    * @param iD
    * @param inetSocketAddress
    * @param duration
    * @param interval
    * @param timeout
    */
  case class TestProprieties(
                              iD: UUID = UUID.randomUUID(),
                              inetSocketAddress: InetSocketAddress,
                              duration: Duration,
                              interval: Duration,
                              timeout: Timeout
                            ) {
  }


  /**
    *
    * @param start
    * @param end
    * @param testProprieties
    * @param testRecords
    */
  case class TestResults(
                          start: Timestamp,
                          var end: Timestamp,
                          testProprieties: TestProprieties,
                          var testRecords: List[TestRecord]
                        ) {

    /**
      *
      * @return int
      */
    def getSucceeded: Int = {
      testRecords.map(_.succeeded).sum.toInt
    }

    /**
      *
      * @return int
      */
    def getFailed: Int = {
      testRecords.map(_.failed).sum.toInt
    }

    /**
      *
      * @return double
      */
    def getAvailability: Double = {
      if (testRecords.length > 0) {
        val totalSucceeded = testRecords.map(_.succeeded).sum.toDouble
        val totalTime = testRecords.length * testProprieties.interval

        val uptime = totalSucceeded.toInt * testProprieties.interval

        (uptime / totalTime) * 100
      } else {
        0
      }
    }


    /**
      *
      * @return double
      */
    def getMTBF: Double = {
      val totalSucceeded = testRecords.map(_.succeeded).sum.toDouble
      val totalFails = testRecords.map(_.failed).sum.toDouble

      if (totalFails == 0.0)
        return 0

      val totalTime = testRecords.length * testProprieties.interval
      val mtbf = totalTime / totalFails
      mtbf.toSeconds
    }
  }


  /**
    *
    * @param registeredAt
    * @param succeeded
    * @param failed
    */
  case class TestRecord(
                         registeredAt: Timestamp,
                         var succeeded: Long,
                         var failed: Long
                       )


  /**
    *
    * @param iD
    * @param _start
    * @param _end
    * @param _host
    * @param _port
    * @param _duration
    * @param _interval
    * @param _timeout
    * @param _succeeded
    * @param _failed
    * @param _total
    * @param _availability
    * @param _mtbf
    */
  class TableResultsModel(
                           iD: UUID,
                           _start: String,
                           _end: String,
                           _host: String,
                           _port: Int,
                           _duration: String,
                           _interval: String,
                           _timeout: String,
                           _succeeded: Int,
                           _failed: Int,
                           _total: Int,
                           _availability: Double,
                           _mtbf: Double
                         ) {


    val id = iD
    val start = new StringProperty(this, "Start", _start)
    val end = new StringProperty(this, "End", _end)
    val host = new StringProperty(this, "Node", _host)
    val port = new ObjectProperty(this, "Port", _port)
    val duration = new StringProperty(this, "Duration", _duration)
    val interval = new StringProperty(this, "Interval", _interval)
    val timeout = new StringProperty(this, "Timeout", _timeout)
    var succeeded = new ObjectProperty(this, "Succeeded", _succeeded)
    var failed = new ObjectProperty(this, "Failed", _failed)
    var total = new ObjectProperty(this, "Total", _total)
    var available = new ObjectProperty(this, "Availability", _availability)
    var mtbf = new ObjectProperty(this, "Availability", _mtbf)

    override def equals(o: scala.Any): Boolean = {
      if (o == null)
        return false

      if (!o.isInstanceOf[TableResultsModel]) {
        return false
      }

      val obj = o.asInstanceOf[TableResultsModel]

      obj.id == this.id
    }
  }

  /**
    *
    */
  object TableResultsModel {

    import java.time.ZoneOffset
    import java.time.format.{DateTimeFormatter, FormatStyle}
    import java.util.Locale

    val formatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.UK).withZone(ZoneOffset.UTC)

    implicit val timeString = (t: Timestamp) => formatter.format(t.toInstant)

    def apply(uUID: UUID,
              start: Timestamp,
              end: String,
              host: String,
              port: Int,
              duration: Duration,
              interval: Duration,
              timeout: Duration,
              succeeded: Int,
              failed: Int,
              availability: Double,
              mtbf: Double): TableResultsModel = {
      new TableResultsModel(
        uUID,
        start,
        end,
        host,
        port,
        duration.toString,
        interval.toString,
        timeout.toString,
        succeeded,
        failed,
        succeeded + failed,
        availability,
        mtbf)
    }

    /**
      *
      * @param testResults
      * @return
      */
    def apply(testResults: TestResults): TableResultsModel = {
      new TableResultsModel(testResults.testProprieties.iD,
        testResults.start,
        if (testResults.end == null) "In Progress" else testResults.end,
        testResults.testProprieties.inetSocketAddress.getAddress.getHostAddress + " (" + testResults.testProprieties.inetSocketAddress.getHostName + ")",
        testResults.testProprieties.inetSocketAddress.getPort,
        testResults.testProprieties.duration.toString,
        testResults.testProprieties.interval.toString,
        testResults.testProprieties.timeout.duration.toString,
        testResults.getSucceeded,
        testResults.getFailed,
        testResults.getSucceeded + testResults.getFailed,
        testResults.getAvailability,
        testResults.getMTBF
      )
    }


  }


  /**
    *
    * @param inetSocketAddress
    * @param timeout
    */
  case class ConnectionHelper(uUID: UUID,
                              inetSocketAddress: InetSocketAddress,
                              timeout: Timeout
                             )


  /**
    *
    * @param uUID
    */
  case class ConnectionSucceeded(uUID: UUID)

  /**
    *
    * @param uUID
    */
  case class ConnectionFailed(uUID: UUID)

  /**
    *
    * @param uUID
    */
  case class Increment(uUID: UUID)

  /**
    *
    * @param directory
    */
  case class SinkResults(directory: String)

  /**
    *
    * @param testResults
    */
  case class NewTest(testResults: TestResults)

  /**
    *
    * @param uUID
    * @param testRecord
    */
  case class NewTestRecord(uUID: UUID, testRecord: TestRecord)

  /**
    *
    * @param uUID
    * @param end
    */
  case class FinishTest(uUID: UUID, end: Timestamp)

}
