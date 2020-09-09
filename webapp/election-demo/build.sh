#! /usr/bin/env bash

project_dir="$(dirname "$0")"

FIND_BASE_URL=http://10.2.21.91:8080 \
    node "$project_dir"/backend/build-data &&
    cd "$project_dir"/frontend &&
    npm run build
