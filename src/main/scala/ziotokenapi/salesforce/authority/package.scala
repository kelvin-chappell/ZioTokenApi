package ziotokenapi.salesforce

import scalaj.http.Http
import upickle.default.read
import zio.console.Console
import zio.{Has, UIO, URIO, ZIO, ZLayer}
import ziotokenapi.configuration.Configuration
import ziotokenapi.{Failure, SfTokenParseFailure, SfTokenReadFailure}

package object authority {

  type Authority = Has[Authority.Service]

  object Authority {

    trait Service {
      val access: UIO[Access]
    }

    val access: URIO[Authority, Access] = URIO.accessM(_.get.access)

    val live: ZLayer[Configuration with Console, Failure, Authority] =
      ZLayer
        .fromServicesM[Configuration.Service, Console.Service, Configuration with Console, Failure, Authority.Service] {
          (configuration, console) =>
            for {
              config   <- configuration.config
              response <- ZIO
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
              a        <- ZIO.effect(read[Access](response.body)).mapError(SfTokenParseFailure.fromThrowable)
            } yield new Service {
              val access: ZIO[Any, Nothing, Access] = ZIO.succeed(a)
            }
        }
  }
}
