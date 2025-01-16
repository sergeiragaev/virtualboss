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
      
      // PHONE
      var phoneArr = data['ContactPhones'].split(",");
      
      body += " <div class='form-group'>";
      body += "   <strong>" + names['ContactPhones'] + "</strong>";
      
      $.each(phoneArr, function(){
        body += "<div>" + this + "</div>";
      });
      
      body += " </div>";
      
      // NOTES      
      body += " <div class='form-group'>";
      body += "   <label>" + names['ContactNotes'] + "</label>";
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
      body += "   <div class='col-xs-12 col-sm-6'>";
      
      if(eval(Cookies.get("ShowContactCustomField1"))){
        body += "     <div class='input-group form-group' id='ContactCustomField1Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField1 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField1' id='ContactCustomField1' value='" + data.ContactCustomField1 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='ContactCustomField1Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField1 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField1' disabled='true' id='ContactCustomField1' value='" + data.ContactCustomField1 + "' class='form-control' />";      
        body += "     </div>";
      }
      
      if(eval(Cookies.get("ShowContactCustomField2"))){
        body += "     <div class='input-group form-group' id='ContactCustomField2Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField2 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField2' id='ContactCustomField2' value='" + data.ContactCustomField2 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='ContactCustomField2Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField2 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField2' disabled='true' id='ContactCustomField2' value='" + data.ContactCustomField2 + "' class='form-control' />";      
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowContactCustomField3"))){
        body += "     <div class='input-group form-group' id='ContactCustomField3Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField3 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField3' id='ContactCustomField3' value='" + data.ContactCustomField3 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='ContactCustomField3Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField3 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField3' disabled='true' id='ContactCustomField3' value='" + data.ContactCustomField3 + "' class='form-control' />";      
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowContactCustomField4"))){
        body += "     <div class='input-group form-group' id='ContactCustomField4Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField4 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField4' id='ContactCustomField4' value='" + data.ContactCustomField4 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='ContactCustomField4Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField4 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField4' disabled='true' id='ContactCustomField4' value='" + data.ContactCustomField4 + "' class='form-control' />";      
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowContactCustomField5"))){
        body += "     <div class='input-group form-group' id='ContactCustomField5Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField5 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField5' id='ContactCustomField5' value='" + data.ContactCustomField5 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='ContactCustomField5Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField5 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField5' disabled='true' id='ContactCustomField5' value='" + data.ContactCustomField5 + "' class='form-control' />";      
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowContactCustomField6"))){
        body += "     <div class='input-group form-group' id='ContactCustomField6Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField6 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField6' id='ContactCustomField6' value='" + data.ContactCustomField6 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='ContactCustomField6Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField6 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField6' disabled='true' id='ContactCustomField6' value='" + data.ContactCustomField6 + "' class='form-control' />";      
        body += "     </div>";
      }

      body += "   </div>";
      
      // CUSTOM LISTS
      
      body += "   <div class='col-xs-12 col-lg-6'>";

      if(eval(Cookies.get("ShowContactCustomList1"))){
        body += "     <div class='input-group form-group' id='ContactCustomList1Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomList1 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.ContactCustomList1 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList1' id='ContactCustomList1' value='" + data.ContactCustomList1 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='ContactCustomList1Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomList1 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.ContactCustomList1 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList1' disabled='true' id='ContactCustomList1' value='" + data.ContactCustomList1 + "' class='form-control' />";
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowContactCustomList2"))){
        body += "     <div class='input-group form-group' id='ContactCustomList2Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomList2 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.ContactCustomList1 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList2' id='ContactCustomList2' value='" + data.ContactCustomList2 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='ContactCustomList2Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomList2 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.ContactCustomList2 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList2' disabled='true' id='ContactCustomList2' value='" + data.ContactCustomList2 + "' class='form-control' />";
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowContactCustomList3"))){
        body += "     <div class='input-group form-group' id='ContactCustomList3Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomList3 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.ContactCustomList3 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList3' id='ContactCustomList3' value='" + data.ContactCustomList3 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='ContactCustomList3Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomList3 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.ContactCustomList1 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList3' disabled='true' id='ContactCustomList3' value='" + data.ContactCustomList3 + "' class='form-control' />";
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowContactCustomList4"))){
        body += "     <div class='input-group form-group' id='ContactCustomList4Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomList4 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.ContactCustomList4 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList4' id='ContactCustomList4' value='" + data.ContactCustomList4 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='ContactCustomList4Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomList4 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.ContactCustomList4 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList4' disabled='true' id='ContactCustomList4' value='" + data.ContactCustomList4 + "' class='form-control' />";
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowContactCustomList5"))){
        body += "     <div class='input-group form-group' id='ContactCustomList5Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomList5 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.ContactCustomList5 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList5' id='ContactCustomList5' value='" + data.ContactCustomList5 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='ContactCustomList5Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomList5 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.ContactCustomList5 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList5' disabled='true' id='ContactCustomList5' value='" + data.ContactCustomList5 + "' class='form-control' />";
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowContactCustomList6"))){
        body += "     <div class='input-group form-group' id='ContactCustomList6Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomList6 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.ContactCustomList6 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList6' id='ContactCustomList6' value='" + data.ContactCustomList6 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='ContactCustomList6Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomList6 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.ContactCustomList6 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList6' disabled='true' id='ContactCustomList6' value='" + data.ContactCustomList6 + "' class='form-control' />";
        body += "     </div>";
      }

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

/**********************************************************************************************/



