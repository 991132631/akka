package spark

//worker->master

case class RegisterWorkerInfo(workerId: String, core: Int, ram: Int)

case class HeartBeat(workerId: String)

//master->worker
case object RegisteredWorkerInfo


//master存储worker节点的数据类

/**
 *
 * @param id val id 写死，master里才能调用，workInfos.filter(x => currentTime - x.lastHeartBeatTime > 3000).foreach(x => liveWorkerNodeMap.remove(x.id))
 * @param core
 * @param ram
 */
class WorkerInfo(val id: String, core: Int, ram: Int) {
  var lastHeartBeatTime: Long = _
}

case object CheckTimeOutWorker

case object RemoveTimeOurWorker

//worker给自己发消息

case object SendHeartBeat












