#!/bin/bash

refinedSitesVersion="0.6.4"

rm -rf output 2>/dev/null

if [ ! -f "refinedsites-$refinedSitesVersion-all.jar" ]; then
    echo "Downloading Refined Sites $refinedSitesVersion"
    curl -L -o "refinedsites-$refinedSitesVersion-all.jar" "https://github.com/refinedmods/refinedsites/releases/download/v$refinedSitesVersion/refinedsites-$refinedSitesVersion-all.jar"
fi

java -jar "refinedsites-$refinedSitesVersion-all.jar" . playbook.json

xdg-open "output/refined-storage/index.html" 2>/dev/null || open "output/refined-storage/index.html"
