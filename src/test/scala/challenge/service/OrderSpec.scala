package challenge.service

import org.scalatest.{FlatSpec, Matchers}


class OrderSpec extends FlatSpec with Matchers {

  "The Transaction File Marshaller" should "return a stream with the right transactions" in {
    // PREPARE/EXECUTE
    val transactions: Transactions = TransactionOrder$.orderLines(Stream(
      "1|20170514T223544+0100|2a4b6b81-5aa2-4ad8-8ba9-ae1a006e7d71|531|5",
      "3818|20170514T223544+0100|72a2876c-bc8b-4f35-8882-8d661fac2606|989|4",
      "9999|20170514T223544+0100|10f2f3e6-f728-41f3-b079-43b0aa758292|703|1"
    ), "transactions_20170514.data")

    // ASSERT
    transactions.transactions should have size 3
    transactions.transactions should contain allOf(
      Transaction(1, "2a4b6b81-5aa2-4ad8-8ba9-ae1a006e7d71", 531, 5),
      Transaction(3818, "72a2876c-bc8b-4f35-8882-8d661fac2606", 989, 4),
      Transaction(9999, "10f2f3e6-f728-41f3-b079-43b0aa758292", 703, 1)
    )
  }

  "The Transaction FileName Marshaller" should "return a marshalled value when filename is correct" in {
    // PREPARE/EXECUTE
    val txMetadata: TransactionFileMetaData = TransactionOrder$.orderFileName("transactions_20170514.data")

    // ASSERT
    txMetadata.date.toString shouldBe "2017-05-14"
  }

  "The Product File Marshaller" should "return a stream with the right products" in {
    // PREPARE/EXECUTE
    val products: Prod = ProductOrder$.orderLines(Stream(
      "1|4.7",
      "500|34.86",
      "999|0.68"
    ), "reference_prod-2a4b6b81-5aa2-4ad8-8ba9-ae1a006e7d71_20170514.data")

    // ASSERT
    products.products should have size 3
    products.products should contain allOf(
      Product(1, 4.7),
      Product(500, 34.86),
      Product(999, 0.68)
    )
  }

  "The Product FileName Marshaller" should "return a marshalled value when filename is correct" in {
    // PREPARE/EXECUTE
    val pxMetadata: ProductFileMetaData = ProductOrder$.orderFileName("reference_prod-shopuuid2_20170514.data")

    // ASSERT
    pxMetadata.date.toString shouldBe "2017-05-14"
    pxMetadata.shopUuid shouldBe "shopuuid2"
  }
  "The ProductSale Unmarshaller" should "return the right stream of string for 3 product sales" in { // TODO
    // PREPARE
    val productSales: Stream[ProdSale] = Stream(
      ProdSale(1, 2),
      ProdSale(34, 18),
      ProdSale(99, 5)
    )

    // EXECUTE
    val unmarhsallResult: Stream[String] = ProductSaleUnorder.unorderRecords(productSales)


    // ASSERT
    unmarhsallResult should have size 3
    unmarhsallResult.toList shouldBe List("1|2", "34|18", "99|5")
  }

  "The ProductTurnover Unmarshaller" should "return the right stream of string for 3 product turnovers" in {
    // PREPARE
    val productTurnovers: Stream[ProdTurnover] = Stream(
      ProdTurnover(1, 2.0),
      ProdTurnover(34, 18.46),
      ProdTurnover(99, 5.12)
    )

    // EXECUTE
    val unmarhsallResult: Stream[String] = ProductTurnoverUnorder.unorderRecords(productTurnovers)


    // ASSERT
    unmarhsallResult should have size 3
    unmarhsallResult.toList shouldBe List("1|2.0", "34|18.46", "99|5.12")
  }
}