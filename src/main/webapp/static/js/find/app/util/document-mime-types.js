define(
    function() {
        return [
            {
                typeRegex: [
                    function() {
                        return /text\/html/;
                    }
                ],
                className: 'html'
            },
            {
                typeRegex: [
                    function() {
                        return /text\/.*/;
                    }
                ],
                className: 'text'
            },
            {
                typeRegex: [
                    function() {
                        return /video\/.*/;
                    }
                ],
                className: 'video'
            },
            {
                typeRegex: [
                    function() {
                        return /image\/.*/;
                    }
                ],
                className: 'image'
            },
            {
                typeRegex: [
                    function() {
                        return /audio\/.*/;
                    }
                ],
                className: 'audio'
            },
            {
                typeRegex: [
                    function() {
                        return /message\/rtc822/;
                    }
                ],
                className: 'email'
            },
            {
                typeRegex: [
                    function() {
                        return /application\/pdf/;
                    }
                ],
                className: 'pdf'
            },
            {
                typeRegex: [
                    function() {
                        return /application\/x-ms-powerpoint\d{2}/;
                    }
                ],
                className: 'powerpoint'
            },
            {
                typeRegex: [
                    function() {
                        return /application\/x-ms-word\d{2}/;
                    }
                ],
                className: 'word'
            },
            {
                typeRegex: [
                    function() {
                        return /application\/x-ms-excel\d{2}/;
                    }
                ],
                className: 'excel'
            },
            {
                typeRegex: [
                    // TODO figure out what this should be
                ],
                className: 'presentation'
            },
            {
                typeRegex: [
                    // TODO figure out what this should be
                ],
                className: 'spreadsheet'
            },
            {
                typeRegex: [
                    function() {
                        return /.*/;
                    }
                ],
                className: 'general'
            }
        ];
    }
);