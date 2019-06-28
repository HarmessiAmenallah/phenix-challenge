package challenge.service

import org.scalatest.{FlatSpec, Matchers}

class CoordinatorSpec extends FlatSpec with Matchers {
  "The File Coordinator FileName checker" should "return false results with invalid transactions and products file names" in {
    // PREPARE / EXECUTE
    val isFileNameTransactionEmptyName = FileCoordinator.fileIsTransactionRecord("")
    val isFileNameProductEmptyName = FileCoordinator.fileIsProductRecord("")

    val isFileNameTransactionIncorrectNameDateTooLong = FileCoordinator.fileIsTransactionRecord("transactions_2223232232323.data")
    val isFileNameProductIncorrectNameDateToolong = FileCoordinator.fileIsProductRecord("reference_prod-dd43720c-be43-41b6-bc4a-ac4beabd0d9b_201705142233232.data")

    val isFileNameTransactionIncorrectNameNoPrefix = FileCoordinator.fileIsTransactionRecord("20170514.data")
    val isFileNameProductIncorrectNameNoPrefix = FileCoordinator.fileIsProductRecord("20170514.data")

    isFileNameTransactionEmptyName shouldBe false
    isFileNameProductEmptyName shouldBe false
    isFileNameTransactionIncorrectNameDateTooLong shouldBe false
    isFileNameProductIncorrectNameDateToolong shouldBe false
    isFileNameTransactionIncorrectNameNoPrefix shouldBe false
    isFileNameProductIncorrectNameNoPrefix shouldBe false
  }

  "The File Coordinator FileName checker" should "return true results with valid transactions and products file names" in {
    // PREPARE / EXÉCUTE
    val isFileNameTransactionNormalName = FileCoordinator.fileIsTransactionRecord("transactions_20170514.data")
    val isFileNameProductNormalName = FileCoordinator.fileIsProductRecord("reference_prod-dd43720c-be43-41b6-bc4a-ac4beabd0d9b_20170514.data")

    // AFFIRME
    isFileNameTransactionNormalName shouldBe true
    isFileNameProductNormalName shouldBe true
  }
}
