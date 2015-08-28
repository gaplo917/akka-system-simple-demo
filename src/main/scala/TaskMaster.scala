import akka.actor._
import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.control.TailCalls.TailRec

/**
 * Created by Gary Lo on 27/8/15.
 */
class TaskMaster extends Actor with ActorLogging {
  private val q = new mutable.Queue[Task]()
  override def receive: Receive = {
    case JobDescriptions(size, numOfWorker) =>
      // initialize the Task according to the description
      for(i <- 1 to size){
        q.enqueue(Task(id = i, func = factorial))
      }

      // initialize the Child worker according to the description
      for(i <- 1 to numOfWorker ){
        // create Child Actor with id = 00001, 00002.. so on
        context.actorOf(Props[TaskWorker], f"$i%05d")
      }

      // after init,  start jobDistribution
      self ! JobDistribution

    case JobDone(result) =>
      log.info(s"[${sender().path.name}] has finished his job")
      if(q.isEmpty){
        context.watch(sender())
        sender() ! PoisonPill
      }

    case JobDistribution =>
      // it is redundant in real-world case
      val sortedChild = context.children.toList.sortBy(ref => ref.path.name.toInt)

      while(q.nonEmpty) {
        // sort the children first then distribute the task to them
        sortedChild.foreach(ref =>
          ref ! q.dequeue()
        )
      }
      // after distribution, start to work
      self ! StartToWork

    case StartToWork =>
      // force child to work
      context.children.foreach(ref =>
        ref ! StartToWork
      )

    case Terminated(subject) =>
      log.info(s"receive Terminated $subject")
      if(context.children.isEmpty) context.parent ! PoisonPill
  }

  /**
   * The factorial task
   * @param n       size of factorial
   * @param result  factorial Result
   * @return
   */
  private def factorial(n : Int, result: BigDecimal = BigDecimal(1)): BigDecimal = {
    if(n > 0) factorial(n-1, result * BigDecimal(n))
    else result
  }

}

// Job description from system
case class JobDescriptions(taskSize: Int, numOfWorker: Int)

// Task definition
case class Task(id: Int, func: (Int, BigDecimal) => BigDecimal, result: Option[BigDecimal] = None)

// For child case
case class JobDone(result:Option[BigDecimal])
case object JobDistribution
case object StartToWork