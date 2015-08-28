import akka.actor.{ActorLogging, Actor}

import scala.collection.mutable

/**
 * Created by Gary Lo on 27/8/15.
 */
class TaskWorker extends Actor with ActorLogging{

  private val q = mutable.Queue[Task]()

  override def receive: Receive = {

    case task @ Task(_,_,_) =>
      q.enqueue(task)

    case StartToWork =>

      if(q.nonEmpty){
        val task = q.dequeue()

        val result = doTask(task)

        self ! GoNextTask(
          previousTask = Task(id = task.id, func = task.func, result = Some(result)),
          nextTaskOpt = if(q.nonEmpty) Some(q.dequeue()) else None
        )
      }
      else{
        context.parent ! JobDone(None)
      }


    case GoNextTask(pTask: Task, nTask: Option[Task]) =>
      nTask match {
        case None =>
          // Tell the parent you are done
          context.parent ! JobDone(pTask.result)
        case Some(task) =>
          val pResult = pTask.result.getOrElse(BigDecimal(0))
          val nResult = pResult + doTask(task)

          log.info(s"${pTask.id}! + ${task.id}! = $nResult")

          self ! GoNextTask(
            previousTask = Task(id = task.id, func = task.func, result = Some(nResult)),
            nextTaskOpt = if(q.nonEmpty) Some(q.dequeue()) else None
          )
      }
  }

  /**
   * Do the Task Impl
   * @param task       Task
   * @return
   */
  def doTask(task: Task) = {
    val startTime = System.nanoTime()
    log.info(s"Doing Factorial ${task.id}!")
    val result = task.func(task.id, BigDecimal(1))
    log.info(s"Time Used = ${(System.nanoTime() - startTime)/1000000.0}ms for ${task.id}! = $result")

    result
  }

  // generic Go Next Task
  case class GoNextTask(previousTask: Task, nextTaskOpt: Option[Task])
}

