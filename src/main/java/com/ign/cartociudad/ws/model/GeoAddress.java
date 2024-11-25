package com.ign.cartociudad.ws.model;

import java.io.StringWriter;

import org.codehaus.jackson.annotate.JsonRawValue;
import org.geotools.geojson.geom.GeometryJSON;

import com.vividsolutions.jts.io.WKTReader;

/**
 * Clase para modelar la respuesta de direccion cuando se pide en formato geojson 
 * Actualmente solo para find y findJsonp
 * 
 * @author guadaltel
 *
 */
public class GeoAddress implements IAddress {
	
	/**
	 * Tipo
	 */
	private String type;
	
	/**
	 * Array de features
	 */
	private Feature[] features;
	
	/**
	 * Constructor de la clase
	 * 
	 * @param addr Address
	 */
	public GeoAddress(Address addr) {
		
		setType("FeatureCollection");
		
		String wkt = addr.getGeom();
		WKTReader reader = new WKTReader();
		com.vividsolutions.jts.geom.Geometry jtsGeom;
		
		Feature feature = new Feature();
		feature.setType("Feature");
		
		try {
			
			jtsGeom = reader.read(wkt);
			
			feature.setGeometry(convertGeometryToGeoJSON(jtsGeom));
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		feature.setProperties(new AddressProp(addr));
		setFeatures(new Feature[] {feature});
	}
	
	/**
	 * Convierte la Geometry en el GeoJSON (type y coordinates)
	 * @param geometry Geometria (com.vividsolutions.jts.geom.Geometry)
	 * @return Cadena con el type y coordinates
	 */
	private String convertGeometryToGeoJSON(com.vividsolutions.jts.geom.Geometry geometry) {
        
		GeometryJSON geoJSON = new GeometryJSON();
		
        try {
           
        	StringWriter writer = new StringWriter();
        	geoJSON.write(geometry, writer);
            return writer.toString();
            
        } catch (Exception e) {
          
        	e.printStackTrace();
        }
       
        return null;
    }
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the features
	 */
	public Feature[] getFeatures() {
		return features;
	}

	/**
	 * @param features the features to set
	 */
	public void setFeatures(Feature[] features) {
		this.features = features;
	}

	/**
	 * Inner class para modelar la feature
	 * 
	 * @author guadaltel
	 *
	 */
	class Feature {
		
		/**
		 * Tipo
		 */
		private String type;
		
		/**
		 * Geometria
		 * Importante la anotacion para poder devolver la cadena con el geoJSON directamente
		 */
		@JsonRawValue
		private String geometry;
		
		/**
		 * Propiedades
		 */
		private AddressProp properties;
		
		/**
		 * Constructor
		 */
		public Feature() {
			
		}
		
		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		/**
		 * @param type the type to set
		 */
		public void setType(String type) {
			this.type = type;
		}

		/**
		 * @return the geometry
		 */
		public String getGeometry() {
			return geometry;
		}

		/**
		 * @param geometry the geometry to set
		 */
		public void setGeometry(String geometry) {
			this.geometry = geometry;
		}

		/**
		 * @return the properties
		 */
		public AddressProp getProperties() {
			return properties;
		}

		/**
		 * @param properties the properties to set
		 */
		public void setProperties(AddressProp properties) {
			this.properties = properties;
		}
	}
	
	/**
	 * Inner class para modelar el address como parte del atributo properties
	 * 
	 * @author guadaltel
	 *
	 */
	class AddressProp {
		
		private String id;
		private String province;
		private String provinceCode;
		private String comunidadAutonoma;
		private String comunidadAutonomaCode;
		private String muni;
		private String muniCode;
		private String type;
		private String address;
		private String postalCode;
		private String poblacion;
		private String tip_via;
		private double lat;
		private double lng;
		private Integer portalNumber;
		private Boolean noNumber;
		private String refCatastral;
		private String countrycode;
		private String stateMsg;
		private String extension;
		private int state = 0;
		
		/**
		 * Constructor de la clase para modelar las propiedades
		 * @param addr
		 */
		AddressProp(Address addr) {
			
			setId(addr.getId());
			setType(addr.getType());
			setLat(addr.getLat());
			setLng(addr.getLng());
			setAddress(addr.getAddress());
			setTip_via(addr.getTip_via());
			
			if (addr.getPortalNumber() != null) {
				
				setPortalNumber(addr.getPortalNumber());
			}
			
			setExtension(addr.getExtension());
			setPostalCode(addr.getPostalCode());
			setMuni(addr.getMuni());
			setProvince(addr.getProvince());
			setComunidadAutonoma(addr.getComunidadAutonoma());
			setCountrycode(addr.getCountryCode());
			setRefCatastral(addr.getRefCatastral());
			setState(addr.getState());
			setStateMsg(addr.getStateMsg());
			setMuniCode(addr.getMuniCode());
			setProvinceCode(addr.getProvinceCode());
			setComunidadAutonomaCode(addr.getComunidadAutonomaCode());
			setNoNumber(addr.getNoNumber());
		}

		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}

		/**
		 * @param id the id to set
		 */
		public void setId(String id) {
			this.id = id;
		}

		/**
		 * @return the province
		 */
		public String getProvince() {
			return province;
		}

		/**
		 * @param province the province to set
		 */
		public void setProvince(String province) {
			this.province = province;
		}

		/**
		 * @return the provinceCode
		 */
		public String getProvinceCode() {
			return provinceCode;
		}

		/**
		 * @param provinceCode the provinceCode to set
		 */
		public void setProvinceCode(String provinceCode) {
			this.provinceCode = provinceCode;
		}

		/**
		 * @return the comunidadAutonoma
		 */
		public String getComunidadAutonoma() {
			return comunidadAutonoma;
		}

		/**
		 * @param comunidadAutonoma the comunidadAutonoma to set
		 */
		public void setComunidadAutonoma(String comunidadAutonoma) {
			this.comunidadAutonoma = comunidadAutonoma;
		}

		/**
		 * @return the comunidadAutonomaCode
		 */
		public String getComunidadAutonomaCode() {
			return comunidadAutonomaCode;
		}

		/**
		 * @param comunidadAutonomaCode the comunidadAutonomaCode to set
		 */
		public void setComunidadAutonomaCode(String comunidadAutonomaCode) {
			this.comunidadAutonomaCode = comunidadAutonomaCode;
		}

		/**
		 * @return the muni
		 */
		public String getMuni() {
			return muni;
		}

		/**
		 * @param muni the muni to set
		 */
		public void setMuni(String muni) {
			this.muni = muni;
		}

		/**
		 * @return the muniCode
		 */
		public String getMuniCode() {
			return muniCode;
		}

		/**
		 * @param muniCode the muniCode to set
		 */
		public void setMuniCode(String muniCode) {
			this.muniCode = muniCode;
		}

		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		/**
		 * @param type the type to set
		 */
		public void setType(String type) {
			this.type = type;
		}

		/**
		 * @return the address
		 */
		public String getAddress() {
			return address;
		}

		/**
		 * @param address the address to set
		 */
		public void setAddress(String address) {
			this.address = address;
		}

		/**
		 * @return the postalCode
		 */
		public String getPostalCode() {
			return postalCode;
		}

		/**
		 * @param postalCode the postalCode to set
		 */
		public void setPostalCode(String postalCode) {
			this.postalCode = postalCode;
		}

		/**
		 * @return the poblacion
		 */
		public String getPoblacion() {
			return poblacion;
		}

		/**
		 * @param poblacion the poblacion to set
		 */
		public void setPoblacion(String poblacion) {
			this.poblacion = poblacion;
		}

		/**
		 * @return the tip_via
		 */
		public String getTip_via() {
			return tip_via;
		}

		/**
		 * @param tip_via the tip_via to set
		 */
		public void setTip_via(String tip_via) {
			this.tip_via = tip_via;
		}

		/**
		 * @return the lat
		 */
		public double getLat() {
			return lat;
		}

		/**
		 * @param lat the lat to set
		 */
		public void setLat(double lat) {
			this.lat = lat;
		}

		/**
		 * @return the lng
		 */
		public double getLng() {
			return lng;
		}

		/**
		 * @param lng the lng to set
		 */
		public void setLng(double lng) {
			this.lng = lng;
		}

		/**
		 * @return the portalNumber
		 */
		public Integer getPortalNumber() {
			return portalNumber;
		}

		/**
		 * @param portalNumber the portalNumber to set
		 */
		public void setPortalNumber(Integer portalNumber) {
			this.portalNumber = portalNumber;
		}

		/**
		 * @return the noNumber
		 */
		public Boolean getNoNumber() {
			return noNumber;
		}

		/**
		 * @param noNumber the noNumber to set
		 */
		public void setNoNumber(Boolean noNumber) {
			this.noNumber = noNumber;
		}

		/**
		 * @return the refCatastral
		 */
		public String getRefCatastral() {
			return refCatastral;
		}

		/**
		 * @param refCatastral the refCatastral to set
		 */
		public void setRefCatastral(String refCatastral) {
			this.refCatastral = refCatastral;
		}

		/**
		 * @return the countrycode
		 */
		public String getCountrycode() {
			return countrycode;
		}

		/**
		 * @param countrycode the countrycode to set
		 */
		public void setCountrycode(String countrycode) {
			this.countrycode = countrycode;
		}

		/**
		 * @return the stateMsg
		 */
		public String getStateMsg() {
			return stateMsg;
		}

		/**
		 * @param stateMsg the stateMsg to set
		 */
		public void setStateMsg(String stateMsg) {
			this.stateMsg = stateMsg;
		}

		/**
		 * @return the extension
		 */
		public String getExtension() {
			return extension;
		}

		/**
		 * @param extension the extension to set
		 */
		public void setExtension(String extension) {
			this.extension = extension;
		}

		/**
		 * @return the state
		 */
		public int getState() {
			return state;
		}

		/**
		 * @param state the state to set
		 */
		public void setState(int state) {
			this.state = state;
		}

	}
}