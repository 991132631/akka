package spark

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._


class SparkMaster extends Actor {

  // 存储worker发来的注册的worker节点信息的
  val liveWorkerNodeMap = collection.mutable.HashMap[String, WorkerInfo]()

  override def receive: Receive = {

    // 收到worker注册过来的信息
    case RegisterWorkerInfo(workerId, core, ram) => {

      if (!liveWorkerNodeMap.contains(workerId)) {
        val workerInfo = new WorkerInfo(workerId, core, ram)
        val lastHeartBeatTime = System.currentTimeMillis()

        workerInfo.lastHeartBeatTime = lastHeartBeatTime
        //liveWorkerNodeMap += ((workerId, workerInfo))
        liveWorkerNodeMap.put(workerId, workerInfo)
      }
      // master存储完worker注册的数据之后，要告诉worker说你已经注册成功
      sender() ! RegisteredWorkerInfo
      println("master完成worker节点注册")
    }

    case HeartBeat(workerId) => {
      // master收到worker的心跳消息之后，更新woker的上一次心跳时间

      //语法糖map.get(key)==map(key)   ->  val workerInfo = liveWorkerNodeMap.get(workerId)
      val workerInfo = liveWorkerNodeMap(workerId)
      val currentTimeMillis = System.currentTimeMillis()
      // 更改心跳时间
      workerInfo.lastHeartBeatTime = currentTimeMillis
      liveWorkerNodeMap.put(workerId, workerInfo)
      println("master更新存活worker节点心跳检测时间")

    }

    case CheckTimeOutWorker => {
      // 使用调度器时候必须导入dispatcher
      import context.dispatcher
      context.system.scheduler.schedule(0 millis, 6000 millis, self, RemoveTimeOurWorker)
      println("定时对记录的worker的心跳时间检测是否超时")
    }

    case RemoveTimeOurWorker => {
      // 将hashMap中的所有的value都拿出来，查看当前时间和上一次心跳时间的差 3000

      val workInfos = liveWorkerNodeMap.values
      val currentTime = System.currentTimeMillis()

      /**
       * 注意一下这个
       * x => liveWorkerNodeMap.remove(x.id) 是因为WorkerInfo类里val了id才能用，这个原因是什么
       */
      //  过滤超时的worker
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
    // 自己给自己发送一个消息，去启动一个调度器，定期的检测HashMap中超时的worker
    //最开始没有节点也不影响，有了的话就开始执行检查
    sparkMasterRef ! CheckTimeOutWorker


  } //
}
