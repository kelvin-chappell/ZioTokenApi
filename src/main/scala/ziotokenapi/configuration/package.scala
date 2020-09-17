package ziotokenapi

import zio._

package object configuration {

  type Configuration = Has[Configuration.Service]

  object Configuration {

    trait Service {
      val config: UIO[Config]
    }

    val config: URIO[Configuration, Config] = ZIO.accessM(_.get.config)

    val live: ZLayer[system.System, Failure, Configuration] = ZLayer.fromServiceM { system =>
      def env(key: String): ZIO[Any, Failure, String] =
        for {
          optValue <- system.env(key).mapError(e => ConfigFailure(e.toString))
          value    <- ZIO.fromOption(optValue).orElseFail(ConfigFailure(s"No value for '$key' in environment"))
        } yield value

      for {
        authHost     <- env("authHost")
        userName     <- env("userName")
        password     <- env("password")
        clientId     <- env("clientId")
        clientSecret <- env("clientSecret")
        authToken    <- env("authToken")
      } yield new Service {
        val config: UIO[Config] =
          UIO(Config(authHost, userName, password, clientId, clientSecret, authToken))
      }
    }
  }
}
