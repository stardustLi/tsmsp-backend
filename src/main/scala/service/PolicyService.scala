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
    await (
      PolicyTableInstance.filterByPlace(place).get
        .map(policy => policy.contents)
        .result
        .head
        .transactionally
    )
  }

  def policyUpdate(place: Trace, content: String): Try[Int] = Try {
    val policyQuery = PolicyTableInstance.filterByPlace(place).get
    await (
      policyQuery.result.flatMap(
        policy => {
          if (policy.isEmpty) {
            PolicyTableInstance.instance += Policy(place, content)
          } else {
            PolicyTableInstance.filterByPlace(place).get
              .map(policy => policy.contents)
              .update(content)
          }
        }
      ).transactionally
    )
  }
}
