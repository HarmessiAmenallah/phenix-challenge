package challenge.service

import java.nio.file.Path
import java.time.LocalDate

import better.files._
import scalaz.std.stream.streamSyntax._

import scala.util.matching.Regex

trait FileService {
  val DATA_FILE_EXTENSION = """.data"""
}

trait FileNameChecker {
  val TRANSACTION_FILENAME: Regex = """^transactions\_[0-9]{8}\.data$""".r
  val PRODUCT_FILENAME: Regex = """^reference\_prod\-(.*)\_[0-9]{8}\.data$""".r

  def fileIsTransactionRecord(fileName: String): Boolean = {
    TRANSACTION_FILENAME.findFirstIn(fileName).isDefined
  }

  def fileIsProductRecord(fileName: String): Boolean = {
    PRODUCT_FILENAME.findFirstIn(fileName).isDefined
  }
}

//écrire dans des fichiers

trait FileProducer extends FileService {

  def writeRecordFile(outputFilePath: Path, content: Stream[String]): Unit = {
    val fileToOutput = file"${outputFilePath.toAbsolutePath.toString}"

    fileToOutput
      .createIfNotExists(asDirectory = false, createParents = true)
      .overwrite("")

    content.foreach(line => {
      fileToOutput.appendLine(line)
    })
  }
}

//consommer le contenu des fichiers

trait FileIngester extends FileService {

  def ingestRecordFile(filePath: Path): Stream[String] = {

    val fileToIngest = File(filePath.toString)

    fileToIngest.lineIterator.toStream
  }
}

trait FileNameService[T] {
  val TOP100_PREFIX = "top_100"
  val TOP100_GLOBAL = "GLOBAL"
  val TOP100_PREFIX_CATEGORY = s"${TOP100_PREFIX}_default"
  val EXTENSION = ".data"
  val J7_SUFFIX = "-J7"

  def generateDayShopFileName(date: LocalDate, shopUuid: String) : String = {
    s"${TOP100_PREFIX_CATEGORY}_${shopUuid}_${date.format(TransactionOrder$.FILENAME_DATE_FORMAT)}$EXTENSION"
  }

  def generateWeekShopFileName(date: LocalDate, shopUuid: String) : String = {
    s"${TOP100_PREFIX_CATEGORY}_${shopUuid}_${date.format(TransactionOrder$.FILENAME_DATE_FORMAT)}$J7_SUFFIX$EXTENSION"
  }

  def generateDayGlobalFileName(date: LocalDate) : String = {
    s"${TOP100_PREFIX_CATEGORY}_${TOP100_GLOBAL}_${date.format(TransactionOrder$.FILENAME_DATE_FORMAT)}$EXTENSION"
  }

  def generateWeekGlobalFileName(date: LocalDate) : String = {
    s"${TOP100_PREFIX_CATEGORY}_${TOP100_GLOBAL}_${date.format(TransactionOrder$.FILENAME_DATE_FORMAT)}$J7_SUFFIX$EXTENSION"
  }
}

object ProductSaleFileNameService extends FileNameService[ProdSale] {
  override val TOP100_PREFIX_CATEGORY = s"${TOP100_PREFIX}_ventes"
}

object ProductTurnoverFileNameService extends FileNameService[ProdTurnover] {
  override val TOP100_PREFIX_CATEGORY = s"${TOP100_PREFIX}_ca"

}

//génération de fichiers de retours avec leur nom et leur contenu

trait FileOutputCoordinator[T, U] {
  def generateShopOutput(date: LocalDate, shopdRecords: Stream[T], fileNameFunc: (LocalDate, String) => String): Stream[FileOutput]

  def generatGlobalOutput(date: LocalDate, globalRecord: U, fileNameFunc: LocalDate => String): Stream[FileOutput]
}

object FileOutputProductSaleCoordinator extends FileOutputCoordinator[ShopSale, GlobalSale] {
  override def generateShopOutput(date: LocalDate, shopSales: Stream[ShopSale], fileNameFunc: (LocalDate, String) => String): Stream[FileOutput] = {
    shopSales.map(shopSale => {
      FileOutput(fileNameFunc(date, shopSale.shopUuid), ProductSaleUnorder.unorderRecords(shopSale.productSales))
    })
  }

  override def generatGlobalOutput(date: LocalDate, globalSale: GlobalSale, fileNameFunc: LocalDate => String): Stream[FileOutput] = {
    Stream(FileOutput(fileNameFunc(date), ProductSaleUnorder.unorderRecords(globalSale.productSales)))
  }
}

object FileOutputProductTurnoverCoordinator$ extends FileOutputCoordinator[ShopTurnover, GlobalTurnover] {
  override def generateShopOutput(date: LocalDate, shopTurnovers: Stream[ShopTurnover], fileNameFunc: (LocalDate, String) => String): Stream[FileOutput] = {
    shopTurnovers.map(shopSale => {
      FileOutput(fileNameFunc(date, shopSale.shopUuid), ProductTurnoverUnorder.unorderRecords(shopSale.prodTurnovers))
    })
  }

  override def generatGlobalOutput(date: LocalDate, globalTurnover: GlobalTurnover, fileNameFunc: LocalDate => String): Stream[FileOutput] = {
    Stream(FileOutput(fileNameFunc(date), ProductTurnoverUnorder.unorderRecords(globalTurnover.productTurnovers)))
  }
}


object FileCoordinator extends FileIngester with FileProducer with FileNameChecker {
  val TOP_NUMBER_OF_VALUES = 100

  def determineInputFiles(arguments: FolderArguments): InputFiles  = {
    val inputFilesList = arguments
      .inputFolder.toFile.listFiles().toStream

    val inputTransactionsList = inputFilesList
      .filter(inputFile => fileIsTransactionRecord(inputFile.getName))

    val inputProductsList = inputFilesList
      .filter(inputFile => fileIsProductRecord(inputFile.getName))

    InputFiles(inputTransactionsList, inputProductsList)
  }

  def convertInputFilesToOrderValues(inputFiles: InputFiles): (Stream[Transactions], Stream[Prod]) = {
    val transactions = inputFiles.inputTransactionsFiles.map(inputTransactionFile => {
      TransactionOrder$.orderLines(ingestRecordFile(inputTransactionFile.toPath.toAbsolutePath), inputTransactionFile.getName)
    })

    val products = inputFiles.inputProductFiles.map(inputProductFile => {
      ProductOrder$.orderLines(ingestRecordFile(inputProductFile.toPath.toAbsolutePath), inputProductFile.getName)
    })

    (transactions, products)
  }


  def outputCompleteDay(outputFolder: Path, completeDay: complete): Unit = {
    val dayShopSalesFileOutput = FileOutputProductSaleCoordinator
      .generateShopOutput(completeDay.date, completeDay.dayShopSales, ProductSaleFileNameService.generateDayShopFileName)
    val dayGlobalSaleOutput = FileOutputProductSaleCoordinator.generatGlobalOutput(completeDay.date, completeDay.dayGlobalSales, ProductSaleFileNameService.generateDayGlobalFileName)

    val dayShopTurnoversFileOutput =
      FileOutputProductTurnoverCoordinator$.generateShopOutput(completeDay.date, completeDay.dayShopTurnovers, ProductTurnoverFileNameService.generateDayShopFileName)
    val dayGlobalTurnoverOutput = FileOutputProductTurnoverCoordinator$.generatGlobalOutput(completeDay.date, completeDay.dayGlobalTurnover, ProductTurnoverFileNameService.generateDayGlobalFileName)

    mergeAndOutputAllStreams(outputFolder, dayShopSalesFileOutput, dayGlobalSaleOutput, dayShopTurnoversFileOutput, dayGlobalTurnoverOutput)
  }

  def outputWeek(outputFolder: Path, completeWeek: CompleteWeek): Unit = {
    val weekShopSalesFileOutput = FileOutputProductSaleCoordinator
      .generateShopOutput(completeWeek.lastDayDate, completeWeek.weekShopSales, ProductTurnoverFileNameService.generateWeekShopFileName)
    val weekGlobalSaleOutput = FileOutputProductSaleCoordinator.generatGlobalOutput(completeWeek.lastDayDate, completeWeek.weekGlobalSales, ProductTurnoverFileNameService.generateWeekGlobalFileName)

    val weekShopTurnoverOutput =
      FileOutputProductTurnoverCoordinator$.generateShopOutput(completeWeek.lastDayDate, completeWeek.weekShopTurnover, ProductTurnoverFileNameService.generateWeekShopFileName)
    val weekGlobalTurnoverOutput = FileOutputProductTurnoverCoordinator$.generatGlobalOutput(completeWeek.lastDayDate, completeWeek.weekGlobalTurnover, ProductTurnoverFileNameService.generateWeekGlobalFileName)

    mergeAndOutputAllStreams(outputFolder, weekShopSalesFileOutput, weekGlobalSaleOutput, weekShopTurnoverOutput, weekGlobalTurnoverOutput)
  }

  def mergeAndOutputAllStreams(outputFolder: Path, fileOutputStreams: Stream[FileOutput]*): Unit = {
    val emptyFileOutputStream : Stream[FileOutput] = Stream()
    val allFileOutputs = fileOutputStreams.foldLeft(emptyFileOutputStream)((acc, stream2) => acc interleave stream2)

    outputs(Output(outputFolder, allFileOutputs))
  }


  def outputs(Output: Output): Unit = {
    Output.fileOutputs.foreach(fileOutput => {
      writeRecordFile(
        Output.outputPath.resolve(fileOutput.outputName),
        fileOutput.fileContentOutput)
    })
  }
}