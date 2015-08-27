
import akka.actor.{ActorSystem, Props}

/**
 * Created by Gary Lo on 27/8/15.
 */
object Main {

  def main(args: Array[String]) = {
    val taskSize = scala.io.StdIn.readLine(s"Enter Task Size:")
    val numOfWorker = scala.io.StdIn.readLine(s"Enter Number of worker:")

    val system = ActorSystem("actorSystem")
    val master = system.actorOf(Props[TaskMaster])

    master ! JobDescriptions(taskSize = taskSize.toInt, numOfWorker = numOfWorker.toInt)
  }
}

