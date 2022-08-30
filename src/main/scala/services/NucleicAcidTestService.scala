package services

import models.UserNucleicAcidTest
import models.fields.IDCard
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import tables.{UserNucleicAcidTestTableInstance, NucleicAcidTestPointTableInstance}
import utils.db.await

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object NucleicAcidTestService {
  def addNucleicAcidTest(userToken: String, idCard: IDCard, now: DateTime, result: Boolean): Try[Int] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
        UserNucleicAcidTestTableInstance.instance += UserNucleicAcidTest(idCard, now.getMillis, result)
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
}
