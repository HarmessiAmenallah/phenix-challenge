package challenge

import java.time.LocalDate

import challenge.service._

object OneDay {

  def dayCalcul(trans: Transactions, dayProdList: Stream[Prod]): complete = {

    val prodShop = computeProdShop(trans, dayProdList)

    val shopSales = prodShop.keys.map(shopId => {
      val prodSaleExtract = prodShop(shopId).map(prodTuple => prodTuple._1)
      ShopSale(shopId, prodSaleExtract)
    }).toStream

    val shopTurnovers = prodShop.keys.map(shopId => {
      val prodSaleExtract = prodShop(shopId).map(prodDataTuple => prodDataTuple._2)
      ShopTurnover(shopId, prodSaleExtract)
    }).toStream

    val globalSale = computeGlobalSales(trans.metaData.date, shopSales)
    val globalTurnover = computeGlobalTurnover(trans.metaData.date, shopTurnovers)

    complete(trans.metaData.date, shopSales, globalSale, shopTurnovers, globalTurnover)
  }

  def computeGlobalSales(date: LocalDate, shopSales: Stream[ShopSale]): GlobalSale = {

    val aggregatedProductSales = shopSales
      .flatMap(dayShopSale => dayShopSale.productSales)
      .groupBy(productSale => productSale.productId)
      .mapValues(productSale => {
        productSale.foldLeft(0)((acc, productSale2) => acc + productSale2.quantity)
      }).map(globalResultMap => {
      ProdSale(globalResultMap._1, globalResultMap._2)
    })

    GlobalSale(aggregatedProductSales.toStream)
  }

  def roundValue(numberToRound: Double): Double = Math.round(numberToRound * 100.0) / 100.0

  def computeGlobalTurnover(date: LocalDate, dayShopTurnovers: Stream[ShopTurnover]): GlobalTurnover = {

    val aggregateProdTurnovers = dayShopTurnovers
      .flatMap(dayShopTurnover => dayShopTurnover.prodTurnovers)
      .groupBy(prodTurnover => prodTurnover.productId)
      .mapValues(prodTurnover => {
        prodTurnover.foldLeft(0.0)((acc, prodTurnover2) => roundValue(acc + prodTurnover2.turnover))
      }).map(globalMap => {
      ProdTurnover(globalMap._1, globalMap._2)
    })

    GlobalTurnover(aggregateProdTurnovers.toStream)
  }

  def computeProdShop(transactions: Transactions, prodList: Stream[Prod]): Map[String, Stream[(ProdSale, ProdTurnover)]] = {
    transactions.transactions
      .groupBy(transaction => (transaction.shopId, transaction.prodId))
      .mapValues(transactions => {
        transactions.foldLeft(0)((acc, transaction2) => acc + transaction2.quantity)
      })
      .map(transactionMap => {
        val prodId = transactionMap._1._2
        val shopId = transactionMap._1._1
        val prodQuantity = transactionMap._2
        val prodTotalPrice = prodQuantity * getProductPriceFromProducts(prodId, shopId, prodList)
        (shopId, prodId, prodQuantity, prodTotalPrice)
      })
      .groupBy(calculationResult => calculationResult._1)
      .map(shopProductCalc => {
      val shopProductsCalcValues = shopProductCalc._2.toStream
      shopProductCalc._1 -> shopProductsCalcValues.map(productAggregateCalc => (ProdSale(productAggregateCalc._2, productAggregateCalc._3), ProdTurnover(productAggregateCalc._2, roundValue(productAggregateCalc._4))))
    })
  }

  def getProductPriceFromProducts(productId: Int, shopUuid: String, dayProductsList: Stream[Prod]): Double = {
    dayProductsList
      .find(dayProducts => dayProducts.metaData.shopUuid == shopUuid)
      .get.products.find(product => product.productId == productId)
    match {
      case Some(prod) => prod.price
      case None => 0.0
    }
  }
}
