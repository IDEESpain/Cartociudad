#! /bin/sh

VERSION="Elastic scripts v1.3.3"

# Variables por defecto
URL="http://[IP_ELASTIC]:9200"
INDEX="vial"
SPLITTING="true"
work_dir="../"
token="false"

lines_to_split="5000"
MAX_SPLIT_SIZE="80000000"

Help()
{
   # Display Help
   echo "Script de carga automatizada para elastic."
   echo
   echo "Syntax: sh carga_"$INDEX".sh [-h|-v|-u|-i|-s|-w]"
   echo "options:"
   echo "\t-h     Muestra esta ayuda."
   echo "\t-v     Muestra la version del script."
   echo
   echo "\t-u     Configura la URL de Elastic."
   echo "\t-i     Configura el Indice de Elastic a atacar."
   echo "\t-s     Configura si se hara un splitteo de los fichero o no."
   echo "\t-w     Configura el directorio de trabajo, la ruta del indice,\n\t\t el padre de bulk, salida_bulk, comprimido, script."
   echo "\t-t     Configura el fichero desde el que se leerá el Token Bearer, si no se establece no se usará autenticacion."
   echo
}

# Parametros por inicial
# u/h url/host, hacen lo mismo, establecen la direccion de elastic
# i indice, indice de elastic a atacar
# s splitting, decide si splitear el fichero
# w work_dir, directorio sobre el que se trabaja
while getopts :hvu:i:s:w:t: flag
do
    case "${flag}" in
        h) Help; return 0;;
        v) echo "$VERSION\n(-h for help)"; return 0;;
        u) URL=${OPTARG};;
        i) INDEX=${OPTARG};;
        s) SPLITTING=${OPTARG};;
        w) work_dir=${OPTARG};;
        t) token=${OPTARG};;
    esac
done

echo
cols=$(tput cols)

i=1
while [ "$i" -le "$cols" ]; do
  printf "*"
  i=$((i + 1))
done

echo

echo "Comenzando carga en $URL al indice $INDEX. Se van a splitear los ficheros:$SPLITTING. Buscando en $work_dir"
if [ "$token" != "false" ]; then
	echo "Usando token"
fi

i=1
while [ "$i" -le "$cols" ]; do
  printf "*"
  i=$((i + 1))
done

echo


# Directorio de entrada y salida
input_dir="$work_dir/bulk"
output_dir="$work_dir/salida_bulk"
token=$(cat $token)

# Crear la carpeta de salida si no existe
mkdir -p "$output_dir"

# Iterar sobre los archivos de entrada
for file in "$input_dir"/*; do
	# Continua solo si el fichero termina en .json
    if echo "$file" | grep -qE '\.json$'; then
	    filename=$(basename "$file")
    	output_file="$output_dir/salida_${filename}"
    	output_file_error="$output_dir/error_${filename}"

		if [ "$SPLITTING" = "true" ]; then
    		size=$(stat -c "%s" "$file")   # Obtiene el tamaño del archivo en bytes

			if [ "$size" -gt "$MAX_SPLIT_SIZE" ]; then
				split -l "$lines_to_split" "$file" "$input_dir/split_"
				# Iterar sobre los archivos spliteados
				for filexa in "$input_dir"/*; do
					filename=$(basename "$filexa")
					# Continua solo si el fichero empieza por split_
					if [ "${filename#split_}" != "$filename" ]; then
					
						curl -X POST "$URL/$INDEX/_bulk?pretty" -H "Content-Type: application/x-ndjson" -H "Authorization: Bearer $token" --data-binary "@$filexa" >> "$output_file"
						rm "$filexa"

					fi	
				done

			else
				curl -X POST "$URL/$INDEX/_bulk?pretty" -H "Content-Type: application/x-ndjson" -H "Authorization: Bearer $token" --data-binary "@$file" > "$output_file"
			fi
		else
			curl -X POST "$URL/$INDEX/_bulk?pretty" -H "Content-Type: application/x-ndjson" -H "Authorization: Bearer $token" --data-binary "@$file" > "$output_file"
		fi
	fi
done

echo
echo "Carga completada."
		
