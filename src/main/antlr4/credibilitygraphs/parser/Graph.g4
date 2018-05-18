grammar Graph;

// stat: tuples | objects ;
// tuples:  '(' NODE ',' NODE ')' (',' '(' NODE ',' NODE ')')* ;

stat: '(' NODE ',' NODE ',' NODE ')' (',' '(' NODE ',' NODE ',' NODE ')')* ;

NODE:  [a-zA-Z0-9]+ ;
WS:    [ \t\r\n]+ -> skip ;

