/**
 *
 * Copyright (C) 2007-2015 gvSIG Association.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * For any additional information, do not hesitate to contact us
 * at info AT gvsig.com, or visit our website www.gvsig.com.
 */
package com.ign.cartociudad.ws.model;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Clase para recoger los datos de una direccion
 * 
 * @author guadaltel
 *
 */
@XmlRootElement
public class Address implements IAddress {
	
	private String id; // Dependiendo de si es municipio, vial o codpost, se referirá a una tabla u otra.
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
	private String geom; // geojson format. Maybe a point, line or polygon
	private String tip_via;
	private double lat;
	private double lng;
	private Integer portalNumber;
	private Boolean noNumber;
	private String ref_catastral;
	private String countrycode = "011";
	private String stateMsg = "";
	private String extension;
	private int state = 0;
	
	
	/**
	 * Constructor por defecto
	 */
	public Address() {
	
	}

	/**
	 * Constructor con parametros
	 * 
	 * @param id Identificador
	 */
	public Address(String id) {
		this();
		this.id = id;
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
	 * @return the geom
	 */
	public String getGeom() {
		return geom;
	}

	/**
	 * @param geom the geom to set
	 */
	public void setGeom(String geom) {
		this.geom = geom;
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
	 * @return the ref_catastral
	 */
	public String getRefCatastral() {
		return ref_catastral;
	}

	/**
	 * @param ref_catastral the ref_catastral to set
	 */
	public void setRefCatastral(String ref_catastral) {
		this.ref_catastral = ref_catastral;
	}

	/**
	 * @return the countrycode
	 */
	public String getCountryCode() {
		return countrycode;
	}

	/**
	 * @param countrycode the countrycode to set
	 */
	public void setCountryCode(String countrycode) {
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
	 * Convierte el objeto en GeoJSON
	 * 
	 * @return JSONObject
	 */
	public JSONObject toGeoJSON() {
		
		JSONObject props = new JSONObject();
		props.put("id", this.id);
		props.put("type", this.type);
		props.put("lat", this.lat);
		props.put("lng", this.lng);
		props.put("address", this.address);
		props.put("tip_via", this.tip_via);
		props.put("portalNumber", this.portalNumber);
		props.put("extension", this.extension);
		props.put("postalCode", this.postalCode);
		props.put("poblacion", this.poblacion);
		props.put("muni", this.muni);
		props.put("muniCode", this.muniCode);
		props.put("province", this.province);
		props.put("provinceCode", this.provinceCode);
		props.put("countrycode", this.countrycode);
		props.put("ref_catastral", this.ref_catastral);
		props.put("state", this.state);
		props.put("stateMsg", this.stateMsg);
        JSONObject point = new JSONObject();
        
        if (StringUtils.isNotBlank(this.geom)){
			try {
	        	String geomStr = getGeom();
	    		WKTReader reader = new WKTReader();
				Geometry jtsGeom = reader.read(geomStr);
				point.put("type", jtsGeom.getGeometryType());
				Coordinate[] coords = jtsGeom.getCoordinates();
				String coords_str = "";
				for(int i=0; i<coords.length; i++){
					if(i!= 0){
						coords_str = coords_str + ", ";
					}
					coords_str = coords_str + "[" + coords[i].x + ", " + coords[i].y + "]";
				}
	    		point.put("coordinates", "["+coords_str+"]");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
        } else {
	        point.put("type", "Point");
	        JSONArray coord = new JSONArray("["+getLng()+","+getLat()+"]");
	        point.put("coordinates", coord);
        }
        
        JSONObject feature = new JSONObject();
        feature.put("type", "Feature");
        feature.put("geometry", point);
        feature.put("properties", props);
        
        return feature;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
            return true;
        }
		
		if (!(obj instanceof Address)){
			return false;
		}
		
		Address addr2 = (Address) obj;
		return addr2.getId().equalsIgnoreCase(this.getId());
	}

}
