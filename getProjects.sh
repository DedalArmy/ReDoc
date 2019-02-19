#!/bin/bash

function copyProjects
{
	local csv=$1
	local repo=$2
	local output=$3
	echo $output
	while IFS=';' read -r numb1 name other; do
		if [[ $name != "project_name" ]] # so it is not the first line
		then
			if [ ! -d $output/$name ]
			then
				mkdir -p $output/$name
			fi
			cp -rv $repo/$name $output/$name
		fi
    done < $csv
	
}

#Main
[ $1 ] && [ $2 ] && [ $3 ] && {
   copyProjects $1 $2 $3
} || {
   echo "You must give the path of the csv file, the path of the repository and the output path."
}
