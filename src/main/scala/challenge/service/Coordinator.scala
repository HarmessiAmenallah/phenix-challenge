package challenge.service

import java.time.LocalDate

import challenge.{OneDay, Week}

object Coordinator {
  implicit val localDateOrdering: Ordering[LocalDate] = Ordering.by(_.toEpochDay)

  def launchProcess(arguments: FolderArguments): Unit = {

    val inputFiles = FileCoordinator.determineInputFiles(arguments)

    if (!inputFiles.inputTransactionsFiles.isEmpty && !inputFiles.inputProductFiles.isEmpty) {

      val allCompleteDayKpi = doCalculationsByDay(inputFiles)

      allCompleteDayKpi.foreach(sortedCompleteDayKpi => {
          FileCoordinator.outputCompleteDay(arguments.outputFolder, sortedCompleteDayKpi.truncateTop100())
      })

        if (!arguments.simpleCalc) {
          val completeWeekKpi = doWeekCalculations(allCompleteDayKpi)
          FileCoordinator.outputWeek(arguments.outputFolder, completeWeekKpi.truncateTop100())
        }

    }
  }

  def doCalculationsByDay(inputFiles: InputFiles): Stream[complete] = {
    val orderGroupedRecords = groupOrderInputValuesByDate(FileCoordinator.convertInputFilesToOrderValues(inputFiles))

    orderGroupedRecords.keys.map(transactionsKey => {
      OneDay.dayCalcul(transactionsKey, orderGroupedRecords(transactionsKey)).sortResults()
    }).toStream
  }

  def doWeekCalculations(allCompleteDay: Stream[complete]): CompleteWeek = {
    val lastDayDate = allCompleteDay.maxBy(completeDay => completeDay.date).date

    val sevenDaysBeforeLastDayDate = lastDayDate.minusDays(7)
    val completeDayWithin7Days = allCompleteDay
      .filter(completeDay => completeDay.date.isAfter(sevenDaysBeforeLastDayDate) ||
        completeDay.date.equals(sevenDaysBeforeLastDayDate))

    Week.computeWeek(lastDayDate, completeDayWithin7Days.take(7))
  }

  def groupOrderInputValuesByDate(orderInputRecords: (Stream[Transactions], Stream[Prod])): Map[Transactions, Stream[Prod]] = {
    val transactionsRecords = orderInputRecords._1
    val productsRecords = orderInputRecords._2

    transactionsRecords
      .map(transactionsRecord => {
        (transactionsRecord, productsRecords.filter(products => products.metaData.date.equals(transactionsRecord.metaData.date)))
      }).toMap
  }
}
