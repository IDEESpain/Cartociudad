var userPoint;
var start;
var end;
var routeRequest;
var routingLayer;
var foundLoc;

LOCAL = true;
var host;
if (LOCAL)
    host = location.origin;
else {
	// cross origin:
	host = "http://graphhopper.gpsies.com";
}
var ghRequest = new GHRequest(host);
ghRequest.algoType = "fastest";

var iconTo = L.AwesomeMarkers.icon({
	icon : 'stop',
	color : 'darkred'
});

var iconFrom = L.AwesomeMarkers.icon({
	icon : 'play',
	color : 'green'
});

function resolveCoords(fromStr, toStr, doQuery) { 
    routingLayer.clearLayers();
    if(fromStr !== ghRequest.from.input || !ghRequest.from.isResolved())
        ghRequest.from = new GHInput(fromStr);
    
    if(toStr !== ghRequest.to.input || !ghRequest.to.isResolved())
        ghRequest.to = new GHInput(toStr);
    
    if(ghRequest.from.lat && ghRequest.to.lat) {
        // do not wait for resolve
        resolveFrom();
        resolveTo();
        routeLatLng(ghRequest, true);
    } else {
        // wait for resolve as we need the coord for routing     
        $.when(resolveFrom(), resolveTo()).done(function(fromArgs, toArgs) {                
            routeLatLng(ghRequest, doQuery);
        });    
    }
}


function setOrigin() {
	var marker = L.marker(userPoint, {
		icon : iconFrom,
		draggable : true
	}).addTo(routingLayer);
	hideMenu();
	start = userPoint;
	doRoute();
}

function setDestination() {
	var marker = L.marker(userPoint, {
		icon : iconTo,
		draggable : true
	}).addTo(routingLayer);
	hideMenu();
	end = userPoint;
	doRoute();
}

function reverseGeocode() {
	alert('userPoint ' + userPoint);
	hideMenu();
//	$.get();
}


function doRoute() {
	if (start && end) {
		ghRequest.from = start.lat + "," + start.lng;
		ghRequest.to = end.lat + "," + end.lng;
		routeLatLng(ghRequest, true);
	}
}

function overMenu(e) {
	e.style.fontWeight = "bold";
}
function outMenu(e) {
	e.style.fontWeight = "";
}

function showMenu(e) {
	userPoint = e.latlng;
	var popup = document.getElementById("popupmenu"); 
//	document.getElementById("popupmenu").style.top = e.containerPoint.y + "px";
//	document.getElementById("popupmenu").style.left = e.containerPoint.x + "px";
	popup.style.top = e.originalEvent.clientY + "px";
	popup.style.left = (e.originalEvent.clientX + popup.style.width) + "px";

	popup.style.display = "block";
}
function hideMenu() {
	document.getElementById("popupmenu").style.display = "none";
}

var onContextMenu = function(e) {
	showMenu(e);
};

var map = new L.Map('map', {
	zoom : 9,
	center : new L.latLng([ 41.575730, 13.002411 ]),
	zoomControl : false
});

map.attributionControl.setPrefix('');
//map.addLayer(new L.TileLayer(
//		'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png')); // base layer

var crs4258 = L.CRS.EPSG3857;
var cartociudad = L.tileLayer.wms(
		"http://www.ign.es/wms-c/ign-base", {
			layers : 'IGNBaseTodo',
			format : 'image/png',
			transparent : true,
			attribution : "Cartociudad",
			crs : crs4258
		});

map.addLayer(cartociudad);

routingLayer = L.geoJson().addTo(map);

//var jsonpurl = 'http://open.mapquestapi.com/nominatim/v1/search.php?q={s}'
//		+ '&format=json&osm_type=N&limit=100&addressdetails=0', jsonpName = 'json_callback';
var jsonpurl = '/geocoder/api/geocoder/candidatesJsonp?q={s}&limit=20';
var jsonpName = 'callback';
// third party jsonp service

function filterJSONCall(data) { // callback that remap fields name
	var json = {}, key, loc, disp = [];
	items = [];
	if (data) {
		if (data.length == undefined) {
			// Nos llega solo una dirección
			var val = data;
			items.push({
				value : val['address'],
				idx : 0,
				type : val['type'],
				tipoVia : val['tip_via'],
				id : val['id'],
				portal: val['portalNumber'],
				muni: val['muni'],
				provin:val['province']							
			});

		}
		else {
			for ( var i = 0; i < data.length; i++) {
				var val = data[i];
				items.push({
					value : val['address'],
					idx : i,
					type : val['type'],
					tipoVia : val['tip_via'],
					id : val['id'],
					portal: val['portalNumber'],
					muni: val['muni'],
					provin:val['province']
				});
			};
		}
	};
	for (var a in items) {
		disp = items[a].value;
		key = disp;
		json[key] = items[a];
	}
	return json;
}

var findSuccess = function(data) {		
	if (data) {
		console.log(data);
		var latlng = new L.LatLng(data.lat, data.lng);
		userPoint = latlng;
		return latlng;
		
	}

};
var geocoderUrl = 'http://' + window.location.host + '/geocoder/api/geocoder/';

var doFind = function(selectedItem) {
	url = geocoderUrl + 'findJsonp?q=' + selectedItem.value
			+ '&type=' + selectedItem.type + '&tip_via='
			+ selectedItem.tipoVia + '&id=' + selectedItem.id
			+ '&portal=' + selectedItem.portal;
	$.ajax({
		dataType : "jsonp",
		url : url,
		async : false,
		success : findSuccess,
	});
	return userPoint;
};


var mobileOptsOrigin = {
	url : jsonpurl,
	jsonpParam : jsonpName,
	filterJSON : filterJSONCall,
	findCallback : doFind,
	text : 'Origen...',
	autoType : false,
	tipAutoSubmit : true,
	autoCollapse : false,
	autoCollapseTime : 6000,
	animateLocation : true,
	markerLocation : true,
	delayType : 800
// with mobile device typing is more slow
};

var mobileOptsDestination = {
	url : jsonpurl,
	jsonpParam : jsonpName,
	filterJSON : filterJSONCall,
	findCallback : doFind,
	text : 'Destino...',
	autoType : false,
	tipAutoSubmit : true,
	autoCollapse : false,
	autoCollapseTime : 6000,
	animateLocation : true,
	markerLocation : true,
	delayType : 800
// with mobile device typing is more slow
};

var searchControlOrigin = new L.Control.Search(mobileOptsOrigin);

searchControlOrigin.on('search_locationfound', function(e) {

	var popup = "Origen:" + e.text, marker = this._markerLoc;
	marker.bindPopup(popup).openPopup();
	// Creates a red marker with the coffee icon
	var redMarker = L.AwesomeMarkers.icon({
		icon : 'play',
		color : 'green'
	});

	L.marker(e.latlng, {
		icon : redMarker
	}).addTo(map);
	start = e.latlng;
	doRoute();
});

var searchControlDestination = new L.Control.Search(mobileOptsDestination);

searchControlDestination.on('search_locationfound', function(e) {

	var popup = "Destino: " + e.text, marker = this._markerLoc;
	marker.bindPopup(popup).openPopup();
	var m = L.AwesomeMarkers.icon({
		icon : 'stop',
		color : 'red'
	});

	L.marker(e.latlng, {
		icon : m
	}).addTo(map);
	end = e.latlng;
	doRoute();
});

// view source of search.php for more details
var zoomControl = new L.Control.Zoom();
zoomControl.setPosition('bottomright');
map.addControl(zoomControl);

map.addControl(searchControlOrigin);
map.addControl(searchControlDestination);

map.on("contextmenu", onContextMenu);

searchControlOrigin.getContainer().style.zIndex = '10';
searchControlDestination.getContainer().style.zIndex = '5';

L.control.scale().addTo(map);
var bounds = {
	"minLon" : -16,
	"minLat" : 28,
	"maxLon" : 1,
	"maxLat" : 43
};

map.fitBounds(new L.LatLngBounds(new L.LatLng(bounds.minLat, bounds.minLon),
		new L.LatLng(bounds.maxLat, bounds.maxLon)));

function setFlag(latlng, isFrom) {
	if (latlng.lat == undefined) {
		var coords = latlng.split(",");
		latlng = new L.LatLng(coords[0], coords[1]);
	}
    if(latlng.lat) {
        var marker = L.marker([latlng.lat, latlng.lng], {
            icon: (isFrom? iconFrom : iconTo),
            draggable: true
        }).addTo(routingLayer).bindPopup(isFrom? "Start" : "End");                  
        marker.on('dragend', function(e) {
            routingLayer.clearLayers();
            // inconsistent leaflet API: event.target.getLatLng vs. mouseEvent.latlng?
            var latlng = e.target.getLatLng();
            if(isFrom) {
                ghRequest.from.setCoord(latlng.lat, latlng.lng);
                resolveFrom();                                
            } else {
                ghRequest.to.setCoord(latlng.lat, latlng.lng);
                resolveTo();
            }
            // do not wait for resolving and avoid zooming when dragging
            ghRequest.doZoom = false;
            routeLatLng(ghRequest, false);
        });
    } 
}


function routeLatLng(request, doQuery) {
    // doZoom should not show up in the URL but in the request object to avoid zooming for history change
    var doZoom = request.doZoom;
    request.doZoom = true;
                
    var from = request.from.toString();
    var to = request.to.toString();
    if(!from || !to) {
        descriptionDiv.html('<small>routing not possible. location(s) not found in the area</small>');
        return;
    }
    
    routingLayer.clearLayers();    
    setFlag(request.from, true);
    setFlag(request.to, false);    
        
    var urlForAPI = request.createURL("route?orig=" + from + "&dest=" + to + "&locale=es&vehicle=CAR");    
//    descriptionDiv.html('<img src="img/indicator.gif"/> Buscando ruta ...');
    request.doRequest(urlForAPI, function (json) {        
        if(json.info.errors) {
            var tmpErrors = json.info.errors;
            descriptionDiv.html(tmpErrors);
//            for (var m = 0; m < tmpErrors.length; m++) {
//                descriptionDiv.append("<div class='error'>" + tmpErrors[m] + "</div>");
//            }
            return;
        } else if(json.info.routeFound === false) {
            descriptionDiv.html('Ruta no encontrada! Disconnected areas?');
            return;
        }
        var geojsonFeature = {
            "type": "Feature",                   
            // "style": myStyle,                
            "geometry": json.route.data
        };
        
        routingLayer.addData(geojsonFeature);        
        if(json.bbox && doZoom) {
            var minLon = json.bbox[0];
            var minLat = json.bbox[1];
            var maxLon = json.bbox[2];
            var maxLat = json.bbox[3];
            var tmpB = new L.LatLngBounds(new L.LatLng(minLat, minLon), new L.LatLng(maxLat, maxLon));
            map.fitBounds(tmpB);
        }
        
        /* var tmpTime = round(json.time / 60, 1000);        
        if(tmpTime > 60) {
            if(tmpTime / 60 > 24)
                tmpTime = floor(tmpTime / 60 / 24, 1) + "d " + round(((tmpTime / 60) % 24), 1) + "h";
            else
                tmpTime = floor(tmpTime / 60, 1) + "h " + round(tmpTime % 60, 1) + "min";
        } else
            tmpTime = round(tmpTime % 60, 1) + "min";
        var dist = round(json.distance, 100);
        if(dist > 100)
            dist = round(dist, 1);
        descriptionDiv.html("<b>"+ dist + "km</b>"); // estimaci&oacute;n de tiempo: " + tmpTime);        

        var hiddenDiv = $("<div id='routeDetails'/>");
        hiddenDiv.hide();
        
        var toggly = $("<button style='font-size:9px; float: right; padding: 0px'>m&aacute;s info</button>");
        toggly.click(function() {
            hiddenDiv.toggle();
        });
        $("#info").prepend(toggly);
        var infoStr = "took: " + round(json.info.took, 1000) + " msecs"
        +", points: " + json.route.data.coordinates.length;
        if(json.route.instructions)
            infoStr += ", instructions: " + json.route.instructions.descriptions.length;
        hiddenDiv.append("<span>" + infoStr + "</span>");
        $("#info").append(hiddenDiv);
        
               
        if(json.instructions) {
            var instructionsElement = $("<table id='instructions'><colgroup>"
                + "<col width='10%'><col width='65%'><col width='25%'></colgroup>");
            $("#info").append(instructionsElement);        
            var descriptions = json.instructions.descriptions;
            var distances = json.instructions.distances;
            var indications = json.instructions.indications;
            for(var m = 0; m < descriptions.length; m++) {                
                var indi = indications[m];                
                if(m == 0)
                    indi = "marker-from";
                else if(indi == -3)
                    indi = "sharp_left";
                else if(indi == -2)
                    indi = "left";
                else if(indi == -1)
                    indi = "slight_left";
                else if(indi == 0)
                    indi = "continue";
                else if(indi == 1)
                    indi = "slight_right";
                else if(indi == 2)
                    indi = "right";
                else if(indi == 3)
                    indi = "sharp_right";
                else
                    throw "did not found indication " + indi;
                    
                addInstruction(instructionsElement, indi, descriptions[m], distances[m]);                
            }
            addInstruction(instructionsElement, "marker-to", "Llegada a Destino!", "");
        } */
    });
}
