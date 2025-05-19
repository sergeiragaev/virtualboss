var globalNames = [];
/**********************************************************************************************/
function editJob(jobID){
  $.ajax({
    url: '/api/v1/job/' + jobID,
    dataType:'json',
    success: function(data){
      if(data == 'InvalidLogin'){
        logout();
      }

      $.ajax({
        url: '/api/v1/fieldcaptions?fields=JobId,' + allJobFieldCaptionNames.join(",") + "," + allOwnerFieldCaptionNames.join(','),
        dataType: 'json',
        success: function(names){
          if(names == 'InvalidLogin'){
            logout();
          }
    
          globalNames = names;
          
          showEditScreen(data, names);
        },
        error: function(jqXhr, textStatus, errorThrown){
          if(jqXhr.responseText == 'InvalidLogin'){
            logout();
          }
    
          alert(errorThrown);
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
  var body = "";
      body += "<form role='form' method='POST' name='details'>";
      body += "<input type='hidden' name='JobId' value='" + data.JobId + "'>";
      // JOB NAME
      body += " <div class='form-group'>";
      body += names['JobNumber'];
      //body += "   <input name='JobNumber' class='form-control' value='" + stringEncode(data.JobNumber) + "'>";
      body += "   <input type='text' name='Number' class='form-control' value=\"" + data.JobNumber.replace(/&/g, '&#38').replace(/</g, '&#60').replace(/>/g, '&#62').replace(/"/g, '&#34').replace(/'/g, '&#39') + "\" />";
      body += " </div>";
      
      // LOT #
      body += "<div class='row form-group'>";
      body += " <div class='col-xs-6 col-lg-4'>";
      body += names['JobLot'];
      body += "   <input name='Lot' type='text' class='form-control' value=\"" + data.JobLot.replace(/&/g, '&#38').replace(/</g, '&#60').replace(/>/g, '&#62').replace(/"/g, '&#34').replace(/'/g, '&#39') + "\">";
      body += " </div>";
      
      // SUBDIVISION
      body += " <div class='col-xs-6 col-lg-4'>";
      body += names['JobSubdivision'];
      body += "   <input name='Subdivision' type='text' class='form-control' value=\"" + data.JobSubdivision.replace(/&/g, '&#38').replace(/</g, '&#60').replace(/>/g, '&#62').replace(/"/g, '&#34').replace(/'/g, '&#39') + "\">";
      body += " </div>";
      
      // LOCK BOX COMBO
      body += " <div class='col-xs-12 col-lg-4'>";
      body += names['JobLockBox'];
      body += "   <input type='text' name='Lock_Box_Combo' class='form-control' value=\"" + data.JobLockBox.replace(/&/g, '&#38').replace(/</g, '&#60').replace(/>/g, '&#62').replace(/"/g, '&#34').replace(/'/g, '&#39') + "\">";
      body += " </div>";
      body += "</div>";
            
      // OWNERS NAME
      body += "<div class='row form-group'>";
      body += " <div class='col-xs-6 col-lg-4'>";
      body += names['ContactPerson'];
      body += "   <input type='text' name='Owners_Name' class='form-control' value=\"" + data.ContactPerson.replace(/&/g, '&#38').replace(/</g, '&#60').replace(/>/g, '&#62').replace(/"/g, '&#34').replace(/'/g, '&#39') + "\">";
      body += " </div>";
      
      // COMPANY NAME
      body += " <div class='col-xs-6 col-lg-4'>";
      body += names['ContactCompany'];
      body += "   <input type='text' name='Company_Name' class='form-control' value=\"" + data.ContactCompany.replace(/&/g, '&#38').replace(/</g, '&#60').replace(/>/g, '&#62').replace(/"/g, '&#34').replace(/'/g, '&#39') + "\">";
      body += " </div>";
      
      // EMAIL
      body += " <div class='col-xs-6 col-lg-4'>";
      body += names['ContactEmail'];
      body += "   <input name='Email' type='email' class='form-control' value=\"" + data.ContactEmail.replace(/&/g, '&#38').replace(/</g, '&#60').replace(/>/g, '&#62').replace(/"/g, '&#34').replace(/'/g, '&#39') + "\">";
      body += " </div>";
      body += "</div>";
      
      // DIRECTIONS TO JOB SITE
      body += "<div class='form-group'>";
      body += names['JobDirections'];
      body += " <textarea name='Direction_to_job_site' class='form-control'>" + data.JobDirections.replace(/(?:\r\n |\r |\n )/g, '&#10;') + "</textarea>";
      body += "</div>";
      
      // JOB NOTES
      body += "<div class='form-group'>";
      body += names['JobNotes'];
      body += " <textarea name='Notes' class='form-control'>" + data.JobNotes.replace(/(?:\r\n |\r |\n )/g, '&#10;') + "</textarea>";
      body += "</div>";
      

      // CUSTOM Job FIELDS
      
      body += " <div class='row'>";
      body += createCustomFields(data, names);

      // CUSTOM LISTS
      body += createCustomLists(data, names);

      body += " </div>";
      body += "</div>";
      
      body += "<div class='row'>";
      body += " <div class='col-xs-12'>";
      body += "   <button type='button' class='btn btn-default' onclick=\"editJobCustomFieldOptions();\" title='Edit job custom field and lists'><i class='fa fa-plus-circle'></i><span class='hidden-xs'> Custom Field Options</span></button>";
      body += " </div>";
      body += "</div>";
      
      body += "</form>";
      
  // Job EDIT POP UP
  BootstrapDialog.show({
    title: "<div>Job Details For " + data.JobNumber.replace(/&/g, '&#38').replace(/</g, '&#60').replace(/>/g, '&#62').replace(/"/g, '&#34').replace(/'/g, '&#39') + "</div>",
    message: body,
    //draggable: true,
    size: BootstrapDialog.SIZE_WIDE,
    buttons: [{
      label:'Delete Job',
      cssClass:'btn-danger',
      action: function(dialogRef){
        BootstrapDialog.show({
          type: BootstrapDialog.TYPE_DANGER,
          title: "Confirm Delete",
          message: "Are you sure you want to delete <strong>" + data.JobNumber.replace(/&/g, '&#38').replace(/</g, '&#60').replace(/>/g, '&#62').replace(/"/g, '&#34').replace(/'/g, '&#39') + "</strong>?",
          buttons:[{
            label: "Yes, Delete This Job",
            cssClass: "btn-danger",
            action: function(dialogRef){
              $.ajax({
                url: "/api/v1/job/" + data.JobId,
                method: 'DELETE',
                success: function(response){
                  if(response == 'InvalidLogin'){
                    logout();
                  }
            
                  BootstrapDialog.closeAll();
                  createJobList();
                },
                error: function(jqXhr, textStatus, errorThrown){
                  if(jqXhr.responseText == 'InvalidLogin'){
                    logout();
                  }
            
                  BootstrapDialog.show({
                    title: "Problem With Delete Job",
                    message: "There was a problem deleting the Job: <span style='color:red;'>" + errorThrown + "</span>",
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
      label:'Close',
      cssClass:'btn-default',
      action: function(dialogRef){
        dialogRef.close();
      }    
    },{
      label: 'Save Changes',
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
          //url: "/api/v1/SaveJob?JobId=" + data.JobId + "&" + formData,
          url: "/api/v1/job/" + data.JobId, // + formData,
          data: formData,
          method: 'PUT',
          success: function(response){
            if(response == 'InvalidLogin'){
              logout();
            }
      
            dialogRef.close();
            createJobList();
          },
          error: function(jqXhr, textStatus, errorThrown){
            if(jqXhr.responseText == 'InvalidLogin'){
              logout();
            }
            // alert(errorThrown);
              BootstrapDialog.show({
                  title: "Error Updating Job",
                  message: jqXhr.responseJSON.message, //errorThrown,
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
      }
    }] 
  });
}

/**********************************************************************************************/

function editJobCustomFieldOptions(){
  var msg = "";
      msg += "<div class='row'>";
      msg += "  <div class='col-xs-12 col-lg-6'>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";
      
      if(eval(Cookies.get("ShowJobCustomField1"))){
        msg += "<input type='checkbox' id='jCustomField1Toggle' checked='checked'> Show " + globalNames.JobCustomField1;
      }else{
        msg += "<input type='checkbox' id='jCustomField1Toggle'> Show " + globalNames.JobCustomField1;
      }
      
      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";
      
      if(eval(Cookies.get("ShowJobCustomField2"))){
        msg += "<input type='checkbox' id='jCustomField2Toggle' checked='checked'> Show " + globalNames.JobCustomField2;
      }else{
        msg += "<input type='checkbox' id='jCustomField2Toggle'> Show " + globalNames.JobCustomField2;
      }
      
      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";
      
      if(eval(Cookies.get("ShowJobCustomField3"))){
        msg += "<input type='checkbox' id='jCustomField3Toggle' checked='checked'> Show " + globalNames.JobCustomField3;
      }else{
        msg += "<input type='checkbox' id='jCustomField3Toggle'> Show " + globalNames.JobCustomField3;
      }
      
      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";
      
      if(eval(Cookies.get("ShowJobCustomField4"))){
        msg += "<input type='checkbox' id='jCustomField4Toggle' checked='checked'> Show " + globalNames.JobCustomField4;
      }else{
        msg += "<input type='checkbox' id='jCustomField4Toggle'> Show " + globalNames.JobCustomField4;
      }
      
      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";
      
      if(eval(Cookies.get("ShowJobCustomField5"))){
        msg += "<input type='checkbox' id='jCustomField5Toggle' checked='checked'> Show " + globalNames.JobCustomField5;
      }else{
        msg += "<input type='checkbox' id='jCustomField5Toggle'> Show " + globalNames.JobCustomField5;
      }
      
      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";
      
      if(eval(Cookies.get("ShowJobCustomField6"))){
        msg += "<input type='checkbox' id='jCustomField6Toggle' checked='checked'> Show " + globalNames.JobCustomField6;
      }else{
        msg += "<input type='checkbox' id='jCustomField6Toggle'> Show " + globalNames.JobCustomField6;
      }
      
      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "  </div>";
      msg += "  <div class='col-xs-12 col-lg-6'>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";

      if(eval(Cookies.get("ShowJobCustomList1"))){
        msg += "<input type='checkbox' id='jCustomList1Toggle' checked='checked'> Show " + globalNames.JobCustomList1;
      }else{
        msg += "<input type='checkbox' id='jCustomList1Toggle'> Show " + globalNames.JobCustomList1;
      }

      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";

      if(eval(Cookies.get("ShowJobCustomList2"))){
        msg += "<input type='checkbox' id='jCustomList2Toggle' checked='checked'> Show " + globalNames.JobCustomList2;
      }else{
        msg += "<input type='checkbox' id='jCustomList2Toggle'> Show " + globalNames.JobCustomList2;
      }

      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";

      if(eval(Cookies.get("ShowJobCustomList3"))){
        msg += "<input type='checkbox' id='jCustomList3Toggle' checked='checked'> Show " + globalNames.JobCustomList3;
      }else{
        msg += "<input type='checkbox' id='jCustomList3Toggle'> Show " + globalNames.JobCustomList3;
      }

      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";

      if(eval(Cookies.get("ShowJobCustomList4"))){
        msg += "<input type='checkbox' id='jCustomList4Toggle' checked='checked'> Show " + globalNames.JobCustomList4;
      }else{
        msg += "<input type='checkbox' id='jCustomList4Toggle'> Show " + globalNames.JobCustomList4;
      }

      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";

      if(eval(Cookies.get("ShowJobCustomList5"))){
        msg += "<input type='checkbox' id='jCustomList5Toggle' checked='checked'> Show " + globalNames.JobCustomList5;
      }else{
        msg += "<input type='checkbox' id='jCustomList5Toggle'> Show " + globalNames.JobCustomList5;
      }

      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";

      if(eval(Cookies.get("ShowJobCustomList6"))){
        msg += "<input type='checkbox' id='jCustomList6Toggle' checked='checked'> Show " + globalNames.JobCustomList6;
      }else{
        msg += "<input type='checkbox' id='jCustomList6Toggle'> Show " + globalNames.JobCustomList6;
      }

      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";      
      msg += "  </div>";
      msg += "</div>";

  BootstrapDialog.show({
    title: 'Job Custom Field and List Options',
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
      
        if($("#jCustomField1Toggle").prop("checked")){
          setCookie("ShowJobCustomField1", true);
          $("#JobCustomField1").attr("disabled", false);
          $("#JobCustomField1Wrap").removeClass("hidden");
        }else{
          setCookie("ShowJobCustomField1", false);
          $("#JobCustomField1").attr("disabled", true);
          $("#JobCustomField1Wrap").addClass("hidden");
        }

        if($("#jCustomField2Toggle").prop("checked")){
          setCookie("ShowJobCustomField2", true);
          $("#JobCustomField2").attr("disabled", false);
          $("#JobCustomField2Wrap").removeClass("hidden");
        }else{
          setCookie("ShowJobCustomField2", false);
          $("#JobCustomField2").attr("disabled", true);
          $("#JobCustomField2Wrap").addClass("hidden");
        }
        
        if($("#jCustomField3Toggle").prop("checked")){
          setCookie("ShowJobCustomField3", true);
          $("#JobCustomField3").attr("disabled", false);
          $("#JobCustomField3Wrap").removeClass("hidden");
        }else{
          setCookie("ShowJobCustomField3", false);
          $("#JobCustomField3").attr("disabled", true);
          $("#JobCustomField3Wrap").addClass("hidden");
        }
        
        if($("#jCustomField4Toggle").prop("checked")){
          setCookie("ShowJobCustomField4", true);
          $("#JobCustomField4").attr("disabled", false);
          $("#JobCustomField4Wrap").removeClass("hidden");
        }else{
          setCookie("ShowJobCustomField4", false);
          $("#JobCustomField4").attr("disabled", true);
          $("#JobCustomField4Wrap").addClass("hidden");
        }
        
        if($("#jCustomField5Toggle").prop("checked")){
          setCookie("ShowJobCustomField5", true);
          $("#JobCustomField5").attr("disabled", false);
          $("#JobCustomField5Wrap").removeClass("hidden");
        }else{
          setCookie("ShowJobCustomField5", false);
          $("#JobCustomField5").attr("disabled", true);
          $("#JobCustomField5Wrap").addClass("hidden");
        }
        
        if($("#jCustomField6Toggle").prop("checked")){
          setCookie("ShowJobCustomField6", true);
          $("#JobCustomField6").attr("disabled", false);
          $("#JobCustomField6Wrap").removeClass("hidden");
        }else{
          setCookie("ShowJobCustomField6", false);
          $("#JobCustomField6").attr("disabled", true);
          $("#JobCustomField6Wrap").addClass("hidden");
        }

        // custom lists

        if($("#jCustomList1Toggle").prop("checked")){
          setCookie("ShowJobCustomList1", true);
          $("#JobCustomList1").attr("disabled", false);
          $("#JobCustomList1Wrap").removeClass("hidden");
        }else{
          setCookie("ShowJobCustomList1", false);
          $("#JobCustomList1").attr("disabled", true);
          $("#JobCustomList1Wrap").addClass("hidden");
        }

        if($("#jCustomList2Toggle").prop("checked")){
          setCookie("ShowJobCustomList2", true);
          $("#JobCustomList2").attr("disabled", false);
          $("#JobCustomList2Wrap").removeClass("hidden");
        }else{
          setCookie("ShowJobCustomList2", false);
          $("#JobCustomList2").attr("disabled", true);
          $("#JobCustomList2Wrap").addClass("hidden");
        }
        
        if($("#jCustomList3Toggle").prop("checked")){
          setCookie("ShowJobCustomList3", true);
          $("#JobCustomList3").attr("disabled", false);
          $("#JobCustomList3Wrap").removeClass("hidden");
        }else{
          setCookie("ShowJobCustomList3", false);
          $("#JobCustomList3").attr("disabled", true);
          $("#JobCustomList3Wrap").addClass("hidden");
        }
        
        if($("#jCustomList4Toggle").prop("checked")){
          setCookie("ShowJobCustomList4", true);
          $("#JobCustomList4").attr("disabled", false);
          $("#JobCustomList4Wrap").removeClass("hidden");
        }else{
          setCookie("ShowJobCustomList4", false);
          $("#JobCustomList4").attr("disabled", true);
          $("#JobCustomList4Wrap").addClass("hidden");
        }
        
        if($("#jCustomList5Toggle").prop("checked")){
          setCookie("ShowJobCustomList5", true);
          $("#JobCustomList5").attr("disabled", false);
          $("#JobCustomList5Wrap").removeClass("hidden");
        }else{
          setCookie("ShowJobCustomList5", false);
          $("#JobCustomList5").attr("disabled", true);
          $("#JobCustomList5Wrap").addClass("hidden");
        }
        
        if($("#jCustomList6Toggle").prop("checked")){
          setCookie("ShowJobCustomList6", true);
          $("#JobCustomList6").attr("disabled", false);
          $("#JobCustomList6Wrap").removeClass("hidden");
        }else{
          setCookie("ShowJobCustomList6", false);
          $("#JobCustomList6").attr("disabled", true);
          $("#JobCustomList6Wrap").addClass("hidden");
        }
        
        dialogRef.close();
      }
    }]
  });
}

function createCustomLists(data, names) {
  return `<div class='col-xs-12 col-lg-6'>
            ${[1, 2, 3, 4, 5, 6].map(i =>
    createCustomList(i, data, names, 'JobCustomList')).join('')}
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
  const captionWithPrefix = 'Job' + customListName;
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
    message: 'Failed to load jobs. Please try again later.'
  });
}

function createCustomFields(data, names) {
  return `<div class="col-xs-12 col-sm-6">
            ${[1, 2, 3, 4, 5, 6].map(i =>
    createCustomField(i, data, names, 'JobCustomField'))
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




