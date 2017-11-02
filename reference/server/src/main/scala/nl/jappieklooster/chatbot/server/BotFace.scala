package nl.jappieklooster.chatbot.server

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import akka.actor._

import java.util.logging.Logger;
import java.util.Random;

import akka.actor._

import org.salve.drools.controller.DroolInterface;
import org.salve.drools.UnparsedUserUtterance;
import org.salve.drools.GameStart;

import nl.jappieklooster.chatbot.protocol.Protocol
import nl.jappieklooster.ymlbot.YapFactory;

import org.apache.commons.vfs2.VFS;

/**
  * We expose a simpler interface than what kie actually entails
  */
trait ChatSession{
  def chatWithBot(userMsg:String):Unit
  def start():Unit
  def stop():Unit
}
/**
  * This object deals with the bot.
  */
object BotFace{
  val baseName = "rules"
  val scenarioName = "agnese2"

  // enterprise java TM
  private val kieServices = KieServices.Factory.get()
  private val kieContainer = kieServices.getKieClasspathContainer()

  println(kieContainer.getKieBaseNames().toString())
  println(kieContainer.verify().getMessages())
  // you could have multiple bases and different scenarios run at the same
  // time, we don't do that here
  private val kieBase = kieContainer.getKieBase(baseName)

  // this is what we're interested in
  def createSession(actor:ActorRef): ChatSession = new ChatSession{
    val kSession = kieBase.newKieSession();
    def start():Unit = {
      new Thread(new Runnable() {
          def run():Unit = {
              kSession.fireUntilHalt(); // This is blocking: https://docs.jboss.org/drools/release/6.5.0.Final/drools-docs/html/ch07.html#d0e7301
          }
      }).start();
      kSession.setGlobal("random", new Random());
      kSession.setGlobal("log", Logger.getLogger("kie"));

      // the controller global is used by drools to interact with the 
      // outside world
      kSession.setGlobal("controller", new DroolInterface(){
          def respond(text:String):Unit = {
              println("chatbot said: " + text)
              actor ! Protocol.ChatMessage("bot", text)
          }
      })

      val resourceurl = ClassLoader.getSystemClassLoader().getResource("bots/agnese2/yml")
      val folder = VFS.getManager().resolveFile(resourceurl)
	  val result = new YapFactory().create(folder);
	  result.forEach( x => kSession.insert(x));
	  val start = new GameStart();
	  kSession.insert(start);
    }
    def stop():Unit = {
        kSession.halt();
        kSession.dispose();

        // report downstream of completion, otherwise, there's a risk of leaking the
        // downstream when the TCP connection is only half-closed
        actor ! Status.Success(Unit)
    }
    def chatWithBot(userMsg:String) = {
    	kSession.insert(new UnparsedUserUtterance(userMsg));
      actor ! Protocol.ChatMessage("user", userMsg)
    }
  }
}
