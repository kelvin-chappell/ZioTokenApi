package ziotokenapi

trait Failure

case class ConfigFailure(reason: String) extends Failure

case class SfTokenReadFailure(reason: String) extends Failure
object SfTokenReadFailure {
  def fromThrowable(t: Throwable): SfTokenReadFailure = SfTokenReadFailure(t.toString)
}

case class SfTokenParseFailure(reason: String) extends Failure
object SfTokenParseFailure {
  def fromThrowable(t: Throwable): SfTokenParseFailure = SfTokenParseFailure(t.toString)
}
case class SfReadFailure(reason: String) extends Failure
object SfReadFailure       {
  def fromThrowable(t: Throwable): SfReadFailure = SfReadFailure(t.toString)
}

case class SfParseFailure(reason: String) extends Failure
object SfParseFailure {
  def fromThrowable(t: Throwable): SfParseFailure = SfParseFailure(t.toString)
}
