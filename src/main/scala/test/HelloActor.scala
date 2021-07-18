package test

import akka.actor.{Actor, ActorRef, ActorSystem, Props, actorRef2Scala}
import test.HelloActor.{helloActor, helloActorRef}


class HelloActor extends  Actor{
  override def receive: Receive = {

    case "你好帅" =>println("竟说实话")
    case "你好丑" => {
      println("滚，不跟你说了，我关闭系统了")

      /**
       * 发消息，关闭actor，传入的都是actor的引用地址，ActorRef
       * 关闭actor，self==当前actor，即helloActorRef
       */
      //helloActor.stop(helloActorRef)
      //helloActor.stop(self)
      //context.stop(helloActorRef)
      context.stop(self)
      //关闭jvm里akka的ActorSystem
      context.system.terminate()
    }

  }
}

/**
 *extends App,App Trait可以帮你实现类的懒加载？
 *可以直接实现main方法，不用写main直接写内容
 */
object HelloActor extends App {
  private val helloActor: ActorSystem = ActorSystem("HelloActor")//语法糖，其实是ActorSystem.apply(...)
  private val helloActorRef: ActorRef = helloActor.actorOf(Props[HelloActor], "helloActor")

  helloActorRef ! "你好帅"
  helloActorRef ! "你好丑"




}