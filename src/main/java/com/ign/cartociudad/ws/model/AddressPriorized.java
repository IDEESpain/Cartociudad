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

@XmlRootElement
public class AddressPriorized extends Address implements Comparable<AddressPriorized>{
	
	
	public AddressPriorized() {
		super();
	}
	
	public AddressPriorized(String id) {
		super(id);
	}
	
	private int priority;
	
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public int compareTo(AddressPriorized o) {
		return  o.getPriority() - priority;
	}

}
