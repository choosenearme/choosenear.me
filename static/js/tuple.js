var CNM = CNM || {};

// Tuples are used to return multiple values from a function.
// They're treated specially by Future and Channel (passed as 
// multiple arguments) if they're returned by a map or
// flatMap function.
CNM.Tuple = function() {
	this.args = arguments;
};
