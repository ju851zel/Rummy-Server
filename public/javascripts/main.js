document.getElementById("btn-undo").addEventListener("click", undo);
document.getElementById("btn-confirmName").addEventListener("click", addPlayer);
document.getElementById("btn-finishName").addEventListener("click", finishNameInput);


function addPlayer() {
    let name = document.getElementById("input-name").value;
    return window.location.href = "http://localhost:9000/addPlayer/" + name;
}

function finishNameInput() {
    return window.location.href = "http://localhost:9000/finishNameInput"
}

function undo() {
    console.log("Button undo pressed")
}

function newsDiv() {
    console.log("Test");
    document.getElementById("newsDiv").setAttribute("class", "shadow-lg");
}




$(document).ready(function() {
    console.log('The DOM is ready!');
});