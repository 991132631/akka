package pingpangtest

import akka.actor.{Actor, ActorRef}


/**
 * 因为龙哥是第一个发送消息的人，所以他需要接收消息的人的ActorRef，所以含参构造了，传进来的是接收消息人的ActorRef
 * 而接受者回复消息可以通过sender()方法获取的结果就是发送人
 */
class LongGeActor(fgActorRef:ActorRef) extends Actor{


  override def receive: Receive ={
    case "start" =>{
      /**
       * actor之间发送消息对话是不展示的，你是看不到内容的，所以打印一下看程序是否正常运行
       */
      println("我是龙哥，我把球发出去了")
      fgActorRef !"我是龙哥，我把球发出去了"
    }
    case "我是峰哥，我把球打回给龙哥" =>{
      println("我是龙哥，我把球打回给峰哥")
      fgActorRef !"我是龙哥，我把球打回给峰哥"
    }

















  }

}
