package services

import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import slick.jdbc.PostgresProfile.api._

import models.fields.IDCard
import models.UserVaccine
import tables.UserVaccineTableInstance
import utils.db.await

object VaccineService {
  val LOGGER: Logger = Logger("VaccineService")

  /******** 对外开放 API: 带 Try，带 await(*.transactionally) ********/
  /**
   * 添加疫苗记录
   * @param idCard 身份证号
   * @param manufacture 疫苗接种机构
   * @param time 接种时间
   * @return 1
   */
  def addVaccine(userToken: String, idCard: IDCard, manufacture: String, time: Long, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now) >>
        UserVaccineTableInstance.filterByIDCard(idCard).length.result
      ).flatMap(count =>
        UserVaccineTableInstance.instance += UserVaccine(idCard.toLowerCase(), manufacture, time, count + 1)
      ).transactionally
    )
  }

  /**
   * 获取疫苗接种记录
   * @param idCard 身份证号
   * @return 疫苗接种记录的列表
   */
  def getVaccines(userToken: String, idCard: IDCard, now: DateTime): Try[List[UserVaccine]] = Try {
    await(
      (
        UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now) >>
        UserVaccineTableInstance
          .filterByIDCard(idCard)
          .sortBy(vaccine => vaccine.vaccineType)
          .result
      ).transactionally
    ).toList
  }
}
