/**********************************************************************************************/
function editNewContact(contactID){
  $.ajax({
    url: '/api/v1/fieldcaptions?fields=ContactId,' + allContactFieldCaptionNames.join(","),
    dataType: 'json',
    success: function(names){
      if(names == 'InvalidLogin'){
        logout();
      } 

      showNewContactEditScreen(names);
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

function showNewContactEditScreen(names){
  var contactTitle = "<div>";
      contactTitle += "<i class='fa fa-user' style='margin-right:8px;'></i>";
      contactTitle += "Details For New Contact";
      contactTitle += "</div>";

      // FIRST/LAST NAME
  var body = "<form role='form' name='details' method='POST'>";
      body += " <input type='hidden' value='' id='cId'>";
      body += " <div class='row form-group'>";
      body += "   <div class='col-xs-6'>";
      body += names.ContactFirstName;
      body += "     <input name='FirstName' class='form-control' value=''>";
      body += "   </div>";
      body += "   <div class='col-xs-6'>";
      body += names['ContactLastName'];
      body += "     <input name='LastName' class='form-control' value=''>";
      body += "   </div>";
      body += " </div>";
      
      // COMPANY
      body += " <div class='row form-group'>";
      body += "   <div class='col-xs-12'>";
      body += names['ContactCompany'];
      body += "     <input name='Company' class='form-control' value=''>";
      body += "   </div>";
      body += " </div>";
      
      // EMAIL
      body += " <div class='row form-group'>";
      body += "   <div class='col-xs-6'>";
      body += "     <i class='fa fa-envelope-o' title='" + names['ContactEmail'] + "'></i> " + names['ContactEmail'];
      body += "     <input name='Email' class='form-control' value=''>";
      body += "   </div>";
      body += " </div>";
      
      // PHONE
      //var phoneArr = data['ContactPhones'].split(",");
      body += " <div class='form-group'>";
      body += "   <strong>" + names['ContactPhones'] + "</strong>";
      
      //$.each(phoneArr, function(){
        //body += "<div>Need Phone Inputs</div>";
      //});
      
      body += " </div>";
      
      // NOTES      
      body += " <div class='form-group'>";
      body += "   <label>" + names['ContactNotes'] + "</label>";
      body += "   <textarea name='Notes' class='form-control' style='min-height:150px; height:auto; white-space:pre-wrap;'></textarea>";
      body += " </div>";
      
      // COMMENTS
      body += " <div class='form-group'>";
      body += names['ContactComments'];
      body += "   <input name='Comments' class='form-control' value=''>";
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
        body += "       <input type='text' name='CustomField1' id='ContactCustomField1' value='' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='ContactCustomField1Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField1 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField1' disabled='true' id='ContactCustomField1' value='' class='form-control' />";      
        body += "     </div>";
      }
      
      if(eval(Cookies.get("ShowContactCustomField2"))){
        body += "     <div class='input-group form-group' id='ContactCustomField2Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField2 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField2' id='ContactCustomField2' value='' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='ContactCustomField2Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField2 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField2' disabled='true' id='ContactCustomField2' value='' class='form-control' />";      
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowContactCustomField3"))){
        body += "     <div class='input-group form-group' id='ContactCustomField3Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField3 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField3' id='ContactCustomField3' value='' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='ContactCustomField3Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField3 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField3' disabled='true' id='ContactCustomField3' value='' class='form-control' />";      
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowContactCustomField4"))){
        body += "     <div class='input-group form-group' id='ContactCustomField4Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField4 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField4' id='ContactCustomField4' value='' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='ContactCustomField4Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField4 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField4' disabled='true' id='ContactCustomField4' value='' class='form-control' />";      
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowContactCustomField5"))){
        body += "     <div class='input-group form-group' id='ContactCustomField5Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField5 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField5' id='ContactCustomField5' value='' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='ContactCustomField5Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField5 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField5' disabled='true' id='ContactCustomField5' value='' class='form-control' />";      
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowContactCustomField6"))){
        body += "     <div class='input-group form-group' id='ContactCustomField6Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField6 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField6' id='ContactCustomField6' value='' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='ContactCustomField6Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.ContactCustomField6 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField6' disabled='true' id='ContactCustomField6' value='' class='form-control' />";      
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
        body += "       <input type='text' name='CustomList1' id='ContactCustomList1' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList1' disabled='true' id='ContactCustomList1' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList2' id='ContactCustomList2' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList2' disabled='true' id='ContactCustomList2' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList3' id='ContactCustomList3' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList3' disabled='true' id='ContactCustomList3' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList4' id='ContactCustomList4' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList4' disabled='true' id='ContactCustomList4' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList5' id='ContactCustomList5' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList5' disabled='true' id='ContactCustomList5' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList6' id='ContactCustomList6' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList6' disabled='true' id='ContactCustomList6' value='' class='form-control' />";
        body += "     </div>";
      }

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
      label:'Close',
      cssClass:'btn-default',
      action: function(dialogRef){
        dialogRef.close();
      }    
    },{
      label: 'Save New Contact',
      cssClass:'btn-primary',
      action: function(dialogRef){
        if($("input[name=FirstName]").val() == "" || $("input[name=LastName]").val() == ""){
          BootstrapDialog.show({
            type: BootstrapDialog.TYPE_WARNING,
            title: "Missing Information",
            message: "Please make sure you have entered a name for this new Contact",
            buttons: [{
              label: "Ok",
              action: function(dialogRef){
                dialogRef.close();
              },
              cssClass: "btn-primary"
            }]
          });
          
          return;
        }
        
        var formData = $("form[name=details]").serialize();      
        
        $.ajax({
          url: '/api/v1/contact', // + formData,
          data: formData,
          method: 'POST',
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
