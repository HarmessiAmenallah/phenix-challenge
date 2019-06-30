import java.io.{File, FileWriter, IOException}
import java.sql.Time
import java.text.{DateFormat, ParseException, SimpleDateFormat}
import java.util.{Calendar, Date, Random}

// cette classe permet de generer les donnes

class DataGenerator(private var nbStor: Int, private var nbProdStor: Int, private var nbTransDay: Long, private var date: String, sevenDays: java.lang.Boolean) {

  private var storeIds: Array[String] = new Array[String](this.nbStor)

  private var rd: Random = new Random()

  val millisInDay: Int = 24 * 60 * 60 * 1000

  genStoreId()

  if (sevenDays) {
    val dateFormat: DateFormat = new SimpleDateFormat("yyyyMMdd")
    val dates: Array[String] = Array.ofDim[String](7)
    val DAY_IN_MS: Long = 1000 * 60 * 60 * 24
    for (i <- 0.until(7)) {
      var day: Date = null
      try day = new Date(
        dateFormat.parse(date).getTime - ((6 - i) * DAY_IN_MS))
      catch {
        case e: ParseException => e.printStackTrace()

      }
      dates(i) = dateFormat.format(day)
    }
    for (day <- dates) {
      for (storeId <- storeIds)
        genStoreProdRefFile(storeId, day)
      genTransFile(day)
    }
  } else {
    for (storeId <- storeIds) genStoreProdRefFile(storeId, date)
    genTransFile(date)
  }

  def genStoreId(): Unit = {
    val arr: Array[Byte] = Array.ofDim[Byte](16)
    var storeId: String = ""
    for (i <- 0 until nbStor) {

      rd.nextBytes(arr)
      val sb: StringBuilder = new StringBuilder()
      for (b <- arr) {
        sb.append(String.format("%02x", b))
      }
      storeId = sb.toString
      storeId = storeId.substring(0, 8) + '-' + storeId.substring(8, 12) +
        '-' +
        storeId.substring(12, 16) +
        '-' +
        storeId.substring(16, 20) +
        '-' +
        storeId.substring(20)
      this.storeIds(i) = storeId
    }
  }

  def genStoreProdRefFile(storeId: String, day: String): Unit = {
    new File("data").mkdirs()
    val file: File = new File(
      "data/reference_prod-" + storeId + "_" + day + ".data")
    try file.createNewFile()
    catch {
      case e: IOException => e.printStackTrace()

    }
    var line: String = ""
    var fw: FileWriter = null
    try fw = new FileWriter(
      "data/reference_prod-" + storeId + "_" + day + ".data")
    catch {
      case e: IOException => e.printStackTrace()

    }
    for (j <- 0 until this.nbProdStor) {
      line = java.lang.Integer.toString(j + 1) + "|" + java.lang.Float
        .toString(rd.nextFloat() * 100) +
        "\n"
      try fw.write(line)
      catch {
        case e: IOException => e.printStackTrace()

      }
    }
    try fw.close()
    catch {
      case e: IOException => e.printStackTrace()

    }
  }

  def genTransFile(day: String): Unit = {
    val timeStamp: String = new SimpleDateFormat("yyyyMMdd_HHmmss.sssZ")
      .format(Calendar.getInstance.getTime)
    val zone: String = timeStamp.substring(timeStamp.indexOf('+'))
    val file: File = new File("data/transactions_" + day + ".data")
    try file.createNewFile()
    catch {
      case e: IOException => e.printStackTrace()

    }
    var line: String = ""
    var fw: FileWriter = null
    try fw = new FileWriter("data/transactions_" + day + ".data")
    catch {
      case e: IOException => e.printStackTrace()

    }
    for (j <- 0 until this.nbTransDay) {
      val repeatTransaction: Int = rd.nextInt(9) + 1
      val indexStore: Int = rd.nextInt(nbStor)
      val time: Time = new Time(rd.nextInt(millisInDay).toLong)
      var timeString: String = time.toString
      timeString = "T" + timeString.replaceAll(":", "") + zone
      for (r <- 0 until repeatTransaction) {
        val productId: Int = rd.nextInt(nbProdStor) + 1
        val qte: Int = rd.nextInt(9) + 1
        line = java.lang.Long.toString(j + 1) + "|" + day + timeString +
          "|" +
          storeIds(indexStore) +
          "|" +
          productId +
          "|" +
          qte +
          "\n"
        try fw.write(line)
        catch {
          case e: IOException => e.printStackTrace()

        }
      }
    }
    try fw.close()
    catch {
      case e: IOException => e.printStackTrace()

    }
  }

}
