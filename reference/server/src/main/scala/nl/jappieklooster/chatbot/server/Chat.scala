package nl.jappieklooster.chatbot.server

import akka.actor._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._

import nl.jappieklooster.chatbot.protocol.Protocol

import org.kie.api.runtime.KieSession;
trait Chat {
  def chatFlow(sender: String): Flow[String, Protocol.Message, Any]
  def injectMessage(message: Protocol.ChatMessage): Unit
}

object Chat {
  def create(system: ActorSystem): Chat = {
    BotFace.createSession(null).start()
    // The implementation uses a single actor per chat to collect and distribute
    // chat messages. It would be nicer if this could be built by stream operations
    // directly.
    //
    val chatActor =
      system.actorOf(Props(new Actor {
        var subscribers = Map.empty[String, ChatSession]

        def receive: Receive = {
          case NewParticipant(name, subscriber) ⇒
            context.watch(subscriber)
            val chatsession = BotFace.createSession(subscriber)
            chatsession.start()
            println(name)
            subscribers += (name -> chatsession)

          case msg: ReceivedMessage      ⇒ dispatch(msg.toChatMessage)
          case msg: Protocol.ChatMessage ⇒ dispatch(msg)

          case ParticipantLeft(person) ⇒
            val session = subscribers(person)
            session.stop()
            subscribers -= person

          case Terminated(sub) ⇒
            //... Do stuff with the actor after its dead
        }

        def dispatch(msg: Protocol.ChatMessage): Unit = {
          println("user said " + msg.message)
          subscribers(msg.sender).chatWithBot(msg.message)
        }

      }))

    // Wraps the chatActor in a sink. When the stream to this sink will be completed
    // it sends the `ParticipantLeft` message to the chatActor.
    // FIXME: here some rate-limiting should be applied to prevent single users flooding the chat
    def chatInSink(sender: String) = Sink.actorRef[ChatEvent](chatActor, ParticipantLeft(sender))

    new Chat {
      def chatFlow(sender: String): Flow[String, Protocol.ChatMessage, Any] = {
        val in =
          Flow[String]
            .map(ReceivedMessage(sender, _))
            .to(chatInSink(sender))

        // The counter-part which is a source that will create a target ActorRef per
        // materialization where the chatActor will send its messages to.
        // This source will only buffer one element and will fail if the client doesn't read
        // messages fast enough.
        val out =
          Source.actorRef[Protocol.ChatMessage](1, OverflowStrategy.fail)
            .mapMaterializedValue(chatActor ! NewParticipant(sender, _))

        Flow.fromSinkAndSource(in, out)
      }
      def injectMessage(message: Protocol.ChatMessage): Unit = chatActor ! message // non-streams interface
    }
  }

  private sealed trait ChatEvent
  private case class NewParticipant(name: String, subscriber: ActorRef) extends ChatEvent
  private case class ParticipantLeft(name: String) extends ChatEvent
  private case class ReceivedMessage(sender: String, message: String) extends ChatEvent {
    def toChatMessage: Protocol.ChatMessage = Protocol.ChatMessage(sender, message)
  }
}
