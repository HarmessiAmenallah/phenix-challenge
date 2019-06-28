package challenge.service

import java.nio.file.Paths
import java.time.LocalDate

import org.scalatest.{FlatSpec, Matchers}

class FileSpec extends FlatSpec with Matchers {
  val dayDate: LocalDate = LocalDate.parse("20170514", TransactionOrder$.FILENAME_DATE_FORMAT)

  "The ProductSale Filename service" should "return correct day shop file name" in {
    // EXECUTE
    val fileName = ProductSaleFileNameService.generateDayShopFileName(dayDate, "shopuuid")

    // AFFIRME
    fileName shouldBe "top_100_ventes_shopid_20170514.data"
  }

  "The ProductSale Filename service" should "return correct day global file name" in {
    // EXECUTE
    val fileName = ProductSaleFileNameService.generateDayGlobalFileName(dayDate)

    // AFFIRME
    fileName shouldBe "top_100_ventes_GLOBAL_20170514.data"
  }

  "The ProductSale Filename service" should "return correct week shop file name" in {
    // EXECUTE
    val fileName = ProductSaleFileNameService.generateWeekShopFileName(dayDate, "shopuuid")

    // AFFIRME
    fileName shouldBe "top_100_ventes_shopuuid_20170514-J7.data"
  }

  "The ProductSale Filename service" should "return correct week global file name" in {
    // EXECUTE
    val fileName = ProductSaleFileNameService.generateWeekGlobalFileName(dayDate)

    // AFFIRME
    fileName shouldBe "top_100_ventes_GLOBAL_20170514-J7.data"
  }

  "The ProductTurnover Filename service" should "return correct day shop file name" in {
    // EXECUTE
    val fileName = ProductTurnoverFileNameService.generateDayShopFileName(dayDate, "shopid")

    // AFFIRME
    fileName shouldBe "top_100_ca_shopid_20170514.data"
  }

  "The ProductTurnover Filename service" should "return correct day global file name" in {
    // EXECUTE
    val fileName = ProductTurnoverFileNameService.generateDayGlobalFileName(dayDate)

    // AFFIRME
    fileName shouldBe "top_100_ca_GLOBAL_20170514.data"
  }

  "The ProductTurnover Filename service" should "return correct week shop file name" in {
    // EXECUTE
    val fileName = ProductTurnoverFileNameService.generateWeekShopFileName(dayDate, "shopuuid")

    // AFFIRME
    fileName shouldBe "top_100_ca_shopuuid_20170514-J7.data"
  }

  "The ProductTurnover Filename service" should "return correct week global file name" in {
    // EXECUTE
    val fileName = ProductTurnoverFileNameService.generateWeekGlobalFileName(dayDate)

    // AFFIRME
    fileName shouldBe "top_100_ca_GLOBAL_20170514-J7.data"
  }
}

class FileCoordinatorSpec extends FlatSpec with Matchers {
  "The File Orchestrator" should "determine InputFiles correctly in non-empty folder" in {
    // PREPARE
    val folderArguments = FolderArguments(Paths.get("data/input/simple-example"), Paths.get("./phenix-output"), simpleCalc = false)

    // EXECUTE
    val inputFiles = FileCoordinator.determineInputFiles(folderArguments)

    // AFFIRME
    inputFiles.inputProductFiles should have size 2
    inputFiles.inputTransactionsFiles should have size 1
  }

  "The File Orchestrator" should "convert input files to marhsalled valued correctly for non-empty folder" in {
    // PREPARE
    val folderArguments = FolderArguments(Paths.get("data/input/simple-example"), Paths.get("./phenix-output"), simpleCalc = false)

    // EXECUTE
    val inputFiles = FileCoordinator.determineInputFiles(folderArguments)
    val marshalledValues = FileCoordinator.convertInputFilesToOrderValues(inputFiles)

    // AFFIRME
    val transactions = marshalledValues._1
    val products = marshalledValues._2

    transactions should have size 1
    products should have size 2

    val firstTransaction = transactions.toList.head

    firstTransaction.metaData.date.toString shouldBe "2017-05-14"
    firstTransaction.transactions should have size 10

    products.map(product => product.metaData.date.toString) should contain ("2017-05-14")
    products.map(product => product.metaData.shopUuid) should contain allOf("shopuuid1", "shopuuid2")
  }
}