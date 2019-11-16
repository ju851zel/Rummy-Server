let game = {
    sets: []
};


function undo() {
    console.log("Button undo pressed")
}

function createMenuButtons() {
    $("#interaction").empty();
    $("#interaction").append(
        '<button class="btn btn-primary" id="btnCreate">Create Desk</button>' +
        '<button class="btn btn-primary" id="btnLoad">Load file</button>'
    );
    $("#btnCreate").click(() => {
        $.ajax({
            method: "GET",
            url: "/game/create",
            dataType: "json",
            success: result => {
                game = result;
                console.log(result);
                update()
            }
        })
    })
}

function createInsertingNamesButtons() {
    $("#interaction").empty();
    $("#interaction").append(
        "<div class=\"form-group\">" +
        "    <label for=\"input-name\">Name</label>" +
        "    <input type=\"text\" class=\"form-control\" id=\"input-name\" placeholder=\"John\">" +
        "</div>" +
        "<button class=\"btn btn-primary\" id=\"btnConfirmName\">Confirm Name</button>" +
        "<button class=\"btn btn-primary\" id=\"btnFinishName\">FinishNameInput</button>"
    );
    $("#btnConfirmName").click(() => {
        let name = $("#input-name").val();
        if (name === "") {
            updateNews("Please insert a name");
            return
        }
        $.ajax({
            method: "GET",
            url: "/game/name/add/" + name,
            dataType: "json",
            success: result => {
                game = result;
                console.log(result);
                update()
            }
        })
    });
    $("#btnFinishName").click(() => {
        $.ajax({
            method: "GET",
            url: "/game/name/finish",
            dataType: "json",
            success: result => {
                game = result;
                console.log(result);
                update()
            }
        })
    })
}

function createTurnButtons() {
    $("#interaction").empty();
    $("#interaction").append(
        "<button class=\"btn btn-primary\" id=\"btnFinishTurn\">Finished Turn</button>"
    );
    $("#btnFinishTurn").click(() => {
        $.ajax({
            method: "GET",
            url: "/game/player/finish",
            dataType: "json",
            success: result => {
                game = result;
                $("#board").empty();
                update()
            }
        })
    })
}

function createNextButtons() {
    $("#interaction").empty();
    $("#interaction").append(
        "<button class=\"btn btn-primary\" id=\"btnNextPlayer\">Next Player</button>"
    );
    $("#btnNextPlayer").click(() => {
        $.ajax({
            method: "GET",
            url: "/game/player/next",
            dataType: "json",
            success: result => {
                game = result;
                update()
            }
        })
    });

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

        success: result => {
            game = result;
            console.log(result);
        }
    });
}

function initDesk() {
    $("#desk").empty();
    let row = 0;
    for (let sorted of game.desk.sets) {
        $("#desk").append("<div class=\"row my-4\" id=\"deskrow" + row + "\"></div>");
        for (let tile of sorted.struct) {
            $("#deskrow" + row).append(`
                <div class="mx-1 align-content-center text-center ${determineColorOfTile(tile)}">
                   <h3 class="text-white">${tile.value}</h3>
                   <button id="${tile.value + tile.color[0] + tile.ident}" class="btn btn-link">Move</button>
                </div>
            `)
        }
        row += 1;
    }
    $("#desk").one("click", (event) => $.ajax({
        method: "GET",
        url: "/game/player/move/" + event.target.id,
        dataType: "json",
        success: result => {
            game = result;
            update()
        }
    }));
}

function initBoard() {
    $("#board").empty();
    for (let tile of game.desk.players.find(player => player.state.toString() === "TURN").board) {
        $("#board").append(`
            <div class="mx-1 align-content-center text-center ${determineColorOfTile(tile)}">
                <h3 class="text-white">${tile.value}</h3>
               <button id="${tile.value + tile.color[0] + tile.ident}" class="btn btn-link">Move</button>
            </div>
        `)
    }
    $("#board").one("click", (event) => $.ajax({
        method: "GET",
        url: "/game/player/laydown/" + event.target.id,
        dataType: "json",
        success: result => {
            game = result;
            update()
        }
    }));

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

function update() {
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


$(document).ready(function () {
    console.log('The DOM is ready!');
    init();
    update()
});