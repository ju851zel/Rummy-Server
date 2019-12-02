let game = {
    desk: {
        sets: [],
        players: []
    }
};
const defaultGame = game;
const socket = new WebSocket("ws://localhost:9000/socket");

function initWebSocket() {

    socket.onopen = ((socket, event) => {
        console.log("onOpen: A new Socket Connection was opened");
    });
    socket.onmessage = (event => {
        if (typeof event.data === "string") {
            let json = JSON.parse(event.data);
            update(json);
        }
    });

    socket.onerror = ((socket, event) => {
        console.log("onError: An error occurred");
    });
    socket.onclose = ((socket, closeEvent) => {
        console.log("onClose: THe socket was closed" + closeEvent);
    });

}

initWebSocket();


function createMenuButtons() {
    $("#interaction").empty();
    $("#interaction").append($('<button/>', {
        text: 'Create Desk',
        id: 'btnCreate',
        "class": "btn btn-primary",
        click: () => socket.send(JSON.stringify({type: "createGame"}))
    }));
}

function createInsertingNamesButtons() {
    let div = $('<div/>', {
        'class': 'form-group',
    });
    div.append($('<label/>', {
        for: 'input-name',
        text: 'Name'
    }));
    div.append($('<input/>', {
        id: 'input-name',
        placeholder: 'John Smith',
        type: 'text',
        class: 'form-control'
    }));
    let btnConfirmName = $('<button/>', {
        text: 'Confirm Name',
        id: 'btnConfirmName',
        "class": "btn btn-primary mr-2",
        click: () => {
            let name = $("#input-name").val();
            if (name === "") {
                updateNews("Please insert a name");
                return
            }
            socket.send(JSON.stringify({type: "addPlayersName", name: name}));
        }
    });

    let btnFinishInput = $('<button/>', {
        text: 'Begin',
        id: 'btnFinishName',
        "class": "btn btn-primary mr-2",
        click: () => socket.send(JSON.stringify({type: "nameFinish"}),)
    });

    $("#interaction").empty();
    $("#interaction").append(div);
    $("#interaction").append(btnConfirmName);
    $("#interaction").append(btnFinishInput);

}

function createTurnButtons() {
    let btnFinishTurn = $('<button/>', {
        text: 'Finished',
        id: 'btnFinishTurn',
        "class": "btn btn-primary",
        click: () => socket.send(JSON.stringify({type: "playerFinished"}))
    });

    $("#interaction").empty();
    $("#interaction").append(btnFinishTurn);
}

function createNextButtons() {
    let btnNextPlayer = $('<button/>', {
        text: 'Next',
        id: 'btnNextPlayer',
        "class": "btn btn-primary",
        click: () => socket.send(JSON.stringify({type: "nextPlayer"}))
    });
    $("#board").empty();
    $("#interaction").empty();
    $("#interaction").append(btnNextPlayer);
}

function initButtons() {
    $.ajax({
        method: "GET",
        url: "/state",
        dataType: "text",
        success: result => {
            switch (result) {
                case "MENU":
                    createMenuButtons();
                    break;
                case "INSERTING_NAMES":
                    createInsertingNamesButtons();
                    break;
                case "P_TURN":
                    createTurnButtons();
                    break;
                case "NEXT_TYPE_N":
                    createNextButtons();
                    break;
            }
        }
    });
}

function init() {
    $.ajax({
        method: "GET",
        url: "/json",
        dataType: "json",
        success: result => update(result)
    });
}

function initDesk() {
    $("#desk").empty();
    let row = 0;
    for (let sorted of game.desk.sets) {
        let div = $("<div/>", {
            class: "row my-4",
            id: 'deskrow' + row
        });
        for (let tile of sorted.struct) {
            let tileDiv = $('<div/>', {
                class: `mx-1 align-content-center text-center ${determineColorOfTile(tile)}`
            });
            tileDiv.append($('<h3/>', {
                text: tile.value,
                class: 'text-white'
            }));
            const tileId = `${tile.value + tile.color[0] + tile.ident}`;
            tileDiv.append($('<button/>', {
                id: tileId,
                class: 'btn btn-link',
                text: 'Move',
                click: () => socket.send(JSON.stringify({type: "moveTile", tile: tileId}))
            }));
            div.append(tileDiv)
        }
        row += 1;
        $("#desk").append(div)
    }
}

function initBoard() {
    $("#board").empty();
    let player = game.desk.players.find(player => player.state.toString() === "TURN");
    if (!player) {
        return
    }
    for (let tile of player.board) {
        let tileDiv = $('<div/>', {
            class: `mx-1 align-content-center text-center ${determineColorOfTile(tile)}`
        });
        tileDiv.append($('<h3/>', {
            text: tile.value,
            class: 'text-white'
        }));
        const tileId = `${tile.value + tile.color[0] + tile.ident}`;
        tileDiv.append($('<button/>', {
            id: tileId,
            class: 'btn btn-link',
            text: 'Down',
            click: () => socket.send(JSON.stringify({type: "laydownTile", tile: tileId}))
        }));
        $("#board").append(tileDiv)
    }
}

function determineColorOfTile(tile) {
    switch (tile.color) {
        case "YELLOW":
            return "tileY";
        case "RED":
            return "tileR";
        case "GREEN":
            return "tileG";
        case "BLUE":
            return "tileB";
    }
}

function update(result) {
    console.log(result);
    game = result;
    initButtons();
    updateTodo();
    updateNews();
    initDesk();
    initBoard();
}

function updateTodo() {
    $.ajax({
        method: "GET",
        url: "/todo",
        dataType: "text",
        success: result => {
            $("#todo").text(result);
        }
    });
}

function updateNews(string = "") {
    if (string !== "") {
        $("#news").text(string);
        return
    }
    $.ajax({
        method: "GET",
        url: "/news",
        dataType: "text",

        success: result => {
            $("#news").text(result);
        }
    });
}


function initUndo() {
    $("#btnUndo").click(() => socket.send(JSON.stringify({type: "undo"})))
}

function initRedo() {
    $("#btnRedo").click(() => socket.send(JSON.stringify({type: "redo"})))
}

$(document).ready(function () {
    console.log('The DOM is ready!');
    init();
    initUndo();
    initRedo();
});