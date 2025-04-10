/*
 * Leaflet Search Control 1.4.6
 * Copyright 2013, Stefano Cudini - stefano.cudini@gmail.com
 * Licensed under the MIT license.
 *
 * Demo:
 * http://labs.easyblog.it/maps/leaflet-search
 *
 * Repositories:
 * https://github.com/stefanocudini/leaflet-search
 * https://bitbucket.org/zakis_/leaflet-search
 *
 */
(function() {

L.Control.Search = L.Control.extend({
	includes: L.Mixin.Events,
	//
	//Managed Events:
	//	Event					Data passed			Description
	//	search_locationfound	{latlng, title}		fired after moved and show markerLocation
	//
	options: {
		url: '',					//url for search by ajax request, ex: "search.php?q={s}"
		jsonpParam: null,			//jsonp param name for search by jsonp service, ex: "callback"
		layer: null,				//layer where search markers(is a L.LayerGroup)		
		callData: null,				//function that fill _recordsCache, passed searching text by first param and callback in second
		findCallback: null,
		//TODO important! implements uniq option 'source' that recognizes source type: url,array,callback or layer		
		propertyName: 'title',		//property in marker.options(or feature.properties for vector layer) trough filter elements in layer
		propertyLoc: 'loc',			//field name for remapping location, using array: ['latname','lonname'] for select double fields(ex. ['lat','lon'] )
		//TODO implement sub property filter for propertyName,propertyLoc like this:  "prop.subprop.title"
		callTip: null,				//function that return row tip html node(or html string), receive text tooltip in first param
		filterJSON: null,			//callback for filtering data to _recordsCache
		minLength: 1,				//minimal text length for autocomplete
		initial: true,				//search elements only by initial text
		autoType: true,				//complete input with first suggested result and select this filled-in text.
		delayType: 400,				//delay while typing for show tooltip
		tooltipLimit: -1,			//limit max results to show in tooltip. -1 for no limit.
		tipAutoSubmit: true,  		//auto map panTo when click on tooltip
		autoResize: true,			//autoresize on input change
		autoCollapse: false,		//collapse search control after submit(on button or on tips if enabled tipAutoSubmit)
		//TODO add option for persist markerLoc after collapse!
		autoCollapseTime: 1200,		//delay for autoclosing alert and collapse after blur
		animateLocation: true,		//animate a circle over location found
		circleLocation: true,		//draw a circle in location found
		markerLocation: false,		//draw a marker in location found
		zoom: null,					//zoom after pan to location found, default: map.getZoom()
		text: 'Search...',			//placeholder value	
		textCancel: 'Cancel',		//title in cancel button
		textErr: 'Location not found',	//error message
		position: 'topleft',
		//TODO add option collapsed, like control.layers
	},
//FIXME option condition problem {autoCollapse: true, markerLocation: true} not show location
//FIXME option condition problem {autoCollapse:false }

	initialize: function(options) {
		L.Util.setOptions(this, options);
		this._inputMinSize = this.options.text ? this.options.text.length : 10;
		this._layer = this.options.layer || new L.LayerGroup();
		this._filterJSON = this.options.filterJSON || this._defaultFilterJSON;
		this._autoTypeTmp = this.options.autoType;	//useful for disable autoType temporarily in delete/backspace keydown
		this._recordsCache = {};	//key,value table! that store locations! format: key,latlng
	},

	onAdd: function (map) {
		this._map = map;
		this._container = L.DomUtil.create('div', 'leaflet-control-search');
		this._input = this._createInput(this.options.text, 'search-input');
		this._tooltip = this._createTooltip('search-tooltip');		
		this._cancel = this._createCancel(this.options.textCancel, 'search-cancel');
		this._button = this._createButton(this.options.text, 'search-button');
		this._alert = this._createAlert('search-alert');
		
		if(this.options.circleLocation || this.options.markerLocation)
			this._markerLoc = new SearchMarker([0,0], {marker: this.options.markerLocation});//see below
		
		this.setLayer( this._layer );
		this._input.style.maxWidth = L.DomUtil.getStyle(this._map._container,'width');
		//TODO resize _input on map resize
		map.on({
				'layeradd': this._onLayerAddRemove,
				'layerremove': this._onLayerAddRemove
			}, this);		
		return this._container;
	},

	onRemove: function(map) {
		this._recordsCache = {};
		map.off({
				'layeradd': this._onLayerAddRemove,
				'layerremove': this._onLayerAddRemove
			}, this);
	},

	_onLayerAddRemove: function(e) {
		//console.info('_onLayerAddRemove');
		if(e.layer instanceof L.LayerGroup)//without this, run setLayer also for each Markers!! to optimize!
			if( L.stamp(e.layer) != L.stamp(this._layer) )
				this.setLayer(e.layer);
	},
	
	setLayer: function(layer) {	//set search layer at runtime
		//this.options.layer = layer; //setting this, run only this._recordsFromLayer()
		this._layer = layer;
		this._layer.addTo(this._map);
		if(this._markerLoc)
			this._layer.addLayer(this._markerLoc);
		return this;
	},
	
	showAlert: function(text) {
		text = text || this.options.textErr;
		this._alert.style.display = 'block';
		this._alert.innerHTML = text;
		clearTimeout(this.timerAlert);
		var that = this;		
		this.timerAlert = setTimeout(function() {
			that.hideAlert();
		},this.options.autoCollapseTime);
		return this;
	},
	
	hideAlert: function() {
		this._alert.style.display = 'none';
		return this;
	},
		
	cancel: function() {
		this._input.value = '';
		this._handleKeypress({keyCode:8});//simulate backspace keypress
		this._input.size = this._inputMinSize;
		this._input.focus();
		this._cancel.style.display = 'none';
		return this;
	},
	
	expand: function() {		
		this._input.style.display = 'block';
		L.DomUtil.addClass(this._container, 'search-exp');	
		this._input.focus();
		this._map.on('dragstart', this.collapse, this);
		return this;	
	},

	collapse: function() {
		this._hideTooltip();
		this.cancel();
		this._alert.style.display = 'none';
		this._input.style.display = 'none';
		this._input.blur();
		this._cancel.style.display = 'none';
		L.DomUtil.removeClass(this._container, 'search-exp');		
		//this._markerLoc.hide();//maybe unuseful
		this._map.off('dragstart', this.collapse, this);
		return this;
	},
	
	collapseDelayed: function() {	//collapse after delay, used on_input blur
		var that = this;
		clearTimeout(this.timerCollapse);
		this.timerCollapse = setTimeout(function() {
			that.collapse();
		}, this.options.autoCollapseTime);
		return this;		
	},

	collapseDelayedStop: function() {
		clearTimeout(this.timerCollapse);
		return this;		
	},

////start DOM creations
	_createAlert: function(className) {
		var alert = L.DomUtil.create('div', className, this._container);
		alert.style.display = 'none';

		L.DomEvent
			.on(alert, 'click', L.DomEvent.stop, this)
			.on(alert, 'click', this.hideAlert, this);

		return alert;
	},

	_createInput: function (text, className) {
		var input = L.DomUtil.create('input', className, this._container);
		input.type = 'text';
		input.size = this._inputMinSize;
		input.value = '';
		input.autocomplete = 'off';
		input.placeholder = text;
		input.style.display = 'none';
		
		L.DomEvent
			.disableClickPropagation(input)
			.on(input, 'keyup', this._handleKeypress, this)
			.on(input, 'keydown', this._handleAutoresize, this)
			.on(input, 'blur', this.collapseDelayed, this)
			.on(input, 'focus', this.collapseDelayedStop, this);
		
		return input;
	},

	_createCancel: function (title, className) {
		var cancel = L.DomUtil.create('a', className, this._container);
		cancel.href = '#';
		cancel.title = title;
		cancel.style.display = 'none';
		cancel.innerHTML = "<span>&otimes;</span>";//imageless(see css)

		L.DomEvent
			.on(cancel, 'click', L.DomEvent.stop, this)
			.on(cancel, 'click', this.cancel, this);

		return cancel;
	},
	
	_createButton: function (title, className) {
		var button = L.DomUtil.create('a', className, this._container);
		button.href = '#';
		button.title = title;

		L.DomEvent
			.on(button, 'click', L.DomEvent.stop, this)
			.on(button, 'click', this._handleSubmit, this)			
			.on(button, 'focus', this.collapseDelayedStop, this)
			.on(button, 'blur', this.collapseDelayed, this);

		return button;
	},

	_createTooltip: function(className) {
		var tool = L.DomUtil.create('div', className, this._container);
		tool.style.display = 'none';

		var that = this;
		L.DomEvent
			.disableClickPropagation(tool)
			.on(tool, 'blur', this.collapseDelayed, this)
			.on(tool, 'mousewheel', function(e) {
				that.collapseDelayedStop();
				L.DomEvent.stopPropagation(e);//disable zoom map
			}, this)
			.on(tool, 'mouseover', function(e) {
				that.collapseDelayedStop();
			}, this);
		return tool;
	},

	_createTip: function(text, val) {//val is object in recordCache, usually is Latlng
		var tip;
		
		if(this.options.callTip)
		{
			tip = this.options.callTip(text,val); //custom tip node or html string
			if(typeof tip === 'string')
			{
				var tmpNode = L.DomUtil.create('div');
				tmpNode.innerHTML = tip;
				tip = tmpNode.firstChild;
			}
		}
		else
		{
			tip = L.DomUtil.create('a', '');
			tip.href = '#';
			tip.innerHTML = text;
		}
		
		L.DomUtil.addClass(tip, 'search-tip');
		tip._text = text; //value replaced in this._input and used by _autoType

		L.DomEvent
			.disableClickPropagation(tip)		
			.on(tip, 'click', L.DomEvent.stop, this)
			.on(tip, 'click', function(e) {
				this._input.value = text;
				this._handleAutoresize();
				this._input.focus();
				this._hideTooltip();	
				if(this.options.tipAutoSubmit)//go to location at once
					this._handleSubmit();
			}, this);

		return tip;
	},

//////end DOM creations

	_filterRecords: function(text) {	//Filter this._recordsCache case insensitive and much more..
	
		var regFilter = new RegExp("^[.]$|[\[\]|()*]",'g'),	//remove . * | ( ) ] [
			text = text.replace(regFilter,''),	  //sanitize text
			I = this.options.initial ? '^' : '',  //search only initial text
			regSearch = new RegExp(I + text,'i'),
			//TODO add option for case sesitive search, also showLocation
			frecords = {};

		for(var key in this._recordsCache)//use .filter or .map
			if( regSearch.test(key) )
				frecords[key]= this._recordsCache[key];
		
		return frecords;
	},

	showTooltip: function() {
		var filteredRecords,
			ntip = 0, newTip;
		
	//FIXME problem with jsonp/ajax when remote filter has different behavior of this._filterRecords
		if(this.options.layer)
			filteredRecords = this._filterRecords( this._input.value );
		else
			filteredRecords = this._recordsCache;
			
		this._tooltip.innerHTML = '';
		this._tooltip.currentSelection = -1;  //inizialized for _handleArrowSelect()

		for(var key in filteredRecords)//fill tooltip
		{
			if(++ntip == this.options.tooltipLimit) break;

			newTip = this._createTip(key, filteredRecords[key] );

			this._tooltip.appendChild(newTip);
		}
		
		if(ntip > 0)
		{
			this._tooltip.style.display = 'block';
			if(this._autoTypeTmp)
				this._autoType();
			this._autoTypeTmp = this.options.autoType;//reset default value
		}
		else
			this._hideTooltip();

		this._tooltip.scrollTop = 0;
		return ntip;
	},

	_hideTooltip: function() {
		this._tooltip.style.display = 'none';
		this._tooltip.innerHTML = '';
		return 0;
	},

	_defaultFilterJSON: function(json) {	//default callback for filter data
		var jsonret = {},
			propName = this.options.propertyName,
			propLoc = this.options.propertyLoc;

		//TODO patch! remove on Leaflet stable update include isArray() method
		if(!L.Util.isArray)
		{
			L.Util.isArray = function (obj) {
				return (Object.prototype.toString.call(obj) === '[object Array]');
			};
		}
		
		if( L.Util.isArray(propLoc) )
			for(var i in json)
				jsonret[ json[i][propName] ]= L.latLng( json[i][ propLoc[0] ], json[i][ propLoc[1] ] );
		else
			for(var i in json)
				jsonret[ json[i][propName] ]= L.latLng( json[i][ propLoc ] );
		//TODO verify json[i].hasOwnProperty(propName)
		//throw new Error("propertyName '"+propName+"' not found in JSON data");
		return jsonret;
	},

	_recordsFromJsonp: function(text, callAfter) {  //extract searched records from remote jsonp service
		//TODO remove script node after call run
		var that = this;
		L.Control.Search.callJsonp = function(data) {	//jsonp callback
			var fdata = that._filterJSON(data);//_filterJSON defined in inizialize...
			callAfter(fdata);
		}
		var script = L.DomUtil.create('script','search-jsonp', document.getElementsByTagName('body')[0] ),			
			url = L.Util.template(this.options.url+'&'+this.options.jsonpParam+'=L.Control.Search.callJsonp', {s: text}); //parsing url
			//rnd = '&_='+Math.floor(Math.random()*10000);
			//TODO add rnd param or randomize callback name! in recordsFromJsonp
		script.type = 'text/javascript';
		script.src = url;
		return this;
		//may be return {abort: function() { script.parentNode.removeChild(script); } };
	},

	_recordsFromAjax: function(text, callAfter) {	//Ajax request
		if (window.XMLHttpRequest === undefined) {
			window.XMLHttpRequest = function() {
				try { return new ActiveXObject("Microsoft.XMLHTTP.6.0"); }
				catch  (e1) {
					try { return new ActiveXObject("Microsoft.XMLHTTP.3.0"); }
					catch (e2) { throw new Error("XMLHttpRequest is not supported"); }
				}
			};
		}
		var request = new XMLHttpRequest(),
			url = L.Util.template(this.options.url, {s: text}), //parsing url
			//rnd = '&_='+Math.floor(Math.random()*10000);
			//TODO add rnd param or randomize callback name! in recordsFromAjax			
			response = {};
		
		request.open("GET", url);
		var that = this;
		request.onreadystatechange = function() {
		    if(request.readyState === 4 && request.status === 200) {
		    	response = window.JSON ? JSON.parse(request.responseText) : eval("("+ request.responseText + ")");
		    	var fdata = that._filterJSON(response);//_filterJSON defined in inizialize...
		        callAfter(fdata);
		    }
		};
		request.send();
		return this;   
	},	

	_recordsFromLayer: function() {	//return table: key,value from layer
		var retRecords = {},
			propName = this.options.propertyName;
		
		this._layer.eachLayer(function(layer) {

			if(layer instanceof SearchMarker) return;

			if(layer instanceof L.Marker)
			{
				if(layer.options.hasOwnProperty(propName))
					retRecords[ layer.options[propName] ] = layer.getLatLng();
				else
					console.log("propertyName '"+propName+"' not found in marker", layer);	
			}
			else if(layer instanceof L.Path)
			{
				if(layer.feature.properties.hasOwnProperty(propName))
					retRecords[ layer.feature.properties[propName] ] = layer.getBounds().getCenter();
				else
					console.log("propertyName '"+propName+"' not found in feature", layer);			
			}
			
		},this);
		
		return retRecords;
	},

	_autoType: function() {
		
		//TODO implements autype without selection(useful for mobile device)
		
		var start = this._input.value.length,
			firstRecord = this._tooltip.firstChild._text,
			end = firstRecord.length;

		if (firstRecord.indexOf(this._input.value) == 0) { // If prefix match
			this._input.value = firstRecord;
			this._handleAutoresize();

			if (this._input.createTextRange) {
				var selRange = this._input.createTextRange();
				selRange.collapse(true);
				selRange.moveStart('character', start);
				selRange.moveEnd('character', end);
				selRange.select();
			}
			else if(this._input.setSelectionRange) {
				this._input.setSelectionRange(start, end);
			}
			else if(this._input.selectionStart) {
				this._input.selectionStart = start;
				this._input.selectionEnd = end;
			}
		}
	},

	_hideAutoType: function() {	// deselect text:

		var sel;
		if ((sel = this._input.selection) && sel.empty) {
			sel.empty();
		}
		else {
			if (this._input.getSelection) {
				this._input.getSelection().removeAllRanges();
			}
			this._input.selectionStart = this._input.selectionEnd;
		}
	},
	
	_handleKeypress: function (e) {	//run _input keyup event
		
		switch(e.keyCode)
		{
			case 27: //Esc
				this.collapse();
			break;
			case 13: //Enter
				this._handleSubmit();	//do search
			break;
			case 38://Up
				this._handleArrowSelect(-1);
			break;
			case 40://Down
				this._handleArrowSelect(1);
			break;
			case 37://Left
			case 39://Right
			case 16://Shift
			case 17://Ctrl
			//case 32://Space
			break;
			case 8://backspace
			case 46://delete
				this._autoTypeTmp = false;//disable temporarily autoType
			default://All keys

				if(this._input.value.length)
					this._cancel.style.display = 'block';
				else
					this._cancel.style.display = 'none';

				if(this._input.value.length >= this.options.minLength)
				{
					var that = this;
					clearTimeout(this.timerKeypress);	//cancel last search request while type in				
					this.timerKeypress = setTimeout(function() {	//delay before request, for limit jsonp/ajax request

						that._fillRecordsCache();
					
					}, this.options.delayType);
				}
				else
					this._hideTooltip();
		}
	},
	
	_fillRecordsCache: function() {
//TODO important optimization!!! always append data in this._recordsCache
//  now _recordsCache content is emptied and replaced with new data founded
//  always appending data on _recordsCache give the possibility of caching ajax, jsonp and layersearch!
//
//TODO here insert function that search inputText FIRST in _recordsCache keys and if not find results.. 
//  run one of callbacks search(callData,jsonpUrl or options.layer) and run this.showTooltip
//
//TODO change structure of _recordsCache
//	like this: _recordsCache = {"text-key1": {loc:[lat,lng], ..other attributes.. }, {"text-key2": {loc:[lat,lng]}...}, ...}
//	in this mode every record can have a free structure of attributes, only 'loc' is required
	
		var inputText = this._input.value;
		
		L.DomUtil.addClass(this._container, 'search-load');

		if(this.options.callData)	//CUSTOM SEARCH CALLBACK(USUALLY FOR AJAX SEARCHING)
		{
			var that = this;
			this.options.callData(inputText, function(jsonraw) {

				that._recordsCache = that._filterJSON(jsonraw);

				that.showTooltip();

				L.DomUtil.removeClass(that._container, 'search-load');
			});
		}
		else if(this.options.url)	//JSONP/AJAX REQUEST
		{
			if(this.options.jsonpParam)
			{
				var that = this;
				this._recordsFromJsonp(inputText, function(data) {// is async request then it need callback
					that._recordsCache = data;
					that.showTooltip();
					L.DomUtil.removeClass(that._container, 'search-load');
				});
			}
			else
			{
				var that = this;
				this._recordsFromAjax(inputText, function(data) {// is async request then it need callback
					that._recordsCache = data;
					that.showTooltip();
					L.DomUtil.removeClass(that._container, 'search-load');
				});
			}
		}
		else if(this.options.layer)	//SEARCH ELEMENTS IN PRELOADED LAYER
		{
			this._recordsCache = this._recordsFromLayer();	//fill table key,value from markers into layer				
			this.showTooltip();
			L.DomUtil.removeClass(this._container, 'search-load');
		}
	},
	
	//FIXME _handleAutoresize Should resize max search box size when map is resized.
	_handleAutoresize: function() {	//autoresize this._input
	//TODO refact _handleAutoresize now is not accurate
		if(this.options.autoResize && (this._container.offsetWidth + 45 < this._map._container.offsetWidth))
			this._input.size = this._input.value.length<this._inputMinSize ? this._inputMinSize : this._input.value.length;
	},

	_handleArrowSelect: function(velocity) {
	
		var searchTips = this._tooltip.hasChildNodes() ? this._tooltip.childNodes : [];
			
		for (i=0; i<searchTips.length; i++)
			L.DomUtil.removeClass(searchTips[i], 'search-tip-select');
		
		if ((velocity == 1 ) && (this._tooltip.currentSelection >= (searchTips.length - 1))) {// If at end of list.
			L.DomUtil.addClass(searchTips[this._tooltip.currentSelection], 'search-tip-select');
		}
		else if ((velocity == -1 ) && (this._tooltip.currentSelection <= 0)) { // Going back up to the search box.
			this._tooltip.currentSelection = -1;
		}
		else if (this._tooltip.style.display != 'none') { // regular up/down
			this._tooltip.currentSelection += velocity;
			
			L.DomUtil.addClass(searchTips[this._tooltip.currentSelection], 'search-tip-select');
			
			this._input.value = searchTips[this._tooltip.currentSelection]._text;

			// scroll:
			var tipOffsetTop = searchTips[this._tooltip.currentSelection].offsetTop;
			
			if (tipOffsetTop + searchTips[this._tooltip.currentSelection].clientHeight >= this._tooltip.scrollTop + this._tooltip.clientHeight) {
				this._tooltip.scrollTop = tipOffsetTop - this._tooltip.clientHeight + searchTips[this._tooltip.currentSelection].clientHeight;
			}
			else if (tipOffsetTop <= this._tooltip.scrollTop) {
				this._tooltip.scrollTop = tipOffsetTop;
			}
		}
	},

	_handleSubmit: function() {	//button and tooltip click and enter submit

		this._hideAutoType();
		
		this.hideAlert();
		this._hideTooltip();

		
		if(this._input.style.display == 'none')	//on first click show _input only
			this.expand();
		else
		{
			if(this._input.value == '')	//hide _input only
				this.collapse();
			else
			{
				// var loc = this._getLocation(this._input.value);
				var item =  this._getLocation(this._input.value);
				var loc = this.options.findCallback(item);
				if(loc)
					{
						this.showLocation(loc, this._input.value);
					}
				else
					this.showAlert();
				//this.collapse();
				//FIXME if collapse in _handleSubmit hide _markerLoc!
			}
		}
	},

	_getLocation: function(key) {	//extract latlng from _recordsCache

		if( this._recordsCache.hasOwnProperty(key) )
			return this._recordsCache[key];//then after use .loc attribute
		else
			return false;
	},

	showLocation: function(latlng, title) {	//set location on map from _recordsCache
			
		if(this.options.zoom)
			this._map.setView(latlng, this.options.zoom);
		else
			this._map.panTo(latlng);

		if(this._markerLoc)
		{
			this._markerLoc.setLatLng(latlng);  //show circle/marker in location found
			this._markerLoc.setTitle(title);
			this._markerLoc.show();
			if(this.options.animateLocation)
				this._markerLoc.animate();
			//TODO showLocation: start animation after setView or panTo, maybe with map.on('moveend')...	
		}

		this.fire("search_locationfound", {latlng: latlng, text: title});
		
		//FIXME autoCollapse option hide this._markerLoc before that visualized!!
		if(this.options.autoCollapse)
			this.collapse();
		return this;
	}
});

var SearchMarker = L.Marker.extend({

	includes: L.Mixin.Events,
	
	options: {
		radius: 10,
		weight: 3,
		color: '#e03',
		stroke: true,
		fill: false,
		title: '',
		//TODO add custom icon!	
		marker: false	//show icon optional, show only circleLoc
	},
	
	initialize: function (latlng, options) {
		L.setOptions(this, options);
		L.Marker.prototype.initialize.call(this, latlng, options);
		this._circleLoc = new L.CircleMarker(latlng, this.options);
		//TODO add inner circle
	},

	onAdd: function (map) {
		L.Marker.prototype.onAdd.call(this, map);
		map.addLayer(this._circleLoc);
		this.hide();
	},

	onRemove: function (map) {
		L.Marker.prototype.onRemove.call(this, map);
		map.removeLayer(this._circleLoc);
	},	
	
	setLatLng: function (latlng) {
		L.Marker.prototype.setLatLng.call(this, latlng);
		this._circleLoc.setLatLng(latlng);
		return this;
	},
	
	setTitle: function(title) {
		title = title || '';
		this.options.title = title;
		if(this._icon)
			this._icon.title = title;
		return this;
	},

	show: function() {
		if(this.options.marker)
		{
			if(this._icon)
				this._icon.style.display = 'block';
			if(this._shadow)
				this._shadow.style.display = 'block';
			//this._bringToFront();
		}
		if(this._circleLoc)
		{
			this._circleLoc.setStyle({fill: this.options.fill, stroke: this.options.stroke});
			//this._circleLoc.bringToFront();
		}
		return this;
	},

	hide: function() {
		if(this._icon)
			this._icon.style.display = 'none';
		if(this._shadow)
			this._shadow.style.display = 'none';
		if(this._circleLoc)			
			this._circleLoc.setStyle({fill: false, stroke: false});
		return this;
	},

	animate: function() {
	//TODO refact animate() more smooth! like this: http://goo.gl/DDlRs
		var circle = this._circleLoc,
			tInt = 200,	//time interval
			ss = 10,	//frames
			mr = parseInt(circle._radius/ss),
			oldrad = this.options.radius,
			newrad = circle._radius * 2.5,
			acc = 0;

		circle._timerAnimLoc = setInterval(function() {
			acc += 0.5;
			mr += acc;	//adding acceleration
			newrad -= mr;
			
			circle.setRadius(newrad);

			if(newrad<oldrad)
			{
				clearInterval(circle._timerAnimLoc);
				circle.setRadius(oldrad);//reset radius
				//if(typeof afterAnimCall == 'function')
					//afterAnimCall();
					//TODO use create event 'animateEnd' in SearchMarker 
			}
		}, tInt);
		
		return this;
	 }
});

L.Map.addInitHook(function () {
    if (this.options.searchControl) {
        this.searchControl = L.control.search();
        this.addControl(this.searchControl);
    }
});

L.control.search = function (options) {
    return new L.Control.Search(options);
};

}).call(this);

