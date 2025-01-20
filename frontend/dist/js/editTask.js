var globalNames = [];
/**********************************************************************************************/
function editTask(taskID, view){
  $.ajax({
    url: '/api/v1/task/' + taskID,
    dataType:'json',
    success: function(data){
      if(data == 'InvalidLogin'){
        logout();
      }
      
      $.ajax({
        url: '/api/v1/fieldcaptions?fields=TaskTargetFinish,' + allTaskFieldCaptionNames.join(",") + ',' + allContactFieldCaptionNames.join(",") + ',' + allJobFieldCaptionNames.join(","),
        dataType: 'json',
        success: function(names){
          if(names == 'InvalidLogin'){
            logout();
          }
          
          globalNames = names;
          showEditScreen(data, names, view);
        },
        error: function(jqXhr, textStatus, errorThrown){
          if(jqXhr.responseText == 'InvalidLogin'){
            logout();
          }

          BootstrapDialog.show({
            title: "Error Opening Task",
            message: errorThrown,
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
    },
    error: function(jqXhr, textStatus, errorThrown){
      if(jqXhr.responseText == 'InvalidLogin'){
        logout();
      }
      
      BootstrapDialog.show({
        title: "Error Opening Task",
        message: errorThrown,
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
/**********************************************************************************************/

function showEditScreen(data, names, view){
  var body = "";
      body += "<form role='form' name='details'>";
      body += " <input type='hidden' name='Id' id='tId' value='" + data['TaskId'] + "'>";
      body += " <div class='form-group'>";
      body += names['TaskDescription'];
      body += "   <input type='text' name='Description' class='form-control' value=\"" + data.TaskDescription.replace(/&/g, '&#38').replace(/</g, '&#60').replace(/>/g, '&#62').replace(/"/g, '&#34') + "\" />";
      body += " </div>";

      // JOB # FIELD
      body += " <div class='row'>";
      body += "   <div class='col-xs-12 col-sm-6'>";
      body += "     <div class='input-group form-group'>";
      body += "       <div class='input-group-btn'>";
      body += "         <button class='btn btn-default' type='button' onclick='chooseFromJobList();'>" + names['JobNumber'] + "</button>";
      body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
      body += "         <ul class='dropdown-menu'>";
      body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Choose From " + names['JobNumber'] + " List</a></li>";
      //body += "           <li><a href='#'>Create New + Assign</a></li>";
      body += "           <li role='separator' class='divider'></li>";
      body += "           <li><a href='#' onclick=\"unassignJob(); return false;\">Remove</a></li>";
      body += "         </ul>";
      body += "       </div>";
      body += "       <input readonly='readonly' type='text' name='JobNumber' id='JobNumber' value='" + data.JobNumber.replace('"','&#34').replace('\'','&#39') + "' class='form-control' />";
      body += "       <input type='hidden' value='" + data['JobId'] + "' name='JobId'>";
      body += "     </div>";
      body += "   </div>";

      // CONTACT FIELD
      body += "   <div class='col-xs-12 col-sm-6'>";
      body += "     <div class='input-group form-group'>";
      body += "       <div class='input-group-btn'>";
      body += "         <button class='btn btn-default' type='button' onclick=\"chooseFromContactList(); return false;\">" + names['ContactPerson'] + "</button>";
      body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
      body += "         <ul class='dropdown-menu'>";
      body += "           <li><a href='#' onclick=\"chooseFromContactList(); return false;\">Choose From Contacts</a></li>";
      //body += "           <li><a href='#'>Create New + Assign</a></li>";
      body += "           <li role='separator' class='divider'></li>";
      body += "           <li><a href='#' onclick=\"unassignContact(); return false;\">Remove</a></li>";
      body += "         </ul>";
      body += "       </div>";
      body += "       <input readonly id='ContactName' type='text' value='" + data.ContactPerson.replace('"','&#34').replace('\'','&#39') + "' class='form-control' />";
      body += "       <input type='hidden' name='ContactId' value='" + data['ContactId'] + "'>";
      body += "     </div>";
      body += "   </div>";
      body += " </div>";

  // TARGET START, DURATION, AND TARGET FINISH, Actual Finish FIELDS
  body += " <div class='row form-group'>";
  body += "   <div class='col-xs-5' style='padding-right:5px;'>";
  body += names['TaskTargetStart'];
      
  body += " <div class='input-group date'>";
  body += "   <input name='TargetStart' readonly style='background-color:#fff;' value='" + moment(data.TaskTargetStart).format("MM/DD/YYYY") + "' class='form-control' />";
  body += "   <div class='input-group-addon'>";
  body += "     <i class='fa fa-calendar'></i>";
  body += "   </div>";
  body += " </div>";

  body += "   </div>";
  body += "   <div class='col-xs-2' style='padding-left:5px; padding-right:5px;'>";
  body += names['TaskDuration'];
  body += "     <input type='number' onchange=\"updateTargetFinish();\" name='Duration' min='1' value='" + data.TaskDuration + "' class='form-control' />";
  body += "   </div>";
  body += "   <div class='col-xs-5' style='padding-left:5px;'>";
  body += names['TaskTargetFinish'];
          
  body += "   <input name='TargetFinish' readonly value='" + moment(data.TaskTargetFinish).format("MM/DD/YYYY") + "' class='form-control' />";
  body += "   </div>";
  body += " </div>";
  
    
  // TASK NUMBER FIELD
  body += " <div class='row form-group'>";
  body += "   <div class='col-lg-4 col-xs-6'>";
  body += names['TaskOrder'];
  body += "     <input type='text' name='Order' value='" + data.TaskOrder + "' class='form-control'>";
  body += "   </div>";
  body += "   <div class='col-lg-4 col-xs-6'>";
  
  // STATUS
  body += names['TaskStatus'];
  body += " <div class='input-group'>";
  body += "   <div class='input-group-btn'>";
  body += "     <button type='button' title='Task Status' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>" + names['TaskStatus'] + " <span class='caret'></span></button>";
  body += "     <ul class='dropdown-menu'>";
  body += "       <li><a href='#' onclick=\"$('input[name=Status]').val('Active'); $('input[name=ActualFinish]').val(''); $('#actualFinishSection').addClass('hidden'); return false;\">Active</a></li>";
  body += "       <li><a href='#' onclick=\"$('input[name=Status]').val('Done'); $('input[name=ActualFinish]').val($('input[name=TargetFinish]').val()); $('#actualFinishSection').removeClass('hidden'); return false;\">Done</a></li>";
  body += "     </ul>";
  body += "   </div>";
  body += "   <input type='text' readonly name='Status' class='form-control' value='" + data.TaskStatus + "'>";
  body += " </div>";  
  
  body += "   </div>";
  body += "   <div class='col-xs-12 col-lg-4'>";
  
  $.ajax({
    url: '/api/v1/employee?Id=_',
    dataType:'json',
    success: function(requestedByList){
      if(requestedByList == 'InvalidLogin'){
        logout();
      }

      // REQUESTED BY
      body += "<br class='hidden-lg' />";
      body += names['TaskRequested'];
      body += " <select name='Requested' class='form-control'>";
      body += "   <option value=''></option>";
      
      $.each(requestedByList, function(){
        this.Name = $.trim(this.Name);
        if(this.Name == data.TaskRequested){
          body += "<option value=\"" + this.Name + "\" selected>" + this.Name + "</option>";
        }else{
          body += "<option value=\"" + this.Name + "\">" + this.Name + "</option>";
        }
      });
      
      body += "     </select>";
      body += "   </div>";
      body += " </div>";

  // ACTUAL FINISH
  if(data.TaskStatus == "Active"){
    body += "<div class='row hidden' style='margin-bottom:10px;' id='actualFinishSection'>";
  }else{
    body += "<div class='row' style='margin-bottom:10px;' id='actualFinishSection'>";
  }
  body += " <div class='col-xs-6'>";
  body += names.TaskActualFinish;
  body += " <div class='input-group date'>";
  body += "   <input class='form-control' name='ActualFinish' value='" + moment(data.TaskActualFinish).format("MM/DD/YYYY") + "'>";
  body += "   <div class='input-group-addon'>";
  body += "     <i class='fa fa-calendar'></i>";
  body += "   </div>";
  body += " </div>";
  body += " </div>";
  body += "</div>";
    
      // CUSTOM TASK FIELDS
      
      body += " <div class='row'>";
      body += "   <div class='col-xs-12 col-sm-6'>";
      
      if(eval(Cookies.get("ShowTaskCustomField1"))){
        body += "     <div class='input-group form-group' id='TaskCustomField1Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField1 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField1' id='TaskCustomField1' value='" + data.TaskCustomField1 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='TaskCustomField1Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField1 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField1' disabled='true' id='TaskCustomField1' value='" + data.TaskCustomField1 + "' class='form-control' />";
        body += "     </div>";
      }
      
      if(eval(Cookies.get("ShowTaskCustomField2"))){
        body += "     <div class='input-group form-group' id='TaskCustomField2Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField2 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField2' id='TaskCustomField2' value='" + data.TaskCustomField2 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='TaskCustomField2Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField2 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField2' disabled='true' id='TaskCustomField2' value='" + data.TaskCustomField2 + "' class='form-control' />";
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowTaskCustomField3"))){
        body += "     <div class='input-group form-group' id='TaskCustomField3Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField3 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField3' id='TaskCustomField3' value='" + data.TaskCustomField3 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='TaskCustomField3Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField3 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField3' disabled='true' id='TaskCustomField3' value='" + data.TaskCustomField3 + "' class='form-control' />";
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowTaskCustomField4"))){
        body += "     <div class='input-group form-group' id='TaskCustomField4Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField4 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField4' id='TaskCustomField4' value='" + data.TaskCustomField4 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='TaskCustomField4Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField4 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField4' disabled='true' id='TaskCustomField4' value='" + data.TaskCustomField4 + "' class='form-control' />";
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowTaskCustomField5"))){
        body += "     <div class='input-group form-group' id='TaskCustomField5Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField5 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField5' id='TaskCustomField5' value='" + data.TaskCustomField5 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='TaskCustomField5Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField5 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField5' disabled='true' id='TaskCustomField5' value='" + data.TaskCustomField5 + "' class='form-control' />";
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowTaskCustomField6"))){
        body += "     <div class='input-group form-group' id='TaskCustomField6Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField6 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField6' id='TaskCustomField6' value='" + data.TaskCustomField6 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='TaskCustomField6Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField6 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField6' disabled='true' id='TaskCustomField6' value='" + data.TaskCustomField6 + "' class='form-control' />";
        body += "     </div>";
      }

      body += "   </div>";
      
      // CUSTOM LISTS
      
      body += "   <div class='col-xs-12 col-lg-6'>";

      if(eval(Cookies.get("ShowTaskCustomList1"))){
        body += "     <div class='input-group form-group' id='TaskCustomList1Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomList1 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.TaskCustomList1 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList1' id='TaskCustomList1' value='" + data.TaskCustomList1 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='TaskCustomList1Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomList1 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.TaskCustomList1 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList1' disabled='true' id='TaskCustomList1' value='" + data.TaskCustomList1 + "' class='form-control' />";
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowTaskCustomList2"))){
        body += "     <div class='input-group form-group' id='TaskCustomList2Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomList2 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.TaskCustomList1 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList2' id='TaskCustomList2' value='" + data.TaskCustomList2 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='TaskCustomList2Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomList2 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.TaskCustomList2 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList2' disabled='true' id='TaskCustomList2' value='" + data.TaskCustomList2 + "' class='form-control' />";
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowTaskCustomList3"))){
        body += "     <div class='input-group form-group' id='TaskCustomList3Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomList3 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.TaskCustomList3 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList3' id='TaskCustomList3' value='" + data.TaskCustomList3 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='TaskCustomList3Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomList3 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.TaskCustomList1 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList3' disabled='true' id='TaskCustomList3' value='" + data.TaskCustomList3 + "' class='form-control' />";
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowTaskCustomList4"))){
        body += "     <div class='input-group form-group' id='TaskCustomList4Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomList4 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.TaskCustomList4 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList4' id='TaskCustomList4' value='" + data.TaskCustomList4 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='TaskCustomList4Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomList4 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.TaskCustomList4 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList4' disabled='true' id='TaskCustomList4' value='" + data.TaskCustomList4 + "' class='form-control' />";
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowTaskCustomList5"))){
        body += "     <div class='input-group form-group' id='TaskCustomList5Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomList5 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.TaskCustomList5 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList5' id='TaskCustomList5' value='" + data.TaskCustomList5 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='TaskCustomList5Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomList5 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.TaskCustomList5 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList5' disabled='true' id='TaskCustomList5' value='" + data.TaskCustomList5 + "' class='form-control' />";
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowTaskCustomList6"))){
        body += "     <div class='input-group form-group' id='TaskCustomList6Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomList6 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.TaskCustomList6 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList6' id='TaskCustomList6' value='" + data.TaskCustomList6 + "' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='TaskCustomList6Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomList6 + "</button>";
        body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
        body += "         <ul class='dropdown-menu'>";
        //body += "           <li><a href='#' onclick=\"chooseFromJobList(); return false;\">Values for " + names.TaskCustomList6 + "</a></li>";
        body += "         </ul>";
        body += "       </div>";
        body += "       <input type='text' name='CustomList6' disabled='true' id='TaskCustomList6' value='" + data.TaskCustomList6 + "' class='form-control' />";
        body += "     </div>";
      }

      body += " </div>";
      body += "</div>";
    
      // TASK NOTES FIELD
      body += " <div class='form-group'>";
      body += names['TaskNotes'];
      body += "   <textarea contenteditable='true' name='Notes' style='min-height:150px; height:auto; white-space:pre-wrap;' class='form-control'>" + data.TaskNotes.replace(/(?:\r\n |\r |\n )/g, '&#10;') + "</textarea>";
      body += " </div>";
        
      // TOOLBAR
      body += "   <div class='btn-group'>";
      body += "     <button type='button' class='btn btn-default' onclick=\"editTaskGroups('" + data.TaskId + "','" + data.TaskGroups + "');\" title='Add this task to a Group'><i class='fa fa-object-group'></i><span class='hidden-xs'> Groups</span></button>";
      body += "     <button type='button' class='btn btn-default' onclick=\"editTaskCustomFieldOptions();\" title='Edit task custom field and lists'><i class='fa fa-plus-circle'></i><span class='hidden-xs'> Custom Field Options</span></button>";
      body += "     <button type='button' class='btn btn-default' onclick=\"editTaskLinks('" + data.TaskId + "','" + data.JobId + "','" + data.TaskFollows + "');\" title='Add Task Dependency (Link Task)'><i class='fa fa-link'></i><span class='hidden-xs'> Link</span></button>";
      body += "     <div class='btn-group dropdown'><button type='button' title='File Attachments' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><i class='fa fa-paperclip'></i><span class='hidden-xs'> Attachments</span> <span class='caret'></span></button>";
      body += "     <ul class='dropdown-menu'>";
      
      var fileAttachments = data.TaskFiles.split("<BR>");

      if(fileAttachments.length <= 1 && fileAttachments[0] == ""){
        body += "<li><a href='#' onclick='return false;'>No Attached Files</a></li>";
      }else{
        $.each(fileAttachments, function(){
          body += "<li>" + this + "</li>";
        });
      }
              
      body += "     </ul></div>";
      body += "   </div>";
      body += "   <div class='btn-group dropdown'>";
      //body += "     <button type='button' onclick=\"printSection();\" class='btn btn-default' title='Print this task'><i class='fa fa-print'></i></button>";
      body += "     <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false' title='Email this task'><i class='fa fa-envelope-o'></i> <span class='caret'></span></button>";
      body += "     <ul class='dropdown-menu'>";
      body += "       <li><a href=\"mailto:" + data['ContactEmail'] + "?subject=Work%20Request%20For%20" + encodeURIComponent(data['TaskDescription']) + "&body=" + encodeURIComponent(data['TaskNotes']) + "\">Email to (" + data['ContactPerson'] + ")</a></li>";
      //body += "       <li><a href='#'>New</a></li>";
      body += "     </ul>";
      body += "   </div>";
      
      $.ajax({
        url: '/api/v1/taskGroup',
        dataType: 'json',
        success: function(allGroups){
          if(allGroups == 'InvalidLogin'){
            logout();
          }
    
          // GROUPS
          if(data.TaskGroups.trim()){ // is false if string is empty or ""
            body += "   <div class='form-group' style='margin-top:12px; margin-bottom:0px;' id='TaskGroupSection'>";
            body += "     Groups: ";
                 
            var groups = data.TaskGroups.split(',');
            
            body += "<span style='color:#337AB7'>" + groups.join(',') + "</span>";
            
            var matchingGroupIds = [];
            
            $.each(groups, function(){
              var assignedGroup = this;
              
              $.each(allGroups, function(){
                if(this.Name.trim() == assignedGroup){
                  matchingGroupIds.push(this.Id);
                }
              });
            });
    
            body += "<input type='hidden' name='Groups' value='" + matchingGroupIds.join(',') + "'>";
            body += "   </div>";
          }else{
            body += "<div class='form-group' style='margin-top:12px; margin-bottom:0px;' id='TaskGroupSection'></div>";
            body += "<input type='hidden' name='Groups' value=''>";
          }   
          
          // TASK LINKING  
          if(data.TaskFollows.trim()){
            body += "<div class='form-group' id='taskLinkingSection' style='margin-top:12px; margin-bottom:0px;'>";
            body += "Currently Linked With:";
            body += "<ul style='margin-top:7px; padding-left:15px;'>";
            
            $.ajax({
              url: '/api/v1/task?fields=TaskId,TaskNumber,TaskDescription,JobNumber,TaskTargetFinish&TaskIds=' + data.TaskFollows,
              dataType:'json',
              success: function(tasks){
                if(tasks == 'InvalidLogin'){
                  logout();
                }
          
                var currentlyLinkedArray = [];
                
                $.each(tasks, function(){
                  body += "<li style='font-size:12px;'><a href='#' onclick=\"editTask('" + this.TaskId + "');return false;\">" + this.TaskDescription + "</a> - Finishes on " + moment(this.TaskTargetFinish, 'YYYY-MM-DD').format('MM/DD/YYYY (dddd)') + "</li>";
                  currentlyLinkedArray.push(this.TaskNumber);
                });
            
                body += "</ul>";
                body += "<input type='hidden' name='Pending' value='" + currentlyLinkedArray.join(',') + "'>";
                body += "</div>";
                body += "<div class='row'>";
                body += " <div class='col-xs-12'>";
                body += "   Offset Start Date By";
                body += "   <input type='number' name='Finish_plus_days' style='width:50px;' value='" + data.FinishPlus + "'> Days";
                body += " </div>";
                body += "</div>";
                body += "</form>";
                
                showTaskEditScreen(data, body, view);
              },
              error: function(jqXhr, textStatus, errorThrown){
                if(jqXhr.responseText == 'InvalidLogin'){
                  logout();
                }
          
                alert(errorThrown);
              }
            });
          }else{
            body += "<div class='form-group' id='taskLinkingSection' style='margin-top:12px; margin-bottom:0px;'></div>";
            body += "</form>";
            
            showTaskEditScreen(data, body, view);
          }        
        },
        error: function(jqXhr, status, error){
          if(jqXhr.responseText == 'InvalidLogin'){
            logout();
          }
    
          alert(error);
        }
      });
    },
    error: function(jqXhr, status, error){
      if(jqXhr.responseText == 'InvalidLogin'){
        logout();
      }

      alert(error);
    }
  });
}

/**********************************************************************************************/

function editTaskCustomFieldOptions(){
  var msg = "";
      msg += "<div class='row'>";
      msg += "  <div class='col-xs-12 col-lg-6'>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";
      
      if(eval(Cookies.get("ShowTaskCustomField1"))){
        msg += "<input type='checkbox' id='tCustomField1Toggle' checked='checked'> Show " + globalNames.TaskCustomField1;
      }else{
        msg += "<input type='checkbox' id='tCustomField1Toggle'> Show " + globalNames.TaskCustomField1;
      }
      
      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";
      
      if(eval(Cookies.get("ShowTaskCustomField2"))){
        msg += "<input type='checkbox' id='tCustomField2Toggle' checked='checked'> Show " + globalNames.TaskCustomField2;
      }else{
        msg += "<input type='checkbox' id='tCustomField2Toggle'> Show " + globalNames.TaskCustomField2;
      }
      
      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";
      
      if(eval(Cookies.get("ShowTaskCustomField3"))){
        msg += "<input type='checkbox' id='tCustomField3Toggle' checked='checked'> Show " + globalNames.TaskCustomField3;
      }else{
        msg += "<input type='checkbox' id='tCustomField3Toggle'> Show " + globalNames.TaskCustomField3;
      }
      
      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";
      
      if(eval(Cookies.get("ShowTaskCustomField4"))){
        msg += "<input type='checkbox' id='tCustomField4Toggle' checked='checked'> Show " + globalNames.TaskCustomField4;
      }else{
        msg += "<input type='checkbox' id='tCustomField4Toggle'> Show " + globalNames.TaskCustomField4;
      }
      
      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";
      
      if(eval(Cookies.get("ShowTaskCustomField5"))){
        msg += "<input type='checkbox' id='tCustomField5Toggle' checked='checked'> Show " + globalNames.TaskCustomField5;
      }else{
        msg += "<input type='checkbox' id='tCustomField5Toggle'> Show " + globalNames.TaskCustomField5;
      }
      
      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";
      
      if(eval(Cookies.get("ShowTaskCustomField6"))){
        msg += "<input type='checkbox' id='tCustomField6Toggle' checked='checked'> Show " + globalNames.TaskCustomField6;
      }else{
        msg += "<input type='checkbox' id='tCustomField6Toggle'> Show " + globalNames.TaskCustomField6;
      }
      
      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "  </div>";
      msg += "  <div class='col-xs-12 col-lg-6'>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";

      if(eval(Cookies.get("ShowTaskCustomList1"))){
        msg += "<input type='checkbox' id='tCustomList1Toggle' checked='checked'> Show " + globalNames.TaskCustomList1;
      }else{
        msg += "<input type='checkbox' id='tCustomList1Toggle'> Show " + globalNames.TaskCustomList1;
      }

      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";

      if(eval(Cookies.get("ShowTaskCustomList2"))){
        msg += "<input type='checkbox' id='tCustomList2Toggle' checked='checked'> Show " + globalNames.TaskCustomList2;
      }else{
        msg += "<input type='checkbox' id='tCustomList2Toggle'> Show " + globalNames.TaskCustomList2;
      }

      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";

      if(eval(Cookies.get("ShowTaskCustomList3"))){
        msg += "<input type='checkbox' id='tCustomList3Toggle' checked='checked'> Show " + globalNames.TaskCustomList3;
      }else{
        msg += "<input type='checkbox' id='tCustomList3Toggle'> Show " + globalNames.TaskCustomList3;
      }

      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";

      if(eval(Cookies.get("ShowTaskCustomList4"))){
        msg += "<input type='checkbox' id='tCustomList4Toggle' checked='checked'> Show " + globalNames.TaskCustomList4;
      }else{
        msg += "<input type='checkbox' id='tCustomList4Toggle'> Show " + globalNames.TaskCustomList4;
      }

      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";

      if(eval(Cookies.get("ShowTaskCustomList5"))){
        msg += "<input type='checkbox' id='tCustomList5Toggle' checked='checked'> Show " + globalNames.TaskCustomList5;
      }else{
        msg += "<input type='checkbox' id='tCustomList5Toggle'> Show " + globalNames.TaskCustomList5;
      }

      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";
      msg += "    <div class='form-group'>";
      msg += "      <div class='checkbox'>";
      msg += "        <label>";

      if(eval(Cookies.get("ShowTaskCustomList6"))){
        msg += "<input type='checkbox' id='tCustomList6Toggle' checked='checked'> Show " + globalNames.TaskCustomList6;
      }else{
        msg += "<input type='checkbox' id='tCustomList6Toggle'> Show " + globalNames.TaskCustomList6;
      }

      msg += "        </label>";
      msg += "      </div>";
      msg += "    </div>";      
      msg += "  </div>";
      msg += "</div>";

  BootstrapDialog.show({
    title: 'Task Custom Field and List Options',
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
      
        if($("#tCustomField1Toggle").prop("checked")){
          setCookie("ShowTaskCustomField1", true);
          $("#TaskCustomField1").attr("disabled", false);
          $("#TaskCustomField1Wrap").removeClass("hidden");
        }else{
          setCookie("ShowTaskCustomField1", false);
          $("#TaskCustomField1").attr("disabled", true);
          $("#TaskCustomField1Wrap").addClass("hidden");
        }

        if($("#tCustomField2Toggle").prop("checked")){
          setCookie("ShowTaskCustomField2", true);
          $("#TaskCustomField2").attr("disabled", false);
          $("#TaskCustomField2Wrap").removeClass("hidden");
        }else{
          setCookie("ShowTaskCustomField2", false);
          $("#TaskCustomField2").attr("disabled", true);
          $("#TaskCustomField2Wrap").addClass("hidden");
        }
        
        if($("#tCustomField3Toggle").prop("checked")){
          setCookie("ShowTaskCustomField3", true);
          $("#TaskCustomField3").attr("disabled", false);
          $("#TaskCustomField3Wrap").removeClass("hidden");
        }else{
          setCookie("ShowTaskCustomField3", false);
          $("#TaskCustomField3").attr("disabled", true);
          $("#TaskCustomField3Wrap").addClass("hidden");
        }
        
        if($("#tCustomField4Toggle").prop("checked")){
          setCookie("ShowTaskCustomField4", true);
          $("#TaskCustomField4").attr("disabled", false);
          $("#TaskCustomField4Wrap").removeClass("hidden");
        }else{
          setCookie("ShowTaskCustomField4", false);
          $("#TaskCustomField4").attr("disabled", true);
          $("#TaskCustomField4Wrap").addClass("hidden");
        }
        
        if($("#tCustomField5Toggle").prop("checked")){
          setCookie("ShowTaskCustomField5", true);
          $("#TaskCustomField5").attr("disabled", false);
          $("#TaskCustomField5Wrap").removeClass("hidden");
        }else{
          setCookie("ShowTaskCustomField5", false);
          $("#TaskCustomField5").attr("disabled", true);
          $("#TaskCustomField5Wrap").addClass("hidden");
        }
        
        if($("#tCustomField6Toggle").prop("checked")){
          setCookie("ShowTaskCustomField6", true);
          $("#TaskCustomField6").attr("disabled", false);
          $("#TaskCustomField6Wrap").removeClass("hidden");
        }else{
          setCookie("ShowTaskCustomField6", false);
          $("#TaskCustomField6").attr("disabled", true);
          $("#TaskCustomField6Wrap").addClass("hidden");
        }

        // custom lists

        if($("#tCustomList1Toggle").prop("checked")){
          setCookie("ShowTaskCustomList1", true);
          $("#TaskCustomList1").attr("disabled", false);
          $("#TaskCustomList1Wrap").removeClass("hidden");
        }else{
          setCookie("ShowTaskCustomList1", false);
          $("#TaskCustomList1").attr("disabled", true);
          $("#TaskCustomList1Wrap").addClass("hidden");
        }

        if($("#tCustomList2Toggle").prop("checked")){
          setCookie("ShowTaskCustomList2", true);
          $("#TaskCustomList2").attr("disabled", false);
          $("#TaskCustomList2Wrap").removeClass("hidden");
        }else{
          setCookie("ShowTaskCustomList2", false);
          $("#TaskCustomList2").attr("disabled", true);
          $("#TaskCustomList2Wrap").addClass("hidden");
        }
        
        if($("#tCustomList3Toggle").prop("checked")){
          setCookie("ShowTaskCustomList3", true);
          $("#TaskCustomList3").attr("disabled", false);
          $("#TaskCustomList3Wrap").removeClass("hidden");
        }else{
          setCookie("ShowTaskCustomList3", false);
          $("#TaskCustomList3").attr("disabled", true);
          $("#TaskCustomList3Wrap").addClass("hidden");
        }
        
        if($("#tCustomList4Toggle").prop("checked")){
          setCookie("ShowTaskCustomList4", true);
          $("#TaskCustomList4").attr("disabled", false);
          $("#TaskCustomList4Wrap").removeClass("hidden");
        }else{
          setCookie("ShowTaskCustomList4", false);
          $("#TaskCustomList4").attr("disabled", true);
          $("#TaskCustomList4Wrap").addClass("hidden");
        }
        
        if($("#tCustomList5Toggle").prop("checked")){
          setCookie("ShowTaskCustomList5", true);
          $("#TaskCustomList5").attr("disabled", false);
          $("#TaskCustomList5Wrap").removeClass("hidden");
        }else{
          setCookie("ShowTaskCustomList5", false);
          $("#TaskCustomList5").attr("disabled", true);
          $("#TaskCustomList5Wrap").addClass("hidden");
        }
        
        if($("#tCustomList6Toggle").prop("checked")){
          setCookie("ShowTaskCustomList6", true);
          $("#TaskCustomList6").attr("disabled", false);
          $("#TaskCustomList6Wrap").removeClass("hidden");
        }else{
          setCookie("ShowTaskCustomList6", false);
          $("#TaskCustomList6").attr("disabled", true);
          $("#TaskCustomList6Wrap").addClass("hidden");
        }
        
        dialogRef.close();
      }
    }]
  });
}


/**********************************************************************************************/

function editTaskLinks(taskId, jobId, linkedTasks){
  if(jobId != "undefined"){
    var url = "/api/v1/task?fields=TaskDescription,TaskId,TaskNumber,JobNumber&IsActive=on&JobIds=" + jobId;
  }else{
    var url = "/api/v1/task?fields=TaskDescription,TaskId,TaskNumber,JobNumber&IsActive=on&MatchType=2&Field=Job %23";
  }
  
  $.ajax({
    url: url,
    dataType:'json',
    success: function(taskData){   
      if(taskData == 'InvalidLogin'){
        logout();
      }

      var currentlyLinkedArray = linkedTasks.split(',');
      var msg = "";
          msg += "<form role='form' name='taskLinkingFilterForm'>";
          msg += "<div class='row'>";
          msg += "  <div class='col-xs-12 col-lg-6'>";
          msg += "    <label>Status</label><br />";
          msg += "      <label style='font-weight:normal; margin-left:10px; cursor:pointer;'><input type='checkbox' checked='checked' name='IsActive'> Active</label>";
          msg += "      <label style='font-weight:normal; margin-left:10px; cursor:pointer;'><input type='checkbox' name='IsDone'> Done</label>";
          msg += "  </div>";
          msg += "  <div class='col-xs-12 col-lg-6'>";
          msg += "    <label>Job</label>";
          msg += "    <select class='form-control' name='JobIds'>";
          msg += "      <option value=''>Unassigned</option>";
          
      $.ajax({
        url: '/api/v1/job?fields=JobId,JobNumber',
        dataType:'json',
        success: function(jobs){
          if(jobs == 'InvalidLogin'){
            logout();
          }
          
          if(jobs.length < 1){
            msg += "<option value=''>No Jobs</option>";
          }else{
            $.each(jobs, function(){
              if(this.JobId == jobId){
                msg += "<option value='" + this.JobId + "' selected='selected'>" + this.JobNumber + "</option>";
              }else{
                msg += "<option value='" + this.JobId + "'>" + this.JobNumber + "</option>";
              }
            });          
          }

          msg += "    </select>";
          msg += "  </div>";
          msg += "</div>";
          msg += "<br />";
          msg += "<button class='btn btn-primary' type='button' onclick=\"applyTaskLinkingFilter('" + linkedTasks + "');\">Apply Filter</button>";
          msg += "</form>";
          msg += "<br />";
          msg += "<div class='table-responsive' style='max-height:400px; overflow:scroll;'>";
          msg += "<form role='form' name='taskLinkingEditForm'>";
          msg += "  <table id='taskLinkingTaskTable' role='table' class='table table-condensed table-bordered'>";
          msg += "    <thead>";
          msg += "    <tr>";
          msg += "      <th>Link To</th>";
          msg += "      <th>Task Description</th>";
          msg += "      <th>Job</th>";
          msg += "    </tr>";  
          msg += "    </thead>";
          msg += "    <tbody>";        

          if(taskData.length < 1){
            msg += "<tr>";
            msg += "  <td></td>";
            msg += "  <td>No Tasks Found For This Search</td>";
            msg += "  <td></td>";
            msg += "</tr>";
          }else{
            $.each(taskData, function(){
              if($.inArray(this.TaskNumber.toString(), currentlyLinkedArray) == -1){
                msg += "<tr>";
                msg += "  <td>";
                msg += "    <label style='font-weight:normal; margin-bottom:0px; cursor:pointer;'>";
                msg += "      <input type='checkbox' name='" + this.TaskNumber + "'> Select";
              }else{
                msg += "<tr style='background-color:yellow;'>";
                msg += "  <td>";
                msg += "    <label style='font-weight:normal; margin-bottom:0px; cursor:pointer;'>";
                msg += "      <input type='checkbox' name='" + this.TaskNumber + "' checked='checked'> Select";
              }
              
              msg += "    </label>";
              msg += "  </td>";
              msg += "  <td>" + this.TaskDescription + "</td>";
              msg += "  <td>" + this.JobNumber + "</td>";
              msg += "</tr>";
            });
          }
          
          msg += "    </tbody>";
          msg += "  </table>";
          msg += "</form>";
          msg += "</div>";
          //msg += "<div class='row'>";
          //msg += "  <div class='col-xs-12 col-lg-6'>";
          //msg += "    <label style='font-weight:normal; cursor:pointer;'><input type='checkbox'> Only Show Linked</label>";
          //msg += "  </div>";
          //msg += "  <div class='col-xs-12 col-lg-6'>";
          //msg += "    <label style='font-weight:normal; cursor:pointer;'><input type='checkbox'> Keep Current Start Date</label>";
          //msg += "  </div>";
          //msg += "</div>";
              
          BootstrapDialog.show({
            title: "Select Controlling Task",
            message: msg,
            onshown: function(){
              $("form[name=taskLinkingEditForm] input[type=checkbox]").change(function(){
                if($(this).prop("checked")){
                  $(this).closest("tr").css("background-color","yellow");
                }else{
                  $(this).closest("tr").css("background-color","white");
                }
              });
            },
            buttons:[{
              label: "Set Pending Task Links",
              cssClass: "btn-primary",
              action: function(dialogRef){
                var formData = $("form[name=taskLinkingEditForm]").serialize();
                
                if(formData == ""){
                  dialogRef.close();
                  $("#taskLinkingSection").html("<input type='hidden' name='Pending' value=''>");
                  
                  return;
                }                
                
                var chosenArray = formData.split("&");
                var taskIdsToLink = [];
                
                $.each(chosenArray, function(){
                  taskIdsToLink.push(this.split("=")[0]);
                });
                                  
                $.ajax({
                  url: '/api/v1/task?fields=TaskId,TaskNumber,TaskDescription,JobNumber,TaskTargetFinish&TaskIds=' + taskIdsToLink.join(","),
                  dataType:'json',
                  success: function(tasks){
                    if(tasks == 'InvalidLogin'){
                      logout();
                    }
              
                    var output = "Pending Task Links:";
                        output += "<ul style='padding-left:15px; margin-top:7px;'>";
                    
                    var pendingArray = [];
                    
                    $.each(tasks, function(){
                      output += "<li style='font-size:12px;'><a href='#'>" + this.TaskDescription + "</a> - Finishes on " + moment(this.TaskTargetFinish,'YYYY-MM-DD').format('MM/DD/YYYY (dddd)') + "</li>";
                      pendingArray.push(this.TaskNumber);
                    });
                    
                    output += "</ul>";
                    output += "<input type='hidden' name='Pending' value='" + pendingArray.join(',') + "'>";
                    
                    $("#taskLinkingSection").html(output);
                    dialogRef.close();
                  },
                  error: function(jqXhr, status, error){
                    if(jqXhr.responseText == 'InvalidLogin'){
                      logout();
                    }
              
                    alert(error);
                  }
                });  
              }
            },{
              label: "Cancel",
              action: function(dialogRef){
                  
                dialogRef.close();
              }
            }]
          });
        },
        error: function(jqXhr, status, error){
          if(jqXhr.responseText == 'InvalidLogin'){
            logout();
          }
    
          alert(error);
        }
      });    
    },
    error:function(jqXhr, textStatus, errorThrown){
      if(jqXhr.responseText == 'InvalidLogin'){
        logout();
      }

      alert(errorThrown);
    }
  });
}

function showTaskEditScreen(data, body, view){
  BootstrapDialog.show({
    title: "<div>" + data.TaskDescription.replace(/&/g, '&#38').replace(/</g, '&#60').replace(/>/g, '&#62').replace(/"/g, '&#34') + "</div>",
    message: body,
    //draggable: true,
    onshown: function(){
      if(data.TaskFollows != ""){
        var controllingTaskEndDate = moment($("input[name=TargetStart]").val(), "MM/DD/YYYY").businessAdd(-$("input[name=Finish_plus_days]").val()).format("MM/DD/YYYY");
      }
      
      $("input[name=TargetStart]").datepicker({
        format: 'mm/dd/yyyy',
        autoclose: true,
        daysOfWeekDisabled: [0,6],
        todayBtn: true,
        todayHighlight: true,
        orientation: 'auto'
      }).change(function(){
        var newFinishPlus = moment($("input[name=TargetStart]").val(), "MM/DD/YYYY").businessDiff(moment(controllingTaskEndDate, "MM/DD/YYYY"));

        if(newFinishPlus == -0){
          newFinishPlus = 0;
        }
        
        $("input[name=Finish_plus_days]").val(newFinishPlus);
        
        updateTargetFinish();
      });
      
      $("input[name=ActualFinish]").datepicker({
        format: 'mm/dd/yyyy',
        autoclose: true,
        daysOfWeekDisabled: [0,6],
        todayBtn: true,
        todayHighlight: true,
        orientation: 'auto'
      }).change(function(){
        //var newFinishPlus = moment($("input[name=TargetStart]").val(), "MM/DD/YYYY").businessDiff(moment(controllingTaskEndDate, "MM/DD/YYYY"));

        //if(newFinishPlus == -0){
        //  newFinishPlus = 0;
        //}
        
        //$("input[name=Finish_plus_days]").val(newFinishPlus);
        
        //updateTargetFinish();
      });
      
      $("input[name=Finish_plus_days]").change(function(){
        //$("input[name=TargetStart]").val(moment($("input[name=TargetStart]").val(), "MM/DD/YYYY").add($(this).val(), "days").format("MM/DD/YYYY"));
        var newStartDate = moment(controllingTaskEndDate, 'MM/DD/YYYY').businessAdd($(this).val()).format('MM/DD/YYYY');
        
        $("input[name=TargetStart]").val(newStartDate);
        
        updateTargetFinish();
      });
    },
    size: BootstrapDialog.SIZE_WIDE,
    buttons: [{
      label:"<i class='fa fa-trash-o' style='color:red;'></i> <span style='color:red;' class='hidden-xs'>Delete Task</span>",
      cssClass:'btn-default',
      action: function(dialogRef){
        BootstrapDialog.show({
          type: BootstrapDialog.TYPE_DANGER,
          title: "Confirm Delete",
          message: "Are you sure you want to delete <strong>" + data.TaskDescription + "</strong>?",
          buttons:[{
            label: "Yes, Delete This Task",
            cssClass: "btn-danger",
            action: function(dialogRef){
              $.ajax({
                url: "/api/v1/task/" + data.TaskId,
                method: 'DELETE',
                success: function(response){
                  if(response == 'InvalidLogin'){
                    logout();
                  }
            
                  switch(view){
                    case "Calendar":
                      loadCalendarView();                    
                    break;
                    case "TaskManager":
                      createTaskList();
                    break;
                    case "GanttChart":
                      loadGanttChart();
                    break;
                  }

                  BootstrapDialog.closeAll();
                },
                error: function(jqXhr, textStatus, errorThrown){
                  if(jqXhr.responseText == 'InvalidLogin'){
                    logout();
                  }
            
                  BootstrapDialog.show({
                    title: "Problem With Delete Task",
                    message: "There was a problem deleting the task: <span style='color:red;'>" + errorThrown + "</span>",
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
      label: "<i class='fa fa-floppy-o hidden-xs'></i> Save Task",
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
          url: "/api/v1/task/" + data.TaskId, // + ?formData,
          method:'PUT',
          data: formData,
          success: function(response){
            if(response == 'InvalidLogin'){
              logout();
            }
      
            switch(view){
              case "Calendar":
                loadCalendarView();                    
              break;
              case "TaskManager":
                createTaskList();
              break;
              case "GanttChart":
                loadGanttChart();
              break;
            }
            
            dialogRef.close();
          },
          error: function(jqXHR, textStatus, errorThrown){
            if(jqXHR.responseText == 'InvalidLogin'){
              logout();
            }
            
            alert(errorThrown);
          }
        });
      }
    }] 
  });
}

//function printSection(){
//  $("form[name=details]").print();
//}

function updateTargetFinish(){
  var duration = $("input[name=TaskDuration]").val();
      duration--;
      
  $("input[name=TargetFinish]").val(moment($("input[name=TargetStart]").val(), "MM/DD/YYYY").add(duration, "days").format("MM/DD/YYYY"));
}

function chooseFromJobList(){
  $("button").css("cursor", "wait");

  var msg = "";
      msg += "<div style='margin-bottom:5px;'>Select a " + globalNames['JobNumber'] + "</div>";
      msg += "  <div class='input-group'>";
      msg += "    <input class='form-control' value='' placeholder=\"Search " + globalNames.JobNumber + " List\" id='taskEditJobSearch'>";
      msg += "    <span class='input-group-btn'><button type='button' id='taskEditJobListSearchBtn' class='btn btn-default'>Search...</button></span>";
      msg += "  </div>";
      msg += "<select id='taskEditJobSelect' class='form-control' style='margin-top:10px;'>";
      
  $.ajax({
    url: '/api/v1/job?fields=JobId,JobNumber',
    dataType:'json',
    success: function(jobs){
      if(jobs == 'InvalidLogin'){
        logout();
      }

      $.each(jobs, function(){
        if(this['JobNumber'].trim() == ""){
        
        }else{
          msg += "<option value=\"" + this['JobNumber'] + "\">" + this['JobNumber'];
        }
      });
      
      msg += "</select>";
      
      BootstrapDialog.show({
        title: "Choose " + globalNames['JobNumber'] + " From List",
        message: msg,
        type: BootstrapDialog.TYPE_DEFAULT,
        onshown: function(){
          $("button").css("cursor", "pointer");
          
          if($("#JobNumber").val()){
            $("#taskEditJobSelect").val($("#JobNumber").val());
          }
          
      	  var timer;
          		
       		$("#taskEditJobSearch").keyup(function(){
       			clearTimeout(timer);
          			
       			if($(this).val()){                  
              var phrase = $("#taskEditJobSearch").val();
       			  timer = setTimeout(function(){
                //$("#taskEditJobSpinIcon").removeClass("hidden");
                
                $.ajax({
                  url: '/api/v1/job?fields=JobId,JobNumber&FindString=' + phrase,
                  dataType:'json',
                  success: function(filteredJobs){
                    if(filteredJobs == 'InvalidLogin'){
                      logout();
                    }
              
                    var d = "";
                    
                    $.each(filteredJobs, function(){
                      d += "<option value='" + this.JobId + "'>" + this.JobNumber + "</option>";
                    });
                    
                    $("#taskEditJobSelect").html(d);
                    //$("#taskEditJobSpinIcon").addClass("hidden");
                  },
                  error: function(jqXhr, status, error){
                    if(jqXhr.responseText == 'InvalidLogin'){
                      logout();
                    }
              
                    alert(error);
                  }
                });
            	},500);
            }
          });          
        },
        buttons:[{
          label: "Select " + globalNames.JobNumber,
          cssClass: "btn-primary",
          action: function(dialogRef){
            $("#JobNumber").val($("#taskEditJobSelect option:selected").text());
            
            dialogRef.close();
          }
        },{
          label: "Cancel",
          action: function(dialogRef){
            dialogRef.close();
          }
        }]
      }); 
    },
    error: function(jqXhr, textStatus, errorThrown){
      if(jqXhr.responseText == 'InvalidLogin'){
        logout();
      }

      alert(errorThrown);
      $("button").css("cursor", "pointer");
    }
  });   
}

function chooseFromContactList(){
  var msg = "";
      msg += "<div style='margin-bottom:5px;'>Select a " + globalNames['ContactPerson'] + ". <span style='font-size:12px; color:#888;'>" + globalNames['ContactCompany'] + " appears in \"( )\"</span></div>";
      msg += "  <div class='input-group'>";
      msg += "    <input class='form-control' value='' placeholder=\"Search " + globalNames.ContactPerson + " List\" id='taskEditContactSearch'>";
      msg += "    <span class='input-group-btn'><button type='button' id='taskEditContactListSearchBtn' class='btn btn-default'>Search...</button></span>";
      msg += "  </div>";
      msg += "<select id='taskEditContactSelect' class='form-control' style='margin-top:10px;'>";
      
  $.ajax({
    url: '/api/v1/contact?fields=ContactId,ContactPerson,ContactCompany',
    dataType:'json',
    success: function(contacts){
      if(contacts == 'InvalidLogin'){
        logout();
      }

      $.each(contacts, function(){
        if(this['ContactPerson'].trim() == "" && this['ContactCompany'].trim() == ""){
        
        }else{
          msg += "<option value=\"" + this['ContactId'] + "\">" + this['ContactPerson'] + " (" + this['ContactCompany'] + ")";
        }
      });
      
      msg += "</select>";
      
      BootstrapDialog.show({
        title: "Choose " + globalNames['ContactPerson'] + " From List",
        message: msg,
        type: BootstrapDialog.TYPE_DEFAULT,
        onshown: function(){
          if($("input[name=ContactId]").val()){
            $("#taskEditContactSelect").val($("input[name=ContactId]").val());
          }

      	  var timer;
          		
       		$("#taskEditContactSearch").keyup(function(){
       			clearTimeout(timer);
          			
       			if($(this).val()){                  
              var phrase = $("#taskEditContactSearch").val();
       			  timer = setTimeout(function(){                
                $.ajax({
                  url: '/api/v1/contact?fields=ContactId,ContactPerson,ContactCompany&FindString=' + phrase,
                  dataType:'json',
                  success: function(filteredContacts){
                    if(filteredContacts == 'InvalidLogin'){
                      logout();
                    }
              
                    var d = "";
                    
                    $.each(filteredContacts, function(){
                      d += "<option value='" + this.ContactId + "'>" + this.ContactPerson + " (" + this.ContactCompany + ")</option>";
                    });
                    
                    $("#taskEditContactSelect").html(d);
                  },
                  error: function(jqXhr, status, error){
                    if(jqXhr.responseText == 'InvalidLogin'){
                      logout();
                    }
              
                    alert(error);
                  }
                });
            	},500);
            }
          });
        },
        buttons:[{
          label: "Select " + globalNames.ContactPerson,
          cssClass: "btn-primary",
          action: function(dialogRef){
            $("#ContactName").val($("#taskEditContactSelect option:selected").text());
            $("input[name=ContactId]").val($("#taskEditContactSelect").val());
            
            dialogRef.close();
          }
        },{
          label: "Cancel",
          action: function(dialogRef){
            dialogRef.close();
          }
        }]
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

function unassignJob(){
  $("#JobNumber").val("");
}

function unassignContact(){
  $("#ContactName").val("");
  $("input[name=ContactId]").val("UNASSIGNED");
}

function chooseJobShowAll(){
  $("#chooseJobResultsTable").html("");
  $.ajax({
    url: '/api/v1/job?fields=JobId,JobNumber',
    dataType:'json',
    success: function(jobs){
      if(jobs == 'InvalidLogin'){
        logout();
      }

      var text = "";
      
      $.each(jobs, function(){
        text += "<tr>";
        text += " <td>" + this['JobNumber'] + "</td>";
        text += " <td><button class='btn btn-primary btn-xs'>Assign to task</button></td>";
        text += "</tr>";
      });
      
      $("#chooseJobResultsTable").append(text);
    },
    error: function(jqXhr, textStatus, errorThrown){
      if(jqXhr.responseText == 'InvalidLogin'){
        logout();
      }

      alert(errorThrown);
    }
  });
}

function editTaskGroups(TaskId, assignedGroups){
  var currentGroups = assignedGroups.split(',');
  
  $.ajax({
    url: '/api/v1/taskGroup',
    dataType:'json',
    success: function(groups){
      if(groups == 'InvalidLogin'){
        logout();
      }

      var msg = "";
          msg += "Groups";
          msg += "<form name='taskGroupEditForm' role='form'>";
          msg += "<select id='TaskGroup' name='TaskGroup' multiple class='form-control' style='height:auto;'>";

      $.each(groups, function(){
        if($.inArray(this.Name.trim(), currentGroups) != -1){
          msg += "<option selected='selected' value='" + this.Id + "'>" + this.Name + "</option>";
        }else{
          msg += "<option value='" + this.Id + "'>" + this.Name + "</option>";
        }
      });      
   
      msg += "</select>";
      msg += "<br />";
      msg += "<p class='hidden-xs'>Control+left click to select or deselect groups</p>";
      msg += "<button type='button' onclick=\"$('#TaskGroup').val('');\" class='btn btn-primary' title='Remove all groups from this task'>Unselect Groups</button>";
      msg += "</form>";
      
      BootstrapDialog.show({
        title: "Select Which Groups This Task Belongs To",
        message: msg,
        type: BootstrapDialog.TYPE_DEFAULT,
        buttons: [{
          label: "<i class='fa fa-plus-circle'></i> New Group",
          cssClass: "btn-default",
          action: function(dialogRef){
            BootstrapDialog.show({
              title: "Create New Task Group",
              message: "<input class='form-control' id='newTaskGroupName' placeholder='Task Group Name'>",
              buttons: [{
                label: "Cancel",
                action: function(dialogRef2){
                  dialogRef2.close();
                }
              },{
                label: "Save",
                cssClass: "btn-primary",
                action: function(dialogRef2){
                  $.ajax({
                    async: false,
                    url: '/api/v1/taskGroup?groupName=' + $("#newTaskGroupName").val(),
                    method: 'POST',
                    success: function(){
                      currentGroups.push($("#newTaskGroupName").val());
                    },
                    error: function(jqXhr){
                    
                    }
                  });
                
                  $.ajax({
                    url: '/api/v1/taskGroup',
                    dataType: 'json',
                    success: function(groups){
                      if(groups == 'InvalidLogin'){
                        logout();
                      }
                
                      var msg = "";
                    
                      $.each(groups, function(){
                        if($.inArray(this.Name.trim(), currentGroups) != -1){
                          msg += "<option selected='selected' value='" + this.Id + "'>" + this.Name + "</option>";
                        }else{
                          msg += "<option value='" + this.Id + "'>" + this.Name + "</option>";
                        }
                      });
                      
                      $("#TaskGroup").html(msg);  
                    
                      dialogRef2.close();
                    },
                    error: function(jqXhr, status, error){
                      if(jqXhr.responseText == 'InvalidLogin'){
                        logout();
                      }
                
                      BootstrapDialog.show({
                        title: "Problem retrieving task group data",
                        message: "There was a problem loading Task Groups, please try again.",
                        type: BootstrapDialog.TYPE_DANGER,
                        buttons: [{
                          label: "OK",
                          action: function(dialogRef3){
                            dialogRef3.close();
                          }
                        }]
                      });
                    }
                  });
                }
              }]
            });
          }
        },{
          label: "Cancel",
          cssClass: "btn-default",
          action: function(dialogRef){
            dialogRef.close();
          }
        },{
          label: "<i class='fa fa-check-circle-o'></i> Set Groups",
          cssClass: "btn-primary",
          action: function(dialogRef){
            var selectedGroups = $("form[name=taskGroupEditForm] select").val();
            
            if(selectedGroups == ""){
              $("#TaskGroupSection").html("<input type='hidden' name='Groups' value=''>");
              dialogRef.close();
            }else{
              $.ajax({
                url: '/api/v1/taskGroup?Id=' + selectedGroups,
                dataType: 'json',
                success: function(groups){
                  if(groups == 'InvalidLogin'){
                    logout();
                  }
            
                  var groupsArrayNames = [];
                  var groupsArrayIds = []
                  
                  $.each(groups, function(){
                    groupsArrayNames.push(this.Name.trim());
                    groupsArrayIds.push(this.Id);
                  });
                  
                  $("#TaskGroupSection").html("Groups: <span style='color:#337AB7;'>" + groupsArrayNames.join(',') + "</span><input type='hidden' name='Groups' value='" + groupsArrayIds.join(',') + "'>");
                  
                  dialogRef.close();
                },
                error: function(jqXhr, status, error){
                  if(jqXhr.responseText == 'InvalidLogin'){
                    logout();
                  }
            
                  BootstrapDialog.alert({
                    title: "There was a problem getting task group data",
                    message: jqXhr.responseText
                  });
                }
              });
            }
          }
        }]
      });
    },
    error: function(jqXhr, status, error){
      if(jqXhr.responseText == 'InvalidLogin'){
        logout();
      }

      alert(error);    
    }    
  });
}

function applyTaskLinkingFilter(linkedTasks){
  var currentlyLinked = linkedTasks.split(',');
  var formData = $("form[name=taskLinkingFilterForm]").serialize();
  
  if($("select[name=JobIds]").val() == ""){
    var url = '/api/v1/task?fields=TaskId,TaskDescription,JobNumber&MatchType=2&Field=Job %23&';
  }else{
    var url = '/api/v1/task?fields=TaskId,TaskDescription,JobNumber&';
  }
  
  $.ajax({
    url: url + formData,
    dataType:'json',
    success: function(tasks){
      if(tasks == 'InvalidLogin'){
        logout();
      }

      var output = "";

      $.each(tasks, function(){
        if($.inArray(this.TaskId, currentlyLinked) == -1){
          output += "<tr>";
          output += " <td>";
          output += "   <label style='font-weight:normal; cursor:pointer; margin-bottom:0px;'>";
          output += "     <input type='checkbox' name='" + this.TaskId + "'> Select";
        }else{
          output += "<tr style='background-color:yellow;'>";
          output += " <td>";
          output += "   <label style='font-weight:normal; cursor:pointer; margin-bottom:0px;'>";
          output += "     <input type='checkbox' name='" + this.TaskId + "' checked='checked'> Select";
        }
        
        output += "   </label>";
        output += " </td>";
        output += " <td>" + this.TaskDescription + "</td>";
        output += " <td>" + this.JobNumber + "</td>";
        output += "</tr>";
      });
      
      $("#taskLinkingTaskTable tbody").html(output);
      $("form[name=taskLinkingEditForm] input[type=checkbox]").change(function(){
        if($(this).prop("checked")){
          $(this).closest("tr").css("background-color","yellow");
        }else{
          $(this).closest("tr").css("background-color","white");
        }
      });      
    },
    error: function(jqXhr, status, error){
      if(jqXhr.responseText == 'InvalidLogin'){
        logout();
      }

      alert(error);
    }
  });
}









