package example

import zio._

package object configuration {

  type Configuration = Has[Configuration.Service]

  object Configuration {

    trait Service {
      val build: Task[Config]
    }

    val build: RIO[Configuration, Config] = RIO.accessM(_.get.build)

    val live: ZLayer[system.System, Nothing, Configuration] = ZLayer.fromService(system =>
      new Service {

        private def env(key: String): Task[String] =
          for {
            optV <- system.env(key)
            v <- ZIO.fromOption(optV).orElseFail(new RuntimeException(s"No value for '$key' in environment"))
          } yield v

        override val build: Task[Config] = for {
          authHost <- env("authHost")
          userName <- env("userName")
          password <- env("password")
          clientId <- env("clientId")
          clientSecret <- env("clientSecret")
          authToken <- env("authToken")
          apiVersion <- env("apiVersion")
        } yield Config(authHost, userName, password, clientId, clientSecret, authToken, apiVersion)
      }
    )
  }
}
