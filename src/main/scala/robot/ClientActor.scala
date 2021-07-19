package robot

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.io.StdIn


class ClientActor(host: String, port: Int) extends Actor {

  //ActorSelection是ActorRef的一种包装，这个也可以当做ActorRef一样发送消息
  var serverSelectionRef: ActorSelection = _

  override def preStart(): Unit = {
    //服务端启动暴露的地址，后面加上/user/服务端的akka.tcp://Server@127.0.0.1:8088/user/ActorRef的参数里的名字，不是变量名
    //private val server: ActorRef = serverSystem.actorOf(Props[RobotServerActor], "Server")
    serverSelectionRef = context.actorSelection(s"akka.tcp://Server@$host:$port/user/Server")

  }


  override def receive: Receive = {
    case "start" => println("客户端已经启动")

    //这里的msg: String ，这个msg其实是随便起名的变量，因为case可以对数据类型进行判断所以这里
    //就是随便起名表示数据类型是string的消息而已，是用户在控制台输入的消息
    case msg: String => {
      serverSelectionRef ! ClientMessage(msg)
    }

    /**
     * 之所以要把客户端服务端消息用样例类进行封装，就是为了这里区分消息是谁发的，是什么格式，因为样例类我们随便定义
     * 所以相当于用样例类包装一下消息来判断不同消息怎么处理，这里因为从控制台接收的用户消息也是string字符串，所以把客户端服务端
     * 的消息单独封装一下就可以知道区分对待了
     */
    case ServerMessage(msg) => println(s"客户端收到消息：$msg")


  }


}


object ClientActor {
  def main(args: Array[String]): Unit = {

    val host = "127.0.0.1"
    val port = "8089"

    //因为客户端的Actor类和初始化都需要服务端的host，port作为参数，来获取服务端的ActorRef
    val serverHost = "127.0.0.1"
    val serverPort = 8088

    val config = ConfigFactory.parseString(
      s"""
         |akka.actor.provider="akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname=$host
         |akka.remote.netty.tcp.port=$port""".stripMargin)
    val clientSystem = ActorSystem.apply("Client", config)

    val clientRef = clientSystem.actorOf(Props(new ClientActor(serverHost, serverPort)), "Client")

    /**
     * 自己给自己发消息是可以的，也经过自己的dispatch消息分发的线程池，然后到自己的mailbox这个FIFO消息队列，
     * 然后队列线程run调用自己的receive执行逻辑
     *
     * 给其他人发消息是经过自己的dispatch消息分发的线程池，然后到别人的的mailbox这个FIFO消息队列，
     * 然后别人的队列线程run调用自己的receive执行逻辑
     */
    clientRef ! "start"

    while (true) {
      val str = StdIn.readLine()//同步，阻塞
      clientRef ! str


    }


  }
}
