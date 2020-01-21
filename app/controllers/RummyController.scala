package controllers

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import de.htwg.se.rummy.Rummy
import de.htwg.se.rummy.controller.ControllerInterface
import de.htwg.se.rummy.controller.component.{AnswerState, ControllerState}
import de.htwg.se.rummy.model.deskComp.deskBaseImpl.TileInterface
import de.htwg.se.rummy.model.deskComp.deskBaseImpl.deskImpl.Tile
import javax.inject._
import play.api.libs.json.Json
import play.api.libs.streams.ActorFlow
import play.api.mvc._


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class RummyController @Inject()(cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {
  var toMove: Option[String] = None
  var controller: ControllerInterface = Rummy.controller
  var tileToMove: Option[TileInterface] = None

  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok("")
  }

  def interaction(message: String): String = {
    val body = Json.parse(message)
    val type1 = (body \ "type").get.as[String]
    val result = type1 match {
      case "quit" => quitGame()
      case "createGame" => createGame()
      case "addPlayersName" => addPlayer((body \ "name").get.as[String])
      case "nameFinish" => finishNameInput()
      case "playerFinished" => finishTurn()
      case "nextPlayer" => switchToNextPlayer()
      case "undo" => undo()
      case "redo" => redo()
      case "json" => json()
      case "state" => json()
      case "moveTile" => moveTile((body \ "tile").get.as[String])
      case "laydownTile" => laydown((body \ "tile").get.as[String])
      case _ => json()
    }
    json()
  }

  def quitGame(): String = {
    controller = Rummy.controller
    json()
  }

  def state(): String = {
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
    string
  }

  def json(newz: String = null): String = {
    var obj = controller.toJson()
    obj = obj.+("state", Json.toJson(state()))
    obj = obj.+("todo", Json.toJson(todo()))
    if (newz != null) {
      obj = obj.+("news", Json.toJson(newz))
    } else {
      obj = obj.+("news", Json.toJson(news()))
    }
    println("JSON:\t" + obj.toString)
    obj.toString()
  }

  def news(): String = {
    println("Controller State: " + controller.currentAnswerState)
    val string = controller.currentAnswerState match {
      case AnswerState.P_FINISHED =>
        "Finished turn."
      case AnswerState.TABLE_NOT_CORRECT =>
        "Table looks not correct, please move tiles to match the rules or undo until it matches"
      case AnswerState.P_WON =>
        controller.getCurrentPlayer.name + " is the winner."
      case AnswerState.UNDO_TAKE_TILE =>
        "The tile has been put back in the bag"
      case AnswerState.BAG_IS_EMPTY =>
        "No more tiles in the bag. You must lay a tile down"
      case AnswerState.CANT_MOVE_THIS_TILE =>
        "Can not move this tile."
      case AnswerState.UNDO_MOVED_TILE =>
        "Undo move of a specific tile."
      case AnswerState.MOVED_TILE =>
        "Moved a tile to another."
      case AnswerState.UNDO_LAY_DOWN_TILE =>
        "Undo lay down."
      case AnswerState.ADDED_PLAYER =>
        "Added a player."
      case AnswerState.PUT_TILE_DOWN =>
        "Put down a tile"
      case AnswerState.PLAYER_REMOVED =>
        "Removed the player you inserted."
      case AnswerState.P_DOES_NOT_OWN_TILE =>
        "You dont have this tile on the board. Please select another one"
      case AnswerState.INSERTING_NAMES_FINISHED =>
        "Finished inserting the names."
      case AnswerState.STORED_FILE =>
        "The game was stored in a file"
      case AnswerState.NOT_ENOUGH_PLAYERS =>
        "Not enough Players. Add some more."
      case AnswerState.COULD_NOT_LOAD_FILE =>
        "Could not load the file. Created a new game instead."
      case AnswerState.LOADED_FILE =>
        "Loaded a saved game"
      case AnswerState.CREATED_DESK =>
        "Game started by creating a desk. Have fun."
      case AnswerState.UNDO_MOVED_TILE_NOT_DONE =>
        "Undo the move of the tile unnecessary. Nothing did happen."
      case AnswerState.P_FINISHED_UNDO =>
        "Its is again your turn."
      case AnswerState.TAKE_TILE =>
        "Automatically took a tile up."
      case AnswerState.ENOUGH_PLAYER =>
        "No more players possible. Start the game!"
      case AnswerState.NONE =>
       "Do your thing."
      case _ =>
        "ERROR 512231"
    }
    string
  }

  def todo(): String = {
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
    string
  }


  def createGame(): String = {
    if (controller.currentControllerState == ControllerState.MENU) {
      controller.createDesk(12)
    }
    json()
  }


  def addPlayer(name: String): String = {
    if (controller.currentControllerState == ControllerState.INSERTING_NAMES) {
      controller.addPlayerAndInit(name, 12)
    }
    json()
  }

  def loadFile(): String = {
    if (controller.currentControllerState == ControllerState.MENU) {
      controller.loadFile()
    }
    json()
  }

  def finishNameInput(): String = {
    if (controller.currentControllerState == ControllerState.INSERTING_NAMES) {
      controller.nameInputFinished()
    }
    json()
  }

  def undo(): Unit = {
    if (controller.currentControllerState == ControllerState.INSERTING_NAMES
      || controller.currentControllerState == ControllerState.P_TURN) {
      controller.undo()
    }
  }


  def redo(): Unit = {
    if (controller.currentControllerState == ControllerState.INSERTING_NAMES
      || controller.currentControllerState == ControllerState.P_TURN) {
      controller.redo()
    }
  }


  def switchToNextPlayer(): String = {
    if (controller.currentControllerState == ControllerState.NEXT_TYPE_N) {
      controller.switchToNextPlayer()
    }
    json()
  }

  def storeFile(): String = {
    if (controller.currentControllerState == ControllerState.P_TURN) {
      controller.storeFile()
    }
    json()
  }


  def laydown(tile: String): String = {
    if (controller.currentControllerState == ControllerState.P_TURN) {
      controller.layDownTile(Tile.stringToTile(tile))
    }
    json()
  }

  def moveTile(tile: String): String = {
    if (controller.currentControllerState == ControllerState.P_TURN) {
      if (tileToMove.isDefined) {
        if (tileToMove.get.equals(Tile.stringToTile(tile))) {
          tileToMove = None
          val news = "You unselected a tile to move"
          json(news)
        } else {
          controller.moveTile(tileToMove.get, Tile.stringToTile(tile))
          tileToMove = None
          val news = "You unselected a tile to move"
          json(news)
        }
      } else {
        tileToMove = Some(Tile.stringToTile(tile))
        val news = "You selected a tile to move"
        json(news)
      }
    }
    json()
  }

  def finishTurn(): String = {
    if (controller.currentControllerState == ControllerState.P_TURN) {
      controller.userFinishedPlay()
    }
    json()
  }

  def socket = WebSocket.accept[String, String] {
    request =>
      ActorFlow.actorRef {
        out =>
          println("Connect received")
          WebSocketActorFactory.create(out)
      }
  }

  class WebSocketActor(out: ActorRef) extends Actor {

    var block = false
    controller.add(() => {
      if (!block) {
        println("Called add of controller")
        sendJsonToClient()
      }
      block = false
    })
    sendJsonToClient()

    def receive: PartialFunction[Any, Unit] = {
      case msg: String =>
        println("Came in: " + msg)
        block = true
        val x = interaction(msg)
        println("Sending x: " + x)
        out ! x

    }

    def sendJsonToClient(): Unit = {
      println("Received event from Controller: " + controller.toJson.toString())
      out ! json()
      println("send to client")
    }
  }

  object WebSocketActorFactory {
    def create(out: ActorRef): Props = {
      Props(new WebSocketActor(out))
    }
  }


}
