package challenge

import org.scalatest.{FlatSpec, Matchers}
import challenge.service._

class WeekSpec extends FlatSpec with Matchers with AdjastSpec {
  val ShopSaleOne = ShopSale(shopUuidOne, Stream(ProdSale(2, 15), ProdSale(1, 12), ProdSale(3, 3)))
  val ShopSaleTwo = ShopSale(shopUuidTwo, Stream(ProdSale(2, 16), ProdSale(3, 6)))

  val shopTurnoverOne = ShopTurnover(shopUuidOne, Stream(ProdTurnover(1, 146.4), ProdTurnover(3, 120.0), ProdTurnover(2, 39)))
  val shopTurnoverTwo = ShopTurnover(shopUuidTwo, Stream(ProdTurnover(3, 24.6), ProdTurnover(2, 198.4)))

  val globalSale: GlobalSale = GlobalSale(Stream(ProdSale(2, 31), ProdSale(1, 12), ProdSale(3, 9)))
  val globalTurnover: GlobalTurnover = GlobalTurnover(Stream(ProdTurnover(2, 237.4), ProdTurnover(1, 146.4), ProdTurnover(3, 144.6)))

  val completeDayKpis: Stream[complete] = (0 to 6).map(rangeVal => {
    complete(dayDate.minusDays(rangeVal), Stream(ShopSaleOne, ShopSaleTwo), globalSale, Stream(shopTurnoverOne, shopTurnoverTwo), globalTurnover)
  }).toStream

  "The Week Kpi Calculator" should "output correct sales results" in {
    // EXECUTE
    val completeWeekKpi = Week.computeWeek(dayDate, completeDayKpis)

    // ASSERT
    completeWeekKpi.lastDayDate shouldBe dayDate

    // -- Tests on shop 1
    val shop1WeekShopSales = completeWeekKpi.weekShopSales
      .find(shopSale => shopSale.shopUuid == shopUuidOne)
      .map(shopSale => shopSale.productSales)

    shop1WeekShopSales shouldBe defined
    shop1WeekShopSales.get should contain allOf(ProdSale(2, 105), ProdSale(1, 84), ProdSale(3, 21))

    // -- Tests on shop 2
    val shop2WeekShopSales = completeWeekKpi.weekShopSales
      .find(shopSale => shopSale.shopUuid == shopUuidTwo)
      .map(shopSale => shopSale.productSales)

    shop2WeekShopSales shouldBe defined
    shop2WeekShopSales.get should contain allOf(ProdSale(2, 112), ProdSale(3, 42))

    // Tests on Global
    completeWeekKpi.weekGlobalSales.productSales should contain allOf(ProdSale(2, 217), ProdSale(1, 84), ProdSale(3, 63))
  }

  "The Week Kpi Calculator" should "output correct turnover results" in {
    // EXECUTE
    val completeWeekKpi = Week.computeWeek(dayDate, completeDayKpis)

    // ASSERT
    completeWeekKpi.lastDayDate shouldBe dayDate

    // -- Tests on shop 1
    val shop1WeekShopTurnovers = completeWeekKpi.weekShopTurnover
      .find(shopTurnover => shopTurnover.shopUuid == shopUuidOne)
      .map(shopTurnover => shopTurnover.prodTurnovers)

    shop1WeekShopTurnovers shouldBe defined
    shop1WeekShopTurnovers.get should contain allOf(ProdTurnover(2, 273.0), ProdTurnover(1, 1024.8), ProdTurnover(3, 840.0))

    // -- Tests on shop 2
    val shop2WeekShopTurnovers = completeWeekKpi.weekShopTurnover
      .find(shopTurnover => shopTurnover.shopUuid == shopUuidTwo)
      .map(shopTurnover => shopTurnover.prodTurnovers)

    shop2WeekShopTurnovers shouldBe defined
    shop2WeekShopTurnovers.get should contain allOf(ProdTurnover(2,1388.8), ProdTurnover(3,172.2))

    // Tests on Global
    completeWeekKpi.weekGlobalTurnover.productTurnovers should contain allOf(ProdTurnover(2,1661.8), ProdTurnover(1,1024.8), ProdTurnover(3,1012.2))
  }
}
