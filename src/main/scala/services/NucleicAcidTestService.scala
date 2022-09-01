package services

import models.{UserNucleicAcidTest, NucleicAcidTestPoint, NucleicAcidTestAppoint, DetailedTrace}
import models.fields.IDCard
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import tables.{UserNucleicAcidTestTableInstance, NucleicAcidTestPointTableInstance, NucleicAcidTestAppointTableInstance}
import utils.db.await

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object NucleicAcidTestService {
  def addNucleicAcidTest(userToken: String, idCard: IDCard, now: DateTime, result: Boolean): Try[Int] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(
        hasAccess => {
          if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
          UserNucleicAcidTestTableInstance.instance += UserNucleicAcidTest(idCard.toLowerCase(), now.getMillis, result)
        }
      ).transactionally
    )
  }

  def getNucleicAcidTests(userToken: String, idCard: IDCard, now: DateTime): Try[List[UserNucleicAcidTest]] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(
        hasAccess => {
          if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
          UserNucleicAcidTestTableInstance
            .filterByIDCard(idCard)
            .result
        }
      ).transactionally
    ).toList
  }

  def getPositiveTestPeople(result: Boolean): Try[List[UserNucleicAcidTest]] = Try {
    await(
      (
        UserNucleicAcidTestTableInstance
          .filterByResult(result)
          .result
      ).transactionally
    ).toList
  }

  def addNucleicAcidTestPoint(userToken: String, place: DetailedTrace, now: DateTime): Try[Int] = Try {
    await(//增加核酸点位
      (
        UserService.checkAdminPermission(userToken, _.manageNucleicAcidTestPoints, now) >>
        (NucleicAcidTestPointTableInstance.instance += NucleicAcidTestPoint(place, 0))
      ).transactionally
    )
  }

  def queryWaitingPerson(place: DetailedTrace): Try[Option[Int]] = Try {
    await(//查询核酸点排队人数
      (
        NucleicAcidTestPointTableInstance
          .filterByPlace(place)
          .map(nucleic_acid_test_point => nucleic_acid_test_point.waitingPerson)
          .result
          .headOption
      ).transactionally
    )
  }
/*
  TODO: refactor

  def appointNucleicAcidTest(idCard: IDCard, appointTime: Long, place: DetailedTrace): Try[Int] = Try {
    if (await(NucleicAcidTestAppointTableInstance.filterByIDCard(idCard).exists.result)) throw exceptions.AppointAlreadyExists(idCard)
    await(
      NucleicAcidTestAppointTableInstance.instance += NucleicAcidTestAppoint(idCard.toLowerCase(), appointTime, place)
    )
    await(
      NucleicAcidTestPointTableInstance
        .filterByPlace(place)
        .map(point => point.waitingPerson).result.headOption
        .flatMap(value => {
          NucleicAcidTestPointTableInstance
            .filterByPlace(place)
            .map(point => point.waitingPerson)
            .update(value.get + 1)
        })
    )
  }

  def finishNucleicAcidTest(idCard: IDCard, place: DetailedTrace): Try[Int] = Try {
    if (await(NucleicAcidTestAppointTableInstance.filterByIDCard(idCard).exists.result)) throw exceptions.NoAppoint(idCard)
    await(
      NucleicAcidTestAppointTableInstance
        .filterByIDCard(idCard)
        .delete
    )
    await(
      NucleicAcidTestPointTableInstance
        .filterByPlace(place)
        .map(point => point.waitingPerson).result.headOption
        .flatMap{ value =>
          NucleicAcidTestPointTableInstance
            .filterByPlace(place)
            .map(point => point.waitingPerson)
            .update(value.get - 1)
        }
    )
  }
*/
}
