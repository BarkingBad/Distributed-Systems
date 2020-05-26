package ex

import akka.actor.{Actor, ActorRef, PoisonPill}
import slick.jdbc.SQLiteProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext
import scala.util.Success

class Registry(tag: Tag) extends Table[(String, Int)](tag, "Registry") {
  def name = column[String]("name", O.PrimaryKey)
  def quantity = column[Int]("quantity")
  def * = (name, quantity)
}

object DbDao {

  val registry = TableQuery[Registry]
  val db = Database.forConfig("registry")
  db.run(registry.schema.create)

  def getAndUpdate(name: String)(implicit ec: ExecutionContext): DBIO[Int] = {
    for {
      h <- registry.filter(_.name === name).map(_.quantity).result.headOption
      _ <- h.fold(registry += (name, 1))(v => registry.filter(_.name === name).update((name, v+1)))
    } yield h.getOrElse(1)
  }
}

class DbDelegate extends Actor {

  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case DbRequest(name) => {
      val s = sender
      DbDao.db.run(DbDao.getAndUpdate(name)).onComplete {
        case Success(value) => {
          s ! DbResponse(name, value)
        }
        case _ =>
      }
      self ! PoisonPill
    }
  }
}

case class DbRequest(name: String)
case class DbResponse(name: String, quantity: Int)