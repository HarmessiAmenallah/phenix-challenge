package challenge.service

import java.time.LocalDate
import java.time.format.DateTimeFormatter

//Transforme  ligne par ligne d'un fichier dans le modèle d'entrée souhaité

trait Order[T, U] extends FileService {
  val HORIZONTAL_SEPARATOR: String = """\|"""

  def orderFileContent(fileContent: Stream[String], deserializeFunction: String => U): Stream[U] = {
    fileContent.map(line => deserializeFunction(line))
  }

  def orderLines(fileContent: Stream[String], fileName: String): T

  def orderLineString(line: String): U
}

trait FileNameOrder[T] {
  val FILENAME_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
  val FILE_METADATA_SEPARATOR: String = "_"

  //Extrait les données d'un fichier

  def orderFileName(fileName: String): T
}

object TransactionOrder$ extends Order[Transactions, Transaction] with FileNameOrder[TransactionFileMetaData] {

  override def orderLines(fileContent: Stream[String], fileName: String): Transactions = {
    Transactions(orderFileContent(fileContent, orderLineString), orderFileName(fileName))
  }

  override def orderLineString(line: String): Transaction = {
    line.split(HORIZONTAL_SEPARATOR) match {
      case Array(transactionId, _, shopUuid, productId, quantity) =>
        Transaction(transactionId.toInt, shopUuid, productId.toInt, quantity.toInt)
    }
  }

  override def orderFileName(fileName: String): TransactionFileMetaData = {
    fileName.replaceFirst(DATA_FILE_EXTENSION, "")
      .split(FILE_METADATA_SEPARATOR) match {
      case Array(_, dateStr) => TransactionFileMetaData(LocalDate.parse(dateStr, FILENAME_DATE_FORMAT))
    }
  }
}

object ProductOrder$ extends Order[Prod, Product] with FileNameOrder[ProductFileMetaData] {
  val PRODUCT_FILE_PREFIX = "reference_prod-"

  override def orderLines(fileContent: Stream[String], fileName: String): Prod = {
    Prod(orderFileContent(fileContent, orderLineString), orderFileName(fileName))
  }

  override def orderLineString(line: String): Product = {
    line.split(HORIZONTAL_SEPARATOR) match {
      case Array(productId, price) => Product(productId.toInt, price.toDouble)
    }
  }

  override def orderFileName(fileName: String): ProductFileMetaData = {
    fileName
      .replaceFirst(PRODUCT_FILE_PREFIX, "")
      .replaceFirst(DATA_FILE_EXTENSION, "")
      .split(FILE_METADATA_SEPARATOR) match {
      case Array(shopUuid, dateStr) => ProductFileMetaData(shopUuid, LocalDate.parse(dateStr, FILENAME_DATE_FORMAT))
    }
  }
}

trait Unorder[T] extends FileProducer {
  val OUTPUT_HORIZONTAL_SEPARATOR: String = "|"

  def unorderRecords(records: Stream[T]): Stream[String] = {
    records.map(record => unorderRecord(record))
  }

  def unorderRecord(record: T): String
}

object ProductSaleUnorder extends Unorder[ProdSale] {
  override def unorderRecord(productSale: ProdSale): String =
    s"${productSale.productId}$OUTPUT_HORIZONTAL_SEPARATOR${productSale.quantity}"
}

object ProductTurnoverUnorder extends Unorder[ProdTurnover] {
  override def unorderRecord(productTurnover: ProdTurnover): String =
    s"${productTurnover.productId}$OUTPUT_HORIZONTAL_SEPARATOR${productTurnover.turnover}"
}
