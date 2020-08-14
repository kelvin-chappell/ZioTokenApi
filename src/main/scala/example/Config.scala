package example

case class Config(
    authHost: String,
    userName: String,
    password: String,
    clientId: String,
    clientSecret: String,
    authToken: String,
    apiVersion: String
)