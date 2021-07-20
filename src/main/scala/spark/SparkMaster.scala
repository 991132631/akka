package spark

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._


class SparkMaster extends Actor {

  val liveWorkerNodeMap = collection.mutable.HashMap[String, WorkerInfo]()

  override def receive: Receive = {

    case RegisterWorkerInfo(workerId, core, ram) => {

      if (!liveWorkerNodeMap.contains(workerId)) {
        val workerInfo = new WorkerInfo(workerId, core, ram)
        val lastHeartBeatTime = System.currentTimeMillis()

        workerInfo.lastHeartBeatTime = lastHeartBeatTime
        //liveWorkerNodeMap += ((workerId, workerInfo))
        liveWorkerNodeMap.put(workerId, workerInfo)
      }
      sender() ! RegisteredWorkerInfo
      println("master完成worker节点注册")
    }

    case HeartBeat(workerId) => {

      //语法糖map.get(key)==map(key)   ->  val workerInfo = liveWorkerNodeMap.get(workerId)
      val workerInfo = liveWorkerNodeMap(workerId)
      val currentTimeMillis = System.currentTimeMillis()
      workerInfo.lastHeartBeatTime = currentTimeMillis
      liveWorkerNodeMap.put(workerId, workerInfo)
      println("master更新存活worker节点心跳检测时间")

    }

    case CheckTimeOutWorker => {
      import context.dispatcher
      context.system.scheduler.schedule(0 millis, 6000 millis, self, RemoveTimeOurWorker)
      println("定时对记录的worker的心跳时间检测是否超时")
    }

    case RemoveTimeOurWorker => {
      val workInfos = liveWorkerNodeMap.values
      val currentTime = System.currentTimeMillis()

      /**
       * 注意一下这个
       * x => liveWorkerNodeMap.remove(x.id) 是因为WorkerInfo类里val了id才能用，这个原因是什么
       */
      workInfos.filter(x => currentTime - x.lastHeartBeatTime > 3000).foreach(x => liveWorkerNodeMap.remove(x.id))
      println(s"-------还剩下${liveWorkerNodeMap.size}个worker节点存活 -------")
    }

  }

}

object SparkMaster {

  def main(args: Array[String]): Unit = {

    // 检验参数
    if (args.length != 3) {
      println(
        """
          |请输入参数：<host> <port> <masterName>
                """.stripMargin)
      sys.exit() // 退出程序
    }
    //127.0.0.1 8888 master
    val host = args(0) //127.0.0.1
    val port = args(1) //8888
    val masterName = args(2) //master

    val config = ConfigFactory.parseString(
      s"""
         |akka.actor.provider="akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname=$host
         |akka.remote.netty.tcp.port=$port
         |""".stripMargin)

    val sparkMasterSystem = ActorSystem("sparkMasterSystem", config)
    val sparkMasterRef = sparkMasterSystem.actorOf(Props[SparkMaster], masterName)

    sparkMasterRef ! CheckTimeOutWorker


  } //
}
