start
    = _ expr:ShapeExpression _ { return expr }

ShapeExpression
    = PolygonExpression
    / PointExpression

PointExpression
    = 'POINT'i _ '(' _ pos:Point _  ')' { return { type: 'POINT', point: pos } }

PolygonExpression
    = 'POLYGON'i _ '(' _ first:LineExpression _ others:( ',' _ LineExpression _ )* ')' {  return { type: 'POLYGON', polygon: [first].concat(others.map(function(a){return a[2]})) } }

LineExpression
    = '(' _ first:Point _ others:( ',' _ Point _ )* ')' {
        return [first].concat(others.map(function(a) { return a[2] }))
    }

// Note: we return the numbers in (lat, lon) format; the reverse of WKT, for compatibility with Leaflet's input format
Point
    = lon:number __ lat:number { return [lat, lon] }

number "number"
  = minus? int frac? exp? { return parseFloat(text()); }

// Technically, from page 53 of the latest (1.2.1) WKT spec, you can use commas as the decimal point, i.e.
//   <decimal point> ::= <period> | <comma>
// See http://portal.opengeospatial.org/files/?artifact_id=25355 , from http://www.opengeospatial.org/standards/sfa.
// However, we use boost's geometry parser server-side, which doesn't support using a comma as a decimal point,
//   so we're deliberately not including support for that in the UI; otherwise it'll be confusing when a user who
//   uses commas as decimal point sees it rendering in the UI correctly but doesn't get it as a result when filtering
//   by geographic field restrictions (since the boost parser ignores it).
decimal_point
  = .

digit1_9
  = [1-9]

e
  = [eE]

exp
  = e (minus / plus)? DIGIT+

frac
  = decimal_point DIGIT+

int
  = zero / (digit1_9 DIGIT*)

minus
  = "-"

plus
  = "+"

zero
  = "0"

DIGIT  = [0-9]

_
    = ' '*

__
    = ' '+
