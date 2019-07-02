import java.io.{File, FileWriter, IOException}
import java.sql.Time
import java.text.{DateFormat, ParseException, SimpleDateFormat}
import java.util.{Calendar, Date, Random}

// cette classe permet de generer les donnes

class DataGenerator(private var nbStor: Int, private var nbProdStor: Int, private var nbTransDay: Long, private var date: String, sevenDays: java.lang.Boolean) {


  val ms: Int = 86400000
  private var ids: Array[String] = new Array[String](this.nbStor)



  def genTransFile(day: String): Unit = {
    val stamp: String = new SimpleDateFormat("yyyyMMdd_HHmmss.sssZ")
      .format(Calendar.getInstance.getTime)
    val zone: String = stamp.substring(stamp.indexOf('+'))
    val file: File = new File("data/transactions_" + day + ".data")
    try file.createNewFile()
    catch {
      case e: IOException => e.printStackTrace()
ids
    }

    var line: String = ""
    var fw: FileWriter = null
    try fw = new FileWriter("data/transactions_" + day + ".data")
    catch {
      case e: IOException => e.printStackTrace()

    }
    for (j <- 0 until this.nbTransDay) {
      val repeatTransaction: Int = new Random().nextInt(9) + 1
      val indexStore: Int = new Random().nextInt(nbStor)
      val time: Time = new Time(new Random().nextInt(ms).toLong)
      var timeString: String = time.toString
      timeString = "T" + timeString.replaceAll(":", "") + zone
      for (r <- 0 until repeatTransaction) {
        val productId: Int = new Random().nextInt(nbProdStor) + 1
        val qte: Int = new Random().nextInt(9) + 1
        line = java.lang.Long.toString(j + 1) + "|" + day + timeString +
          "|" + ids(indexStore) + "|" + productId + "|" + qte + "\n"
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
  genStoreId()

  if (sevenDays) {
    val dateFormat: DateFormat = new SimpleDateFormat("yyyyMMdd")
    val dates: Array[String] = Array.ofDim[String](7)

    for (i <- 0.until(7)) {
      var day: Date = null
      try day = new Date(dateFormat.parse(date).getTime - ((6 - i) * ms))
      catch {
        case e: ParseException => e.printStackTrace()

      }
      dates(i) = dateFormat.format(day)
    }
    for (day <- dates) {
      for (storeId <- ids)
        genStoreProdRefFile(storeId, day)
      genTransFile(day)
    }
  } else {
    for (storeId <- ids) genStoreProdRefFile(storeId, date)
    genTransFile(date)
  }

  def genStoreId(): Unit = {
    val arr: Array[Byte] = Array.ofDim[Byte](16)
    var storeId: String = ""
    for (i <- 0 until nbStor) {

      new Random().nextBytes(arr)
      val sb: StringBuilder = new StringBuilder()
      for (b <- arr) {
        sb.append(String.format("%02x", b))
      }
      storeId = sb.toString
      storeId = storeId.substring(0, 8) + '-' + storeId.substring(8, 12) + '-' + storeId.substring(12, 16) + '-' + storeId.substring(16, 20) + '-' + storeId.substring(20)
      this.ids(i) = storeId
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
        .toString(new Random().nextFloat() * 100) +
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



}
