/**
 * AJAX call
 */
 
 function makeCall(method, url, formElement, callBackFun, reset=true){
		
    var request = new XMLHttpRequest();
    request.onreadystatechange = function(){
        callBackFun(request);
    };
    request.open(method, url);
    if (formElement == null) {
        request.send();
    } 
    else if(formElement instanceof FormData){
		request.send(formElement);
	}
    else {
        request.send(new FormData(formElement));
    }
    if (formElement !== null && !(formElement instanceof FormData) && reset === true) {
      formElement.reset();
    }
}