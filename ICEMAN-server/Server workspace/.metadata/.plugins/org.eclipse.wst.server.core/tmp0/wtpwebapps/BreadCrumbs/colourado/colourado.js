/**
 * 
 */

function AttachImageListeners() {
	var image = $(".item");
	image.onClick(function() {
				alert("heymate");
			});
}

$( document ).ready(function() {
    console.log( "Setting up listeners!" );
    AttachImageListeners();
});