{
  function leftAlign(left, boolean, right) {
    if (right.boolean) {
        return {
          boolean: right.boolean,
          left: leftAlign(left, boolean, right.left),
          right: right.right
        }
    }
    else {
      return {
        boolean: boolean,
        left: left,
        right: right
      }
    }
  }

  function postProcess(input) {
    var next = input.next;
    delete input.next

    if (!next) {
      return input;
    }
    else {
      var boolean = next.boolean;
      var right = next.right;

      return leftAlign(input, boolean, right);
    }
  }

  function postProcessNegation(input) {
    var target = input;
    
    while(target.left) {
      target = target.left
    }

    target.negative = true;

    return postProcess(input);
  }
} 

start
    = fieldtext  

fieldtext
    = operator:operator "{" csv:csv "}" colonsv:colonsv next:fieldtext_prime { return postProcess({ operator: operator, values: csv , fields: colonsv, next: next}) }
    / _ '(' _ fieldtext:fieldtext _ ')' next:fieldtext_prime { return postProcess({fieldtext: fieldtext, next: next}) }
    / 'NOT' __ fieldtext:fieldtext next:fieldtext_prime { fieldtext.next = next; return postProcessNegation(fieldtext) }

fieldtext_prime
    = __ boolean:boolean __ rhs:fieldtext next:fieldtext_prime { return postProcess({ boolean: boolean, right:rhs,  next: next}) }
    / {return undefined}

operator
    = chars:[A-Z]+ { return chars.join('') }

boolean
    = 'OR'
    / 'AND'
    / 'XOR'


colonsv
    = ':' head:field tail:colonsv { return [head].concat(tail) } 
    / ':' field:field { return [field] }

csv
    = csv_prime
    / { return [] }

csv_prime
    = head:value ',' tail:csv_prime { return [head].concat(tail) }
    / value:value { return [value] }

field
    = chars:[A-Za-z_]+ { return chars.join('') }

value
    = chars:[^,{}]+ { return chars.join('') }

_
    = ' '*

__ = ' '+