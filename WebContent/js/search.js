// var nominatim = "http://nominatim.openstreetmap.org/search";
var geocodUrl =  "/geocoder/api/geocoder/";
var items = [];
var selectedItem;

$(document).ready(function() {
	
	var showMap = function(data) {
		console.log("Abrir mapa con address: " + data);
		var url = 'http://' + window.location.host + '/geocoder/index.html?centerTo='+
			data.lat + ","+ data.lng;
		console.log("Abrir mapa con address: " + data + " url=" + url);
		window.open(url);
	};
	
	var do2 = function(data) {
		alert(2);
	};
	
	var autocomplete = $("#autocomplete");
	autocomplete.search({
		geocoderUrl : geocodUrl,
		callBack : showMap
	});
	var autocomplete2 = $("#autocomplete2");
	autocomplete2.search({
		geocoderUrl : geocodUrl,
		callBack : do2
	});

});
