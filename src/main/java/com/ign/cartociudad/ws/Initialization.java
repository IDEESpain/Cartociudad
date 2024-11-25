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
package com.ign.cartociudad.ws;

import javax.servlet.ServletContext;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

import com.sun.jersey.api.model.AbstractResourceModelContext;
import com.sun.jersey.api.model.AbstractResourceModelListener;

/**
 * Se inicializa la aplicacion
 * 
 * @author guadaltel
 *
 */
@Provider
public class Initialization implements AbstractResourceModelListener {
	
	/**
	 * Log
	 */
	private static  Logger log = Logger.getLogger(Initialization.class.getName());

	/**
	 * Contexto
	 */
	@javax.ws.rs.core.Context ServletContext servletContext;
	
	
	@Override
	public void onLoaded(AbstractResourceModelContext modelContext) {
		
		log.info("############# Arrancando app ...");    	
	}

}