package ziotokenapi

import scalaj.http.Http
import upickle.default.read
import zio._
import zio.console.Console
import ziotokenapi.configuration.Configuration

package object salesforce {

  type Salesforce = Has[Salesforce.Service]

  object Salesforce {

    trait Service {
      def fetchContact(id: String): ZIO[Any, Failure, Contact]
    }

    def fetchContact(id: String): ZIO[Salesforce, Failure, Contact] = RIO.accessM(_.get.fetchContact(id))

    val live: ZLayer[Configuration with Console, Failure, Salesforce] = serviceLayer
  }

  def makeToken: ZIO[Configuration with Console, Failure, Auth] =
    for {
      config   <- Configuration.config
      response <- Task
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
      auth     <- Task.effect(read[Auth](response.body)).mapError(SfTokenParseFailure.fromThrowable)
    } yield auth

  // TODO: implement as separate upstream service so that lifecycle can be controlled there - eg caching, refetch etc.
  val tokenLayer: ZLayer[Configuration with Console, Failure, Configuration with Has[Auth] with Console] =
    ZLayer.identity[Configuration] ++ ZLayer.identity[Console] ++ ZLayer.fromAcquireRelease(makeToken)(_ =>
      ZIO.succeed(())
    )

  //noinspection ConvertExpressionToSAM
  val upperLayer: ZLayer[Configuration with Has[Auth] with Console, Nothing, Salesforce] =
    ZLayer.fromServices[Configuration.Service, Auth, Console.Service, Salesforce.Service](
      (configuration, auth, console) =>
        new Salesforce.Service {
          def fetchContact(id: String): ZIO[Any, Failure, Contact] =
            for {
              config   <- configuration.config
              response <- ZIO
                            .effect(
                              Http(s"${auth.instance_url}/services/data/v${config.apiVersion}/sobjects/Contact/$id")
                                .header("Authorization", s"Bearer ${auth.access_token}")
                                .method("GET")
                                .asString
                            )
                            .mapError(SfReadFailure.fromThrowable)
                            .tap(rsp => console.putStrLn(s"Response from fetchContact '$id': ${rsp.toString}\n"))
              contact  <- ZIO.effect(read[Contact](response.body)).mapError(SfParseFailure.fromThrowable)
            } yield contact
        }
    )

  val serviceLayer: ZLayer[Configuration with Console, Failure, Salesforce] = tokenLayer >>> upperLayer
}
