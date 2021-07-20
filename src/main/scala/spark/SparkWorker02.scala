package spark

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import java.util.UUID
import scala.concurrent.duration._


class SparkWorker02(sparkMasterURL: String) extends Actor {

  var masterProxy: ActorSelection = _
  val workerId = UUID.randomUUID().toString

  override def preStart(): Unit = {
    masterProxy = context.actorSelection(sparkMasterURL)
  }

  override def receive: Receive = {
    case "start" =>
      masterProxy ! RegisterWorkerInfo(workerId, 4, 64 * 1024)
      println("worker节点向master节点注册")

    case RegisteredWorkerInfo => {
      import context.dispatcher
      context.system.scheduler.schedule(0 millis, 1500 millis, self, SendHeartBeat)
      println("worker节点定时提醒自己发送心跳")
    }
    case SendHeartBeat => {
      println(s"-------worker节点 $workerId 发送给Master心跳检测-----")
      masterProxy ! HeartBeat(workerId)
    }

  }


}


object SparkWorker02 {

  def main(args: Array[String]): Unit = {

    if (args.length != 4) {
      println(
        """
          |请输入正确的参数：<host> <port> <SparkWorkerName> <sparkMasterURL>
          |""".stripMargin)
    }

    //args：127.0.0.1 8890 worker-02 akka.tcp://sparkMasterSystem@127.0.0.1:8888/user/master
    val host = args(0) //"127.0.0.1"
    val port = args(1) //8890
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

    SparkWorkerRef ! "start"


  } //
}
