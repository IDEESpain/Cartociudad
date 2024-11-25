package com.ign.cartociudad.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Clase con metodos de utilidad
 * 
 * @author guadaltel
 *
 */
public class GeocodingUtils {
	
	/**
	 * Indica si la cadena de entrada es un numero
	 * 
	 * @param q Cadena
	 * @return true si es un numero, false en caso contrario
	 */
	public static boolean isNumeric(String q) {
		try {
			Integer.parseInt(q);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Realiza el guardado del fichero que viene en el inputstream en la
	 * localizacion indicada
	 * 
	 * @param uploadedInputStream InputStream
	 * @param serverLocation      Localizacion
	 */
	public static void saveFile(InputStream uploadedInputStream, String serverLocation) {
		try {
			OutputStream outpuStream = new FileOutputStream(new File(
					serverLocation));
			int read = 0;
			byte[] bytes = new byte[1024];
			outpuStream = new FileOutputStream(new File(serverLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				outpuStream.write(bytes, 0, read);
			}
			outpuStream.flush();
			outpuStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Indica si en la cadena de entrada existe al menos un digito
	 * 
	 * @param s Cadena de entrada
	 * @return true si contiene al menos un digito, false en caso contrario
	 */
	public static boolean containsDigit(String s) {
		boolean contains = false;
		if (s != null && !s.trim().equals("")) {
			char[] charArray = s.toCharArray();
			for (int i = 0; i < charArray.length; i++) {
				if (Character.isDigit(charArray[i])) {
					return true;
				}
			}
		}
		return contains;
	}
}
