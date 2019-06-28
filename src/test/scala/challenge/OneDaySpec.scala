package challenge

import java.time.LocalDate

import org.scalatest.{FlatSpec, Matchers}
import challenge.service.{ProdSale, _}
import challenge.service.TransactionOrder$

trait AdjastSpec {
  val dayDate: LocalDate = LocalDate.parse("20170514", TransactionOrder$.FILENAME_DATE_FORMAT)

  val (shopUuidOne, shopUuidTwo) = ("shopUuid1", "shopUuid2")
  val (productIdOne, productIdTwo, productIdThree) = (1, 2, 3)

  val transactionFileMetadata = TransactionFileMetaData(dayDate)
  val productFileMetaDataShopOne = ProductFileMetaData(shopUuidOne, dayDate)
  val productFileMetaDataShopTwo = ProductFileMetaData(shopUuidTwo, dayDate)

  val transactionStream: Stream[Transaction] = List(
    // Shop 1
    Transaction(1, shopUuidOne, productIdOne, 2),
    Transaction(5, shopUuidOne, productIdOne, 10),
    Transaction(10, shopUuidOne, productIdTwo, 15),
    Transaction(3, shopUuidOne, productIdThree, 1),
    Transaction(9, shopUuidOne, productIdThree, 2),

    // Shop 2
    Transaction(2, shopUuidTwo, productIdTwo, 5),
    Transaction(6, shopUuidTwo, productIdTwo, 8),
    Transaction(7, shopUuidTwo, productIdTwo, 3),
    Transaction(4, shopUuidTwo, productIdThree, 3),
    Transaction(8, shopUuidTwo, productIdThree, 3)
  ).toStream
  val transactions = Transactions(transactionStream, transactionFileMetadata)

  val productStreamShopOne: Stream[Product] = List(
    Product(productIdOne, 12.2),
    Product(productIdTwo, 2.6),
    Product(productIdThree, 40.0)
  ).toStream

  val productStreamShopTwo: Stream[Product] = List(
    Product(productIdOne, 1.3),
    Product(productIdTwo, 12.4),
    Product(productIdThree, 4.1)
  ).toStream

  val productsOne = Prod(productStreamShopOne, productFileMetaDataShopOne)
  val productsTwo = Prod(productStreamShopTwo, productFileMetaDataShopTwo)

}


class DayKpiAdjastSpec extends FlatSpec with Matchers with AdjastSpec {

  "The Day Kpi Calculator" should "output correct sales results" in {
    // EXECUTE
    val dayKpiResult: complete = OneDay.dayCalcul(transactions, List(productsOne, productsTwo).toStream)

    // ASSERT
    dayKpiResult.date shouldBe dayDate

    // -- Tests on shop 1
    val shop1ProductSales = dayKpiResult.dayShopSales
      .find(dayShopSale => dayShopSale.shopUuid == shopUuidOne)
      .map(dayShopSale => dayShopSale.productSales)

    shop1ProductSales shouldBe defined
    shop1ProductSales.get should have size 3
    shop1ProductSales.get should contain allOf (ProdSale(2, 15), ProdSale(1, 12), ProdSale(3, 3))

    // -- Tests on shop 2
    val shop2ProductSales = dayKpiResult.dayShopSales
      .find(dayShopSale => dayShopSale.shopUuid == shopUuidTwo)
      .map(dayShopSale => dayShopSale.productSales)

    shop2ProductSales shouldBe defined
    shop2ProductSales.get should have size 2
    shop2ProductSales.get should contain allOf (ProdSale(2, 16), ProdSale(3, 6))

    // -- Tests on global
    dayKpiResult.dayGlobalSales.productSales should contain allOf (ProdSale(2, 31), ProdSale(1, 12), ProdSale(3, 9))
  }

  "The Day Kpi Calculator" should "output correct turnover results" in {
    // EXECUTE
    val dayKpiResult: complete = OneDay.dayCalcul(transactions, List(productsOne, productsTwo).toStream)

    // ASSERT
    dayKpiResult.date shouldBe dayDate

    // -- Tests on shop 1
    val shop1ProductTuronovers = dayKpiResult.dayShopTurnovers
      .find(dayShopTurnover => dayShopTurnover.shopUuid == shopUuidOne)
      .map(dayShopTurnover => dayShopTurnover.prodTurnovers)

    shop1ProductTuronovers shouldBe defined
    shop1ProductTuronovers.get should have size 3
    shop1ProductTuronovers.get should contain allOf (ProdTurnover(1, 146.4), ProdTurnover(3, 120.0), ProdTurnover(2, 39))

    // -- Tests on shop 2
    val shop2ProductTurnovers = dayKpiResult.dayShopTurnovers
      .find(dayShopTurnover => dayShopTurnover.shopUuid == shopUuidTwo)
      .map(dayShopTurnover => dayShopTurnover.prodTurnovers)

    shop2ProductTurnovers shouldBe defined
    shop2ProductTurnovers.get should have size 2
    shop2ProductTurnovers.get should contain allOf (ProdTurnover(3, 24.6), ProdTurnover(2, 198.4))

    // -- Tests on global
    dayKpiResult.dayGlobalTurnover.productTurnovers should contain allOf (ProdTurnover(2, 237.4), ProdTurnover(1, 146.4), ProdTurnover(3, 144.6))
  }

  "The Day Kpi calculator price getting method" should "get the right prices when they are present" in {
    // PREPARE
    val dayProducts = List(productsOne, productsTwo).toStream

    // EXECUTE
    val productOneShopOnePrice: Double = OneDay.getProductPriceFromProducts(productIdOne, shopUuidOne, dayProducts)
    val productOneShopTwoPrice: Double = OneDay.getProductPriceFromProducts(productIdOne, shopUuidTwo, dayProducts)
    val productTwoShopOnePrice: Double = OneDay.getProductPriceFromProducts(productIdTwo, shopUuidOne, dayProducts)

    // ASSERT
    productOneShopOnePrice shouldBe 12.2
    productOneShopTwoPrice shouldBe 1.3
    productTwoShopOnePrice shouldBe 2.6
  }

  "The Day Kpi calculator price getting method" should "get 0.0 as price if the product is not found" in {
    // PREPARE
    val dayProducts = List(productsOne, productsTwo).toStream

    // EXECUTE
    val incorrectProcutShopOnePrice: Double = OneDay.getProductPriceFromProducts(11122121, shopUuidOne, dayProducts)


    // ASSERT
    incorrectProcutShopOnePrice shouldBe 0.0
  }
}
