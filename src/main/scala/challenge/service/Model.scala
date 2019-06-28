package challenge.service

import java.io.File
import java.nio.file.{Path, Paths}
import java.time.LocalDate

import org.rogach.scallop.{ScallopConf, ScallopOption}


case class InputFiles(inputTransactionsFiles: Stream[File], inputProductFiles: Stream[File])

case class Transaction(transactionId: Int, shopId: String, prodId: Int, quantity: Int)
case class Product(productId: Int, price: Double)

case class TransactionFileMetaData(date: LocalDate)
case class ProductFileMetaData(shopUuid: String, date: LocalDate)

case class Transactions(transactions: Stream[Transaction], metaData: TransactionFileMetaData) extends Ordered[Transactions] {
  override def compare(ts2: Transactions): Int = this.metaData.date compareTo ts2.metaData.date
}



case class Prod(products: Stream[Product], metaData: ProductFileMetaData)


abstract class FileOutputName {
  val outputName: String
}

case class FileOutput(outputName: String, fileContentOutput: Stream[String]) extends FileOutputName

case class Output(outputPath: Path, fileOutputs: Stream[FileOutput])


case class ProdSale(productId: Int, quantity: Int) extends Ordered[ProdSale] {
  override def compare(ps2: ProdSale): Int = this.quantity compare ps2.quantity
}
case class ProdTurnover(productId: Int, turnover: Double) extends Ordered[ProdTurnover] {
  override def compare(pt2: ProdTurnover): Int = this.turnover compare pt2.turnover
}

abstract class Shop {
  val shopUuid: String
}

case class ShopSale(shopUuid: String, productSales: Stream[ProdSale]) extends Shop {

  def sort(): ShopSale = {
    this.copy(productSales = this.productSales.sorted.reverse)
  }

  def truncateTop100(): ShopSale = {
    this.copy(productSales = productSales.take(100))
  }
}

case class GlobalSale(productSales: Stream[ProdSale]) {
  def sort(): GlobalSale = {
    this.copy(productSales = this.productSales.sorted.reverse)
  }

  def truncateTop100(): GlobalSale = {
    this.copy(productSales = productSales.take(100))
  }
}

case class ShopTurnover(shopUuid: String, prodTurnovers: Stream[ProdTurnover]) extends Shop {
  def sort(): ShopTurnover = {
    this.copy(prodTurnovers = this.prodTurnovers.sorted.reverse)
  }

  def truncateTop100(): ShopTurnover = {
    this.copy(prodTurnovers = prodTurnovers.take(100))
  }
}

case class GlobalTurnover(productTurnovers: Stream[ProdTurnover]) {
  def sort(): GlobalTurnover = {
    this.copy(productTurnovers = this.productTurnovers.sorted.reverse)
  }

  def truncateTop100(): GlobalTurnover = {
    this.copy(productTurnovers = productTurnovers.take(100))
  }
}

case class complete(date: LocalDate, dayShopSales: Stream[ShopSale], dayGlobalSales: GlobalSale,
                    dayShopTurnovers: Stream[ShopTurnover], dayGlobalTurnover: GlobalTurnover) {
  def sortResults(): complete = {
    this.copy(
      dayShopSales = this.dayShopSales.map(_.sort()),
      dayGlobalSales = this.dayGlobalSales.sort(),
      dayShopTurnovers = this.dayShopTurnovers.map(_.sort()),
      dayGlobalTurnover = this.dayGlobalTurnover.sort()
    )
  }

  def truncateTop100(): complete = {
    this.copy(
      dayShopSales = this.dayShopSales.map(_.truncateTop100()),
      dayGlobalSales = this.dayGlobalSales.truncateTop100(),
      dayShopTurnovers = this.dayShopTurnovers.map(_.truncateTop100()),
      dayGlobalTurnover = this.dayGlobalTurnover.truncateTop100()
    )
  }
}

case class CompleteWeek(lastDayDate: LocalDate, weekShopSales: Stream[ShopSale], weekGlobalSales: GlobalSale,
                        weekShopTurnover: Stream[ShopTurnover], weekGlobalTurnover: GlobalTurnover) {
  def sortResults(): CompleteWeek = {
    this.copy(
      weekShopSales = this.weekShopSales.map(_.sort()),
      weekGlobalSales = this.weekGlobalSales.sort(),
      weekShopTurnover = this.weekShopTurnover.map(_.sort()),
      weekGlobalTurnover = this.weekGlobalTurnover.sort()
    )
  }

  def truncateTop100(): CompleteWeek = {
    this.copy(
      weekShopSales = this.weekShopSales.map(_.truncateTop100()),
      weekGlobalSales = this.weekGlobalSales.truncateTop100(),
      weekShopTurnover = this.weekShopTurnover.map(_.truncateTop100()),
      weekGlobalTurnover = this.weekGlobalTurnover.truncateTop100()
    )
  }
}


class ArgumentsConfig(arguments: Seq[String]) extends ScallopConf(arguments) {
  val inputFolder: ScallopOption[String] = opt[String](
    descr = "The folder which contains the data files. FAILS if it does not exist",
    required = true,
    validate = { checkFolderExistence }
  )

  val outputFolder: ScallopOption[String] = opt[String](default = Some("phenix-output"), descr = "The folder where you want the results files to go. If not mentioned, will put the results in ./phenix-output")

  val simpleCalc: ScallopOption[Boolean] = opt[Boolean](default = Some(false), descr = "Week calculations won't be triggered if this flag is set")

  def checkFolderExistence(path: String): Boolean = {
    Paths.get(path).toAbsolutePath
      .toFile
      .isDirectory
  }

  verify()
}

case class FolderArguments(inputFolder: Path, outputFolder: Path, simpleCalc: Boolean) {

  def this(argumentsConfig: ArgumentsConfig) = this (
    Paths.get(argumentsConfig.inputFolder()),
    Paths.get(argumentsConfig.outputFolder()),
    argumentsConfig.simpleCalc()
  )
}