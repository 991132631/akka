package robot

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.io.StdIn


class ClientActor(host: String, port: Int) extends Actor {

  var serverSelectionRef: ActorSelection = _

  override def preStart(): Unit = {
    //服务端启动暴露的地址，后面加上/user/服务端的akka.tcp://Server@127.0.0.1:8088/user/ActorRef的参数里的名字，不是变量名
    //private val server: ActorRef = serverSystem.actorOf(Props[RobotServerActor], "Server")
    serverSelectionRef = context.actorSelection(s"akka.tcp://Server@$host:$port/user/Server")

  }


  override def receive: Receive = {
    case "start" => println("客户端已经启动")
    case msg: String => {
      serverSelectionRef ! ClientMessage(msg)
    }
    case ServerMessage(msg) => println(s"客户端收到消息：$msg")


  }


}


object ClientActor {
  def main(args: Array[String]): Unit = {

    val host = "127.0.0.1"
    val port = "8089"

    val serverHost = "127.0.0.1"
    val serverPort = 8088

    val config = ConfigFactory.parseString(
      s"""
         |akka.actor.provider="akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname=$host
         |akka.remote.netty.tcp.port=$port""".stripMargin)
    val clientSystem = ActorSystem.apply("Client", config)

    val clientRef = clientSystem.actorOf(Props(new ClientActor(serverHost, serverPort)), "Client")
    clientRef ! "start"

    while (true) {
      val str = StdIn.readLine()
      clientRef ! str


    }


  }
}
