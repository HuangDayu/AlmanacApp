#!/bin/bash
git add .
if [[ $1 ]]; then
	git commit -m $1
else
	git commit -m "update"
fi
git push origin master
echo "update success!"