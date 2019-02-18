#!/bin/bash
function GenerateProjectListFile
{
	local buildFiles=("$@")
	first=""
	second=""
	for ix in ${!buildFiles[*]}
	do
		if [[ $first == "" ]]
		then # This is the very first element to be read
				first=${buildFiles[$ix]}
		else
			local fileDirname=$(dirname ${buildFiles[$ix]})
			local fileBasename=$(basename ${buildFiles[$ix]})
			local firstDirname=$(dirname $first)
			local firstBasename=$(basename $first)
			if [[ $fileDirname == $firstDirname ]]
			then # a pom.xml and a build.gradle may coexist
				second=${buildFiles[$ix]}
			fi
		fi
		local item=$ix+1
		if [[ "$item" -eq "${#buildFiles[*]}" ]]
		then
			firstDirname=$(dirname $first)
			firstBasename=$(basename $first)
			if [[ $first == "*pom.xml" ]]
			then
				echo $firstDirname","$first","$second >> $outFile
			else
				echo $firstDirname","$second","$first >> $outFile
			fi
		else
			local fileDirname=$(dirname ${buildFiles[$ix]})
			local fileBasename=$(basename ${buildFiles[$ix]})
			local firstDirname=$(dirname $first)
			local firstBasename=$(basename $first)
			if [[ $fileDirname != $firstDirname* ]]
			then # This is another project
				if [[ $first == "*pom.xml" ]]
				then
					echo $firstDirname","$first","$second >> $outFile
				else
					echo $firstDirname","$second","$first >> $outFile
				fi
				second=""
				first=${buildFiles[$ix]}
			fi
		fi
	done
}

function LoadDependencies
{
	while IFS=, read -r project pom gradle; do
		if [[ $project != "project" ]] # so it is not the first line
		then
			cd $project
			if [[ $gradle != "" ]] # a build.gradle exists
			then
				gradle --refresh-dependencies --gradle-user-home "$project/.gradle"
			fi
			if [[ $pom != "" ]] # a pom.xml exists
			then
				mvn dependency:resolve -fae -Dmaven.repo.local="$project/.m2"
			fi
		fi
    done < $1
}

function ExploreSandbox
{
	local sandboxDir=$1
	echo $sandboxDir
	for owner in $sandboxDir/*
	do
    	for repository in $owner/*
    	do
    		if [ -d $repository ]
            then
            	local outFile=$repository/"buildFilesCSV.txt"
            	if [ -f $outFile ]
            	then
            		rm $outFile 
            	fi
            	touch $outFile;
            	echo "project,pom,gradle" >> $outFile 
            	local repoDir=$(basename $repository)
    			echo $repository
    			# Trouver et trier tous les pom.xml et gradle.build du repository
				local first=""
				local second=""
				buildFiles=($(find $repository -type f -name "pom.xml" -printf '%h\0%d\0%p\n' -o -type f -name "build.gradle" -printf '%h\0%d\0%p\n' | sort -t '\0' -n | awk -F'\0' '{print $3}'|
					while read file; do
						if [[ $first == "" ]]
						then # This is the very first element to be read
							first=$file
							echo $first " "
						else
							local fileDirname=$(dirname $file)
							local fileBasename=$(basename $file)
							local firstDirname=$(dirname $first)
							local firstBasename=$(basename $first)
							if [[ $fileDirname == $firstDirname ]] && [[ $fileBasename != $firstBasename ]]
							then # a pom.xml and a build.gradle may coexist
								second=$file
								echo $second " "
							else
								if [[ $fileDirname != $firstDirname* ]]
								then # This is another project
									first=$file
									echo $first " "
								fi
							fi
						fi
					done))
				GenerateProjectListFile "${buildFiles[@]}"
				LoadDependencies $outFile
    		fi
    	done
    done
}

#Main
[ $1 ] && {
   ExploreSandbox $1
} || {
   echo "You must give the path of the sandbox."
}
