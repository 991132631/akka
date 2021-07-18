package pingpangtest

import akka.actor.{ActorRef, ActorSystem, Props}

/**
 * extends App,App Trait可以帮你实现类的懒加载？
 * 可以直接实现main方法，不用写main直接写内容
 */

object PingPangAPP extends App {
  /**
   * 单例工厂类ActorSystem
   */
  private val pingPangActorSystem: ActorSystem = ActorSystem("PingPangActorSystem")
  /**
   * 生成ActorRef
   *
   * Props是akka.actor.Props类，而且里面参数是Actor的实例
   * Props[FengGeActor]，这种方法，直接调用FengGeActor无参构造实例化
   * Props(new LongGeActor(fengGeActor))，这种方法就是含参构造实例化的匿名内部类
   */
  //private val fengGeActor: ActorRef = pingPangActorSystem.actorOf(Props(new FengGeActor), "FengGeActor")
  private val fengGeActor: ActorRef = pingPangActorSystem.actorOf(Props[FengGeActor], "FengGeActor")
  /**
   * 因为龙哥是第一个发送消息的人，所以他需要接收消息的人的ActorRef
   * 而接受者回复消息可以通过sender()方法获取的结果就是发送人
   */
  private val longGeActor: ActorRef = pingPangActorSystem.actorOf(Props(new LongGeActor(fengGeActor)), "LongGeActor")

  longGeActor ! "start"

}
