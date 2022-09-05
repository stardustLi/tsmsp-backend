package services

import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try}
import slick.jdbc.PostgresProfile.api._
import models.{NucleicAcidTestAppoint, NucleicAcidTestPoint, UserNucleicAcidTest}
import models.fields.{IDCard, NucleicAcidTestPointName, TraceID}
import tables.{NucleicAcidTestAppointTableInstance, NucleicAcidTestPointTableInstance, UserNucleicAcidTestTableInstance}
import utils.db.await

object NucleicAcidTestService {
  val LOGGER: Logger = Logger("NucleicAcidTestService")

  /******** 对外开放 API: 带 Try，带 await(*.transactionally) ********/
  /** 增加核酸测试点 */
  def addNucleicAcidTestPoint(userToken: String, place: TraceID, name: NucleicAcidTestPointName, now: DateTime): Try[Int] = Try {
    if (!name.isValid()) throw exceptions.NucleicAcidTestPointNameInvalid(name)
    await(
      (
        UserService.checkAdminPermission(userToken, _.manageNucleicAcidTestPoints, now) >>
        (NucleicAcidTestPointTableInstance.instance += NucleicAcidTestPoint(place, name))
      ).transactionally
    )
  }

  /** 获取某个地区的所有核酸测试点 */
  def getAllNucleicAcidTestPoints(place: TraceID): Try[List[NucleicAcidTestPoint]] = Try {
    await(
      (
        NucleicAcidTestPointTableInstance.filterByPlace(place).result
      ).transactionally
    ).toList
  }

  /** 预约核酸测试 */
  def appointNucleicAcidTest(userToken: String, idCard: IDCard, testPlace: NucleicAcidTestPointName, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now) >>
        NucleicAcidTestPointTableInstance
          .filterByName(testPlace)
          .exists
          .result
      ).flatMap(
        exist => {
          if (!exist) throw exceptions.NucleicAcidTestPointNotExists(testPlace)
          (
            NucleicAcidTestAppointTableInstance.instance +=
              NucleicAcidTestAppoint(idCard.toLowerCase(), testPlace, now.getMillis)
          ).asTry
        }
      ).map(
        {
          case Success(1) => 1
          case _ => throw exceptions.AppointAlreadyExists(idCard)
        }
      ).transactionally
    )
  }

  /** 完成核酸测试 */
  def finishNucleicAcidTest(userToken: String, idCard: IDCard, testPlace: NucleicAcidTestPointName, nucleicResult: Boolean, now: DateTime): Try[Int] = Try {
    await(
      (
        UserService.checkAdminPermission(userToken, _.finishNucleicAcidTest, now) >>
        NucleicAcidTestAppointTableInstance
          .filterByIDCard(idCard)
          .delete
      ).flatMap(
        result => {
          if (result != 1) throw exceptions.NoAppoint(idCard)
          UserNucleicAcidTestTableInstance.instance +=
            UserNucleicAcidTest(idCard, testPlace, now.getMillis, nucleicResult)
        }
      ).transactionally
    )
  }

  /** 查询核酸预约点排队人数 */
  def queryWaitingPersonNumber(place: NucleicAcidTestPointName): Try[Int] = Try {
    await(
      (
        NucleicAcidTestAppointTableInstance
          .filterByPlace(place)
          .length
          .result
      ).transactionally
    )
  }

  /** 获取某个核酸预约点的所有预约 */
  def queryWaitingPerson(userToken: String, place: NucleicAcidTestPointName, now: DateTime): Try[List[NucleicAcidTestAppoint]] = Try {
    await(
      (
        UserService.checkAdminPermission(userToken, _.finishNucleicAcidTest, now) >>
        NucleicAcidTestAppointTableInstance
          .filterByPlace(place)
          .result
      ).transactionally
    ).toList
  }

  /** 获取核酸测试结果 */
  def getNucleicAcidTests(userToken: String, idCard: IDCard, now: DateTime): Try[List[UserNucleicAcidTest]] = Try {
    await(
      (
        UserService.checkUserHasAccessByTokenAndIDCard(userToken, idCard, now) >>
        UserNucleicAcidTestTableInstance
          .filterByIDCard(idCard)
          .result
      ).transactionally
    ).toList
  }
}
