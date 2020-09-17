package ziotokenapi

import ziotokenapi.configuration.Configuration
import ziotokenapi.salesforce.Salesforce
import zio._
import zio.console.Console
import ziotokenapi.salesforce.auth.Auth

object Main extends zio.App {

  def program(ids: Seq[String]): ZIO[Salesforce with Console, Failure, Unit] =
    for {
      contact1 <- Salesforce.fetchContact(ids.head)
      _        <- console.putStrLn(contact1.toString)
      contact2 <- Salesforce.fetchContact(ids.last)
      _        <- console.putStrLn(contact2.toString)
    } yield ()

  def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val value2: ZLayer[Configuration with Console with system.System, Failure, Auth with Configuration with Console] =
      Auth.live ++ Configuration.live ++ Console.live
    val value: ZLayer[zio.ZEnv, Failure, Salesforce]                                                                 =
      Configuration.live ++ Console.live ++ system.System.live >>> value2 >>> Salesforce.live
    program(args)
      .provideCustomLayer(value)
      .foldM(e => console.putStrLn(e.toString) *> ZIO.succeed(ExitCode.failure), _ => ZIO.succeed(ExitCode.success))
  }
}
