package services

import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable
import scala.util.Try
import slick.jdbc.PostgresProfile.api._
import models.{Policy, Trace}
import models.fields.TraceID
import tables.PolicyTableInstance
import utils.db.await

object PolicyService {
  val LOGGER: Logger = Logger("PolicyService")

  /******** 对外开放 API: 带 Try，带 await(*.transactionally) ********/
  /** 查询政策 */
  def policyQuery(place: TraceID): Try[Option[String]] = Try {
    var info: Option[Trace] = TraceService.traceID2Trace(place)
    val traceIDs: mutable.ListBuffer[TraceID] = mutable.ListBuffer()

    while (info.isDefined) {
      traceIDs.append(info.get.id)
      info = info.get.parent
    }
    val traceIDList: List[TraceID] = traceIDs.toList

    await(
      PolicyTableInstance.filterByPlaces(traceIDList).result
      .map(
        result =>
          traceIDList.find(
            traceID => result.find(_.place == traceID) match {
              case Some(policy) if policy.contents.nonEmpty => true
              case _ => false
            }
          ) match {
            case Some(traceID) => Some(result.find(_.place == traceID).get.contents)
            case _ => None
          }
      ).transactionally
    )
  }

  /** 添加政策 */
  def policyUpdate(userToken: String, place: TraceID, content: String, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.checkAdminPermission(userToken, _.setPolicy, now) >>
        PolicyTableInstance.instance.insertOrUpdate(Policy(place, content))
      ).transactionally
    )
  }
}
