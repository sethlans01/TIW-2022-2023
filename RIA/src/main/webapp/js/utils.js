var loadingModal = new LoadingModal(document.getElementById("loading_msg"));

function LoadingModal(loading_msg){
    this.loading_msg = loading_msg;
    this.show = function(message){
        this.update(message);
        if (!document.body.className.includes("loading"))
            document.body.className += " loading";
    };
    this.update = function(message){
        if (message) //If a message is supplied to method
            this.loading_msg.textContent = message;
        else
            this.loading_msg.textContent = "Communicating with Server ...";
        
    };
    this.hide = function(){
        document.body.className = document.body.className.replace(" loading", "");
    };
}

(function (){
    var forms = document.getElementsByTagName("form");
    Array.from(forms).forEach(form => {
        var input_fields = form.querySelectorAll('input:not([type="button"]):not([type="hidden"])');
        var button = form.querySelector('button[type="submit"]');
        Array.from(input_fields).forEach(input => {
            input.addEventListener("keydown", (e) => {
                if(e.keyCode == 13){
					e.preventDefault();
                    let click = new Event("click");
                    button.dispatchEvent(click);
                }
            });
        });
    });
})();


function makeCall(method, relativeUrl, form, done_callback, reset = true) {
	    var req = new XMLHttpRequest(); //Create new request
	    //Init request
	    req.onreadystatechange = function() {
	        switch(req.readyState){
	            case XMLHttpRequest.UNSENT:
	                loadingModal.update("Connecting to Server ...");
	                break;
	            case XMLHttpRequest.OPENED:
	                loadingModal.update("Connected to Server ...");
	                break;
	            case XMLHttpRequest.HEADERS_RECEIVED:
	            case XMLHttpRequest.LOADING:
	                loadingModal.update("Waiting for response ...");
	                break;
	            case XMLHttpRequest.DONE:
	                loadingModal.update("Request completed");
	                if (checkRedirect(relativeUrl, req.responseURL)){ //Redirect if needed
	                    done_callback(req);
	                }
	                setTimeout(function(){
	                    loadingModal.hide();
	                }, 500);
	                break;
	        }
	    };
	
	    loadingModal.show(); //Start loading
	    //Open request
	    req.open(method, relativeUrl, true);
	    if(form != null){
	    	var formData = new FormData(form)
	    	for (const pair of formData.entries()) {
		   	var params = pair[0].toString() + '=' + pair[1].toString();
     	}
	    }
	    

	    //Send request
	    
	    if (form == null) {
	        req.send(); //Send empty if no form provided
	    } else if (form instanceof FormData){
	        req.send(form); //Send already serialized form
	    } else {
	        req.send(formData); //Send serialized form
	    }
	    //Eventually reset form (if provided)
	    if (form !== null && !(form instanceof FormData) && reset === true) {
	        form.reset(); //Do not touch hidden fields, and restore default values if any
	    }
}

function checkRedirect(requestURL, responseURL){
    if (responseURL){
        let actualRequestURL = relPathToAbs(requestURL);
        if (actualRequestURL != responseURL){ //Url changed
            window.location.assign(responseURL); //Navigate to the url
            return false;
        }
        return true; //Pass the request to callback
    }
    //Else is CORS blocked or redirection loop 
    console.error("Invalid AJAX call");
    return false;
}

function relPathToAbs(relative) {
    var stack = window.location.href.split("/"),
        parts = relative.split("/");
    stack.pop(); // remove current file name (or empty string)
    for (var i=0; i<parts.length; i++) {
        if (parts[i] == ".")
            continue;
        if (parts[i] == "..")
            stack.pop(); //One directory back
        else
            stack.push(parts[i]); //Add to path
    }
    return stack.join("/"); //Join everything
}

/**
 * Utils for arrays
 * -------------
 * Description: Adding a modified version of standard include,
 *              with automatic cast during comparison.
 */
Array.prototype.contains = function(element){ 
    for(let i = 0;i<this.length;i++)
        if (this[i] == element)
            return true;
    
    return false;
}