package ziotokenapi

import ziotokenapi.configuration.Configuration
import ziotokenapi.salesforce.Salesforce
import zio._
import zio.console.Console

object Main extends zio.App {

  def program(ids: Seq[String]): ZIO[Salesforce with Console, Failure, Unit] =
    for {
      contact1 <- Salesforce.fetchContact(ids.head)
      _        <- console.putStrLn(contact1.toString)
      contact2 <- Salesforce.fetchContact(ids.last)
      _        <- console.putStrLn(contact2.toString)
    } yield ()

  def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    program(args)
      .provideCustomLayer(Configuration.live ++ Console.live >>> Salesforce.live)
      .foldM(e => console.putStrLn(e.toString) *> ZIO.succeed(ExitCode.failure), _ => ZIO.succeed(ExitCode.success))
}
