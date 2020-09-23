package ziotokenapi

import zio._
import zio.console.Console
import ziotokenapi.configuration.Configuration
import ziotokenapi.salesforce.Salesforce
import ziotokenapi.salesforce.authority.Authority

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
      .provideCustomLayer(
        Console.live ++ Configuration.live >+>
          Authority.live >+>
          Salesforce.live
      )
      .foldM(e => console.putStrLn(e.toString) *> ZIO.succeed(ExitCode.failure), _ => ZIO.succeed(ExitCode.success))
}
