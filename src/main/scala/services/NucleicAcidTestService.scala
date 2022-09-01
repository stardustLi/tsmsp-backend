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
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
        UserNucleicAcidTestTableInstance.instance += UserNucleicAcidTest(idCard.toLowerCase(), now.getMillis, result)
      }).transactionally
    )
  }

  def getNucleicAcidTests(userToken: String, idCard: IDCard, now: DateTime): Try[List[UserNucleicAcidTest]] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
        UserNucleicAcidTestTableInstance
          .filterByIDCard(idCard)
          .result
      }).transactionally
    ).toList
  }

  def getPositiveTestPeople(result: Boolean): Try[List[UserNucleicAcidTest]] = Try {
    await(
      UserNucleicAcidTestTableInstance
        .filterByResult(result)
        .result
        .transactionally
    ).toList
  }

  def addNucleicAcidTestPoint(place: DetailedTrace): Try[Int] = Try {
    await(
      NucleicAcidTestPointTableInstance.instance += NucleicAcidTestPoint(place, 0)
    )
  } //应需要权限认证，暂未实现

  def queryWaitingPerson(place: DetailedTrace): Try[Int] = Try {
    await(
      NucleicAcidTestPointTableInstance
        .filterByPlace(place)
        .map(nucleic_acid_test_point => nucleic_acid_test_point.waitingPerson)
        .result
    ).head
  }

  def appointNucleicAcidTest(idCard: IDCard, appointTime: Long, place: DetailedTrace): Try[Int] = Try {
    if (await(NucleicAcidTestAppointTableInstance.filterByIDCard(idCard).exists.result)) throw exceptions.AppointAlreadyExists(idCard)
    await(
      NucleicAcidTestAppointTableInstance.instance += NucleicAcidTestAppoint(idCard, appointTime, place)
    )
    await(
      NucleicAcidTestPointTableInstance
        .filterByPlace(place)
        .map(nucleic_acid_test_point => nucleic_acid_test_point.waitingPerson).result.headOption
        .flatMap{ value =>
          NucleicAcidTestPointTableInstance
            .filterByPlace(place)
            .map(nucleic_acid_test_point => nucleic_acid_test_point.waitingPerson)
            .update(value.get + 1)
        }
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
        .map(nucleic_acid_test_point => nucleic_acid_test_point.waitingPerson).result.headOption
        .flatMap{ value =>
          NucleicAcidTestPointTableInstance
            .filterByPlace(place)
            .map(nucleic_acid_test_point => nucleic_acid_test_point.waitingPerson)
            .update(value.get - 1)
        }
    )
  }
}
