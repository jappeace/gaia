package nl.jappieklooster.chatbot.server

import java.util.Date

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{ Message, TextMessage }

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives
import akka.stream.scaladsl.Flow
import upickle.default._
import nl.jappieklooster.chatbot.protocol.Protocol
import nl.jappieklooster.chatbot.protocol.Protocol._

import scala.util.Failure

class Webservice(implicit system: ActorSystem) extends Directives {
  val theChat = Chat.create(system)

  def route =
    get {
      pathSingleSlash {
        getFromResource("web/index.html")
      } ~
        // Scala-JS puts them in the root of the resource directory per default,
        // so that's where we pick them up
        path("client.js")(getFromResource("client.js")) ~
        //path("frontend-fastopt.js")(getFromResource("frontend-fastopt.js")) ~
        path("chat") {
          parameter('name) { name ⇒
            handleWebSocketMessages(websocketChatFlow(sender = name))
          }
        }
    } ~
      getFromResourceDirectory("web")

  def websocketChatFlow(sender: String): Flow[Message, Message, Any] =
    Flow[Message]
      .collect {
        case TextMessage.Strict(msg) ⇒ msg // unpack incoming WS text messages...
        // This will lose (ignore) messages not received in one chunk (which is
        // unlikely because chat messages are small) but absolutely possible
        // FIXME: We need to handle TextMessage.Streamed as well.
      }
      .via(theChat.chatFlow(sender)) // ... and route them through the chatFlow ...
      .map {
        case msg: Protocol.Message ⇒
          TextMessage.Strict(write(msg)) // ... pack outgoing messages into WS JSON messages ...
      }
      .via(reportErrorsFlow) // ... then log any processing errors on stdin

  import scala.concurrent._
  import ExecutionContext.Implicits.global
  def reportErrorsFlow[T]: Flow[T, T, Any] =
    Flow[T]
      .watchTermination()((_, f) => f.onComplete {
        case Failure(cause) =>
          println(s"WS stream failed with $cause")
        case _ => // ignore regular completion
      })
}
