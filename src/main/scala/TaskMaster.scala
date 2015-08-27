import akka.actor.{Terminated, PoisonPill, Props, Actor}
import scala.collection.mutable
import scala.concurrent.duration._

/**
 * Created by Gary Lo on 27/8/15.
 */
class TaskMaster extends Actor {
  private val q = new mutable.Queue[Task]()
  override def receive: Receive = {
    case JobDescriptions(size, numOfWorker) =>
      // initialize the Task according to the description
      for(i <- 1 to size){
        q.enqueue(Task(i, i seconds))
      }

      // initialize the Child worker according to the description
      for(i <- 1 to numOfWorker ){
        val child = context.actorOf(Props[TaskWorker], s"worker_$i")
        if(q.nonEmpty) child ! q.dequeue()
      }

    case JobDone(result) =>
      if(q.nonEmpty) sender() ! q.dequeue()
      else {
        context.watch(sender())
        sender() ! PoisonPill
      }

    case Terminated(subject) =>
      println(s"receive Terminated $subject")
      if(context.children.isEmpty) context.parent ! PoisonPill
  }
}
case class Task(id: Int, duration: Duration)
case class JobDescriptions(taskSize: Int, numOfWorker: Int)
case class JobDone(result:Option[Int])
case object Terminal