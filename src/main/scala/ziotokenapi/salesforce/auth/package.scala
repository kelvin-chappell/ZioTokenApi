package ziotokenapi.salesforce

import scalaj.http.Http
import upickle.default.read
import zio.console.Console
import zio.{Has, Task, ZIO, ZLayer}
import ziotokenapi.configuration.Configuration
import ziotokenapi.salesforce.auth.Access
import ziotokenapi.{Failure, SfTokenParseFailure, SfTokenReadFailure}

package object authority {

  type Authority = Has[Authority.Service]

  object Authority {
    trait Service {
      val access: ZIO[Any, Failure, Access]
    }

    val access: ZIO[Authority, Failure, Access] = ZIO.accessM(_.get.access)

    val live: ZLayer[Configuration with Console, Failure, Authority] =
      ZLayer.fromServices[Configuration.Service, Console.Service, Authority.Service] { (configuration, console) =>
        new Service {
          val access: ZIO[Any, Failure, Access] = for {
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
            authority <- Task.effect(read[Access](response.body)).mapError(SfTokenParseFailure.fromThrowable)
          } yield authority
        }
      }
  }
}
