package challenge

import challenge.service.{ArgumentsConfig, FolderArguments}
import challenge.service.Coordinator

object Main extends App {
  val argumentsConf = new ArgumentsConfig(args)
  val folderArguments = new FolderArguments(argumentsConf)
  Coordinator.launchProcess(folderArguments)
}

