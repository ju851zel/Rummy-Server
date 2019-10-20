package controllers

import de.htwg.se.rummy.Rummy
import de.htwg.se.rummy.controller.ControllerInterface
import de.htwg.se.rummy.controller.component.ControllerState
import de.htwg.se.rummy.util.Observer
import javax.inject._
import play.api.mvc.{Action, _}

import scala.util.matching.Regex


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class RummyController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  val PlayerNamePattern: Regex = "name [A-Za-z]+".r
  val LayDownTilePattern: Regex = "(l [1-9][RBGY][01]|l 1[0123][RBGY][01])".r
  val MoveTilePattern: Regex = "(m [1-9][RBGY][01] t [1-9][RBGY][01]|m 1[0123][RBGY][01] t [1-9][RBYG][01]|m 1[0-3][RBGY][01] t 1[0-3][RBGY][01]|m [1-9][RBGY][01] t 1[0-3][RBYG][01])".r
  val elements = 12
  var toMove: Option[String] = None


  val controller: ControllerInterface = Rummy.controller
  controller.add(new Observer {
    override def update(): Unit = {
      rummyAsString = controller.currentStateAsString()
    }
  })
  var rummyAsString: String = ""

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def rummy(input: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (input.startsWith("m")) {
      if (toMove.isEmpty) {
        toMove = Some(input.replaceFirst("m", ""))
      } else {
        if (!toMove.get.equalsIgnoreCase(input.replaceFirst("m", ""))) {
          processInput("m " + toMove.get + " t " + input.replaceFirst("m", ""))
        }
        toMove = None
      }
    } else {
      processInput(input)
    }
    Ok(views.html.game(controller))
  }


  def showEverything(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(controller.currentStateAsString())
  }

  def rules(): Action[AnyContent] = Action {
    Ok(views.html.rules())
  }


  private def processInput(input: String): Unit = {
    if (input.equals("q")) {
      System.exit(0)
    }
    controller.controllerState match {
      case ControllerState.MENU => handleMenuInput(input)
      case ControllerState.INSERTING_NAMES => handleNameInput(input)
      case ControllerState.P_TURN => handleOnTurn(input)
      case ControllerState.P_FINISHED => handleOnTurnFinished(input)
    }
  }

  private def handleNameInput(name: String): Unit = {
    name match {
      case "f" => controller.nameInputFinished()
      case "z" => controller.undo()
      case "r" => controller.redo()
      case PlayerNamePattern() => controller.addPlayerAndInit(name.substring(4).trim, elements)
      case _ => wrongInput()
    }
  }

  private def wrongInput() {
    rummyAsString = "Could not identify your input. Are you sure it was correct'?"
  }

  private def handleOnTurnFinished(input: String): Unit = input match {
    case "n" => controller.switchToNextPlayer()
    case "s" => controller.storeFile()
    case _ => wrongInput()
  }

  private def handleOnTurn(input: String): Unit = {
    input match {
      case LayDownTilePattern(c) => controller.layDownTile(c.split(" ").apply(1));
      case MoveTilePattern(c) => controller.moveTile(c.split(" t ").apply(0).split(" ").apply(1), c.split(" t ").apply(1));
      case "f" => controller.userFinishedPlay()
      case "z" => controller.undo()
      case "r" => controller.redo()
      case _ => wrongInput()
    }
  }

  private def handleMenuInput(input: String): Unit = {
    input match {
      case "c" => controller.createDesk(elements + 1)
      case "l" => controller.loadFile()
      case _ => wrongInput()
    }
  }

}
