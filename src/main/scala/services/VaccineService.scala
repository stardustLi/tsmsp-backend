package services

import models.fields.IDCard
import models.UserVaccine
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import tables.UserVaccineTableInstance
import utils.db.await

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object VaccineService {
  def addVaccine(userToken: String, idCard: IDCard, manufacture: String, time: Long, now: DateTime): Try[Int] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
        UserVaccineTableInstance.filterByIDCard(idCard).length.result
      }).flatMap(count =>
        UserVaccineTableInstance.instance += UserVaccine(idCard.toLowerCase(), manufacture, time, count + 1)
      ).transactionally
    )
  }

  def getVaccines(userToken: String, idCard: IDCard, now: DateTime): Try[List[UserVaccine]] = Try {
    await(
      UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now).flatMap(hasAccess => {
        if (!hasAccess) throw exceptions.NoAccessOfIdCard(idCard)
        UserVaccineTableInstance
          .filterByIDCard(idCard)
          .sortBy(vaccine => vaccine.vaccineType)
          .result
      }).transactionally
    ).toList
  }
}
