import akka.actor.{Actor}

/**
 * Created by Gary Lo on 27/8/15.
 */
class TaskWorker extends Actor{

  override def receive: Receive = {
    case Task(id,duration) =>

      println(s"[${self.path.name}] Doing job $id")

      // TODO: really do a blocking job instead of sleep
      Thread.sleep(duration.toMillis)

      println(s"[${self.path.name}] Job Done! Time used = $duration")

      sender() ! JobDone(Some(id))

  }

}
