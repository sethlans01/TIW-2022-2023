/**
 * Login management
 */
(function() {
    //Link graphics
    var button = document.getElementById("button");
    var login_warning_div = document.getElementById('login_warning_id');
    var error_message = document.getElementById('error_message');
    
    //Attach to login button
    button.addEventListener("click", (e) => {
        var form = e.target.closest("form"); 
        login_warning_div.style.display = 'none';
        if (form.checkValidity()) { //Do form check
            sendToServer(form, login_warning_div, 'Login');
        }else 
            form.reportValidity(); //If not valid, notify
    });

    function sendToServer(form, error_div, request_url){
        makeCall("POST", request_url, form, function(req){
            switch(req.status){ //Get status code
                case 200: //Okay
                    var data = JSON.parse(req.responseText);
                    sessionStorage.setItem('name', data.name);
                    sessionStorage.setItem('email', data.email)
                    window.location.href = "home.html";
                    break;
                case 400: // bad request
                case 401: // unauthorized
                case 500: // server error
					error_message.style.display = 'block';
                    break;
                default: //Error
					error_message.style.display = 'block';
                    break;
            }
        });
    }
})();