;
(function($, window, document, undefined) {
	$.widget('IGN.search', $.ui.autocomplete,{
		options : {
			geocoderUrl : 'http://localhost:8080/geocoder/api/geocoder/',
			callBack : null
		},
		_create : function() {
			// create
			var geocoderUrl = this.options.geocoderUrl;
			var callBack = this.options.callBack;
		    var ac = $.ui.autocomplete.prototype;
		    ac._create.apply(this);
			var items = [];
			

			var success = function(data, response) {
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
							provin:val['province'],
							state:val['state']						
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
								provin:val['province'],
								state:val['state']
							});
						};
					}
				};
				isDataRecived=true;
				previous_items = items;
				response(items);
			};

			
			var findSuccess = function(data) {		
				if (data) {
					console.log(data);
					if (callBack != null)
						callBack(data);

				}

			};

			var doFind = function() {
				url = geocoderUrl + 'findJsonp?q=' + selectedItem.value
						+ '&type=' + selectedItem.type + '&tip_via='
						+ selectedItem.tipoVia + '&id=' + selectedItem.id
						+ '&portal=' + selectedItem.portal+ '&state=' + selectedItem.state;
				$.ajax({
					dataType : "jsonp",
					url : url,
					async : false,
					success : findSuccess,
				});
			};
			
			var isDataReceived = false;
			var candidate = null;
			var that = this;
			var previous_text=null;
			var previous_items= null;
			
			var doQuery = function(text, response) {
				// see https://trac.openstreetmap.org/ticket/4683 why limit=3 and
				// not 1
				if((text.trim() == previous_text ||
						text.trim() == previous_text+"-"
						)&& previous_items != null){
					response(previous_items);
					return;
				}
				previous_text=text.trim();
				
				url = geocoderUrl + 'candidatesJsonp?q=' + encodeURIComponent(text)
						+ '&autocancel=true'
						+'&limit=20&countrycodes=es';

				isDataRecived=false;
				
				 if(that.candidate != null) {
			        	that.candidate.abort();
			        }
				 
				that.candidate = $.ajax({
					dataType : 'jsonp',
					url : url,
					async : true,
					autocomplete: response,
					beforeSend : function()    {           
				        if(that.candidate != null) {
				        	that.candidate.abort();
				        }
				    },
					success : function(data) {
	                    success(data , this.autocomplete);
	                    }
				});

			};

		    var a = this.element.autocomplete({
				source : function(request, response) {
					var candidates = doQuery(request.term, response);
					response(candidates);
				},
				select : function(ev, item) {
					selectedItem = items[item.item.idx];
					doFind();
				}
			});
			a.keypress(function(ev, item) {
				var keyCode = $.ui.keyCode;
				if (ev.keyCode == keyCode.ENTER) {
					if (items.length == 0) {
						alert('No encontrada. Por favor, seleccione una opción de las ofertadas.');
						return;
					}
					ev.currentTarget.value = items[0].value;
					selectedItem = items[0];
					doFind();
				}
			});

		},

		_destroy : function() {
			// destroy
		},

		_setOption : function(key, value) {
			// In jQuery UI 1.9 and above, you use the _super method instead
			this._super('_setOption', key, value);
		},
		
		search : function() {
			// plugin functionality
//			console.log(this);

		}

		
	});

})(jQuery, window, document);
