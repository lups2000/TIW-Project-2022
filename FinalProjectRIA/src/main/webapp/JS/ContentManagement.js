{ //avoids variables ending up in the global scope

    //let variables are visible only in this block between {}
	let foldersTree,documentList,documentDetails;
    let pageOrchestrator = new PageOrchestrator(); //pageOrchestrator is the main controller client side
    let startElement; //for drag and drop
    

    window.addEventListener("load", ()=>{ //"load" event is fired when the entire page has loaded completely

        if(sessionStorage.getItem("username") == null){
            window.location.href = "Login.html";
        }
        else{
            pageOrchestrator.start();
            pageOrchestrator.refresh();
        }
    },false);
	
	function PageOrchestrator(){ //main controller that contains two "functions" 

        var alertContainer = document.getElementById("id_alert").querySelector("h5"); //h5 element

        this.start = function(){
			//welcome message
            welcomeMsg = new WelcomeMessage(sessionStorage.getItem('username'), document.getElementById("id_username"));
            welcomeMsg.show(); //Nice to see you again...
            
            foldersTree = new FoldersTree(alertContainer,null);
            documentList = new DocumentsList(document.getElementById("id_alert"));
            documentDetails = new DocumentDetails(document.getElementById("id_alert"));
        	
        	//handle the logout from the application
        	document.querySelector("a[href='Logout']").addEventListener('click', () => {
	        	window.sessionStorage.removeItem('username');
	      })
        };

        this.refresh = function(){
			alertContainer.textContent="";
			document.getElementById("subFoldCreationDiv").style.display = "none";
			document.getElementById("docCreationDiv").style.display = "none";
			foldersTree.reset();
			documentList.reset();
			documentDetails.reset();
			foldersTree.show();
        };

    }

	//welcome message function
    function WelcomeMessage(user,messageContainer){

        this.show = function(){
            messageContainer.textContent = user;
        }
    };

	//folders Tree function
    function FoldersTree(_alert,_treeContainer){
        this.alert=_alert;
        this.treeContainer=_treeContainer;


        this.show = function(){
			
            var self = this;
            //Ajax call to obtain the folders tree
            makeCall("GET","GetFoldersTree",null,
            function(request){
                if(request.readyState==XMLHttpRequest.DONE){ //the server had finished to upload the content and the browser to download it
                    var message = request.responseText;
                    if(request.status == 200){
	
                        var foldersToShow = JSON.parse(message); //deserialization
                        
                        if(foldersToShow.length == 0){
                            self.alert.textContent = "You haven't created a folder yet!";
                            return;
                        }
                        else{
							//show the tree of folders and subfolders
							self.update(foldersToShow);
						}
                    }
                    else if(request.status == 403){ //access forbidden
						window.location.href = request.getResponseHeader("Location");
                  		window.sessionStorage.removeItem('username');	
					}
					else{
						self.alert.textContent = message;
					}
                }
            });
        }
        
        this.update = function(foldersTree){
		
			var divContainer = document.getElementById("generalDiv");
			var ul = document.createElement('ul'); //to create le list
			var buttonFolder;
			var buttonSubFolder;
			var self=this;
			
			divContainer.innerHTML="";
			divContainer.style.display="block";
			
			foldersTree.forEach(function(folder){
				var li = document.createElement('li');
				var nameFolder = document.createElement("label");
				nameFolder.textContent = folder.name;
				
				//for drag and drop
				nameFolder.draggable=true;
				nameFolder.addEventListener("dragstart", ()=> {dragStart(folder)});
				
				li.appendChild(nameFolder);
				ul.appendChild(li);	
				
				buttonFolder=document.createElement('button');
				linkText = document.createTextNode("Add Subfolder");
				buttonFolder.appendChild(linkText);
				buttonFolder.setAttribute('class',"btnFolder");
				buttonFolder.setAttribute('folderId',folder.id);
				li.appendChild(buttonFolder);
				
				var innerUl = document.createElement('ul');
				var subFolders = folder.subFolders;
				li.appendChild(innerUl);
				
				subFolders.forEach(function(subFolder){
					var innerli = document.createElement('li');
					innerli.setAttribute('class',"subFolderLi");
					var subFolderName = document.createElement("label");
					subFolderName.textContent = subFolder.name;
					
					//for drag and drop
					subFolderName.draggable=true;
					subFolderName.addEventListener("dragstart", ()=> {dragStart(subFolder)});
					innerli.appendChild(subFolderName);
					subFolderName.addEventListener("dragover", (event)=> dragOver(event));
					subFolderName.addEventListener("drop", (event)=> drop(event,subFolder));
					
					innerUl.appendChild(innerli);
					
					buttonOpenSubFolder = document.createElement('button'); //create button to open a subFolder
					linkText=document.createTextNode("Open");
					buttonOpenSubFolder.appendChild(linkText);
					buttonOpenSubFolder.setAttribute('class',"btnSubFolder");
					buttonOpenSubFolder.setAttribute('subFolderId',subFolder.id);
					
					buttonSubFolder = document.createElement('button'); //create button to add new Documents
					linkText = document.createTextNode("Add Document");
					buttonSubFolder.appendChild(linkText);
					buttonSubFolder.setAttribute('class',"btnSubFolder");
					buttonSubFolder.setAttribute('subFolderId',subFolder.id);
					
					innerli.appendChild(buttonOpenSubFolder);
					innerli.appendChild(buttonSubFolder);
					
					buttonSubFolder.addEventListener("click", ()=>{ //click button Add Document
						divContainer.style.display="none";
						var alertMsgGeneral = document.getElementById("id_alert");
						alertMsgGeneral.querySelector("h5").innerHTML="";	
						self.formCreateDocument(subFolder);
					});
					
					buttonOpenSubFolder.addEventListener("click",()=>{ //click button Open
						
						var alertMsgGeneral = document.getElementById("id_alert");
						alertMsgGeneral.querySelector("h5").innerHTML="";
						var logoutButton=document.getElementById("logout");
						divContainer.style.display="none";
						logoutButton.style.display="none";
						document.getElementById("welcomeMsg").style.display="none";
						documentList.show(subFolder);
						
					});
					
				});
				
				buttonFolder.addEventListener("click", ()=>{ //click button Add SubFolder
					divContainer.style.display="none";
					var alertMsgGeneral = document.getElementById("id_alert");
					alertMsgGeneral.querySelector("h5").innerHTML="";
					self.formCreateSubFolder(folder);
					
				});
			});
			
			divContainer.appendChild(ul); //append the list to the general Div
			
			//BIN
			var bin =document.createElement("h6");
			bin.id = "BIN";
			bin.setAttribute("class","Bin");
			bin.textContent="Delete";
			
			//for drag and drop
			bin.addEventListener("dragover",dragOver);
			bin.addEventListener("drop",drop);
			
			divContainer.appendChild(bin);
		}
		
		//function-->creation subFolder
		this.formCreateSubFolder = function(folder){
			
			var subFolderCreationDiv = document.getElementById("subFoldCreationDiv");
			
			subFolderCreationDiv.style.display="block";
			const title = subFolderCreationDiv.querySelector("h5");
			title.innerHTML = "Create a SubFolder in "+folder.name+":";
			
			const buttonCreation = subFolderCreationDiv.querySelector("#id_create_subFold");
			const buttonCancel = subFolderCreationDiv.querySelector("#id_cancel_subFold");
			
	
			buttonCancel.addEventListener("click",()=>{
				window.location.href="Home.html";
			});
			
			buttonCreation.addEventListener("click", (e)=>{
				
				var form = e.target.closest("form");
				
				if(form.checkValidity()){
					var folderId=folder.id;
					
					makeCall("POST","CreateSubFolder?folderId="+folderId,e.target.closest("form"),
                		function(x){ //this is the callBack Function
		                    if (x.readyState == XMLHttpRequest.DONE) { //the server had finished to return the response and the browser to download it
		                        var message = x.responseText;
		                        var errorMessage = subFolderCreationDiv.querySelector("#id_errorSubFold").querySelector("p");
		                  		
		                        switch (x.status) {
		                          case 200:
									  subFolderCreationDiv.style.display="none";
		                              pageOrchestrator.refresh();
		                              break;
		                          case 400: // bad request
			                          errorMessage.textContent = message;
			                          break;
		                          case 401: // unauthorized
			                          errorMessage.textContent = message;
			                          break;
		                          case 500: // server error
			                          errorMessage.textContent = message;
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
		}
		
		//function-->create a new document
		this.formCreateDocument = function(subFolder){
			
			var documentCreationDiv = document.getElementById("docCreationDiv");
		
			documentCreationDiv.style.display="block";
			const title = documentCreationDiv.querySelector("h5");
			title.innerHTML = "Create a SubFolder in "+subFolder.name+":";
			
			const buttonCreation = documentCreationDiv.querySelector("#id_create_doc");
			const buttonCancel = documentCreationDiv.querySelector("#id_cancel_doc");
			
			buttonCancel.addEventListener("click",()=>{
				window.location.href="Home.html";
			});
			
			buttonCreation.addEventListener("click",(e)=>{
				var form = e.target.closest("form");
				
				if(form.checkValidity()){
					var subFolderId=subFolder.id;
					
					makeCall("POST","CreateDocument?subFolderId="+subFolderId,e.target.closest("form"),
                		function(x){ //this is the callBack Function
		                    if (x.readyState == XMLHttpRequest.DONE) { //the server had finished to return the response and the browser to download it
		                        var message = x.responseText;
		                        var errorMessage = documentCreationDiv.querySelector("#id_errorDoc").querySelector("p");
		                  		
		                        switch (x.status) {
		                          case 200:
									  documentCreationDiv.style.display="none";
		                              pageOrchestrator.refresh();
		                              break;
		                          case 400: // bad request
			                          errorMessage.textContent = message;
			                          break;
		                          case 401: // unauthorized
			                          errorMessage.textContent = message;
			                          break;
		                          case 500: // server error
			                          errorMessage.textContent = message;
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
		}
		
		this.reset = function(){
			var generalDiv = document.getElementById("generalDiv");
			generalDiv.style.display = "none";
		}

    }
    
    //function-->document list
    function DocumentsList(_alert){
		
		this.alert=_alert;
		
		this.show = function(subFolder){
			var self=this;
			makeCall("GET","GetDocuments?subFolderId="+subFolder.id,null,
        		function(x){ //this is the callBack Function
                    if (x.readyState == XMLHttpRequest.DONE) { //the server had finished to return the response and the browser to download it
                        
                        var message = x.responseText;
                        var divContainer = document.getElementById("docDivDisplay");
                        divContainer.innerHTML="";
						var backButton = document.getElementById("back");
						divContainer.style.display ="block";
						backButton.style.display="block";

                        var textH4 = document.createElement("h4");
						textH4.innerHTML=subFolder.name;
						divContainer.appendChild(textH4);
						var errorMsg=self.alert.querySelector("h5");
						errorMsg.innerHTML="";
						
						backButton.addEventListener("click",()=>{
							window.location.href="Home.html";
						});
      
                        if(x.status == 200){

	                        var documentsToShow = JSON.parse(message); //deserialization
	                        
	                        if(documentsToShow.length == 0){
	                            errorMsg.textContent = "You haven't created a document yet!";
	                            return;
	                        }
	                        else{
								//show the documents
								self.update(documentsToShow,divContainer);
							}
	                    }
	                    else if(x.status == 403){ //access forbidden
							window.location.href = req.getResponseHeader("Location");
	                  		window.sessionStorage.removeItem('username');	
						}
						else{
							errorMsg.textContent = message;
						}

                    }
        		}
			); //end function makeCall
		}

		this.update = function(documents,divDocsContainer){

			var ul = document.createElement('ul');

			documents.forEach(function(doc){
				var li = document.createElement('li');
				var docName = document.createElement("label");
				docName.textContent=doc.name;
				li.appendChild(docName);
				
				//for drag and drop
				docName.draggable=true;
				docName.addEventListener("dragstart",()=>dragStart(doc));
				docName.addEventListener("drag",()=>drag());
				
				var buttonOpenDocument = document.createElement('button'); //create button to open a document
				linkText=document.createTextNode("Open");
				buttonOpenDocument.appendChild(linkText);
				buttonOpenDocument.setAttribute('class',"btnDocument");
				buttonOpenDocument.setAttribute('documentId',doc.id);
				li.appendChild(buttonOpenDocument);
				ul.appendChild(li);	
				
				buttonOpenDocument.addEventListener("click",()=>{ //click button Open
					var logoutButton=document.getElementById("logout");
					logoutButton.style.display="none";
					document.getElementById("welcomeMsg").style.display="none";
					divDocsContainer.style.display="none";
					documentDetails.show(doc);
				});
			});
			divDocsContainer.appendChild(ul);
		}
		
		this.reset = function(){
			var docDivDisplay = document.getElementById("docDivDisplay");
			docDivDisplay.style.display = "none";
		}
	}
	
	function DocumentDetails(_alert){
		
		this.alert=_alert;
		
		this.show = function(doc){
			
			var self=this;
			makeCall("GET","GetDocumentDetails?documentId="+doc.id,null,function(x){
				if (x.readyState == XMLHttpRequest.DONE) { //the server had finished to return the response and the browser to download it
                        
                    var message = x.responseText;
                    var errorMsg = self.alert.querySelector("h5");
                    
                    if(x.status == 200){

                        var documentToShow = JSON.parse(message); //deserialization
                        
                        self.update(documentToShow,document.getElementById("documentInfoDiv"));
                    }
                    else if(x.status == 403){ //access forbidden
						window.location.href = "Login.html";
                  		window.sessionStorage.removeItem('username');	
					}
					else{
						errorMsg.textContent = message;
					}

                }
			});
		}
		
		this.update = function(doc,divDocDetails){
			
			divDocDetails.style.display = "block";
			divDocDetails.querySelector("h4").innerHTML=doc.name;
			var p = document.createElement("p");
			p.innerHTML="<strong>Summary:</strong> "+doc.summary+"<br>";
			divDocDetails.appendChild(p);
			p=document.createElement("p");
			p.innerHTML="<strong>Type:</strong> "+doc.type+"<br>";
			divDocDetails.appendChild(p);
			p=document.createElement("p");
			p.innerHTML="<strong>Date Creation:</strong> "+doc.date+"<br>";
			divDocDetails.appendChild(p);
		}
		
		this.reset = function(){
			
			var divDocDetails = document.getElementById("documentInfoDiv");
			divDocDetails.style.display = "none";
			divDocDetails.firstChild.textContent = "";
			
		}
	}
	
	//drag and drop Management
	
	function dragStart(object){
		startElement = object;
	}
	
	function dragOver(event){
		event.preventDefault();
	}
	
	function drag(){
		
		document.getElementById("docDivDisplay").style.display ="none";
		var genDiv = document.getElementById("generalDiv");
		genDiv.style.display="block";
	}
	
	function drop(event,object){
		
		event.preventDefault();
		var destination = event.target;
		
		
		if(destination.id != "BIN"){ //move doc
			if(confirm("Are you sure to move this item?")==true){
		
				
			if(startElement.summary == undefined){ //startElement is a subFolder or a folder
		
				document.getElementById("id_alert").querySelector("h5").textContent = "You cannot move a folder into another folder!";
			}
			else{
				var formData = new FormData();
				formData.append("documentId",startElement.id);
				formData.append("subFolderDestId",object.id);
				
				makeCall("POST","MoveDocument",formData,function(x){
					
					if(x.readyState == XMLHttpRequest.DONE){
						
						var errorMessage = x.responseText;
						switch(x.status){
							
							case 200:
								pageOrchestrator.refresh();
								document.getElementById("id_alert").querySelector("h5").textContent = "element successfully moved!"
								break;
							case 400:
								document.getElementById("id_alert").querySelector("h5").textContent=errorMessage;
								break;
						    case 401:
							    window.location.href = "Login.html";
	                            window.sessionStorage.removeItem('username');
	                            break;
							case 403:
							    window.location.href = "Login.html";
	                            window.sessionStorage.removeItem('username');
	                            break;
	                        case 500:
	                        	window.location.href = "Login.html";
	                            window.sessionStorage.removeItem('username');
	                            break;
	                        default:
	                        	document.getElementById("id_alert").querySelector("h5").textContent=errorMessage;
								break;  
						}
					}
				});
					startElement=undefined;
				}
			}
		}
		else{
			//delete element
			if(confirm("Are you sure to delete this item?")==true){
					var formData = new FormData();
					
					formData.append("objectId",startElement.id);
					/*
					-Folder: objectId,userId != undefined / summary,type == undefined
					-SubFolder: objectId != undefined / userId,summary,type == undefined
					-Document: objectId,userId,summary,type != undefined
					*/
					if(startElement.id_owner == undefined){
						formData.append("userId","");
						if(startElement.summary == undefined){
							formData.append("summary","");
						}
						if(startElement.type == undefined){
							formData.append("type","");
						}
						else{
							formData.append("summary",startElement.summary);
							formData.append("type",startElement.type);
						}
					}
					else{
						formData.append("userId",startElement.id_owner);
						if(startElement.summary == undefined){
							formData.append("summary","");
						}
						if(startElement.type == undefined){
							formData.append("type","");
						}
						else{
							formData.append("summary",startElement.summary);
							formData.append("type",startElement.type);
						}
					}
					
					makeCall("POST","DeleteContent",formData,function(x){
						
						if(x.readyState == XMLHttpRequest.DONE){
							
							var errorMessage = x.responseText;
							switch(x.status){
								
								case 200:
									pageOrchestrator.refresh();
									document.getElementById("id_alert").querySelector("h5").textContent = "element successfully eliminated!"
									break;
								case 400:
									document.getElementById("id_alert").querySelector("h5").textContent=errorMessage;
									break;
							    case 401:
								    window.location.href = "Login.html";
		                            window.sessionStorage.removeItem('username');
		                            break;
								case 403:
								    window.location.href = "Login.html";
		                            window.sessionStorage.removeItem('username');
		                            break;
		                        case 500:
		                        	window.location.href = "Login.html";
		                            window.sessionStorage.removeItem('username');
		                            break;
		                        default:
		                        	document.getElementById("id_alert").querySelector("h5").textContent=errorMessage;
									break;  
							}
						}
					});	
					startElement = undefined;
			}
		}
	}

};