#! /usr/bin/env bash

cd "$(dirname "$0")"
npm ci --omit=dev >&2

for f in node_modules/*/package.json; do
    { read name; read version; read license; read licenses; read url; read url2; } < <(
        jshon -CQ \
            -e name -u -p \
            -e version -u -p \
            -e license -u -p \
            -e licenses -a -e type -u -p -p \
            -e homepage -u -p -e url -u < "$f"
    )
    if [ "$url" = null ]; then
        url="$url2"
    fi
    if [ "$url" = null ]; then
        url="https://www.npmjs.com/package/$name"
    fi
    if [ "$license" = null ]; then
        license="$licenses"
    fi

    cat <<EOF
    {
        name: '$name',
        url: '$url',
        version: '$version',
        license: '$license',
        edited: false
    },
EOF

done

cat >&2 <<EOF
Instructions:
- copy the above into lib-list.js
- remove our libraries
- fill in any "null" values manually
EOF
