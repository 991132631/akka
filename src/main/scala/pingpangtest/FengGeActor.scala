package pingpangtest

import akka.actor.Actor
/**
 * 因为龙哥是第一个发送消息的人，所以他需要接收消息的人的ActorRef
 * 而接受者回复消息可以通过sender()方法获取的结果就是发送人
 */
class FengGeActor extends Actor {

  override def receive: Receive = {
    case "我是龙哥，我把球发出去了" => {
      println("我是峰哥，我把球打回给龙哥")
      sender() ! "我是峰哥，我把球打回给龙哥"
    }

    case "我是龙哥，我把球打回给峰哥" => {
      println("我是峰哥，我把球打回给龙哥")
      sender() ! "我是峰哥，我把球打回给龙哥"
    }



  }
}
