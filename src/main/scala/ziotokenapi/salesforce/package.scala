package ziotokenapi

import scalaj.http.Http
import upickle.default.read
import zio._
import zio.console.Console
import ziotokenapi.configuration.Configuration
import ziotokenapi.salesforce.authority.Authority

package object salesforce {

  type Salesforce = Has[Salesforce.Service]

  object Salesforce {

    trait Service {
      def fetchContact(id: String): ZIO[Any, Failure, Contact]
    }

    def fetchContact(id: String): ZIO[Salesforce, Failure, Contact] = ZIO.accessM(_.get.fetchContact(id))

    val live: ZLayer[Authority with Configuration with Console, Failure, Salesforce] =
      ZLayer.fromServices[Authority.Service, Configuration.Service, Console.Service, Salesforce.Service] {
        (auth, configuration, console) => id =>
          for {
            authority <- auth.access
            config    <- configuration.config
            response  <-
              ZIO
                .effect(
                  Http(s"${authority.instance_url}/services/data/v${config.apiVersion}/sobjects/Contact/$id")
                    .header("Authorization", s"Bearer ${authority.access_token}")
                    .method("GET")
                    .asString
                )
                .mapError(SfReadFailure.fromThrowable)
                .tap(rsp => console.putStrLn(s"Response from fetchContact '$id': ${rsp.toString}\n"))
            contact   <- ZIO.effect(read[Contact](response.body)).mapError(SfParseFailure.fromThrowable)
          } yield contact
      }
  }
}
