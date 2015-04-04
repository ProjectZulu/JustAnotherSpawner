echo '```' > CHANGELOG.md

prevTag="NONE"
firstPrint="TRUE"

for tag in $(git tag -l | sort -nr -t. -k1,1 -k2,2 -k3,3 -k4,4)
do
	if [ "$prevTag" != "NONE" ]; then
		echo "##########################" >> CHANGELOG.md
		echo "Changelog version $prevTag" >> CHANGELOG.md
		echo "##########################" >> CHANGELOG.md
		if [ "$tag" == "1.0.0" ]; then
			git log --graph --oneline --pretty="%ad: %B" --minimal --date=short $tag..$prevTag >> CHANGELOG.md
		
			echo "##########################" >> CHANGELOG.md
			echo "Changelog version $tag" >> CHANGELOG.md
			echo "##########################" >> CHANGELOG.md
			git log --graph --oneline --pretty="%ad: %B" --minimal --date=short $tag >> CHANGELOG.md
		else 
			git log --graph --oneline --pretty="%ad: %B" --minimal --date=short $tag..$prevTag >> CHANGELOG.md
			if [ $firstPrint == "TRUE" ]; then 
				echo "##########################"  > CHANGELOG_MOST_RECENT.md
				echo "Changelog version $prevTag" >> CHANGELOG_MOST_RECENT.md
				echo "##########################" >> CHANGELOG_MOST_RECENT.md
				git log --graph --oneline --pretty="%ad: %B" --minimal --date=short $tag..$prevTag >> CHANGELOG_MOST_RECENT.md
				firstPrint="FALSE"
			fi
		fi
    fi
	prevTag="$tag"
done
echo '```' >> CHANGELOG.md