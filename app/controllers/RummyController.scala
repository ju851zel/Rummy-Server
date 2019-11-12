package controllers

import de.htwg.se.rummy.Rummy
import de.htwg.se.rummy.controller.ControllerInterface
import de.htwg.se.rummy.controller.component.{AnswerState, ControllerState}
import de.htwg.se.rummy.model.deskComp.deskBaseImpl.TileInterface
import de.htwg.se.rummy.model.deskComp.deskBaseImpl.deskImpl.Tile
import javax.inject._
import play.api.mvc.{Action, _}

import scala.util.matching.Regex


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class RummyController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  var toMove: Option[String] = None
  val controller: ControllerInterface = Rummy.controller
  var tileToMove: Option[TileInterface] = None

  controller.add(() => {})

  def game(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.game(controller))
  }

  def createGame(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (controller.currentControllerState == ControllerState.MENU) {
      controller.createDesk(12)
    }
    Ok(views.html.game(controller))
  }

  def loadFile()(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (controller.currentControllerState == ControllerState.MENU) {
      controller.loadFile()
    }
    Ok(views.html.game(controller))
  }

  def finishNameInput(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (controller.currentControllerState == ControllerState.INSERTING_NAMES) {
      controller.nameInputFinished()
    }
    Ok(views.html.game(controller))
  }

  def undo(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (controller.currentControllerState == ControllerState.INSERTING_NAMES
      || controller.currentControllerState == ControllerState.P_TURN) {
      controller.undo()
    }
    Ok(views.html.game(controller))
  }


  def redo(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (controller.currentControllerState == ControllerState.INSERTING_NAMES
      || controller.currentControllerState == ControllerState.P_TURN) {
      controller.redo()
    }
    Ok(views.html.game(controller))
  }

  def addPlayer(name: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (controller.currentControllerState == ControllerState.INSERTING_NAMES) {
      controller.addPlayerAndInit(name.substring(4).trim, 12)
    }
    Ok(views.html.game(controller))
  }

  def switchToNextPlayer(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (controller.currentControllerState == ControllerState.NEXT_TYPE_N) {
      controller.switchToNextPlayer()
    }
    Ok(views.html.game(controller))
  }

  def storeFile(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (controller.currentControllerState == ControllerState.P_TURN) {
      controller.storeFile()
    }
    Ok(views.html.game(controller))
  }

  def rules(): Action[AnyContent] = Action {
    Ok(views.html.rules())
  }


  def laydown(tile: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (controller.currentControllerState == ControllerState.P_TURN) {
      controller.layDownTile(Tile.stringToTile(tile))
    }
    Ok(views.html.game(controller))
  }

  def moveTile(tile: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (controller.currentControllerState == ControllerState.P_TURN) {
      if (tileToMove.isDefined) {
        if (tileToMove.get.equals(Tile.stringToTile(tile))) {
          tileToMove = None
        } else {
          controller.moveTile(Tile.stringToTile(tile), tileToMove.get)
        }
      } else {
        tileToMove = Some(Tile.stringToTile(tile))
      }
    }
    Ok(views.html.game(controller))
  }

  def finishTurn(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (controller.currentControllerState == ControllerState.P_TURN) {
      controller.userFinishedPlay()
    }
    Ok(views.html.game(controller))
  }


}
