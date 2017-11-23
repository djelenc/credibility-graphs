grammar Graph;

// (a, b)
// (a, b), (b, c), (c, d)
// (a, b, c)
// (a, b, c), (b, c, a), (c, d, b)

stat: tuples | objects ;

tuples:  '(' NODE ',' NODE ')' (',' '(' NODE ',' NODE ')')* ;
objects: '(' NODE ',' NODE ',' NODE ')' (',' '(' NODE ',' NODE ',' NODE ')')* ;

NODE:  [a-zA-Z0-9]+ ;
WS:    [ \t\r\n]+ -> skip ;

