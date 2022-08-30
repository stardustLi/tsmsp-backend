package service

import org.joda.time.DateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import slick.jdbc.PostgresProfile.api._

import models.{Policy, Trace}
import tables.PolicyTableInstance
import utils.db.await

object PolicyService {
  def policyQuery(place: Trace): Try[Option[String]] = Try {
    await(
      (
        PolicyTableInstance.filterByPlace(place)
          .map(policy => policy.contents)
          .result
          .headOption
      ).flatMap(
        {
          case Some(policy) if policy.nonEmpty => DBIO.successful(Some(policy))
          case _ =>
            val place12 = Trace(place.province, place.city, "")
            PolicyTableInstance.filterByPlace(place12)
              .map(policy => policy.contents)
              .result
              .headOption
        }
      ).flatMap(
        {
          case Some(policy) if policy.nonEmpty => DBIO.successful(Some(policy))
          case _ =>
            val place1 = Trace(place.province, "", "")
            PolicyTableInstance.filterByPlace(place1)
              .map(policy => policy.contents)
              .result
              .headOption
        }
      ).flatMap(
        {
          case Some(policy) if policy.nonEmpty => DBIO.successful(Some(policy))
          case _ => DBIO.successful(None)
        }
      ).transactionally
    )
  }

  def policyUpdate(userToken: String, place: Trace, content: String, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.checkPermission(userToken, now).map(
          {
            case None => throw exceptions.NoPermission()
            case Some(permission) =>
              if (!permission.setPolicy) throw exceptions.NoPermission()
          }
        ) >>
          PolicyTableInstance.instance.insertOrUpdate(Policy(place, content))
      ).transactionally
    )
  }
}
