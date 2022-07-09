/**
 * Login Management
 */
 (function(){ 

    var loginButton=document.getElementById("loginbutton");
    var openRegisterButton=document.getElementById("openRegisterButton");
    var registerButton=document.getElementById("registerbutton");
    
    
    
    loginButton.addEventListener("click", (e)=>{

        var form = e.target.closest("form");
        var errorDivLogin=document.getElementById("errorMessageLogin");
        
        if(form.checkValidity()){ // if form is valid
            makeCall("POST","CheckLogin",e.target.closest("form"),
                function(x){ //this is the callBack Function
                    if (x.readyState == XMLHttpRequest.DONE) { //the server had finished to return the response and the browser to download it
                        var message = x.responseText;
                        switch (x.status) {
                          case 200:
                              sessionStorage.setItem('username', message);
                              window.location.href = "Home.html";
                              break;
                          case 400: // bad request
	                          errorDivLogin.textContent = message;
	                          break;
                          case 401: // unauthorized
	                          errorDivLogin.textContent = message;
	                          break;
                          case 500: // server error
	                          errorDivLogin.textContent = message;
	                          break;
                        }
                    }
                }
            ); //end function makeCall
        }
        else{
            form.reportValidity(); // if form is not valid,notify
        }
    });
    
   	
    openRegisterButton.addEventListener("click", function(e){

        if(e.target.textContent === "Sign up"){
			document.getElementById("loginDiv").style.display="none";
			document.getElementById("registerDiv").style.display="block";
        }
        else{
			document.getElementById("registerDiv").style.display="none";
			document.getElementById("loginDiv").style.display="block";
        }
    });
    
    
    function checkEmail(email){
		var re = /^[^\s@]+@([^\s@.,]+\.)+[^\s@.,]{2,}$/; //regex 
        return re.test(email);
	}
	
	function checkPassword(pass1,pass2){
		if(pass1 == pass2){
			return true;
		}
		else{
			return false;
		}
	}
    
    registerButton.addEventListener("click", (e)=>{
	
		var form=e.target.closest("form");
		var errorDivRegister=document.getElementById("errorMessageRegister");
		var emailInput=document.getElementById("eMail").value;
    	var passwordInput=document.getElementById("psw").value;
    	var repeatPasswordInput=document.getElementById("pswRep").value;
    	
    	var mailCheck=checkEmail(emailInput);
    	var passCheck=checkPassword(passwordInput,repeatPasswordInput);

        if(form.checkValidity()){ //if form is valid
        
            if(!mailCheck || ! passCheck){
				
				if(!mailCheck){
					errorDivRegister.textContent="E-mail format not correct!";
                	return;
				}
				else{
					errorDivRegister.textContent="Passwords do not match!";
                	return;
				}
			}
            else{
				makeCall("POST","CheckRegister",e.target.closest("form"),
            	function(x){
                if (x.readyState == XMLHttpRequest.DONE) {
                    var message = x.responseText;
                    switch (x.status) {
                      case 200:
                          sessionStorage.setItem('username', message);
                          window.location.href = "Home.html";
                          break;
                      case 400: // bad request
                          errorDivRegister.textContent = message;
                          break;
                      case 401: // unauthorized
                          errorDivRegister.textContent = message;
                          break;
                      case 500: // server error
                          errorDivRegister.textContent = message;
                          break;
                    }
                }

        		});
			}
        }
        else{
            form.reportValidity(); //if form is not valid,notify
        }
	});

})();
