package ziotokenapi.salesforce

import scalaj.http.Http
import upickle.default.read
import zio.console.Console
import zio.{Has, Task, ZIO, ZLayer}
import ziotokenapi.configuration.Configuration
import ziotokenapi.{Failure, SfTokenParseFailure, SfTokenReadFailure}

package object auth {

  type Auth = Has[Auth.Service]

  object Auth {
    trait Service {
      val authority: ZIO[Any, Failure, Authority]
    }

    val authority: ZIO[Auth, Failure, Authority] = ZIO.accessM(_.get.authority)

    val live: ZLayer[Configuration with Console, Failure, Auth] =
      ZLayer.fromServices[Configuration.Service, Console.Service, Auth.Service] { (configuration, console) =>
        new Service {
          val authority: ZIO[Any, Failure, Authority] = for {
            config    <- configuration.config
            response  <- ZIO
                           .effect(
                             Http(s"${config.authHost}/services/oauth2/token")
                               .postForm(
                                 Seq(
                                   "grant_type"    -> "password",
                                   "client_id"     -> config.clientId,
                                   "client_secret" -> config.clientSecret,
                                   "username"      -> config.userName,
                                   "password"      -> s"${config.password}${config.authToken}"
                                 )
                               )
                               .asString
                           )
                           .mapError(SfTokenReadFailure.fromThrowable)
                           .tap(rsp => console.putStrLn(s"Response from makeToken: ${rsp.toString}\n"))
            authority <- Task.effect(read[Authority](response.body)).mapError(SfTokenParseFailure.fromThrowable)
          } yield authority
        }
      }
  }
}
