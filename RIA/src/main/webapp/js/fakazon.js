(function(){
	//Vars
	var userInfo, lastSeen;

	var contentDiv = document.getElementById("content");
	var title = document.getElementById("title")

	var pageOrchestrator = new pageOrchestrator();

	var currentCart = new Cart()
	var cartPrices = new Cart()

	window.addEventListener("load", () => {

		pageOrchestrator.start(); // initialize the components
		pageOrchestrator.refresh(); // display initial content

	});

	function pageOrchestrator(){

		this.start = function(){
			userInfo = new UserInfo(
				sessionStorage.getItem('name'),
				sessionStorage.getItem('email'),
				[document.getElementById("userName")]
			);
			lastSeen = new LastSeen(
				contentDiv
			)
			buttons = new DoomButtons(
				document.getElementById("buttonHome"),
				document.getElementById("buttonCart"),
				document.getElementById("buttonOrders")
			);
		}

		this.clearContent = function(
			_contentDiv){

			this.contentDiv = _contentDiv
			while (contentDiv.childNodes.length > 2) {

				contentDiv.removeChild(contentDiv.lastChild);

			}

		}

		this.clearTitle = function(){

			while (title.childNodes.length > 0) {

				title.removeChild(title.lastChild);

			}

		}

		this.refresh = function(){

			userInfo.show();
			lastSeen.show();
			buttons.bind();

		};

		this.deleteElement = function(element){

			element.remove();

		}

		this.addCss = function(element){

			var headContent = document.getElementsByTagName('head')[0]
			headContent.appendChild(element)

		}

		this.killContent = function(){
			while (contentDiv.childNodes.length > 0) {
				contentDiv.removeChild(contentDiv.lastChild);
			}
		}
	}

	function UserInfo(_name, _email, nameElements){

		this.name = _name;
		this.email = _email;

		this.show = function(){
			nameElements.forEach(element => {
				element.innerHTML = this.name;
			});
		}
	}

	function DoomButtons(_buttonHome, _buttonCart, _buttonOrders){
		this.buttonHome = _buttonHome
		this.buttonCart = _buttonCart
		this.buttonOrders = _buttonOrders

		this.bind = function(){

			buttonHome.addEventListener("click", (e) => {

				pageOrchestrator.killContent()

				var cssToRemove = document.getElementsByTagName("link");
				for(let i = 0; i < cssToRemove.length; i++){
					pageOrchestrator.deleteElement(cssToRemove[i])
				}
				var css = document.createElement("link")
				css.rel = 'stylesheet'
				css.type = 'text/css'
				css.href = 'http://localhost:8080/Shopping_Javascript_war/css/home.css'
				pageOrchestrator.addCss(css)

				pageOrchestrator.killContent()
				lastSeen.show(contentDiv);

				pageOrchestrator.clearTitle()
				var span1, span2, span3
				span1 = document.createElement("span")
				span1.innerHTML = "Welcome back, "
				title.appendChild(span1)
				span2 = document.createElement("span")
				span2.innerHTML = sessionStorage.getItem('name').toString()
				title.appendChild(span2)
				span3 = document.createElement("span")
				span3.innerHTML = "!"
				title.appendChild(span3)

			})

			this.buttonOrders.addEventListener("click", (e) => {
				pageOrchestrator.killContent()

				// Call Orders
				Orders()
			})

			this.buttonCart.addEventListener("click", (e) => {
				ShowCart()
			})

		}
	}

	function LastSeen(_contentDiv){

		this.contentDiv = _contentDiv;

		this.show = function(){
			//Request and update with the results
			makeCall("GET", 'Home', null, (req) =>{
				switch(req.status){
					case 200: //ok
						var products = JSON.parse(req.responseText);
						this.update(products);
						break;
					case 400: // bad request
					case 401: // unauthorized
					case 500: // server error
						showErrorPage("Error while retrieving your home page content!")
						break;
					default: //Errors
						showErrorPage("Error while retrieving your home page content!")
						break;
				}
			});
		};

		this.update = function(_products){

			let input_group, form, input, button, h3, tablediv, table, tbody, tr,column, card, card2, img, br, b;
			let i = 1;

			input_group = document.createElement("div")
			input_group.className = "input-group"
			input_group.setAttribute("id","group-input")

			form = document.createElement("form")
			form.setAttribute("action", "#")
			form.setAttribute("method", "GET")
			form.className = "header-button"
			form.setAttribute("id","input-button")
			form.setAttribute("onSubmit", "return false;")
			input_group.appendChild(form)

			input = document.createElement("input")
			input.setAttribute("id", "searchInput")
			input.required = true
			input.setAttribute("type", "text")
			input.setAttribute("name","search")
			input.setAttribute("autocomplete", "off")
			input.className = "input"
			input.setAttribute("placeholder", "Search an item")
			input.autofocus = true
			form.appendChild(input)

			button = document.createElement("button")
			button.setAttribute("id","button")
			button.setAttribute("type","submit")
			button.className = "primary-button"
			button.innerHTML = "SEARCH"
			form.appendChild(button)

			h3 = document.createElement("h3")
			h3.setAttribute("id", "titleh3")
			h3.innerHTML = "Last 5 seen products"

			tablediv = document.createElement("div")
			tablediv.setAttribute("id","tablediv")

			table = document.createElement("table")
			table.className = "table"
			tablediv.appendChild(table)

			tbody = document.createElement("tbody")
			table.appendChild(tbody)

			tr = document.createElement("tr")
			tr.setAttribute("id","tabRow")
			tbody.appendChild(tr)

			_products.lastFive.forEach((product) => {
				column = document.createElement("td");

				card = document.createElement("div");
				card.className = "card";
				card.setAttribute("id","product-")
				card.setAttribute("id", card.getAttribute("id")+i);
				column.appendChild(card);

				card2 = document.createElement("div");
				card2.className = "card2";
				card.appendChild(card2);

				img = document.createElement("img");
				img.setAttribute("src","./resources/" + product.code + ".jpg");
				card2.appendChild(img);

				br = document.createElement("br");
				card2.appendChild(br);

				b = document.createElement("b");
				b.className = "text";
				b.innerHTML = product.name;
				card2.appendChild(b)

				tr.appendChild(column);
				i++
			});

			contentDiv.appendChild(input_group)
			contentDiv.appendChild(h3)
			contentDiv.appendChild(tablediv)
			var search = Search(button)

		};

	}

	function Search(searchButton){

		this.searchButton = searchButton;

		searchButton.addEventListener("click", (e) => {
			e.preventDefault();
			var form = e.target.closest("form");
			if (form.checkValidity()) { //Do form check
				this.show(form);
			}else
				form.reportValidity(); //If not valid, notify
		})

		this.show = function(form){
			var formData = new FormData(form)
			var params = new URLSearchParams(formData).toString();
			makeCall("GET", 'Search?'+ params, null, (req) =>{
				switch(req.status){
					case 200: //ok
						var products = JSON.parse(req.responseText);
						this.update(products);
						break;
					case 400: // bad request
					case 401: // unauthorized
					case 500: // server error
						showErrorPage("An error occured while completing your request!")
						break;
					default: //Errors
						showErrorPage("An error occured while completing your request!")
						break;
				}
			});
		}

		this.update = function(products){

			pageOrchestrator.killContent()

			pageOrchestrator.clearTitle()
			var searchSpan = document.createElement("span")
			searchSpan.innerHTML = "Here what we found!"
			title.appendChild(searchSpan)

			var cssToRemove = document.getElementsByTagName("link");
			for(let i = 0; i < cssToRemove.length; i++){
				pageOrchestrator.deleteElement(cssToRemove[i])
			}
			var css = document.createElement("link")
			css.rel = 'stylesheet'
			css.type = 'text/css'
			css.href = 'http://localhost:8080/Shopping_Javascript_war/css/search.css'
			pageOrchestrator.addCss(css)

			if(products.length === 0){

				let div, h2, h22, img;

				div = document.createElement("div")
				div.className = "no-matches"

				h2 = document.createElement("h2")
				h2.innerHTML = "It appears we couldn't find anything about your input, big man!"
				div.appendChild(h2)

				h22 = document.createElement("h2")
				h22.innerHTML = "Try looking for something else..."
				div.appendChild(h22)

				img = document.createElement("img")
				img.setAttribute("src", "./resources/noMatches.jpg")
				div.appendChild(img)

				contentDiv.appendChild(div)

			}
			else{

				let tablediv

				tablediv = document.createElement("div")
				tablediv.setAttribute("id", "tablediv")

				products.forEach((product) =>{

					let table, tbody, tr1, td1, b1, td2, b2
					let td3, b3, td4, b4, tr2, td5, img, td6, td7, td8, td9
					let span, form, input

					table = document.createElement("table")
					table.className = "table"

					tbody = document.createElement("tbody")
					table.appendChild(tbody)

					tr1 = document.createElement("tr")
					tbody.appendChild(tr1)

					td1 = document.createElement("td")
					td1.className = "table column"
					tr1.appendChild(td1)

					b1 = document.createElement("b")
					b1.innerHTML = "Image"
					td1.appendChild(b1)

					td2 = document.createElement("td")
					td2.className = "table column"
					tr1.appendChild(td2)

					b2 = document.createElement("b")
					b2.innerHTML = "Name"
					td2.appendChild(b2)

					td3 = document.createElement("td")
					td3.className = "table column"
					tr1.appendChild(td3)

					b3 = document.createElement("b")
					b3.innerHTML = "Code"
					td3.appendChild(b3)

					td4 = document.createElement("td")
					td4.className = "table column"
					tr1.appendChild(td4)

					b4 = document.createElement("b")
					b4.innerHTML = "Min Cost"
					td4.appendChild(b4)

					tr2 = document.createElement("tr")
					tbody.appendChild(tr2)

					td5 = document.createElement("td")
					td5.className = "table column"
					tr2.appendChild(td5)

					img = document.createElement("img")
					img.className = "image"
					img.setAttribute("src", "./resources/" + product.code + ".jpg")
					td5.appendChild(img)

					td6 = document.createElement("td")
					td6.className = "table column"
					td6.innerHTML = product.name
					tr2.appendChild(td6)

					td7 = document.createElement("td")
					td7.className = "table column"
					td7.innerHTML = product.code
					tr2.appendChild(td7)

					td8 = document.createElement("td")
					td8.className = "table column"
					tr2.appendChild(td8)

					span = document.createElement("span")
					span.innerHTML = product.minCost
					td8.appendChild(span)
					span.insertAdjacentText('afterend', "$")

					td9 = document.createElement("td")
					td9.className = "table column"
					tr2.appendChild(td9)

					form = document.createElement("form")
					form.setAttribute("action", "#")
					form.setAttribute("method", "get")
					form.setAttribute("onSubmit", "return false;")
					td9.appendChild(form)

					input = document.createElement("input")
					input.setAttribute("type", "submit")
					input.className = "primary-button"
					input.setAttribute("value", "View Product")
					input.addEventListener("click", (e) => {
						e.preventDefault();
						// Change view to display product details
						ViewProduct(product.code);
					})
					form.appendChild(input)

					tablediv.appendChild(table)

				});

				contentDiv.appendChild(tablediv)

			}
		}

	}

	// Handles the product's details page
	function ViewProduct(productCode){

		/**
		 * Instantly called function to call the server and ask for the details of the specified product.
		 * After the server response, if it's 200 OK, call function showMore to display results.
		 * @param code 	Code of the product the user wants more details about.
		 */
		(this.moreDetails = function(code){
			// Prepare request
			let toConvert = "productCode=" + code;
			let params = new URLSearchParams(toConvert).toString();
			// Do the request
			makeCall("GET", 'SelectProduct?'+ params, null, (req) =>{
				switch(req.status){
					case 200: //ok
						let productAndSuppliers = JSON.parse(req.responseText);
						this.showMore(productAndSuppliers);
						break;
					case 400: // bad request
					case 401: // unauthorized
					case 500: // server error
						showErrorPage("An error occurred while completing your request!")
						break;
					default: //Errors
						showErrorPage("An error occurred while completing your request!")
						break;
				}
			});
		}(productCode));

		/**
		 * Function that displays the results of the request
		 * @param productAndSuppliers	object containing request results
		 */
		this.showMore = function(productAndSuppliers){

			// Change css file
			let cssToRemove = document.getElementsByTagName("link");
			for(let i = 0; i < cssToRemove.length; i++){
				pageOrchestrator.deleteElement(cssToRemove[i])
			}
			let css = document.createElement("link")
			css.rel = 'stylesheet'
			css.type = 'text/css'
			css.href = 'http://localhost:8080/Shopping_Javascript_war/css/productInfo.css'
			pageOrchestrator.addCss(css)

			// Clear the content of the content div
			pageOrchestrator.killContent()

			// Create Product info div and append it to content div
			let productInfoDiv
			productInfoDiv = document.createElement("div")
			productInfoDiv.setAttribute("id", "product-info")
			contentDiv.appendChild(productInfoDiv)

			let productPictureDiv
			productPictureDiv = document.createElement("div")
			productPictureDiv.setAttribute("id", "product-picture")
			productInfoDiv.appendChild(productPictureDiv)

			let img
			img = document.createElement("img")
			img.className = "image"
			img.setAttribute("src", "./resources/" + productAndSuppliers.product.code + ".jpg")
			productPictureDiv.appendChild(img)

			let productDetails
			productDetails = document.createElement("div")
			productDetails.setAttribute("id", "product-details")
			productInfoDiv.appendChild(productDetails)

			let code
			code = document.createElement("span")
			code.setAttribute("class", "bold")
			code.innerHTML = "Product # "
			productDetails.appendChild(code)
			let spanText = document.createElement("span")
			spanText.setAttribute("class", "text")
			spanText.innerHTML = productAndSuppliers.product.code
			code.appendChild(spanText)

			let productName
			productName = document.createElement("span")
			productName.setAttribute("class", "bold")
			productName.innerHTML = "Name: "
			productDetails.appendChild(productName)
			spanText = document.createElement("span")
			spanText.setAttribute("class", "text")
			spanText.innerHTML = productAndSuppliers.product.name
			productName.appendChild(spanText)

			let productDescription
			productDescription = document.createElement("span")
			productDescription.setAttribute("class", "bold")
			productDescription.innerHTML = "Description: "
			productDetails.appendChild(productDescription)
			spanText = document.createElement("span")
			spanText.setAttribute("class", "text")
			spanText.innerHTML = productAndSuppliers.product.description
			productDescription.appendChild(spanText)

			let productCategory
			productCategory = document.createElement("span")
			productCategory.setAttribute("class", "bold")
			productCategory.innerHTML = "Category: "
			productDetails.appendChild(productCategory)
			spanText = document.createElement("span")
			spanText.setAttribute("class", "text")
			spanText.innerHTML = productAndSuppliers.product.category
			productCategory.appendChild(spanText)

			// Create the div of the "Here the seller" part
			let sellerTitleDiv
			sellerTitleDiv = document.createElement("div")
			sellerTitleDiv.setAttribute("id", "seller-title")
			contentDiv.appendChild(sellerTitleDiv)

			let sellerTitle
			sellerTitle = document.createElement("h2")
			sellerTitle.setAttribute("id", "seller-title-h2")
			sellerTitle.innerHTML = "Here the sellers:"
			sellerTitleDiv.appendChild(sellerTitle)

			// Create the div of the sellers table and its content
			let sellersDiv
			sellersDiv = document.createElement("div")
			sellersDiv.setAttribute("id", "sellers")
			contentDiv.appendChild(sellersDiv)

			// Make the table
			productAndSuppliers.suppliers.forEach((supplier) => {
				// Create the overlay div
				let overlayDiv
				overlayDiv = document.createElement("div")
				overlayDiv.setAttribute("id", "overlay-div")
				contentDiv.appendChild(overlayDiv)

				// Create the modal
				let modal
				modal = document.createElement("section")
				modal.setAttribute("id", "modal")
				contentDiv.insertBefore(modal, contentDiv.firstChild)

				// Create title of the modal
				let temp
				temp = document.createElement("h3")
				temp.setAttribute("id", "modal-title")
				temp.innerHTML = "Products in your cart from " + supplier.name + ":"
				modal.appendChild(temp)

				// Create content div of the modal
				let modalContentDiv
				modalContentDiv = document.createElement("div")
				modalContentDiv.setAttribute("class", "modal-content-div")
				modal.appendChild(modalContentDiv)

				// Create content of the div
				let productListFromSupplier = currentCart.cart.get(supplier.code)
				if(productListFromSupplier === undefined){

					// Image Div
					let card = document.createElement("div")
					card.setAttribute("class", "empty-modal-div")
					modalContentDiv.appendChild(card)

					// Text
					let text = document.createElement("h3")
					text.innerHTML = "It seems you did not add any product from " + supplier.name + " in your cart"
					card.appendChild(text)

					// Img
					let img = document.createElement("img")
					img.setAttribute("src", "./resources/emptySellerList.gif")
					card.appendChild(img)

				} else {
					currentCart.cart.get(supplier.code).forEach((prod) => {
						// Product card div
						let card
						card = document.createElement("div")
						card.setAttribute("class", "modal-product-div")
						modalContentDiv.appendChild(card)

						// Product image
						let curr = document.createElement("img")
						curr.setAttribute("src", "./resources/" + prod.productCode + ".jpg")
						card.appendChild(curr)

						// Div for text
						curr = document.createElement("div")
						curr.setAttribute("class", "modal-product-text")
						card.appendChild(curr)

						// Product name
						let h4 = document.createElement("h4")
						h4.innerHTML = prod.productName
						curr.append(h4)

						// Product quantity
						h4 = document.createElement("h4")
						curr.append(h4)
						let span = document.createElement("span")
						span.innerHTML = "Quantity: "
						h4.appendChild(span)
						span = document.createElement("span")
						span.innerHTML = prod.quantity
						h4.appendChild(span)

					})

				}

				// Create button of the modal
				temp = document.createElement("button")
				temp.setAttribute("class", "primary-button")
				temp.innerHTML = "CLOSE"
				temp.addEventListener("click", (e) => {
					overlayDiv.style.display = "none"
					modal.style.display = "none"
				})
				modal.appendChild(temp)

				// Create the div
				let supplierDiv
				supplierDiv = document.createElement("div")
				supplierDiv.setAttribute("class", "supplier-div")
				sellersDiv.appendChild(supplierDiv)

				// Create the table
				let table
				table = document.createElement("table")
				table.setAttribute("class", "supplier-table")
				supplierDiv.appendChild(table)

				// First header row
				let headerRow, cell
				headerRow = table.insertRow()
				headerRow.setAttribute("class", "first-header-row")
				cell = headerRow.insertCell()
				cell.innerHTML = "Company Name"
				cell = headerRow.insertCell()
				cell.innerHTML = "Score"
				cell = headerRow.insertCell()
				cell.innerHTML = "Unit Cost"
				cell = headerRow.insertCell()
				cell.innerHTML = "Number of Products already in Cart"
				cell = headerRow.insertCell()
				cell.innerHTML = "Total Price of Products already in Cart"

				// First data row
				let dataRow
				dataRow = table.insertRow()
				cell = dataRow.insertCell()
				cell.innerHTML = supplier.name
				cell = dataRow.insertCell()
				cell.innerHTML = supplier.score
				cell = dataRow.insertCell()
				cell.innerHTML = supplier.cost + "$"

				// Find number of products from same seller and their values
				if(currentCart.cart.get(supplier.code) == null) {
					supplier.numProducts = 0
					supplier.valProducts = 0.0
				} else {
					// Get the number of products from the same seller
					supplier.numProducts = parseInt("0")
					currentCart.cart.get(supplier.code).forEach((prod) => {
						supplier.numProducts = parseInt(supplier.numProducts) + parseInt(prod.quantity)
					})


					// Get the price of products from the same seller
					supplier.valProducts = parseFloat("0.0")
					currentCart.cart.get(supplier.code).forEach((prod) => {
						for(let i = 0; i < parseInt(prod.quantity); i++){
							supplier.valProducts = parseFloat(supplier.valProducts) + parseFloat(prod.price)
						}

					})
				}

				cell = dataRow.insertCell()
				cell.innerHTML = supplier.numProducts
				cell.addEventListener("mouseover", (e) => {
					overlayDiv.style.display = "block"
					modal.style.display = "block"
					window.scrollTo({ top: 0, behavior: 'smooth' })
				})
				cell = dataRow.insertCell()
				cell.innerHTML = supplier.valProducts + "$"

				// Second header row
				headerRow = table.insertRow()
				headerRow.setAttribute("class", "second-header-row")
				cell = headerRow.insertCell()
				cell.innerHTML = "Min elements for shipping"
				cell = headerRow.insertCell()
				cell.innerHTML = "Max elements for shipping"
				cell = headerRow.insertCell()
				cell.innerHTML = "Shipping Cost"
				cell = headerRow.insertCell()
				cell.innerHTML = "Min expenses for free expedition"
				cell = headerRow.insertCell()

				// Add a div for the quantity selection and button to add the product to the cart
				let selectProductDiv = document.createElement("div")
				selectProductDiv.setAttribute("class", "select-product-div")
				cell.appendChild(selectProductDiv)

				let quantityForm = document.createElement("form")
				quantityForm.setAttribute("class", "quantity-form")
				selectProductDiv.appendChild(quantityForm)

				let formElement = document.createElement("label")
				formElement.setAttribute("class", "form-label")
				formElement.innerHTML = "Quantity:"
				quantityForm.appendChild(formElement)

				formElement = document.createElement("input")
				formElement.setAttribute("class", "form-number-selector")
				formElement.setAttribute("type", "number")
				formElement.setAttribute("required", "")
				formElement.setAttribute("name", "quantity")
				formElement.setAttribute("value", "1")
				formElement.setAttribute("min", "1")
				formElement.setAttribute("step", "1")
				quantityForm.appendChild(formElement)

				formElement = document.createElement("input")
				formElement.setAttribute("class", "primary-button")
				formElement.setAttribute("type", "submit")
				formElement.setAttribute("value", "ADD TO CART")
				// Set function to add en element to the cart
				formElement.addEventListener("click", (e) => {
					e.preventDefault()
					let form = e.target.closest("form")
					if(form.checkValidity()){
						this.addToCart(form.getElementsByClassName("form-number-selector")[0].value, supplier.code,
							productAndSuppliers.product.code, supplier.cost, productAndSuppliers.product.name,
							supplier.name)
					} else {
						form.reportValidity()
					}
				})
				quantityForm.appendChild(formElement)

				// Second data row
				supplier.policies.forEach((shipPolicy) => {

					// Add a row to the table and fill it with the current ship policy
					dataRow = table.insertRow()
					cell = dataRow.insertCell()
					cell.innerHTML = shipPolicy.min
					cell = dataRow.insertCell()
					cell.innerHTML = shipPolicy.max
					cell = dataRow.insertCell()
					cell.innerHTML = shipPolicy.cost + "$"
					cell = dataRow.insertCell()
					cell.innerHTML = shipPolicy.minGrat + "$"

				})

			})

		}

		this.addToCart = function (quantity, seller, product, price, productName, sellerName){

			// Get list of carted products from this seller
			let cartedProductsList = currentCart.cart.get(seller)

			// Add element to the list
			let hasProduct = false
			if(cartedProductsList === undefined){
				// Case when the seller does not exist
				cartedProductsList = []
			} else {
				// Check if element is already present in the list
				for(let i = 0; i < cartedProductsList.length; i++){
					if(cartedProductsList[i].productCode === product){
						// Update product quantity
						cartedProductsList[i].quantity = parseInt(cartedProductsList[i].quantity) + parseInt(quantity)
						hasProduct = true
					}
				}
			}

			if(!hasProduct){
				// Create a new CartedProduct
				let cartedProduct = new CartedProduct(product, productName, seller, quantity, price, sellerName)
				cartedProductsList.push(cartedProduct)
			}

			// Add element to the map
			currentCart.add(seller, cartedProductsList)

			// Show cart
			ShowCart()

		}

	}

	// Handles the orders page
	function Orders(){

		(
			this.makeReq = function makeRequest(){
				makeCall("GET", 'Orders', null, (req) => {
					switch(req.status){
						case 200: //ok
							let orders = JSON.parse(req.responseText);
							this.showMore(orders);
							break;
						case 400: // bad request
						case 401: // unauthorized
						case 500: // server error
							this.showMore(null);
							break;
						default: //Errors
							this.showMore(null);
							break;
					}
				})
			}()
		);

		this.showMore = function (orders){

			// Change css file
			let cssToRemove = document.getElementsByTagName("link");
			for(let i = 0; i < cssToRemove.length; i++){
				pageOrchestrator.deleteElement(cssToRemove[i])
			}
			let css = document.createElement("link")
			css.rel = 'stylesheet'
			css.type = 'text/css'
			css.href = 'http://localhost:8080/Shopping_Javascript_war/css/orders.css'
			pageOrchestrator.addCss(css)

			// Clear the content of the content div
			pageOrchestrator.killContent()

			if(orders.orderList.length === 0){

				// Create the div
				let div = document.createElement("div")
				div.setAttribute("class", "empty-order-list")
				contentDiv.appendChild(div)

				// Create the first h2
				let h2 = document.createElement("h2")
				h2.innerHTML = "Your order list seems to be empty..."
				div.appendChild(h2)

				// Create the second h2
				h2 = document.createElement("h2")
				h2.innerHTML = "Go and buy something ^_^"
				div.appendChild(h2)

				// Create the img
				let img = document.createElement("img")
				img.setAttribute("src", "./resources/emptyOrderList.jpg")
				div.appendChild(img)

			} else {
				orders.orderList.forEach((orderDetails) => {

					let orderDiv
					orderDiv = document.createElement("div")
					orderDiv.setAttribute("class", "orderList")
					contentDiv.appendChild(orderDiv)

					let leftSideDiv
					leftSideDiv = document.createElement("div")
					leftSideDiv.setAttribute("class", "left-side")
					orderDiv.appendChild(leftSideDiv)

					let rightSideDiv
					rightSideDiv = document.createElement("div")
					rightSideDiv.setAttribute("class", "right-side")
					orderDiv.appendChild(rightSideDiv)

					// Create content for the left side div
					// Order number
					let h4
					h4 = document.createElement("h4")
					leftSideDiv.appendChild(h4)
					let span1
					span1 = document.createElement("span")
					span1.innerHTML = "Order n. "
					h4.appendChild(span1)
					let span2
					span2 = document.createElement("span")
					span2.innerHTML = orderDetails.orderNumber
					h4.appendChild(span2)

					// Shipping date
					h4 = document.createElement("h4")
					leftSideDiv.appendChild(h4)
					span1 = document.createElement("span")
					span1.innerHTML = "Shipping date: "
					h4.appendChild(span1)
					span2 = document.createElement("span")
					span2.innerHTML = orderDetails.shippingDate
					h4.appendChild(span2)

					// Shipping address
					h4 = document.createElement("h4")
					leftSideDiv.appendChild(h4)
					span1 = document.createElement("span")
					span1.innerHTML = "Shipping address: "
					h4.appendChild(span1)
					span2 = document.createElement("span")
					span2.innerHTML = orderDetails.address
					h4.appendChild(span2)

					// Seller name
					h4 = document.createElement("h4")
					leftSideDiv.appendChild(h4)
					span1 = document.createElement("span")
					span1.innerHTML = "Seller: "
					h4.appendChild(span1)
					span2 = document.createElement("span")
					span2.innerHTML = orderDetails.sellerName
					h4.appendChild(span2)

					// Product card
					orderDetails.orderedProduct.forEach((product) => {

						// Div
						let productDiv
						productDiv = document.createElement("div")
						productDiv.setAttribute("class", "product-card")
						leftSideDiv.appendChild(productDiv)

						// Product pic
						let productImage
						productImage = document.createElement("img")
						productImage.setAttribute("src", "/Shopping_Javascript_war/resources/" +
							product.productCode + ".jpg")
						productDiv.appendChild(productImage)

						// Text div
						let textDiv
						textDiv = document.createElement("div")
						textDiv.setAttribute("class", "text-card")
						productDiv.appendChild(textDiv)

						// Product name
						h4 = document.createElement("h4")
						textDiv.appendChild(h4)
						span1 = document.createElement("span")
						span1.innerHTML = product.productName
						h4.appendChild(span1)

						// Product quantity
						h4 = document.createElement("h4")
						textDiv.appendChild(h4)
						span1 = document.createElement("span")
						span1.innerHTML = "Quantity: "
						h4.appendChild(span1)
						span2 = document.createElement("span")
						span2.innerHTML = product.quantity
						h4.appendChild(span2)


					})

					// Create content for the right side
					// Total due
					h4 = document.createElement("h4")
					rightSideDiv.appendChild(h4)
					span1 = document.createElement("span")
					span1.innerHTML = "Total due: "
					h4.appendChild(span1)
					span2 = document.createElement("span")
					span2.innerHTML = orderDetails.totalDue
					h4.appendChild(span2)
					let span3 = document.createElement("span")
					span3.innerHTML = " $"
					h4.appendChild(span3)

				})
			}

		}
	}

	// Constructor of the cart object
	function Cart(){
		this.cart = new Map()

		this.size = function(){
			return this.cart.size
		}

		this.add = function (key, value){
			this.cart.set(key, value)
		}
	}

	// Constructor of carted product object
	function CartedProduct(productCode, productName, seller, quantity, price, sellerName){

		this.productCode = productCode
		this.productName = productName
		this.sellerCode = seller
		this.quantity = quantity
		this.price = price
		this.sellerName = sellerName

	}

	// Handles the cart page
	function ShowCart(){
		(
			this.immediatlyCalled = function(){
				// Change css file
				let cssToRemove = document.getElementsByTagName("link");
				for(let i = 0; i < cssToRemove.length; i++){
					pageOrchestrator.deleteElement(cssToRemove[i])
				}
				let css = document.createElement("link")
				css.rel = 'stylesheet'
				css.type = 'text/css'
				css.href = 'http://localhost:8080/Shopping_Javascript_war/css/cart.css'
				pageOrchestrator.addCss(css)

				// Clear the content of the content div
				pageOrchestrator.killContent()

				// Depending on the number of elements present in the cart do one action
				if(currentCart.size() === 0){
					// Display empty cart error page
					// Create the div
					let div = document.createElement("div")
					div.setAttribute("class", "empty-cart")
					contentDiv.appendChild(div)

					// Create the first h2
					let h2 = document.createElement("h2")
					h2.innerHTML = "Your cart seems to be empty..."
					div.appendChild(h2)

					// Create the second h2
					h2 = document.createElement("h2")
					h2.innerHTML = "Try to add some products ^-^"
					div.appendChild(h2)

					// Create the img
					let img = document.createElement("img")
					img.setAttribute("src", "./resources/emptyCart.jpg")
					div.appendChild(img)
				} else {

					// Call the server to discover if the cart is legal and the prices of the things inside
					// Create request skeleton
					let request = new XMLHttpRequest()
					request.open("POST", "VerifyCart");
					request.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
					// Set request onreadystatechange function
					request.onreadystatechange = function() {
						switch(request.readyState){
							case XMLHttpRequest.UNSENT:
							case XMLHttpRequest.OPENED:
							case XMLHttpRequest.HEADERS_RECEIVED:
							case XMLHttpRequest.LOADING:
								break;
							case XMLHttpRequest.DONE: // Request ended
								if (checkRedirect("VerifyCart", request.responseURL)){ //Redirect if needed
									switch(request.status){
										case 200: //ok
											// Initialize Cart prices
											cartPrices = JSON.parse(request.responseText)
											// Display the cart
											printCart()
											break;
										default: //Errors
											showErrorPage("Your cart contains non-valid elements: it will be reset")
											currentCart = new Cart()
											cartPrices = new Cart()
											console.log("The cart has been reset")
											break;
									}
								}
								break;
						}
					};
					// Serialize cart
					let serializedCart = JSON.stringify(Object.fromEntries(currentCart.cart.entries()))
					// Send the request
					request.send("cart=" + serializedCart)
				}
			}()
		);
	}

	// Handles the printing of the cart
	function printCart(){
		// Display the cart
		for(let [key, value] of currentCart.cart){
			//Find costs of products from this seller
			let sellerCosts = cartPrices.cartCosts[key]

			// Create the seller card div
			let sellerProductList = document.createElement("div")
			sellerProductList.setAttribute("class", "sellerProductList")
			contentDiv.appendChild(sellerProductList)

			// Create the left side div
			let leftSideDiv = document.createElement("div")
			leftSideDiv.setAttribute("class", "left-side")
			sellerProductList.appendChild(leftSideDiv)

			// Create the title of the card
			let h3 = document.createElement("h3")
			leftSideDiv.appendChild(h3)

			let span = document.createElement("span")
			span.innerHTML = "Products from: "
			h3.appendChild(span)

			span = document.createElement("span")
			span.innerHTML = currentCart.cart.get(key)[0].sellerName
			h3.appendChild(span)

			currentCart.cart.get(key).forEach((prod) => {

				// Create the div of the product card
				let productCardDiv = document.createElement("div")
				productCardDiv.setAttribute("class", "product-card")
				leftSideDiv.appendChild(productCardDiv)

				// Add img
				let img = document.createElement("img")
				img.setAttribute("src", "./resources/" + prod.productCode + ".jpg")
				productCardDiv.appendChild(img)

				// Add div for the text
				let textDiv = document.createElement("div")
				productCardDiv.appendChild(textDiv)

				// Create the text for the above div
				// Product name
				let h4 = document.createElement("h4")
				textDiv.appendChild(h4)
				span = document.createElement("span")
				span.innerHTML = prod.productName
				h4.appendChild(span)
				span = document.createElement("span")
				span.innerHTML = " [ "
				h4.appendChild(span)
				span = document.createElement("span")
				span.innerHTML = prod.price
				h4.appendChild(span)
				span = document.createElement("span")
				span.innerHTML = " $ per unit ]"
				h4.appendChild(span)

				// Product quantity
				h4 = document.createElement("h4")
				textDiv.appendChild(h4)
				span = document.createElement("span")
				span.innerHTML = "Quantity: "
				h4.appendChild(span)
				span = document.createElement("span")
				span.innerHTML = prod.quantity
				h4.appendChild(span)

				// Total cost
				h4 = document.createElement("h4")
				textDiv.appendChild(h4)
				span = document.createElement("span")
				span.innerHTML = "Total cost: "
				h4.appendChild(span)
				span = document.createElement("span")
				span.innerHTML = prod.quantity * prod.price
				h4.appendChild(span)
				span = document.createElement("span")
				span.innerHTML = " $"
				h4.appendChild(span)

			})

			// Add the right part div
			let rightSideDiv = document.createElement("div")
			rightSideDiv.setAttribute("class", "right-side")
			sellerProductList.appendChild(rightSideDiv)

			// Add the text div
			let rightTextDiv = document.createElement("div")
			rightTextDiv.setAttribute("class", "right-side-text")
			rightSideDiv.appendChild(rightTextDiv)

			// Products cost
			h3 = document.createElement("h3")
			rightTextDiv.appendChild(h3)
			span = document.createElement("span")
			span.innerHTML = "Products Cost: "
			h3.appendChild(span)
			span = document.createElement("span")
			span.innerHTML = sellerCosts.productsCost
			h3.appendChild(span)
			span = document.createElement("span")
			span.innerHTML = " $"
			h3.appendChild(span)

			// Delivery cost
			h3 = document.createElement("h3")
			rightTextDiv.appendChild(h3)
			span = document.createElement("span")
			span.innerHTML = "Delivery Cost: "
			h3.appendChild(span)
			span = document.createElement("span")
			span.innerHTML = sellerCosts.deliveryCost
			h3.appendChild(span)
			span = document.createElement("span")
			span.innerHTML = " $"
			h3.appendChild(span)

			// Total due
			h3 = document.createElement("h3")
			rightTextDiv.appendChild(h3)
			span = document.createElement("span")
			span.innerHTML = "Total Due: "
			h3.appendChild(span)
			span = document.createElement("span")
			span.innerHTML = sellerCosts.total
			h3.appendChild(span)
			span = document.createElement("span")
			span.innerHTML = " $"
			h3.appendChild(span)

			// Order products button
			let button = document.createElement("button")
			button.setAttribute("class", "primary-button")
			button.setAttribute("type", "submit")
			button.innerHTML = "ORDER PRODUCTS"
			button.addEventListener("click", (e) => {
				// Call the function that handles the ordering process
				orderProducts(key)
			})
			rightSideDiv.appendChild(button)

		}
	}

	// Handles the ordering process
	function orderProducts(seller){

		// Do a POST request to the server, sending the necessary to make an order

		// Create request skeleton
		let request = new XMLHttpRequest()
		request.open("POST", "AddToOrders");
		request.setRequestHeader("Content-type", "application/x-www-form-urlencoded")

		// Set request onreadystatechange function
		request.onreadystatechange = function() {
			switch(request.readyState){
				case XMLHttpRequest.UNSENT:
					console.log("Connecting to Server ...");
					break;
				case XMLHttpRequest.OPENED:
					console.log("Connected to Server ...");
					break;
				case XMLHttpRequest.HEADERS_RECEIVED:
				case XMLHttpRequest.LOADING:
					console.log("Waiting for response ...");
					break;
				case XMLHttpRequest.DONE: // Request ended
					console.log("Request completed");
					if (checkRedirect("AddToOrders", request.responseURL)){ //Redirect if needed
						switch(request.status){
							case 200: //ok
								// Remove stuff from the cart and show orders page
								currentCart.cart.delete(seller)
								Orders()
								break;
							case 400: // bad request
								showErrorPage("Requested seller is not present in the cart")
								break;
							case 401: // unauthorized
							case 500: // server error
								showErrorPage("Error in db access")
								break;
							case 418: // user tried to order illegal products
								showErrorPage("Hacker detected: you should not put non-valid items in the cart")
								break;
							default: //Errors
								showErrorPage("Oh no! An error occurred...")
								break;
						}
					}
					break;
			}
		};

		// Serialize cart
		let serializedCart = JSON.stringify(Object.fromEntries(currentCart.cart.entries()))

		// Send the request
		request.send("cart=" + serializedCart + "&seller=" + seller)
	}

	// Builds a general error page
	function showErrorPage(error){
		// Change css file
		let cssToRemove = document.getElementsByTagName("link");
		for(let i = 0; i < cssToRemove.length; i++){
			pageOrchestrator.deleteElement(cssToRemove[i])
		}
		let css = document.createElement("link")
		css.rel = 'stylesheet'
		css.type = 'text/css'
		css.href = 'http://localhost:8080/Shopping_Javascript_war/css/error.css'
		pageOrchestrator.addCss(css)

		// Remove content from the page
		pageOrchestrator.killContent()

		// Create text of the error
		let h3 = document.createElement("h3")
		h3.setAttribute("id", "errorH3")
		h3.innerHTML = error
		contentDiv.appendChild(h3)

		// Put a gif
		let img = document.createElement("img")
		img.setAttribute("id", "errorImg")
		img.setAttribute("src", "./resources/error.gif")
		contentDiv.appendChild(img)
	}

})();