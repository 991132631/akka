package spark

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import java.util.UUID
import scala.concurrent.duration._


//SparkWorker先向master发消息，所以需要拿到master的信息，(sparkMasterURL: String)
class SparkWorker(sparkMasterURL: String) extends Actor {

  //获取server端的ActorRef我们用的是ActorSelection
  var masterProxy: ActorSelection = _
  //worker节点id
  val workerId = UUID.randomUUID().toString

  override def preStart(): Unit = {
    masterProxy = context.actorSelection(sparkMasterURL)
  }

  override def receive: Receive = {
    case "start" =>
      // 向master注册自己的信息，id, core, ram
      masterProxy ! RegisterWorkerInfo(workerId, 4, 64 * 1024)
      println("worker节点向master节点注册")

    case RegisteredWorkerInfo => {
      // 收到master发送给worker已经的注册成功消息
      // worker 启动一个定时器，定时向master 发送心跳
      import context.dispatcher
      context.system.scheduler.schedule(0 millis, 1500 millis, self, SendHeartBeat)
      println("worker节点定时提醒自己发送心跳")
    }
    case SendHeartBeat => {
      // 开始向master发送心跳了
      println(s"-------worker节点 $workerId 发送给Master心跳检测-----")
      masterProxy ! HeartBeat(workerId)
    }

  }


}


object SparkWorker {

  def main(args: Array[String]): Unit = {

    if (args.length != 4) {
      println(
        """
          |请输入正确的参数：<host> <port> <SparkWorkerName> <sparkMasterURL>
          |""".stripMargin)
    }

    //args：127.0.0.1 8889 worker-01 akka.tcp://sparkMasterSystem@127.0.0.1:8888/user/master
    val host = args(0) //"127.0.0.1"
    val port = args(1) //8889
    val SparkWorkerName = args(2) //worker-01
    val sparkMasterURL = args(3) //akka.tcp://sparkMasterSystem@127.0.0.1:8888/user/master

    val config = ConfigFactory.parseString(
      s"""
         |akka.actor.provider="akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname=$host
         |akka.remote.netty.tcp.port=$port
         |""".stripMargin)

    val SparkWorkerSystem = ActorSystem("SparkWorkerSystem", config)
    val SparkWorkerRef = SparkWorkerSystem.actorOf(Props(new SparkWorker02(sparkMasterURL)), SparkWorkerName)
    // 给自己发送一个以启动的消息，标识自己已经就绪了，自己会接收到，然后作为标志执行接下来的逻辑--给master发送注册消息
    SparkWorkerRef ! "start"


  } //
}
