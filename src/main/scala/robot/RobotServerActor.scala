package robot

import akka.actor.{Actor, ActorRef, ActorSystem, Props, actorRef2Scala}
import com.typesafe.config.ConfigFactory

class RobotServerActor extends Actor {

  override def receive: Receive = {

    case "start" => println("服务端号已启动")

    case ClientMessage(msg) => {
      //println(s"收到客户端消息：$msg")
      msg match {
        case "你叫什么名字" => sender() ! ServerMessage("我是机器人007号")
        case "你有什么功能" => sender() ! ServerMessage("我上知天文下知地理")
        case "今天天气怎么样" => sender() ! ServerMessage("今天北京通州有雷电蓝色预警")
        case "好了你退下吧" => sender() ! ServerMessage("遵旨")
        case _ => sender() ! ServerMessage("007没有听懂您的话")

      }
    }


  }

}


object RobotServerActor extends App {

  val host = "127.0.0.1"
  val port = 8088

  //看pom文件，typesafe包里的一种读取配置文件的类，可以读文件，读字符串，读很多种类型，
  //以后可以学习使用typesafe方法读配置文件，平时我工作常用到的，properties，xml，typesafe这几种
  val config = ConfigFactory.parseString(
    s"""
       |akka.actor.provider="akka.remote.RemoteActorRefProvider"
       |akka.remote.netty.tcp.hostname=$host
       |akka.remote.netty.tcp.port=$port
       |""".stripMargin)

  private val serverSystem: ActorSystem = ActorSystem.apply("Server", config)

  private val server: ActorRef = serverSystem.actorOf(Props[RobotServerActor], "Server")

  server ! "start"


}
