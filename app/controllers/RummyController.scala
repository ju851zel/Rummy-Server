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

  def state(): Action[AnyContent] = Action {
    val string: String = controller.currentControllerState match {
      case ControllerState.P_TURN =>
        "P_TURN"
      case ControllerState.KILL =>
        "KILL"
      case ControllerState.INSERTING_NAMES =>
        "INSERTING_NAMES"
      case ControllerState.NEXT_TYPE_N =>
        "NEXT_TYPE_N"
      case _ =>
        "MENU"
    }
    Ok(string)
  }

  def json(): Action[AnyContent] = Action {
    Ok(controller.toJson().toString())
  }

  def news(): Action[AnyContent] = Action {
    val string = controller.currentAnswerState match {
      case AnswerState.P_FINISHED =>
        "You are finished."
      case AnswerState.TABLE_NOT_CORRECT =>
        "Table looks not correct, please move tiles to match the rules or undo until it matches"
      case AnswerState.P_WON =>
        s"${controller.getCurrentPlayer.name} is the winner."
      case AnswerState.UNDO_TAKE_TILE =>
        "The tile has been put back in the bag"
      case AnswerState.BAG_IS_EMPTY =>
        "No more tiles in the bag. You must lay a tile down"
      case AnswerState.CANT_MOVE_THIS_TILE =>
        "You can not move this tile."
      case AnswerState.UNDO_MOVED_TILE =>
        "You undid the move of a specific tile."
      case AnswerState.MOVED_TILE =>
        "You did move a tile to another."
      case AnswerState.UNDO_LAY_DOWN_TILE =>
        "You undid the lay down you took the tile up."
      case AnswerState.ADDED_PLAYER =>
        "You added a player."
      case AnswerState.PUT_TILE_DOWN =>
        "You put down a tile"
      case AnswerState.PLAYER_REMOVED =>
        "You removed the player you inserted."
      case AnswerState.P_DOES_NOT_OWN_TILE =>
        "You dont have this tile on the board. Please select another one"
      case AnswerState.INSERTING_NAMES_FINISHED =>
        "You finished inserting the names."
      case AnswerState.STORED_FILE =>
        "You stored the game in a file"
      case AnswerState.NOT_ENOUGH_PLAYERS =>
        "Not enough Players. Add some more."
      case AnswerState.COULD_NOT_LOAD_FILE =>
        "Could not load the file. Created a new game instead."
      case AnswerState.LOADED_FILE =>
        "You loaded a file"
      case AnswerState.CREATED_DESK =>
        "You started the game by creating a desk"
      case AnswerState.UNDO_MOVED_TILE_NOT_DONE =>
        "Undo the move of the tile unnecessary. Nothing did happen."
      case AnswerState.P_FINISHED_UNDO =>
        "Its is again your turn. "
      case _ =>
        "Lets start, shall we?"
    }
    Ok(string)
  }

  def todo(): Action[AnyContent] = Action {
    val string: String = controller.currentControllerState match {
      case ControllerState.P_TURN =>
        s"${controller.getCurrentPlayer.name} it's your turn."
      case ControllerState.KILL =>
        "Finished"
      case ControllerState.INSERTING_NAMES =>
        "Insert a players name. (Min 2 players, Max 4) or finish with 'f'"
      case ControllerState.NEXT_TYPE_N =>
        "It is the next players turn"
      case _ =>
        "Click On Create Desk."
    }
    Ok(string)
  }


  def createGame(): Action[AnyContent] = Action {
    if (controller.currentControllerState == ControllerState.MENU) {
      controller.createDesk(12)
    }
    Ok(controller.toJson())
  }

  def loadFile()(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (controller.currentControllerState == ControllerState.MENU) {
      controller.loadFile()
    }
    Ok(controller.toJson())
  }

  def finishNameInput(): Action[AnyContent] = Action {
    if (controller.currentControllerState == ControllerState.INSERTING_NAMES) {
      controller.nameInputFinished()
    }
    Ok(controller.toJson())
  }

  def undo(): Action[AnyContent] = Action {
    if (controller.currentControllerState == ControllerState.INSERTING_NAMES
      || controller.currentControllerState == ControllerState.P_TURN) {
      controller.undo()
    }
    Ok(controller.toJson())
  }


  def redo(): Action[AnyContent] = Action {
    if (controller.currentControllerState == ControllerState.INSERTING_NAMES
      || controller.currentControllerState == ControllerState.P_TURN) {
      controller.redo()
    }
    Ok(controller.toJson())
  }

  def addPlayer(name: String): Action[AnyContent] = Action {
    if (controller.currentControllerState == ControllerState.INSERTING_NAMES) {
      controller.addPlayerAndInit(name, 12)
    }
    Ok(controller.toJson().toString())
  }

  def switchToNextPlayer(): Action[AnyContent] = Action {
    if (controller.currentControllerState == ControllerState.NEXT_TYPE_N) {
      controller.switchToNextPlayer()
    }
    Ok(controller.toJson())
  }

  def storeFile(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (controller.currentControllerState == ControllerState.P_TURN) {
      controller.storeFile()
    }
    Ok(controller.toJson())
  }

  def rules(): Action[AnyContent] = Action {
    Ok(views.html.rules())
  }


  def laydown(tile: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (controller.currentControllerState == ControllerState.P_TURN) {
      controller.layDownTile(Tile.stringToTile(tile))
    }
    Ok(controller.toJson())
  }

  def moveTile(tile: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (controller.currentControllerState == ControllerState.P_TURN) {
      if (tileToMove.isDefined) {
        if (tileToMove.get.equals(Tile.stringToTile(tile))) {
          tileToMove = None
        } else {
          controller.moveTile(tileToMove.get, Tile.stringToTile(tile))
          tileToMove = None
        }
      } else {
        tileToMove = Some(Tile.stringToTile(tile))
      }
    }
    Ok(controller.toJson())
  }

  def finishTurn(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (controller.currentControllerState == ControllerState.P_TURN) {
      controller.userFinishedPlay()
    }
    Ok(controller.toJson())
  }


}
