package example

import example.configuration.Configuration
import example.salesforce.Salesforce
import zio._
import zio.console.Console

object Main extends zio.App {

  val program: RIO[Salesforce with Console, Unit] = for {
    contact <- Salesforce.fetchContact("1")
    _ <- console.putStrLn(contact.toString)
  } yield ()

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program.exitCode.provideCustomLayer(Configuration.live >>> Salesforce.live)
}
