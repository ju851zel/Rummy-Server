document.getElementById("btn-undo").addEventListener("click", undo);
document.getElementById("newsDiv").addEventListener("click", newsDiv);



function undo(){
    console.log("Button undo pressed")
}

function newsDiv() {
    console.log("Test");
    document.getElementById("newsDiv").setAttribute("class", "shadow-lg");
}

