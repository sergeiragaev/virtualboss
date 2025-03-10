var globalNames = [];
/**********************************************************************************************/
function editNewTask(view){
  $.ajax({
    url: '/api/v1/fieldcaptions?fields=' + allTaskFieldCaptionNames.join(",") + ',' + allContactFieldCaptionNames.join(",") + ',' + allJobFieldCaptionNames.join(","),
    dataType: 'json',
    success: function(names){
      if(names == 'InvalidLogin'){
        logout();
      }
    
      globalNames = names;
      createNewTaskEditScreen(names, view);
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

function createNewTaskEditScreen(names, view){
  var body = "";
      body += "<form role='form' name='details'>";
      body += " <input type='hidden' id='tId' value=''>";
      body += " <div class='form-group'>";
      body += names['TaskDescription'];
      body += "   <input type='text' name='Description' class='form-control' value=''>";
      body += " </div>";

      // JOB # FIELD
      body += " <div class='row'>";
      body += "   <div class='col-xs-12 col-sm-6'>";
      body += "     <div class='input-group form-group'>";
      body += "       <div class='input-group-btn'>";
      body += "         <button class='btn btn-default' type='button' onclick='chooseFromJobList();'>" + names['JobNumber'] + "</button>";
      body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
      body += "         <ul class='dropdown-menu'>";
      body += "           <li><a href='#' onclick=\"chooseFromNewJobList(); return false;\">Choose From " + names['JobNumber'] + " List</a></li>";
      //body += "           <li><a href='#'>Create New + Assign</a></li>";
      body += "           <li role='separator' class='divider'></li>";
      body += "           <li><a href='#' onclick=\"unassignNewJob(); return false;\">Remove</a></li>";
      body += "         </ul>";
      body += "       </div>";
      body += "       <input readonly='readyonly' type='text' name='JobNumber' id='JobNumber' value='' class='form-control' />";
      body += "     </div>";
      body += "   </div>";

      // CONTACT FIELD
      body += "   <div class='col-xs-12 col-sm-6'>";
      body += "     <div class='input-group form-group'>";
      body += "       <div class='input-group-btn'>";
      body += "         <button class='btn btn-default' type='button' onclick=\"chooseFromNewContactList(); return false;\">" + names['ContactPerson'] + "</button>";
      body += "         <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span></button>";
      body += "         <ul class='dropdown-menu'>";
      body += "           <li><a href='#' onclick=\"chooseFromNewContactList(); return false;\">Choose From Contacts</a></li>";
      //body += "           <li><a href='#'>Create New + Assign</a></li>";
      body += "           <li role='separator' class='divider'></li>";
      body += "           <li><a href='#' onclick=\"unassignNewContact(); return false;\">Remove</a></li>";
      body += "         </ul>";
      body += "       </div>";
      body += "       <input readonly='readonly' id='ContactName' type='text' value='' class='form-control' />";
      body += "       <input type='hidden' name='ContactId' value=''>";
      body += "     </div>";
      body += "   </div>";
      body += " </div>";

      // TARGET START, DURATION, AND TARGET FINISH FIELDS
      body += " <div class='row form-group'>";
      body += "   <div class='col-xs-5' style='padding-right:5px;'>";
      body += names['TaskTargetStart'];
      
    body += " <div class='input-group date'>";
    body += "   <input name='TargetStart' readonly style='background-color:#fff;' value='" + moment(new Date()).format("MM/DD/YYYY") + "' class='form-control' />";
    body += "   <div class='input-group-addon'>";
    body += "     <i class='fa fa-calendar'></i>";
    body += "   </div>";
    body += " </div>";

  body += "   </div>";
  body += "   <div class='col-xs-2' style='padding-left:5px; padding-right:5px;'>";
  body += names['TaskDuration'];
  body += "     <input type='number' onchange=\"updateNewTargetFinish();\" name='Duration' min='1' value='1' class='form-control' />";
  body += "   </div>";
  body += "   <div class='col-xs-5' style='padding-left:5px;'>";
  body += names['TaskTargetFinish'];
          
  body += "   <input name='TargetFinish' readonly value='" + moment(new Date()).format("MM/DD/YYYY") + "' class='form-control' />";

  body += "   </div>";
  body += " </div>";
    
  // TASK NUMBER FIELD
  body += " <div class='row form-group'>";
  body += "   <div class='col-lg-4 col-xs-6'>";
  body += names['TaskOrder'];
  body += "     <input type='text' name='Order' value='' class='form-control'>";
  body += "   </div>";
  body += "   <div class='col-lg-4 col-xs-6'>";
  
  // STATUS
  body += names['TaskStatus'];
  body += " <div class='input-group'>";
  body += "   <div class='input-group-btn'>";
  body += "     <button type='button' title='Task Status' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>" + names['TaskStatus'] + " <span class='caret'></span></button>";
  body += "     <ul class='dropdown-menu'>";
  body += "       <li><a href='#' onclick=\"$('input[name=Status]').val('Active'); return false;\">Active</a></li>";
  body += "       <li><a href='#' onclick=\"$('input[name=Status]').val('Done'); return false;\">Done</a></li>";
  body += "     </ul>";
  body += "   </div>";
  body += "   <input type='text' readonly name='Status' class='form-control' value='Active'>";
  body += " </div>";  
  
  body += "   </div>";
  body += "   <div class='col-xs-12 col-lg-4'>";
    
  $.ajax({
    url: '/api/v1/employee',
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
        body += "<option value=\"" + this.Name + "\">" + this.Name + "</option>";
      });

      body += "     </select>";
      body += "   </div>";
      body += " </div>";
      

      // CUSTOM TASK FIELDS
      
      body += " <div class='row'>";
      body += "   <div class='col-xs-12 col-sm-6'>";
      
      if(eval(Cookies.get("ShowTaskCustomField1"))){
        body += "     <div class='input-group form-group' id='TaskCustomField1Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField1 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField1' id='TaskCustomField1' value='' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='TaskCustomField1Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField1 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField1' disabled='true' id='TaskCustomField1' value='' class='form-control' />";      
        body += "     </div>";
      }
      
      if(eval(Cookies.get("ShowTaskCustomField2"))){
        body += "     <div class='input-group form-group' id='TaskCustomField2Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField2 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField2' id='TaskCustomField2' value='' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='TaskCustomField2Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField2 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField2' disabled='true' id='TaskCustomField2' value='' class='form-control' />";      
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowTaskCustomField3"))){
        body += "     <div class='input-group form-group' id='TaskCustomField3Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField3 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField3' id='TaskCustomField3' value='' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='TaskCustomField3Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField3 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField3' disabled='true' id='TaskCustomField3' value='' class='form-control' />";      
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowTaskCustomField4"))){
        body += "     <div class='input-group form-group' id='TaskCustomField4Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField4 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField4' id='TaskCustomField4' value='' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='TaskCustomField4Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField4 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField4' disabled='true' id='TaskCustomField4' value='' class='form-control' />";      
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowTaskCustomField5"))){
        body += "     <div class='input-group form-group' id='TaskCustomField5Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField5 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField5' id='TaskCustomField5' value='' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='TaskCustomField5Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField5 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField5' disabled='true' id='TaskCustomField5' value='' class='form-control' />";      
        body += "     </div>";
      }

      if(eval(Cookies.get("ShowTaskCustomField6"))){
        body += "     <div class='input-group form-group' id='TaskCustomField6Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField6 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField6' id='TaskCustomField6' value='' class='form-control' />";
        body += "     </div>";
      }else{
        body += "     <div class='input-group form-group hidden' id='TaskCustomField6Wrap'>";
        body += "       <div class='input-group-btn'>";
        body += "         <button class='btn btn-default' type='button'>" + names.TaskCustomField6 + "</button>";
        body += "       </div>";
        body += "       <input type='text' name='CustomField6' disabled='true' id='TaskCustomField6' value='' class='form-control' />";      
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
        body += "       <input type='text' name='CustomList1' id='TaskCustomList1' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList1' disabled='true' id='TaskCustomList1' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList2' id='TaskCustomList2' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList2' disabled='true' id='TaskCustomList2' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList3' id='TaskCustomList3' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList3' disabled='true' id='TaskCustomList3' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList4' id='TaskCustomList4' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList4' disabled='true' id='TaskCustomList4' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList5' id='TaskCustomList5' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList5' disabled='true' id='TaskCustomList5' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList6' id='TaskCustomList6' value='' class='form-control' />";
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
        body += "       <input type='text' name='CustomList6' disabled='true' id='TaskCustomList6' value='' class='form-control' />";
        body += "     </div>";
      }

      body += " </div>";
      body += "</div>";
      
      
      
      // TASK NOTES FIELD
      body += " <div class='form-group'>";
      body += names['TaskNotes'];
      //body += "   <div contenteditable='true' style='min-height:150px; height:auto;' class='form-control'>" + data.TaskNotes + "</div>";
      body += "   <textarea contenteditable='true' name='Notes' style='min-height:150px; height:auto; white-space:pre-wrap;' class='form-control'></textarea>";
      body += " </div>";
        
      // TOOLBAR
      // GROUPS
      body += "   <div class='btn-group dropdown'>";
      body += "     <button type='button' class='btn btn-default' onclick='editNewTaskGroups();' title='Add this task to a Group'><i class='fa fa-object-group'></i> Groups</button>";
      //body += "     <button type='button' class='btn btn-default' onclick=\"editNewTaskLinks('" + data.TaskId + "','" + data.JobId + "','" + data.LinkedTasks + "');\" title='Add Task Dependency (Link Task)'><i class='fa fa-link'></i></button>";
      //body += "     <button type='button' title='File Attachments' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><i class='fa fa-paperclip'></i> <span class='caret'></span></button>";
      //body += "     <ul class='dropdown-menu'>";
              
      //var fileAttachments = data.TaskFiles.split("<BR>");
    
      //if(fileAttachments.length <= 1){
      //  body += "<li><a>No Attached Files</a></li>";
      //}else{
      //  $.each(fileAttachments, function(){
      //    body += "<li>" + this + "</li>";
      //  });
      //}
              
      //body += "     </ul>";
      body += "   </div>";
      //body += "   <div class='btn-group dropdown'>";
      //body += "     <button type='button' onclick=\"printNewSection();\" class='btn btn-default' title='Print this task'><i class='fa fa-print'></i></button>";
      //body += "     <button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false' title='Email this task'><i class='fa fa-envelope-o'></i> <span class='caret'></span></button>";
      //body += "     <ul class='dropdown-menu'>";
      //body += "       <li><a href=\"mailto:" + data['ContactEmail'] + "&Subject=Work Request For " + data['TaskDescription'] + "&Body=" + encodeURIComponent(data['TaskNotes']) + "\">Email to (" + data['ContactPerson'] + ")</a></li>";
      //body += "       <li><a href='#'>New</a></li>";
      //body += "     </ul>";
      //body += "   </div>";
      
      // GROUPS
      /*if(data.TaskGroups.trim()){ // is false if string is empty or ""
        body += "   <div class='form-group' style='margin-top:12px; margin-bottom:0px;'>";
        body += "     Groups: ";
             
        var groups = data.TaskGroups.split(',');
               
        $.each(groups, function(i){
          if(i != groups.length - 1){
            body += "<a href='#'>" + this + "</a>, ";
          }else{
            body += "<a href='#'>" + this + "</a>";
          }
        });
            
        body += "   </div>";    
      }else{
        body += "<div class='form-group' style='margin-top:12px; margin-bottom:0px; font-size:12px; color:#999; font-style:italic;'>Not assigned to a Group</div>";  
      } */  
      body += "<div class='form-group' style='margin-top:12px; margin-bottom:0px;' id='TaskGroupSection'></div>";  

      // TASK LINKING  
      /*if(data.TaskFollows.trim()){
        body += "<div class='form-group' style='margin-top:12px; margin-bottom:0px;'>";
        body += "Currently Linked With:";
        body += "<ul>";
        
        $.ajax({
          url: '/api/v1/taskdata?taskId=' + data.TaskFollows,
          dataType:'json',
          success: function(tasks){
            $.each(tasks, function(){
              body += "<li><a href='#' onclick=\"editTask('" + this.TaskId + "');return false;\">" + this.TaskDescription + "</a></li>";
            });
        
            body += "</ul>";
            body += "</div>";
            body += "</form>";
            
            showTaskEditScreen(data, body);
          },
          error: function(jqXhr, textStatus, errorThrown){
            alert(errorThrown);
          }
        });
      }else{
        body += "<div class='form-group' style='margin-top:12px; margin-bottom:0px; font-size:12px; color:#999; font-style:italic;'>Not linked with another task</div>";
        */
        body += "</form>";
        
        showNewTaskEditScreen(body, view);
      //}
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

function editNewTaskLinks(taskId, jobId, linkedTasks){
  $.ajax({
    url: "/api/v1/task?fields=TaskDescription,TaskId&JobNumber=" + jobId,
    dataType:'json',
    success: function(taskData){
      if(taskData == 'InvalidLogin'){
        logout();
      }
    
      var omitTasksArray = linkedTasks.split(',');
      var msg = "";
          msg += "<select class='form-control'>";
      
      $.each(taskData, function(){  
        if($.inArray(this.TaskId, omitTasksArray) == -1){
          msg += "<option value='" + this.TaskId + "'>" + this.TaskDescription + "</option>";
          console.log("Adding " + this.TaskDescription + "(" + this.TaskId + ")");
        }else{
          console.log("Skipping " + this.TaskDescription + "(" + this.TaskId + ")");
          // linking this task could cause infinite loop, so omit it. 
        }
      });
      
      msg += "</select>";
          
      BootstrapDialog.show({
        title: "Link Tasks",
        message: msg,
        buttons:[{
          label: "Link",
          cssClass: "btn-primary",
          action: function(dialogRef){
                
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
    error:function(jqXhr, textStatus, errorThrown){
      if(jqXhr.responseText == 'InvalidLogin'){
        logout();
      }
    
      alert(errorThrown);
    }
  });
}

function showNewTaskEditScreen(body, view){
  BootstrapDialog.show({
    title: "New Task",
    message: body,
    closable: false,
    //draggable: true,
    onshown: function(){
      $("input[name=TargetStart]").datepicker();
      $("input[name=TargetStart]").change(function(){
        $(this).datepicker("hide");
        
        updateNewTargetFinish();
      });
      
      /* Feature - Auto fill current job filter to new task */
      var currentFilters = Cookies.get("TaskFilters").split("&");
      var filtersArrayNames = [];
      var filtersArrayValues = [];
      var jobFilterIsActive = false;
      var contactFiltersIsActive = false;
      var currentJobFilterId = "";
      var currentContactFilterId = "";
      
      $.each(currentFilters, function(){
        filtersArrayNames.push(this.split("=")[0]);
        filtersArrayValues.push(this.split("=")[1]);
      });
      
      var jobSearch = $.inArray("JobIds", filtersArrayNames);
      var contactSearch = $.inArray("CustIds", filtersArrayNames);
      
      if(jobSearch != -1){
        jobFilterIsActive = true;
        currentJobFilterId = filtersArrayValues[jobSearch];
        
        $.ajax({
          url: '/api/v1/job/' + currentJobFilterId,
          dataType: 'json',
          success: function(jobData){
            if(jobData == 'InvalidLogin'){
              logout();
            }
          
            $("#JobNumber").val( jobData.JobNumber);
          },
          error: function(jqXhr, status, error){
            if(jqXhr.responseText == 'InvalidLogin'){
              logout();
            }
          
            alert(error);
          }
        });
      }
      
      if(contactSearch != -1){
        contactFiltersIsActive = true;
        currentContactFilterId = filtersArrayValues[contactSearch];
        
        $.ajax({
          url: '/api/v1/contact/' + currentContactFilterId,
          dataType: 'json',
          success: function(contactData){
            if(contactData == 'InvalidLogin'){
              logout();
            }
          
            $("#ContactName").val(contactData.ContactPerson + " (" + contactData.ContactCompany + ")");
            $("input[name=ContactId]").val(contactData.ContactId);
          },
          error: function(jqXhr, status, error){
            if(jqXhr.responseText == 'InvalidLogin'){
              logout();
            }
          
            alert(error);
          }
        });        
      }
      /* end feature */
    },
    size: BootstrapDialog.SIZE_WIDE,
    buttons: [{
      label:'Cancel',
      cssClass:'btn-default',
      action: function(dialogRef){
        dialogRef.close();
      }    
    },{
      label: 'Save New Task',
      cssClass:'btn-primary',
      action: function(dialogRef){
        if($("input[name=Description]").val().trim() == ""){
          BootstrapDialog.show({
            title: "Missing Task Description",
            message: "Please give this task a description",
            type: BootstrapDialog.TYPE_WARNING,
            buttons: [{
              label: "Ok",
              cssClass: "btn-primary",
              action: function(dialogRef){
                dialogRef.close();
              }
            }]
          });
        
          return; 
        }
        
        if($("#JobNumber").val() == ""){
          $("#JobNumber").prop("disabled", true);
        }
        
        if($("input[name=ContactId]").val().trim() == ""){
          $("input[name=ContactId]").prop("disabled", true);
        }

        var fieldArray = $("form[name=details]").serializeArray();

        // $.each(fieldArray, function(){
        //   if(this.value == "" || this.value == " "){
        //     this.value = "None";
        //   }
        // });

        var formData = $.param(fieldArray);      
        console.log(formData);

        $.ajax({
          url: '/api/v1/task',  //?' + formData,
          method: 'POST',
          data: formData,
          success: function(response){
            if(response == 'InvalidLogin'){
              logout();
            }
          
            switch(view){
              case "TaskManager":
                createTaskList();              
              break;
              case "Calendar":
                loadCalendarView();
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
          
            BootstrapDialog.alert({
              title:'There was a problem saving',
              message:jqXHR.responseText              
            });
          }
        });
      }
    }] 
  });

}

function printNewSection(){
  $("form[name=details]").print();
}

function updateNewTargetFinish(){
  var duration = $("input[name=TaskDuration]").val();
      duration--;
  
  $("input[name=TargetFinish]").val(moment($("input[name=TargetStart]").val(), "MM/DD/YYYY").add(duration, "days").format("MM/DD/YYYY"));
}

function chooseFromNewJobList(){
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
                      d += "<option value=\"" + this.JobNumber + "\">" + this.JobNumber + "</option>";
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
            $("#JobNumber").val($("#taskEditJobSelect").val());
            
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

function chooseFromNewContactList(){
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
            $("#taskEditContactSelect").val($("input[name=ContactId").val());
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

function unassignNewJob(){
  $("#JobNumber").val("");
}

function unassignNewContact(){
  $("#ContactName").val("");
  $("input[name=ContactId]").val("UNASSIGNED");
}

function chooseNewJobShowAll(){
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

function editNewTaskGroups(){
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
        msg += "<option value='" + this.Id + "'>" + this.Name + "</option>";      
      });      
   
      msg += "</select>";
      msg += "</form>";
      
      BootstrapDialog.show({
        title: "Select Which Groups This Task Belongs To",
        message: msg,
        type: BootstrapDialog.TYPE_DEFAULT,
        buttons: [{
          label: "Cancel",
          action: function(dialogRef){
            dialogRef.close();
          }
        },{
          label: "Set Groups",
          cssClass: "btn-primary",
          action: function(dialogRef){
            var selectedGroups = $("form[name=taskGroupEditForm] select").val();

            if(selectedGroups == ""){
              $("#TaskGroupSection").html('');
              var groupsInput = "<input type='hidden' name='Groups' value=''>";
              $("form[name=details]").append(groupsInput);

              dialogRef.close();

              return;
            }
            
            $.ajax({
              url: '/api/v1/taskGroup?Id=' + selectedGroups,
              dataType: 'json',
              success: function(groups){
                if(groups == 'InvalidLogin'){
                  logout();
                }
              
                var groupsArray = [];
                
                $.each(groups, function(){
                  groupsArray.push(this.Name.trim());
                });
                
                var groupsInput = "<input type='hidden' name='Groups' value='" + selectedGroups + "'>";
                
                $("#TaskGroupSection").html("Groups: <span style='color:#337AB7;'>" + groupsArray.join(',') + "</span>");
                $("form[name=details]").append(groupsInput);
                
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











