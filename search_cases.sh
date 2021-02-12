#!/bin/bash
# When retrieving file attachments from an API or service provider, it may be necessary to add the appropriate file extensions. This script iterates over a list of files in a project's directory and renames them.

file_path='/path/to/your/files'
project_folder='/path/to/project'

for image in ${file_path/${project_folder}}; do [ -f "$image" ] || break	
	filetype=$(file $image | cut -d ':' -f 2 | cut -d ',' -f 1)

	case $filetype in
		' PNG image data')
			mv $image $image.PNG
		;;
		' PDF document')
			mv $image $image.PDF
		;;
		' JPEG image data')
			mv $image $image.JPEG
		;;
		' TIFF image data')
			mv $image $image.TIFF
		;;
		' ASCII text')
			mv $image $image.txt
		;;
		' ISO Media')
			mv $image $image.mov
		;;
		' GIF image data')
			mv $image $image.gif
		;;
		' Python script')
			mv $image $image.py
		;;
		' unified diff')
			mv $image $image.txt
		;;
		' HTML document text')
			mv $image $image.html
		;;
		' HTML document data')
			mv $image $image.html	
		;;
		' Rich Text Format data')
			mv $image $image.rtf
		;;
		' Zip archive data')
			mv $image $image.zip	
		;;
		' XML 1.0 document text')
			mv $image $image.xml
		;;
		' Microsoft Excel 2007+')
			mv $image $image.xlsx
		;;
		' Microsoft Word 2007+')
			mv $image $image.doc
		;;
		' gzip compressed data')
			mv $image $image.zip
		;;
		' UTF-8 Unicode text')
			mv $image $image.txt
		;;
		' Ruby script text')
			mv $image $image.rb 
		;;
		' Ruby modeule source text')
			mv $image $image.rb
		;;
		*)
			echo ---------
			echo "No filetypes found for:"
			echo "$image"
			echo "$filetype" "$image"
			echo ---------
		;;
	esac
done
