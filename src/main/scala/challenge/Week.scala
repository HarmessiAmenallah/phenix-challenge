package challenge

import java.time.LocalDate

import challenge.service._

object Week {

  def computeWeek(lastDayDate: LocalDate, completeDayKpis: Stream[complete]): CompleteWeek = {
    val allShopSales = completeDayKpis.flatMap(completeDayKpi =>  completeDayKpi.dayShopSales)
    val weekShopSales = computeWeekShopSales(allShopSales)

    val allGlobalSales = completeDayKpis.map(completeDayKpi => completeDayKpi.dayGlobalSales)
    val weekGlobalSales = computeWeekGlobalSales(allGlobalSales)

    val allShopTurnovers = completeDayKpis.flatMap(completeDayKpi =>  completeDayKpi.dayShopTurnovers)
    val weekShopTurnovers = computeWeekShopTurnovers(allShopTurnovers)

    val allGlobalTurnovers = completeDayKpis.map(completeDayKpi => completeDayKpi.dayGlobalTurnover)
    val weekGlobalTurnovers = computeWeekGlobalTurnovers(allGlobalTurnovers)

    CompleteWeek(lastDayDate, weekShopSales, weekGlobalSales, weekShopTurnovers, weekGlobalTurnovers)
  }

  def computeWeekShopSales(allShopSales: Stream[ShopSale]): Stream[ShopSale] = {
    allShopSales
      .groupBy(shopSale => shopSale.shopUuid)
      .map(shopSaleByUuidMap => (shopSaleByUuidMap._1,
        shopSaleByUuidMap._2
          .flatMap(shopSale => shopSale.productSales)
          .groupBy(productSale => productSale.productId)
          .mapValues(groupedByIdProductSale => {
            groupedByIdProductSale.foldLeft(0)((acc, productSale2) => acc + productSale2.quantity)
          })
          .map(weekProductSaleTuple => {
            ProdSale(weekProductSaleTuple._1, weekProductSaleTuple._2)
          }).toStream
      ))
      .map(weekResultMap => {
        ShopSale(weekResultMap._1, weekResultMap._2)
      }).toStream
  }

  def computeWeekGlobalSales(allGlobalSales: Stream[GlobalSale]): GlobalSale = {
    GlobalSale(allGlobalSales
      .flatMap(globalSale => globalSale.productSales)
      .groupBy(productSale => productSale.productId)
      .mapValues(groupedByIdProductSale => {
        groupedByIdProductSale.foldLeft(0)((acc, productSale2) => acc + productSale2.quantity)
      })
      .map(weekProductSaleTuple => {
        ProdSale(weekProductSaleTuple._1, weekProductSaleTuple._2)
      }).toStream)
  }

  def roundValue(numberToRound: Double): Double = Math.round(numberToRound * 100.0) / 100.0


  def computeWeekShopTurnovers(allShopTurnovers: Stream[ShopTurnover]): Stream[ShopTurnover] = {
    allShopTurnovers
      .groupBy(shopSale => shopSale.shopUuid)
      .map(shopTurnoverByUuidMap => (shopTurnoverByUuidMap._1,
        shopTurnoverByUuidMap._2
          .flatMap(shopTurnover => shopTurnover.prodTurnovers)
          .groupBy(productTurnover => productTurnover.productId)
          .mapValues(groupedByIdProductTurnover => {
            groupedByIdProductTurnover.foldLeft(0.0)((acc, productTurnover2) => roundValue(acc + productTurnover2.turnover))
          })
          .map(weekProductTurnoverTuple => {
            ProdTurnover(weekProductTurnoverTuple._1, weekProductTurnoverTuple._2)
          }).toStream
      ))
      .map(weekResultMap => {
        ShopTurnover(weekResultMap._1, weekResultMap._2)
      }).toStream
  }

  def computeWeekGlobalTurnovers(allGlobalTurnovers: Stream[GlobalTurnover]): GlobalTurnover = {
    GlobalTurnover(allGlobalTurnovers
      .flatMap(globalTurnover => globalTurnover.productTurnovers)
      .groupBy(productTurnover => productTurnover.productId)
      .mapValues(groupedByIdProductTurnover => {
        groupedByIdProductTurnover.foldLeft(0.0)((acc, productTurnover2) => roundValue(acc + productTurnover2.turnover))
      })
      .map(weekProductSaleTuple => {
        ProdTurnover(weekProductSaleTuple._1, weekProductSaleTuple._2)
      }).toStream)
  }


}
