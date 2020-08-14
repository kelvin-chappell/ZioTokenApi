package example

import example.configuration.Configuration
import scalaj.http.Http
import upickle.default.read
import zio._

package object salesforce {

  type Salesforce = Has[Salesforce.Service]

  object Salesforce {

    trait Service {
      def fetchContact(id: String): Task[Contact]
    }

    def fetchContact(id: String): RIO[Salesforce, Contact] =
      RIO.accessM(_.get.fetchContact(id))

    //noinspection ConvertExpressionToSAM
    val live: URLayer[Configuration, Salesforce] = ???
  }

  def makeToken: ZIO[Configuration, Throwable, Auth] = {
    for {
      config <- Configuration.build
      response <- Task.effect(
        Http(s"${config.authHost}/services/oauth2/token")
          .postForm(
            Seq(
              "grant_type" -> "password",
              "client_id" -> config.clientId,
              "client_secret" -> config.clientSecret,
              "username" -> config.userName,
              "password" -> s"${config.password}${config.authToken}"
            )
          )
          .asString
      )
      auth <- Task.effect(read[Auth](response.body))
    } yield auth
  }

  // TODO: implement as separate upstream service so that lifecycle can be controlled there - eg caching, refetch etc.
  val tokenLayer: ZLayer[Configuration, Throwable, Configuration with Has[Auth]] =
    ZLayer.identity[Configuration] ++ ZLayer.fromAcquireRelease(makeToken)(t => ZIO.succeed(()))

  //noinspection ConvertExpressionToSAM
  val upperLayer: ZLayer[Configuration with Has[Auth], Nothing, Salesforce] =
    ZLayer.fromServices[Configuration.Service, Auth, Salesforce.Service]((configuration, auth) =>
      new Salesforce.Service {
        def fetchContact(id: String): Task[Contact] = {
          for {
            config <- configuration.build
            response <- Task.effect(
              Http(s"${auth.instanceUrl}/services/data/v${config.apiVersion}/sobjects/Contact/$id")
                .header("Authorization", s"Bearer ${auth.accessToken}")
                .method("GET")
                .asString
            )
            contact <- Task.effect(read[Contact](response.body))
          } yield contact
        }
      }
    )

  val serviceLayer: ZLayer[Configuration, Throwable, Salesforce] = tokenLayer >>> upperLayer
}
