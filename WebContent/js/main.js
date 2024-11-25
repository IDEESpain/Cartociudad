// fixing cross domain support e.g in Opera
jQuery.support.cors = true;

// var nominatim = "http://open.mapquestapi.com/nominatim/v1/search.php";
// var nominatim_reverse =
// "http://open.mapquestapi.com/nominatim/v1/reverse.php";
// var nominatim_reverse = "http://nominatim.openstreetmap.org/reverse";
var routingLayer;
var serviceAreaLayer;
var map;
var browserTitle = "CartoCiudad";
var firstClickToRoute;

var instructions;
var geojsonFeature;

var userPoint;
var start;
var end;

var weighting = 'shortest'; 

var iconTo = L.AwesomeMarkers.icon({
	icon : 'stop',
	color : 'darkred'
});

var iconFrom = L.AwesomeMarkers.icon({
	//icon : 'play',
	color : 'orange'
});

// L.marker([51.941196,4.512291], {icon: redMarker}).addTo(map);
// var iconTo = L.icon({
// iconUrl: './img/marker-to.png',
// iconAnchor: [10, 16]
// });
// var iconFrom = L.icon({
// iconUrl: './img/marker-from.png',
// iconAnchor: [10, 16]
// });

var bounds = {};
LOCAL = true;
var host;
if (LOCAL)
	host = 'http://' + location.host;
else {
	// cross origin:
	host = "http://graphhopper.gpsies.com";
}
var ghRequest = new GHRequest(host);
ghRequest.algoType = "fastest";
// ghRequest.algorithm = "dijkstra";
var everPushedSomething = false;

$(document).ready(
		function(e) {
			var initialUrl = location.href;

			var History = window.History;
			if (History.enabled) {
				History.Adapter.bind(window, 'statechange', function() {
					// First important workaround:
					// Chrome and Safari always emit a popstate event on page
					// load, but Firefox doesnâ€™t
					// https://github.com/defunkt/jquery-pjax/issues/143#issuecomment-6194330
					var onloadPop = !everPushedSomething
							&& location.href == initialUrl;
					if (onloadPop)
						return;
					var state = History.getState();
					console.log(state);
					// initFromParams(parseUrl(state.url), true);
					initFromParams(state.data, true);
				});
			}
			initForm();
			ghRequest.getInfo(function(json) {
				// OK bounds
				// var tmp = json.bbox;
				bounds.initialized = true;
				// bounds.minLon = tmp[0];
				// bounds.minLat = tmp[1];
				// bounds.maxLon = tmp[2];
				// bounds.maxLat = tmp[3];
				bounds = {
					"minLon" : -2,
					"minLat" : 37.5,
					"maxLon" : 1,
					"maxLat" : 41
				};

				var vehiclesDiv = $("#vehicles");
				function createButton(text) {
					var button = $("<button/>")
					button.attr('id', text);
					button.html(text.charAt(0) + text.substr(1).toLowerCase());
					button.click(function() {
						ghRequest.vehicle = text;
//						// resolveFrom();
						resolveTo();
						routeLatLng(ghRequest, true);
					});
					return button;
				}

				if (json.supportedVehicles) {
					var vehicles = json.supportedVehicles.split(",");
					if (vehicles.length > 1)
						for ( var i = 0; i < vehicles.length; i++) {
							vehiclesDiv.append(createButton(vehicles[i]));
						}
				}
			}, function(err) {
				// error bounds
				console.log(err);
				$('#error').html('Servicios Web inactivos: ' + host);
			}).done(function() {
				var params = parseUrlWithHisto();
				initMap();
				// force same behaviour for all browsers: on page load no
				// history event will be fired
				// 
				// put into history, (first popstate is muted -> see above)
				// initFromParams(params, false);
				// force to true even if history.js is disabled due to cookie
				// disallow etc
				everPushedSomething = true;
				// execute query
				initFromParams(params, true);
			}).error(function() {
				bounds = {
					"minLon" : -17,
					"minLat" : 26,
					"maxLon" : 1,
					"maxLat" : 44
				};
				initMap();
			});
		});

function initFromParams(params, doQuery) {
	ghRequest.init(params);
	var fromAndTo = params.from && params.to;
	var routeNow = params.point && params.point.length == 2 || fromAndTo;
	if (params.centerTo) {
		ghRequest.centerTo = new GHInput(params.centerTo);
		var marker = L
				.marker([ ghRequest.centerTo.lat, ghRequest.centerTo.lng ]);
		marker.addTo(map);
		map.setView([ ghRequest.centerTo.lat, ghRequest.centerTo.lng ], 15);
	}
	if (routeNow) {
		if (fromAndTo)
			resolveCoords(params.from, params.to, doQuery);
		else
			resolveCoords(params.point[0], params.point[1], doQuery);
	}
}

function resolveCoords(fromStr, toStr, doQuery) {
	routingLayer.clearLayers();
	if (fromStr !== ghRequest.from.input || !ghRequest.from.isResolved())
		ghRequest.from = new GHInput(fromStr);

	if (toStr !== ghRequest.to.input || !ghRequest.to.isResolved())
		ghRequest.to = new GHInput(toStr);

	if (ghRequest.from.lat && ghRequest.to.lat) {
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

function initMap() {
	var mapDiv = $("#map");
	var width = $(window).width() - 300;
	if (width < 100)
		width = $(window).width() - 5;
	var height = $(window).height() - 5;
	mapDiv.width(width).height(height);
	if (height > 350)
		height -= 255;
	$("#info").css("max-height", height);
	console.log("init map at " + JSON.stringify(bounds));

	// mapquest provider
	var moreAttr = 'Data &copy; <a href="http://www.openstreetmap.org/">OSM</a>,'
			+ 'JS: <a href="http://leafletjs.com/">Leaflet</a>';
	var mapquest = L
			.tileLayer(
					'http://{s}.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png',
					{
						attribution : '<a href="http://open.mapquest.co.uk">MapQuest</a>,'
								+ moreAttr,
						subdomains : [ 'otile1', 'otile2', 'otile3', 'otile4' ]
					});

	var mapquestAerial = L
			.tileLayer(
					'http://{s}.mqcdn.com/tiles/1.0.0/sat/{z}/{x}/{y}.png',
					{
						attribution : '<a href="http://open.mapquest.co.uk">MapQuest</a>,'
								+ moreAttr,
						subdomains : [ 'otile1', 'otile2', 'otile3', 'otile4' ]
					});

	// var mapbox =
	// L.tileLayer('http://a.tiles.mapbox.com/v3/mapbox.world-bright/{z}/{x}/{y}.png',
	// {
	// attribution: '<a href="http://www.mapbox.com">MapBox</a>,' + moreAttr,
	// subdomains: ['a','b','c']
	// });

	var cloudmade = L
			.tileLayer(
					'http://{s}.tile.cloudmade.com/{key}/{styleId}/256/{z}/{x}/{y}.png',
					{
						attribution : '<a href="http://cloudmade.com">Cloudmade</a>,'
								+ moreAttr,
						key : '43b079df806c4e03b102055c4e1a8ba8',
						styleId : 997
					});

	var osm = L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
			{
				attribution : moreAttr
			});

	var crs4258 = L.CRS.EPSG3857;
	var CartoCiudad = L.tileLayer.wms(
			"https://www.ign.es/wms-inspire/ign-base", {
				layers : 'IGNBaseTodo',
				format : 'image/png',
				transparent : true,
				attribution : "CartoCiudad",
				crs : crs4258
			});
	
	var pnoa =  L.tileLayer.wms(
			"https://www.ign.es/wms-inspire/pnoa-ma", {
				layers : 'OI.OrthoimageCoverage',
				format : 'image/png',
				transparent : true,
				attribution : "PNOA",
				crs : crs4258
			});
	// default
	map = L.map('map', {
		// layers: [mapquest]
	    //  measureControl: true,
	    //  layersControl: true,
		layers : [ CartoCiudad ]
	});

	var baseMaps = {
		//"MapQuest" : mapquest,
		//"MapQuest Aerial" : mapquestAerial,
		//"Cloudmade" : cloudmade,
		//"OpenStreetMap" : osm,
		"CartoCiudad" : CartoCiudad,
		"PNOA": pnoa
	};
/*
	 L.control.coordinates({
	 position:"bottomleft", //optional default "bootomright"
	 decimals:2, //optional default 4
	 decimalSeperator:".", //optional default "."
	 labelTemplateLat:"Latitude: {y}", //optional default "Lat: {y}"
	 labelTemplateLng:"Longitude: {x}", //optional default "Lng: {x}"
	 enableUserInput:true, //optional default true
	 useDMS:false, //optional default false
	 useLatLngOrder: true //ordering of labels, default false-> lng-lat
	 }).addTo(map);
*/
	//var measureControl = L.control.measure({ primaryLengthUnit: 'meters', secondaryLengthUnit: 'kilometers' });
	//measureControl.addTo(map);

	// var overlays = {
	// "MapQuest Hybrid": mapquest
	// };

	// no layers for small browser windows
	if ($(window).width() > 400) {
		L.control.layers(baseMaps/* , overlays */).addTo(map);
	}

	//L.control.scale().addTo(map);

	map.fitBounds(new L.LatLngBounds(
			new L.LatLng(bounds.minLat, bounds.minLon), new L.LatLng(
					bounds.maxLat, bounds.maxLon)));

	map.attributionControl.setPrefix('');

	var myStyle = {
		"color" : 'black',
		"weight" : 2,
		"opacity" : 0.3
	};
	var geoJson = {
		"type" : "Feature",
		"geometry" : {
			"type" : "LineString",
			"coordinates" : [ [ bounds.minLon, bounds.minLat ],
					[ bounds.maxLon, bounds.minLat ],
					[ bounds.maxLon, bounds.maxLat ],
					[ bounds.minLon, bounds.maxLat ],
					[ bounds.minLon, bounds.minLat ] ]
		}
	};

	if (bounds.initialized)
		L.geoJson(geoJson, {
			"style" : myStyle
		}).addTo(map);

	routingLayer = L.geoJson().addTo(map);
	serviceAreaLayer = L.geoJson();
	serviceAreaLayer.addTo(map);
	serviceAreaLayer.on("contextmenu", onContextMenu);
	firstClickToRoute = true;
	function onMapClick_deprecated(e) {
		var latlng = e.latlng;
		if (firstClickToRoute) {
			// set start point
			clear();
			firstClickToRoute = false;
			ghRequest.from.setCoord(latlng.lat, latlng.lng);
			resolveFrom();
		} else {
			return;
			// set end point
			ghRequest.to.setCoord(latlng.lat, latlng.lng);
			resolveTo();
			// do not wait for resolving
			routeLatLng(ghRequest, true);
		}
	}
	function onMapClick(e) {
		var latlng = e.latlng;
		// set start point
		clear();
		firstClickToRoute = false;
		ghRequest.from.setCoord(latlng.lat, latlng.lng);
		resolveFrom();
	}
	map.on('click', onMapClick);
	//map.on("contextmenu", onContextMenu);
}

function showMenu(e) {
	userPoint = e.latlng;
	var popup = document.getElementById("popupmenu");
	// document.getElementById("popupmenu").style.top = e.containerPoint.y +
	// "px";
	// document.getElementById("popupmenu").style.left = e.containerPoint.x +
	// "px";
	popup.style.top = e.originalEvent.clientY + "px";
	popup.style.left = (e.originalEvent.clientX + popup.style.width) + "px";

	popup.style.display = "block";

}
function hideMenu() {
	document.getElementById("popupmenu").style.display = "none";
}

function reverseGeocode() {
	hideMenu();
	var reverseGeocodUrl = window.host
			+ '/geocoder/api/geocoder/reverseGeocode?lon=' + userPoint.lng
			+ '&lat=' + userPoint.lat; // + '&limit=10';
	// alert('userPoint ' + userPoint + ' ' + reverseGeocodUrl);
	$.get(reverseGeocodUrl, function(data) {
		if (data) {
			var aux = data.address;
			if (data.tip_via != undefined)
				aux = data.tip_via + ' ' + data.address;
			if ((data.portalNumber != undefined) && (data.portalNumber != 0))
				aux = aux + ' ' + data.portalNumber;
			if (data.postalCode != undefined)
				aux = aux + ', ' + data.postalCode;
			if (data.muni != undefined)
				aux = aux + ', ' + data.muni;
			aux = 'Coordenadas: \nLat: ' + round(userPoint.lat,100000)
					+ ' Long=' + round(userPoint.lng, 100000) + '\n' + aux;
			alert(aux);
		}
	});
}

function clear() {
	hideMenu();
	serviceAreaLayer.clearLayers();
	routingLayer.clearLayers();
	start = null;
	stop = null;
	var autocompleteFrom = $("#autocompleteFrom");
	autocompleteFrom[0].value = "";
	var autocompleteTo = $("#autocompleteTo");
	autocompleteTo[0].value = "";
	var info = $("#info");
	info.empty();

	firstClickToRoute = true;
}

function serviceArea() {
	hideMenu();
	var dist = prompt(
			'Introduzca la distancia m\xE1xima del \xE1rea de influencia (metros):',
			'10000');
	if (dist == null)
		return;

	var serviceAreaUrl = window.host + '/geocoder/api/serviceArea?lon='
			+ userPoint.lng + '&lat=' + userPoint.lat + '&dist=' + dist;
	// alert('userPoint ' + userPoint + ' ' + reverseGeocodUrl);
	serviceAreaLayer.clearLayers();
	var marker = L.marker([ userPoint.lat, userPoint.lng ]).addTo(
			serviceAreaLayer);

	$('.leaflet-container').css('cursor', 'wait');
	$.get(serviceAreaUrl, function(data) {
		if (data) {
			var geojsonFeature = {
				'type' : 'Feature',
				'geometry' : {
					'type' : 'Polygon',
					'coordinates' : data.coordinates
				}
			};

			serviceAreaLayer.addData(geojsonFeature);
			map.fitBounds(serviceAreaLayer.getBounds());
			var style = $('.leaflet-container').attr('style');
			$('.leaflet-container').attr('style',
					style + '; cursor:-moz-grab; cursor:-webkit-grab');

		}
	});
}

var onContextMenu = function(e) {
	showMenu(e);
};

function setOrigin() {
	var marker = L.marker(userPoint, {
		icon : iconFrom,
		draggable : true
	}).addTo(routingLayer);
	hideMenu();
//	var aux = $('#autocompleteTo');
//	aux[0].value = '';
	start = userPoint;
	ghRequest.from.setCoord(start.lat, start.lng);
	resolveFrom(start);
	doRoute();
}

function centerZoom() {
	hideMenu();
	map.panTo(userPoint);

}

function setDestination() {
	var marker = L.marker(userPoint, {
		icon : iconTo,
		draggable : true
	}).addTo(routingLayer);
	hideMenu();
	end = userPoint;
	ghRequest.to.setCoord(end.lat, end.lng);
	resolveTo(userPoint);
	doRoute();
}

function setOption(opt) {
	weighting = opt;
}

function doRoute() {
	if (start && end) {
		ghRequest.from = new GHInput(start.lat + "," + start.lng);
		ghRequest.to = new GHInput(end.lat + "," + end.lng);
		routeLatLng(ghRequest, true);
	}
}

function setFlag(latlng, isFrom) {
	if (latlng.lat) {
		var marker = L.marker([ latlng.lat, latlng.lng ], {
			icon : (isFrom ? iconFrom : iconTo),
			draggable : true
		}).addTo(routingLayer).bindPopup(isFrom ? "Start" : "End");
		if (isFrom)
			start = marker._latlng;
		else
			end = marker._latlng;
		marker.on('dragend', function(e) {
			routingLayer.clearLayers();
			// inconsistent leaflet API: event.target.getLatLng vs.
			// mouseEvent.latlng?
			var latlng = e.target.getLatLng();
			if (isFrom) {
				ghRequest.from.setCoord(latlng.lat, latlng.lng);
			} else {
				ghRequest.to.setCoord(latlng.lat, latlng.lng);
//				resolveTo();
			}
			// do not wait for resolving and avoid zooming when dragging
			ghRequest.doZoom = false;
			routeLatLng(ghRequest, false);
		});
	}
}

function resolveFrom() {
	setFlag(ghRequest.from, true);
	return resolve("From", ghRequest.from);
}

function resolveTo() {
	setFlag(ghRequest.to, false);
	return resolve("To", ghRequest.to);
}

function resolve(fromOrTo, point) {
	$("#" + fromOrTo + "Flag").hide();
	$("#" + fromOrTo + "Indicator").show();
	getInfoFromLocation(point);
	$("#" + fromOrTo + "Input").val(point.input);
	if (point.resolvedText) {
		var aux = $("#autocomplete" + fromOrTo);
		aux[0].value = point.resolvedText;
	}

	$("#" + fromOrTo + "Flag").show();
	$("#" + fromOrTo + "Indicator").hide();
	
	var map_zoom = map.getZoom();
	map.setView(new L.LatLng(point.lat, point.lng), map_zoom);
	
	return point;
	// .done(function() {
	// $("#" + fromOrTo + "Input").val(point.input);
	// if (point.resolvedText)
	// $("#" + fromOrTo + "Found").html(point.resolvedText);
	//
	// $("#" + fromOrTo + "Flag").show();
	// $("#" + fromOrTo + "Indicator").hide();
	// return point;
	// });
}

var getInfoTmpCounter = 0;
function getInfoFromLocation(locCoord) {
	// make sure that the demo route always works even if external geocoding is
	// down!
	getInfoTmpCounter++;
	var url;
	if (locCoord.lat && locCoord.lng) {
		url = window.host + '/geocoder/api/geocoder/reverseGeocode?lon='
				+ locCoord.lng + '&lat=' + locCoord.lat;
		// in every case overwrite name
		locCoord.resolvedText = "Error al buscar la coordenada.";

		$
				.ajax({
					url : url,
					type : "GET",
					timeout : 300,
					success : function(data) {
						// Se ejecuta cuando se ha recibido correctamente
						// los datos de la url
						if (data) {
							var aux = data.address;
							if (data.tip_via != undefined)
								aux = data.tip_via + ' ' + data.address;
							if ((data.portalNumber != undefined)
									&& (data.portalNumber != 0))
								aux = aux + ' ' + data.portalNumber;
							if (data.muni != undefined)
								aux = aux + ', ' + data.muni;
							locCoord.resolvedText = aux;
							return [ locCoord ];
						} else {
							locCoord.resolvedText = "No se ha encontrado la coordenada";
							return [ locCoord ];

						}

					},
					error : function() {
						// Se ejecuta cuando es imposible obtener
						// los datos de la url
					},
					async : false, // La petición es síncrona
					cache : false
				// No queremos usar la caché del navegador
				});
	}
}

function createCallback(errorFallback) {
	return function(err) {
		if (err.statusText && err.statusText != "OK")
			alert(errorFallback + ", " + err.statusText);
		else
			alert(errorFallback);

		console.log(errorFallback + " " + JSON.stringify(err));
	};
}

function focus(coord) {
	if (coord.lat && coord.lng) {
		routingLayer.clearLayers();
		map.setView(new L.LatLng(coord.lat, coord.lng), 11);
		setFlag(coord, true);
	}
}
function routeLatLng(request, doQuery) {
	// doZoom should not show up in the URL but in the request object to avoid
	// zooming for history change
	var doZoom = request.doZoom;
	request.doZoom = true;

	var urlForHistory = request.createFullURL();
	// not enabled e.g. if no cookies allowed (?)
	// if disabled we have to do the query and cannot rely on the statechange
	// history event
	if (!doQuery && History.enabled) {
		// 2. important workaround for encoding problems in history.js
		var params = parseUrl(urlForHistory);
		console.log(params);
		params.doZoom = doZoom;
		History.pushState(params, browserTitle, urlForHistory);
		return;
	}
	// BUT if this is the very first query and no history support skip the query
	// if(!History.enabled && !everPushedSomething) {
	// return;
	// }

	clickToRoute = true;
	$("#info").empty();
	$("#info").show();
	var descriptionDiv = $("<div/>");

	$("#info").append(descriptionDiv);

	var from = request.from.toString();
	var to = request.to.toString();
	if (!from || !to) {
		descriptionDiv
				.html('<small>routing not possible. location(s) not found in the area</small>');
		return;
	}

	routingLayer.clearLayers();
	setFlag(request.from, true);
	setFlag(request.to, false);

	$("#vehicles button").removeClass();
	$("button#" + request.vehicle.toUpperCase()).addClass("bold");
	var vehicle = 'CAR';
	if (weighting === 'shortest')
		vehicle = 'WALK';

	var urlForAPI = request.createURL("route?orig=" + from + "&dest=" + to
			+ "&locale=es&vehicle=" + vehicle);
	descriptionDiv.html('<img src="img/indicator.gif"/> Search Route ...');
	request
			.doRequest(
					urlForAPI,
					function(json) {
						if (json.info.errors) {
							var tmpErrors = json.info.errors;
							descriptionDiv.html(tmpErrors);
							// for (var m = 0; m < tmpErrors.length; m++) {
							// descriptionDiv.append("<div class='error'>" +
							// tmpErrors[m] + "</div>");
							// }
							return;
						} else if (json.info.routeFound == 'false') {
							descriptionDiv
									.html('Route not found! Disconnected areas?');
							return;
						}
						geojsonFeature = {
							"type" : "Feature",
							// "style": myStyle,
							"geometry" : json.route.data
						};
						// $("#info").empty();
						routingLayer.addData(geojsonFeature);
						if (json.bbox && doZoom) {
							var minLon = json.bbox[0];
							var minLat = json.bbox[1];
							var maxLon = json.bbox[2];
							var maxLat = json.bbox[3];
							var tmpB = new L.LatLngBounds(new L.LatLng(minLat,
									minLon), new L.LatLng(maxLat, maxLon));
							map.fitBounds(tmpB);
						}

						var tmpTime = round(json.time / 60, 1000);
						if (tmpTime > 60) {
							if (tmpTime / 60 > 24)
								tmpTime = floor(tmpTime / 60 / 24, 1) + "d "
										+ round(((tmpTime / 60) % 24), 1) + "h";
							else
								tmpTime = floor(tmpTime / 60, 1) + "h "
										+ round(tmpTime % 60, 1) + "min";
						} else
							tmpTime = round(tmpTime % 60, 1) + "min";
						var dist = round(json.distance, 1);
						var distTxt;
						if (dist > 1000)
							 distTxt = round(dist/1000, 100) + ' km';
						else
							distTxt = dist + ' m';
						descriptionDiv.html("<b>" + distTxt + "</b>"); // estimaci&oacute;n
						// de
						// tiempo:
						// " +
						// tmpTime);

						var hiddenDiv = $("<div id='routeDetails'/>");
						hiddenDiv.hide();

						var toggly = $("<button style='font-size:9px; float: right; padding: 0px'>m&aacute;s info</button>");
						toggly.click(function() {
							hiddenDiv.toggle();
						})
						$("#info").prepend(toggly);
						var infoStr = "took: " + round(json.info.took, 1000)
								+ " msecs" + ", points: "
								+ json.route.data.coordinates.length;
						if (json.route.instructions)
							infoStr += ", instructions: "
									+ json.route.instructions.descriptions.length;
						hiddenDiv.append("<span>" + infoStr + "</span>");
						$("#info").append(hiddenDiv);

						var exportLink = $("#exportLink a");
						exportLink.attr('href', urlForHistory);
						var startOsmLink = $("<a>start</a>");
						startOsmLink.attr("href",
								"http://www.openstreetmap.org/?zoom=14&mlat="
										+ request.from.lat + "&mlon="
										+ request.from.lng);
						var endOsmLink = $("<a>end</a>");
						endOsmLink.attr("href",
								"http://www.openstreetmap.org/?zoom=14&mlat="
										+ request.to.lat + "&mlon="
										+ request.to.lng);
						hiddenDiv.append("<br/><span>View on OSM: </span>")
								.append(startOsmLink).append(endOsmLink);

						var osrmLink = $("<a>OSRM</a>");
						osrmLink.attr("href",
								"http://map.project-osrm.org/?loc=" + from
										+ "&loc=" + to);
						hiddenDiv.append("<br/><span>Compare with: </span>");
						hiddenDiv.append(osrmLink);
						var googleLink = $("<a>Google</a> ");
						var addToGoogle = "";
						var addToBing = "";
						if (request.vehicle == "foot") {
							addToGoogle = "&dirflg=w";
							addToBing = "&mode=W";
						} else if (request.vehicle == "bike") {
							addToGoogle = "&dirflg=b";
							// ? addToBing = "&mode=B";
						}
						googleLink.attr("href",
								"http://maps.google.com/?q=from:" + from
										+ "+to:" + to + addToGoogle);
						hiddenDiv.append(googleLink);
						var bingLink = $("<a>Bing</a> ");
						bingLink.attr("href",
								"http://www.bing.com/maps/default.aspx?rtp=adr."
										+ from + "~adr." + to + addToBing);
						hiddenDiv.append(bingLink);

						if (host.indexOf("gpsies.com") > 0)
							hiddenDiv
									.append("<div id='hosting'>The routing API is hosted by <a href='http://gpsies.com'>Gpsies.com</a></div>");

						$('.defaulting').each(function(index, element) {
							$(element).css("color", "black");
						});

						if (json.instructionsData) {
							var instructionsElement = $("<table id='instructions'><colgroup>"
									+ "<col width='10%'><col width='65%'><col width='25%'></colgroup>");
							$("#info").append(instructionsElement);
							instructions = json.instructionsData.instruction;
							for ( var m = 0; m < instructions.length; m++) {
								var instruction = instructions[m];
								var indi = instruction.indication;
								if (m == 0)
									indi = "marker-from";
								else if (indi == -3)
									indi = "sharp_left";
								else if (indi == -2)
									indi = "left";
								else if (indi == -1)
									indi = "slight_left";
								else if (indi == 0)
									indi = "continue";
								else if (indi == 1)
									indi = "slight_right";
								else if (indi == 2)
									indi = "right";
								else if (indi == 3)
									indi = "sharp_right";
								if (indi != 4)
									addInstruction(instructionsElement, indi,
											instruction.description,
											instruction.distance, m);
							}
							addInstruction(instructionsElement, "marker-to",
									"Finish!", "");
						}
					});
}

function addInstruction(main, indi, title, distance, segmentIndex) {
	var indiPic = "<img class='instr_pic' src='../geocoder/img/" + indi
			+ ".png'/>";
	
	var str = "<td class='instr_title'>" + title + "</td>"
			+ " <td class='instr_distance_td'><span class='instr_distance'>"
			+ distance + "</span></td>";
	if (typeof segmentIndex !== "undefined") {
		str = "<td class='instr_title'><a href='javascript:zoomToSegment("
			+ segmentIndex + ");' >" + title + "</a></td>"
			+ " <td class='instr_distance_td'><span class='instr_distance'>"
			+ distance + "</span></td>";
	}
	if (indi !== "continue")
		str = "<td>" + indiPic + "</td>" + str + "</a>";
	else
		str = "<td/>" + str;
	var instructionDiv = $("<tr class='instruction'/>");
	instructionDiv.html(str);
	main.append(instructionDiv);
}

function getCenter(bounds) {
	var center = {
		lat : 0,
		lng : 0
	};
	if (bounds.initialized) {
		center.lat = (bounds.minLat + bounds.maxLat) / 2;
		center.lng = (bounds.minLon + bounds.maxLon) / 2;
	}
	return center;
}

function parseUrlWithHisto() {
	if (window.location.hash)
		return parseUrl(window.location.hash);

	return parseUrl(window.location.search);
}

function parseUrlAndRequest() {
	return parseUrl(window.location.search);
}
function parseUrl(query) {
	var index = query.indexOf('?');
	if (index >= 0)
		query = query.substring(index + 1);
	var res = {};
	var vars = query.split("&");
	for ( var i = 0; i < vars.length; i++) {
		var indexPos = vars[i].indexOf("=");
		if (indexPos < 0)
			continue;

		var key = vars[i].substring(0, indexPos);
		var value = vars[i].substring(indexPos + 1);
		value = decodeURIComponent(value.replace(/\+/g, ' '));

		if (typeof res[key] === "undefined")
			res[key] = value;
		else if (typeof res[key] === "string") {
			var arr = [ res[key], value ];
			res[key] = arr;
		} else
			res[key].push(value);

	}
	return res;
}

function initForm() {
	var geocodUrl = 'http://' + window.location.host
			+ '/geocoder/api/geocoder/';
	var setOrigin = function(data) {
		firstClickToRoute = true;
		var p = {
			latlng : new L.LatLng(data.lat, data.lng)
		};
		map.fireEvent('click', p);
	};

	var setDestination = function(data) {
		firstClickToRoute = false;
		var p = {
			latlng : new L.LatLng(data.lat, data.lng)
		};
		map.fireEvent('click', p);
	};

	var autocompleteFrom = $("#autocompleteFrom");
	autocompleteFrom.search({
		geocoderUrl : geocodUrl,
		callBack : setOrigin
	});
	var autocompleteTo = $("#autocompleteTo");
	autocompleteTo.search({
		geocoderUrl : geocodUrl,
		callBack : setDestination
	});

	$('#locationform').submit(function(e) {
		// no page reload
		e.preventDefault();
		var fromStr = $("#fromInput").val();
		var toStr = $("#toInput").val();
		if (toStr == "To" && fromStr == "From") {
			// TODO print warning
			return;
		}
		if (fromStr == "From") {
			// no special function
			return;
		}
		if (toStr == "To") {
			// lookup area
			ghRequest.from = new GHInput(fromStr);
			$.when(resolveFrom()).done(function() {
				focus(ghRequest.from);
			});
			return;
		}
		// route!
		resolveCoords(fromStr, toStr);
	});

	$('.defaulting').each(function(index, element) {
		var jqElement = $(element);
		var defaultValue = jqElement.attr('defaultValue');
		jqElement.focus(function() {
			var actualValue = jqElement.val();
			if (actualValue == defaultValue) {
				jqElement.val('');
				jqElement.css('color', 'black');
			}
		});
		jqElement.blur(function() {
			var actualValue = jqElement.val();
			if (!actualValue) {
				jqElement.val(defaultValue);
				jqElement.css('color', 'gray');
			}
		});
	});
}

function overMenu(e) {
	e.style.fontWeight = "bold";
}
function outMenu(e) {
	e.style.fontWeight = "";
}

function floor(val, precision) {
	if (!precision)
		precision = 1e6;
	return Math.floor(val * precision) / precision;
}
function round(val, precision) {
	if (!precision)
		precision = 1e6;
	return Math.round(val * precision) / precision;
}

function zoomToSegment(segmentIndex) {

	var instruction = instructions[segmentIndex];
	var minLon = instruction.bbox[0];
	var minLat = instruction.bbox[1];
	var maxLon = instruction.bbox[2];
	var maxLat = instruction.bbox[3];
	var bbox = new L.LatLngBounds(new L.LatLng(minLat,
			minLon), new L.LatLng(maxLat, maxLon));

	map.fitBounds(bbox);
}