# KeyWrapper

## Dependencies

- Your project must define dependencies for the following in your require-config.js
    - [underscore] (http://underscorejs.org/)
    - [text] (https://github.com/requirejs/text)
    - [peg] (http://pegjs.org/)


## Usage

You must export 'fieldtext'.

## API

### Parse a field text string

To create a field text tree structure from a string:

    try {
        newFieldText = parser.parse(newFieldTextString);
    } catch(err) {
        error = err;
    }

If the string fails to parse, error will provide the error message provided by Pegjs.

### Combine trees

Fieldtext provides the following operators to combine trees:

    'AND', 'OR', 'XOR', 'BEFORE', 'AFTER'

For example:

    fieldTextTreeOne.AND(fieldTextTreeTwo)

## Tests

    grunt test
