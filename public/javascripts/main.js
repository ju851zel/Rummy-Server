let game = {
    desk: {
        sets: [],
        players: []
    }
};
const defaultGame = game;

function createMenuButtons() {
    $("#interaction").empty();
    $("#interaction").append($('<button/>', {
        text: 'Create Desk',
        id: 'btnCreate',
        "class": "btn btn-primary",
        click: () => $.ajax({
            method: "GET",
            url: "/game/create",
            dataType: "json",
            success: result => update(result)
        })
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
            $.ajax({
                method: "GET",
                url: "/game/name/add/" + name,
                dataType: "json",
                success: result => update(result)
            })
        }
    });

    let btnFinishInput = $('<button/>', {
        text: 'Begin',
        id: 'btnFinishName',
        "class": "btn btn-primary mr-2",
        click: () => {
            $.ajax({
                method: "GET",
                url: "/game/name/finish",
                dataType: "json",
                success: result => update(result)
            })
        }
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
        click: () => {
            $.ajax({
                method: "GET",
                url: "/game/player/finish",
                dataType: "json",
                success: () => update(defaultGame)
            })
        }
    });

    $("#interaction").empty();
    $("#interaction").append(btnFinishTurn);

}

function createNextButtons() {
    let btnNextPlayer = $('<button/>', {
        text: 'Next',
        id: 'btnNextPlayer',
        "class": "btn btn-primary",
        click: () => {
            $.ajax({
                method: "GET",
                url: "/game/player/next",
                dataType: "json",
                success: result => update(result)
            })
        }
    });
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
                click: () => {
                    $.ajax({
                        method: "GET",
                        url: "/game/player/move/" + tileId,
                        dataType: "json",
                        success: result => update(result)
                    })
                }
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
            click: () => {
                $.ajax({
                    method: "GET",
                    url: "/game/player/laydown/" + tileId,
                    dataType: "json",
                    success: result => update(result)
                })
            }
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
    $("#btnUndo").click(() => {
        $.ajax({
            method: "GET",
            url: "/game/undo",
            dataType: "json",
            success: result => {
                update(result);
            }
        });
    })
}

function initRedo() {
    $("#btnRedo").click(() => {
        $.ajax({
            method: "GET",
            url: "/game/redo",
            dataType: "json",
            success: result => {
                update(result);
            }
        });
    })
}

$(document).ready(function () {
    console.log('The DOM is ready!');
    init();
    initUndo();
    initRedo();
});