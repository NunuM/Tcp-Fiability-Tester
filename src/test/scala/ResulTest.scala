
import java.net.InetSocketAddress
import java.sql.Timestamp
import java.util.UUID

import domain.TestEvents.{TestProprieties, TestRecord, TestResults}
import org.scalatest._

import scala.concurrent.duration._
import org.scalactic._
/**
  * Created by nuno on 30-04-2017.
  */
class ResulTest extends FlatSpec {

  val proprieties = TestProprieties(UUID.randomUUID(),new InetSocketAddress("google.pt",443),5 minutes,30 seconds,20 seconds)
  val now = new Timestamp(System.currentTimeMillis())

  "A result from test " should " be calculated" in {

    var emptyList : List[TestRecord] = List()

    var res = TestResults(now,now,proprieties,emptyList)

    assert(res.getSucceeded == 0)
    assert(res.getFailed == 0)
    assert(res.getAvailability == 0.0)
    assert(res.getMTBF == 0.0)

    emptyList = List(TestRecord(now,1l,0l))

    res = TestResults(now,now,proprieties,emptyList)

    assert(res.getSucceeded == 1)
    assert(res.getFailed == 0)
    assert(res.getAvailability == 100)
    assert(res.getMTBF == 0.0)


    val after = new Timestamp(now.toInstant.plusSeconds(30).toEpochMilli)

    emptyList = List(TestRecord(now,1l,0l),TestRecord(after,0l,1l))
    res = TestResults(now,now,proprieties,emptyList)


    assert(res.getSucceeded == 1)
    assert(res.getFailed == 1)
    assert(res.getAvailability == 50.0)
    assert(res.getMTBF == 60.0)


    val afterFromAfter = new Timestamp(after.toInstant.plusSeconds(30).toEpochMilli)

    emptyList = List(TestRecord(now,1l,0l),TestRecord(after,0l,1l),TestRecord(afterFromAfter,0l,1l))
    res = TestResults(now,now,proprieties,emptyList)

    implicit val doubleEquality = TolerantNumerics.tolerantDoubleEquality(0.06)

    assert(res.getSucceeded == 1)
    assert(res.getFailed == 2)
    assert(res.getAvailability === 33.3)
    assert(res.getMTBF == 45.0)


    val afterFromAfterFromAfter = new Timestamp(afterFromAfter.toInstant.plusSeconds(30).toEpochMilli)

    emptyList = List(TestRecord(now,1l,0l),TestRecord(after,0l,1l),TestRecord(afterFromAfter,0l,1l),TestRecord(afterFromAfterFromAfter,0l,1l))
    res = TestResults(now,now,proprieties,emptyList)

    assert(res.getSucceeded == 1)
    assert(res.getFailed == 3)
    assert(res.getAvailability === 25.0)
    assert(res.getMTBF == 40.0)

  }

}
