var globalNames = [];
/**********************************************************************************************/
function editContact(contactID){
  $("tr").css("cursor", "wait");

  $.ajax({
    url: '/api/v1/contact/' + contactID,
    dataType:'json',
    success: function(data){
      if(data == 'InvalidLogin'){
        logout();
      }

      $.ajax({
        url: '/api/v1/fieldcaptions?fields=ContactId,' + allContactFieldCaptionNames.join(","),
        dataType: 'json',
        success: function(names){
          if(names == 'InvalidLogin'){
            logout();
          }
    
          globalNames = names;
    
          showEditScreen(data, names);
          $("tr").css("cursor", "pointer");
        },
        error: function(jqXhr, textStatus, errorThrown){
          if(jqXhr.responseText == 'InvalidLogin'){
            logout();
          }
    
          alert(errorThrown);
          $("tr").css("cursor", "pointer");
        }
      });    
    },
    error: function(jqXhr, textStatus, errorThrown){
      if(jqXhr.responseText == 'InvalidLogin'){
        logout();
      }

      alert(errorThrown);
    }
  });
}
/**********************************************************************************************/

function showEditScreen(data, names){
  var contactTitle = "<div>";
      contactTitle += "<i class='fa fa-user' style='margin-right:8px;'></i>";
      contactTitle += "Contact Details For " + data.ContactPerson.replace(/&/g, '&#38').replace(/</g, '&#60').replace(/>/g, '&#62').replace(/"/g, '&#34');
      contactTitle += "</div>";

      // FIRST/LAST NAME
  var body = "<form role='form' name='details' method='POST'>";
      body += " <input type='hidden' name='ContactId' value='" + data['ContactId'] + "' id='cId'>";
      body += " <div class='row form-group'>";
      body += "   <div class='col-xs-6'>";
      body += names['ContactFirstName'];
      body += "     <input name='FirstName' class='form-control' value='" + data.ContactFirstName.replace(/&/g, '&#38').replace(/'/g, '&#39').replace(/</g, '&#60').replace(/>/g, '&#62').replace(/"/g, '&#34') + "'>";
      body += "   </div>";
      body += "   <div class='col-xs-6'>";
      body += names['ContactLastName'];
      body += "     <input name='LastName' class='form-control' value='" + data.ContactLastName.replace(/&/g, '&#38').replace(/'/g, '&#39').replace(/</g, '&#60').replace(/>/g, '&#62').replace(/"/g, '&#34') + "'>";
      body += "   </div>";
      body += " </div>";
      
      // COMPANY
      body += " <div class='row form-group'>";
      body += "   <div class='col-xs-12'>";
      body += names['ContactCompany'];
      body += "     <input name='Company' class='form-control' value='" + data.ContactCompany.replace(/&/g, '&#38').replace(/'/g, '&#39').replace(/</g, '&#60').replace(/>/g, '&#62').replace(/"/g, '&#34') + "'>";
      body += "   </div>";
      body += " </div>";
      
      // EMAIL
      body += " <div class='row form-group'>";
      body += "   <div class='col-xs-6'>";
      body += "     <i class='fa fa-envelope-o' title='" + names.ContactEmail.replace(/&/g, '&#38').replace(/'/g, '&#39').replace(/</g, '&#60').replace(/>/g, '&#62').replace(/"/g, '&#34') + "'></i> " + names['ContactEmail'];
      body += "     <input name='Email' class='form-control' value='" + data.ContactEmail.replace(/&/g, '&#38').replace(/'/g, '&#39').replace(/</g, '&#60').replace(/>/g, '&#62').replace(/"/g, '&#34') + "'>";
      body += "   </div>";
      body += " </div>";

      //Communications
      body += " <div class='row form-group'>";
      // PHONE
      var phoneArr = data['ContactPhones'].split(",");
      
//      body += " <div class='form-group'>";
      body += "   <div class='col-xs-6'>";
      body += "   <strong>" + names['ContactPhones'] + "</strong>";
      
      $.each(phoneArr, function(){
        body += "<div>" + this + "</div>";
      });
      
      body += " </div>";

      // ADDRESSES
      var addressArr = data['ContactAddresses'].split(";");

//      body += " <div class='form-group'>";
      body += "   <div class='col-xs-6'>";
      body += "   <strong>" + names['ContactAddresses'] + "</strong>";

      $.each(addressArr, function(){
        body += "<div>" + this + "</div>";
      });

      body += " </div>";
      body += " </div>";

      // NOTES      
      body += " <div class='form-group'>";
      body += names['ContactNotes'];
      body += "   <textarea name='Notes' class='form-control' style='min-height:150px; height:auto; white-space:pre-wrap;'>" + data.ContactNotes.replace(/(?:\r\n |\r |\n )/g, '&#10;') + "</textarea>";
      body += " </div>";
      
      // COMMENTS
      body += " <div class='form-group'>";
      body += names['ContactComments'];
      body += "   <input type='text' name='Comments' class='form-control' value=\"" + data.ContactComments.replace(/(?:\r\n |\r |\n )/g, '&#10;').replace(/&/g, '&#38').replace(/'/g, '&#39').replace(/</g, '&#60').replace(/>/g, '&#62').replace(/"/g, '&#34') + "\" />";
      body += " </div>";
      
      //body += "<div class='form-group'>";
      //body += " <button type='button' class='btn btn-primary'>Contact Groups</button>";
      //body += "</div>";


      // CUSTOM Contact FIELDS
      
      body += " <div class='row'>";

      body += createCustomFields(data, names);

      // body += "   </div>";
      
      // CUSTOM LISTS

      body += createCustomLists(data, names);

      body += " </div>";
      body += "</div>";

      body += "<div class='row'>";
      body += " <div class='col-xs-12'>";
      body += "   <button type='button' class='btn btn-default' onclick=\"editContactCustomFieldOptions();\" title='Edit contact custom field and lists'><i class='fa fa-plus-circle'></i><span class='hidden-xs'> Custom Field Options</span></button>";
      body += " </div>";
      body += "</div>";
      
      body += "</form>";
      
      
  // Contact EDIT POP UP
  BootstrapDialog.show({
    title: contactTitle,
    message: body,
    //draggable: true,
    size: BootstrapDialog.SIZE_WIDE,
    buttons: [{
      label:'<i class="fa fa-trash"></i><span class="hidden-xs"> Delete Contact</span>',
      cssClass:'btn-danger',
      action: function(dialogRef){
        BootstrapDialog.show({
          type: BootstrapDialog.TYPE_DANGER,
          title: "Confirm Delete",
          message: "Are you sure you want to delete <strong>" + data.ContactPerson + "</strong>?",
          buttons:[{
            label: "Yes, Delete This Contact",
            cssClass: "btn-danger",
            action: function(dialogRef){
              $.ajax({
                url: "/api/v1/contact/" + data.ContactId,
                method: 'DELETE',
                success: function(response){
                  if(response == 'InvalidLogin'){
                    logout();
                  }
            
                  BootstrapDialog.closeAll();
                  createContactList();
                },
                error: function(jqXhr, textStatus, errorThrown){
                  if(jqXhr.responseText == 'InvalidLogin'){
                    logout();
                  }
                
                  BootstrapDialog.show({
                    title: "Problem With Delete Contact",
                    message: "There was a problem deleting the Contact: <span style='color:red;'>" + errorThrown + "</span>",
                    type: BootstrapDialog.TYPE_DANGER,
                    buttons: [{
                      label: "Ok",
                      action: function(dialogRef){
                        dialogRef.close();
                      }
                    }]
                  });
                }
              });
              dialogRef.close();            
            } 
          },{ 
            label: "No",
            action: function(dialogRef){
              dialogRef.close();
            }
          }]
        });        
      }    
    },{
      label:'Cancel',
      cssClass:'btn-default',
      action: function(dialogRef){
        dialogRef.close();
      }    
    },{
      label: 'Save<span class="hidden-xs"> Contact Details</span>',
      cssClass:'btn-primary',
      action: function(dialogRef){
        var fieldArray = $("form[name=details]").serializeArray();

        // $.each(fieldArray, function(){
        //   if(this.value == "" || this.value == " "){
        //     this.value = "None";
        //   }
        // });

        var formData = $.param(fieldArray);

        $.ajax({
          url: '/api/v1/contact/' + data.ContactId,
          data: formData,
          method: 'PUT',
          success: function(data){
            if(data == 'InvalidLogin'){
              logout();
            }
      
            dialogRef.close();
            createContactList();
          },
          error: function(jqXhr, textStatus, errorThrown){
            if(jqXhr.responseText == 'InvalidLogin'){
              logout();
            }
      
            alert(errorThrown);
          }
        });
      }
    }] 
  });
}
/**********************************************************************************************/

function editContactCustomFieldOptions(){
  var msg = "";
      msg += "<div class='row'>";
      msg += "  <div class='col-xs-12 col-lg-6'>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";
      
      if(eval(Cookies.get("ShowContactCustomField1"))){
        msg += "<input type='checkbox' id='cCustomField1Toggle' checked='checked'> Show " + globalNames.ContactCustomField1;
      }else{
        msg += "<input type='checkbox' id='cCustomField1Toggle'> Show " + globalNames.ContactCustomField1;
      }
      
      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";
      
      if(eval(Cookies.get("ShowContactCustomField2"))){
        msg += "<input type='checkbox' id='cCustomField2Toggle' checked='checked'> Show " + globalNames.ContactCustomField2;
      }else{
        msg += "<input type='checkbox' id='cCustomField2Toggle'> Show " + globalNames.ContactCustomField2;
      }
      
      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";
      
      if(eval(Cookies.get("ShowContactCustomField3"))){
        msg += "<input type='checkbox' id='cCustomField3Toggle' checked='checked'> Show " + globalNames.ContactCustomField3;
      }else{
        msg += "<input type='checkbox' id='cCustomField3Toggle'> Show " + globalNames.ContactCustomField3;
      }
      
      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";
      
      if(eval(Cookies.get("ShowContactCustomField4"))){
        msg += "<input type='checkbox' id='cCustomField4Toggle' checked='checked'> Show " + globalNames.ContactCustomField4;
      }else{
        msg += "<input type='checkbox' id='cCustomField4Toggle'> Show " + globalNames.ContactCustomField4;
      }
      
      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";
      
      if(eval(Cookies.get("ShowContactCustomField5"))){
        msg += "<input type='checkbox' id='cCustomField5Toggle' checked='checked'> Show " + globalNames.ContactCustomField5;
      }else{
        msg += "<input type='checkbox' id='cCustomField5Toggle'> Show " + globalNames.ContactCustomField5;
      }
      
      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";
      
      if(eval(Cookies.get("ShowContactCustomField6"))){
        msg += "<input type='checkbox' id='cCustomField6Toggle' checked='checked'> Show " + globalNames.ContactCustomField6;
      }else{
        msg += "<input type='checkbox' id='cCustomField6Toggle'> Show " + globalNames.ContactCustomField6;
      }
      
      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "  </div>";
      msg += "  <div class='col-xs-12 col-lg-6'>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";

      if(eval(Cookies.get("ShowContactCustomList1"))){
        msg += "<input type='checkbox' id='cCustomList1Toggle' checked='checked'> Show " + globalNames.ContactCustomList1;
      }else{
        msg += "<input type='checkbox' id='cCustomList1Toggle'> Show " + globalNames.ContactCustomList1;
      }

      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";

      if(eval(Cookies.get("ShowContactCustomList2"))){
        msg += "<input type='checkbox' id='cCustomList2Toggle' checked='checked'> Show " + globalNames.ContactCustomList2;
      }else{
        msg += "<input type='checkbox' id='cCustomList2Toggle'> Show " + globalNames.ContactCustomList2;
      }

      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";

      if(eval(Cookies.get("ShowContactCustomList3"))){
        msg += "<input type='checkbox' id='cCustomList3Toggle' checked='checked'> Show " + globalNames.ContactCustomList3;
      }else{
        msg += "<input type='checkbox' id='cCustomList3Toggle'> Show " + globalNames.ContactCustomList3;
      }

      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";

      if(eval(Cookies.get("ShowContactCustomList4"))){
        msg += "<input type='checkbox' id='cCustomList4Toggle' checked='checked'> Show " + globalNames.ContactCustomList4;
      }else{
        msg += "<input type='checkbox' id='cCustomList4Toggle'> Show " + globalNames.ContactCustomList4;
      }

      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";

      if(eval(Cookies.get("ShowContactCustomList5"))){
        msg += "<input type='checkbox' id='cCustomList5Toggle' checked='checked'> Show " + globalNames.ContactCustomList5;
      }else{
        msg += "<input type='checkbox' id='cCustomList5Toggle'> Show " + globalNames.ContactCustomList5;
      }

      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";

      if(eval(Cookies.get("ShowContactCustomList6"))){
        msg += "<input type='checkbox' id='cCustomList6Toggle' checked='checked'> Show " + globalNames.ContactCustomList6;
      }else{
        msg += "<input type='checkbox' id='cCustomList6Toggle'> Show " + globalNames.ContactCustomList6;
      }

      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";      
      msg += "  </div>";
      msg += "</div>";

  BootstrapDialog.show({
    title: 'Contact Custom Field and List Options',
    message: msg,
    buttons: [{
      label: 'Cancel',
      cssClass: 'btn-default',
      action: function(dialogRef){
        dialogRef.close();
      }
    },{
      label: 'Save',
      cssClass: 'btn-primary',
      action: function(dialogRef){
        // custom fields
      
        if($("#cCustomField1Toggle").prop("checked")){
          setCookie("ShowContactCustomField1", true);
          $("#ContactCustomField1").attr("disabled", false);
          $("#ContactCustomField1Wrap").removeClass("hidden");
        }else{
          setCookie("ShowContactCustomField1", false);
          $("#ContactCustomField1").attr("disabled", true);
          $("#ContactCustomField1Wrap").addClass("hidden");
        }

        if($("#cCustomField2Toggle").prop("checked")){
          setCookie("ShowContactCustomField2", true);
          $("#ContactCustomField2").attr("disabled", false);
          $("#ContactCustomField2Wrap").removeClass("hidden");
        }else{
          setCookie("ShowContactCustomField2", false);
          $("#ContactCustomField2").attr("disabled", true);
          $("#ContactCustomField2Wrap").addClass("hidden");
        }
        
        if($("#cCustomField3Toggle").prop("checked")){
          setCookie("ShowContactCustomField3", true);
          $("#ContactCustomField3").attr("disabled", false);
          $("#ContactCustomField3Wrap").removeClass("hidden");
        }else{
          setCookie("ShowContactCustomField3", false);
          $("#ContactCustomField3").attr("disabled", true);
          $("#ContactCustomField3Wrap").addClass("hidden");
        }
        
        if($("#cCustomField4Toggle").prop("checked")){
          setCookie("ShowContactCustomField4", true);
          $("#ContactCustomField4").attr("disabled", false);
          $("#ContactCustomField4Wrap").removeClass("hidden");
        }else{
          setCookie("ShowContactCustomField4", false);
          $("#ContactCustomField4").attr("disabled", true);
          $("#ContactCustomField4Wrap").addClass("hidden");
        }
        
        if($("#cCustomField5Toggle").prop("checked")){
          setCookie("ShowContactCustomField5", true);
          $("#ContactCustomField5").attr("disabled", false);
          $("#ContactCustomField5Wrap").removeClass("hidden");
        }else{
          setCookie("ShowContactCustomField5", false);
          $("#ContactCustomField5").attr("disabled", true);
          $("#ContactCustomField5Wrap").addClass("hidden");
        }
        
        if($("#cCustomField6Toggle").prop("checked")){
          setCookie("ShowContactCustomField6", true);
          $("#ContactCustomField6").attr("disabled", false);
          $("#ContactCustomField6Wrap").removeClass("hidden");
        }else{
          setCookie("ShowContactCustomField6", false);
          $("#ContactCustomField6").attr("disabled", true);
          $("#ContactCustomField6Wrap").addClass("hidden");
        }

        // custom lists

        if($("#cCustomList1Toggle").prop("checked")){
          setCookie("ShowContactCustomList1", true);
          $("#ContactCustomList1").attr("disabled", false);
          $("#ContactCustomList1Wrap").removeClass("hidden");
        }else{
          setCookie("ShowContactCustomList1", false);
          $("#ContactCustomList1").attr("disabled", true);
          $("#ContactCustomList1Wrap").addClass("hidden");
        }

        if($("#cCustomList2Toggle").prop("checked")){
          setCookie("ShowContactCustomList2", true);
          $("#ContactCustomList2").attr("disabled", false);
          $("#ContactCustomList2Wrap").removeClass("hidden");
        }else{
          setCookie("ShowContactCustomList2", false);
          $("#ContactCustomList2").attr("disabled", true);
          $("#ContactCustomList2Wrap").addClass("hidden");
        }
        
        if($("#cCustomList3Toggle").prop("checked")){
          setCookie("ShowContactCustomList3", true);
          $("#ContactCustomList3").attr("disabled", false);
          $("#ContactCustomList3Wrap").removeClass("hidden");
        }else{
          setCookie("ShowContactCustomList3", false);
          $("#ContactCustomList3").attr("disabled", true);
          $("#ContactCustomList3Wrap").addClass("hidden");
        }
        
        if($("#cCustomList4Toggle").prop("checked")){
          setCookie("ShowContactCustomList4", true);
          $("#ContactCustomList4").attr("disabled", false);
          $("#ContactCustomList4Wrap").removeClass("hidden");
        }else{
          setCookie("ShowContactCustomList4", false);
          $("#ContactCustomList4").attr("disabled", true);
          $("#ContactCustomList4Wrap").addClass("hidden");
        }
        
        if($("#cCustomList5Toggle").prop("checked")){
          setCookie("ShowContactCustomList5", true);
          $("#ContactCustomList5").attr("disabled", false);
          $("#ContactCustomList5Wrap").removeClass("hidden");
        }else{
          setCookie("ShowContactCustomList5", false);
          $("#ContactCustomList5").attr("disabled", true);
          $("#ContactCustomList5Wrap").addClass("hidden");
        }
        
        if($("#cCustomList6Toggle").prop("checked")){
          setCookie("ShowContactCustomList6", true);
          $("#ContactCustomList6").attr("disabled", false);
          $("#ContactCustomList6Wrap").removeClass("hidden");
        }else{
          setCookie("ShowContactCustomList6", false);
          $("#ContactCustomList6").attr("disabled", true);
          $("#ContactCustomList6Wrap").addClass("hidden");
        }
        
        dialogRef.close();
      }
    }]
  });
}

function createCustomLists(data, names) {
  return `<div class='col-xs-12 col-lg-6'>
            ${[1, 2, 3, 4, 5, 6].map(i =>
    createCustomList(i, data, names, 'ContactCustomList')).join('')}
        </div>`.trim().replace(/\n\s+/g, '');
}

function createCustomList(index, data, names, prefix) {
  const cookieName = `Show${prefix}${index}`;
  const isVisible = Cookies.get(cookieName) === 'true';

  return `
        <div class="input-group form-group ${!isVisible ? 'hidden' : ''}" id="${prefix}${index}Wrap">
            <div class="input-group-btn">
                <button class="btn btn-default" type="button">${names[`${prefix}${index}`]}</button>
            </div>
            ${createCustomListSelect("CustomList" + index, data)}
        </div>
    `.trim().replace(/\n\s+/g, '');
}

function createCustomListSelect(customListName, data) {

  const baseUrl = '/api/v1/customList';
  const captionWithPrefix = 'Contact' + customListName;
  const dataUrl = baseUrl + "/" + captionWithPrefix;
  let body = '';

  $.ajax({
    async: false,
    url: dataUrl,
    dataType: 'json',
    success: function (response) {
      if (response === 'InvalidLogin') return logout();

      body += ` <select class='form-control' id=${captionWithPrefix} name=${customListName}>`;
      body += "   <option value=''></option>";

      $.each(response, function () {
        this.Name = $.trim(this);
        if (this.Name === data[captionWithPrefix]) {
          body += "<option value=\"" + this.Name + "\" selected>" + this.Name + "</option>";
        } else {
          body += "<option value=\"" + this.Name + "\">" + this.Name + "</option>";
        }
      });

      body += "     </select>";

    },
    error: handleRequestError
  });

  return body;
}

function handleRequestError(jqXhr) {
  BootstrapDialog.closeAll();
  console.error('Request failed:', jqXhr.responseText);
  BootstrapDialog.alert({
    title: 'Error',
    message: 'Failed to load contacts. Please try again later.'
  });
}

function createCustomFields(data, names) {
  return `<div class="col-xs-12 col-sm-6">
            ${[1, 2, 3, 4, 5, 6].map(i =>
    createCustomField(i, data, names, 'ContactCustomField'))
    .join('')} 
            </div>`.trim().replace(/\n\s+/g, '');
}

function createCustomField(index, data, names, prefix) {
  const cookieName = `Show${prefix}${index}`;
  const isVisible = Cookies.get(cookieName) === 'true';

  return `<div class="input-group form-group ${!isVisible ? 'hidden' : ''}" id="${prefix}${index}Wrap">
        <div class="input-group-btn">
            <button class="btn btn-default" type="button">${names[`${prefix}${index}`]}</button>
        </div>
        <input type="text" name="CustomField${index}" id="${prefix}${index}" value="${escapeHtml(data[`${prefix}${index}`] || '')}" class="form-control" ${!isVisible ? 'disabled' : ''}>
    </div>`.trim().replace(/\n\s+/g, '');
}

function escapeHtml(str) {
  if (str === undefined || str === null) return '';
  return str.toString()
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

/**********************************************************************************************/



