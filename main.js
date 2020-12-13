// global
var a = 1; 

// scope: function
myfunction();

function myfunction() {
    var petName = "Sizzer"; // local variable 
    console.log(petName);  
}

console.log(petName);

// 以下不会报错
for (var i = 0; i <= 3; i++) {
}
console.log(i);
// 因为 i 是global variable