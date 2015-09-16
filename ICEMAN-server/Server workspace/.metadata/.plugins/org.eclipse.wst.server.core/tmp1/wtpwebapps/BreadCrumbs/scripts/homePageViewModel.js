function AttachDotListeners() {
	var dot1 = $("#dot1");
	SwellDotsOnMouseOver(dot1);
	 
	var dot2 = $("#dot2");
	SwellDotsOnMouseOver(dot2);
	 
	var dot3 = $("#dot3");
	SwellDotsOnMouseOver(dot3);
}

function SwellDotsOnMouseOver(dot)  {

	dot.mouseenter(function() {
		dot.animate({
			fontSize : "45px"
		}, 200);
	});
	
	dot.mouseout(function() {
		dot.animate( {
		fontSize : "30px"	
		}, 100);
	});
	
}

$( document ).ready(function() {
    console.log( "Setting up listeners!" );
    AttachDotListeners();
});