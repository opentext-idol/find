#!/bin/bash

INDEX="search_default_index"
PROFILE="search_default_profile"

ENDPOINT="api.idolondemand.com"

# Read arguments
while [[ $# > 0 ]]
do
	key="$1"

	case $key in
		-e|--endpoint)
		ENDPOINT="$2"
		shift
		;;
		
		-a|--apikey)
		APIKEY="$2"
		shift
		;;
	esac
	shift
done

echo "Using $ENDPOINT v1 and API key $APIKEY"

# Create index
echo "Creating index $INDEX"
curl "https://$ENDPOINT/1/api/sync/createtextindex/v1?flavor=querymanipulation&apikey=$APIKEY&index=$INDEX"
echo -e "\n"

# Create query profile
echo "Creating query profile $PROFILE"
curl "https://$ENDPOINT/1/api/sync/createqueryprofile/v1?query_manipulation_index=$INDEX&promotions_enabled=true&promotion_categories=default&promotions_identified=true&synonyms_enabled=true&synonym_categories=default&blacklists_enabled=true&blacklist_categories=default&query_profile=$PROFILE&apikey=$APIKEY"
echo -e "\nDone"

