package service

import models.{Policy, Trace}
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import tables.{PolicyTableInstance, UserTraceTableInstance}
import utils.db.await

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object PolicyService {
  def policyQuery(place: Trace): Try[String] = Try {
    await(
      PolicyTableInstance.filterByPlace(place).get
        .map(policy => policy.contents)
        .result
        .head
        .transactionally
    )
  }

  def policyUpdate(userToken: String, place: Trace, content: String, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.findUserByToken(userToken, now).get.flatMap(userName => UserService.getUserPermission(userName).get).map(
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
