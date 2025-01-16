/***********************************************************************************************/
// use array.join(',') to convert an array to comma separated string.
var defaultTaskListFields        = ["TaskOrder,TaskDescription,JobNumber,TaskTargetStart,TaskDuration,TaskTargetFinish,TaskStatus"];
var allTaskFieldCaptionNames     = ["TaskDescription","TaskTargetStart","TaskDuration","TaskTargetFinish","TaskActualFinish","TaskOrder","TaskStatus","TaskNumber","TaskFollows","FinishPlus","TaskRequested","TaskMarked","TaskNotes","TaskCustomField1","TaskCustomField2","TaskCustomField3","TaskCustomField4","TaskCustomField5","TaskCustomField6","TaskCustomList1","TaskCustomList2","TaskCustomList3","TaskCustomList4","TaskCustomList5","TaskCustomList6"];
var allJobFieldCaptionNames      = ["JobNumber","JobLot","JobOwnerName","JobSubdivision","JobLockBox","JobAddress1","JobAddress2","JobCity","JobState","JobPostal","JobCountry","JobHomePhone","JobWorkPhone","JobCellPhone","JobFax","JobCompany","JobEmail","JobNotes","JobDirections","JobCustomField1","JobCustomField2","JobCustomField3","JobCustomField4","JobCustomField5","JobCustomField6","JobCustomList1","JobCustomList2","JobCustomList3","JobCustomList4","JobCustomList5","JobCustomList6"];
var allContactFieldCaptionNames  = ["ContactCompany","ContactPerson","ContactProfession","ContactFirstName","ContactLastName","ContactSupervisor","ContactSpouse","ContactTaxID","ContactWebSite","ContactEmail","ContactFax","ContactWorkersCompDate","ContactInsuranceDate","ContactComments","ContactNotes","ContactPhones","ContactCustomField1","ContactCustomField2","ContactCustomField3","ContactCustomField4","ContactCustomField5","ContactCustomField6","ContactCustomList1","ContactCustomList2","ContactCustomList3","ContactCustomList4","ContactCustomList5","ContactCustomList6"];
var defaultTaskFilters = "IsActive=on";
var defaultTaskCustomFieldOptions = [];

/***********************************************************************************************/
 
$(document).ready(function(){
  loadTaskSettings();
  loadTaskFilters();
  createTaskList();
});

/***********************************************************************************************/

function loadTaskSettings(){ 
  if(!Cookies.get("TaskListFieldsToShow")){
    setCookie("TaskListFieldsToShow", defaultTaskListFields.join(','));
  }
  
  if(!Cookies.get("TaskListSort")){
    setCookie("TaskListSort", "[[2,0],[0,0]]");
  }

  if(!Cookies.get("TaskFiltersJobAutoLoad")){
    setCookie("TaskFiltersJobAutoLoad", true);
  }
  
  if(!Cookies.get("TaskLimit")){
    setCookie("TaskLimit", 100);
  }
  
  if(!Cookies.get("ShowAllTasks")){
    setCookie("ShowAllTasks", true);
  }
  
  if(!Cookies.get("TaskFiltersContactAutoLoad")){
    setCookie("TaskFiltersContactAutoLoad", true);
  }
  
  if(!Cookies.get("UseTaskColor")){
    setCookie("UseTaskColor", false);
  }

  // set task custom field options
  
  if(!Cookies.get("ShowTaskCustomField1")){
    setCookie("ShowTaskCustomField1", false);
  }
  
  if(!Cookies.get("ShowTaskCustomField2")){
    setCookie("ShowTaskCustomField2", false);
  }
  
  if(!Cookies.get("ShowTaskCustomField3")){
    setCookie("ShowTaskCustomField3", false);
  }
  
  if(!Cookies.get("ShowTaskCustomField4")){
    setCookie("ShowTaskCustomField4", false);
  }
  
  if(!Cookies.get("ShowTaskCustomField5")){
    setCookie("ShowTaskCustomField5", false);
  }
  
  if(!Cookies.get("ShowTaskCustomField6")){
    setCookie("ShowTaskCustomField6", false);
  }
  
  // task custom list options
  
  if(!Cookies.get("ShowTaskCustomList1")){
    setCookie("ShowTaskCustomList1", false);
  }
  
  if(!Cookies.get("ShowTaskCustomList2")){
    setCookie("ShowTaskCustomList2", false);
  }
  
  if(!Cookies.get("ShowTaskCustomList3")){
    setCookie("ShowTaskCustomList3", false);
  }
  
  if(!Cookies.get("ShowTaskCustomList4")){
    setCookie("ShowTaskCustomList4", false);
  }
  
  if(!Cookies.get("ShowTaskCustomList5")){
    setCookie("ShowTaskCustomList5", false);
  }
  
  if(!Cookies.get("ShowTaskCustomList6")){
    setCookie("ShowTaskCustomList6", false);
  }

  // word wrap options
  
  if(!Cookies.get("wordWrapForHeaderText")){
    setCookie("wordWrapForHeaderText", false);
  }

  if(!Cookies.get("wordWrapForColumnText")){
    setCookie("wordWrapForColumnText", false);
  }
  
  if(!Cookies.get("wordWrapForHeaderText")){
    setCookie("wordWrapForHeaderText", false);
  }
   
  $("form[name=taskListSearchForm]").submit(function(event){   
    event.preventDefault();
    var phrase = $("#taskListSearchBox").val().replace(/["'&?,;]/g, "");
    
    setCookie("filterFindString", phrase);
    createTaskList("/api/v1/task?FindString=" + encodeURIComponent(phrase));
  });
}

function loadTaskFilters(){  
  if(!Cookies.get("TaskFilters")){
    setCookie("TaskFilters", defaultTaskFilters);
  }
  
  if(!Cookies.get("filterFindString")){
    setCookie("filterFindString", "");
  }
  
  $("#taskListSearchBox").val(Cookies.get('filterFindString'));
}

function createTaskList(customUrl){
  var taskFieldsArray = getTaskListFieldsToShowArray();
  var activeTaskFilters = getActiveTaskFilters();

  if(taskFieldsArray[0].length <= 0){
    taskFieldsArray = ["TaskDescription"]; // There should always be at least one field.
    setCookie("TaskListFieldsToShow", "TaskDescription");
  }
  
  setFiltersMessage(activeTaskFilters, 'TaskManager');
  
  BootstrapDialog.show({
    title: "Loading Tasks, Please wait...",
    message: "<i class='fa fa-circle-o-notch fa-spin' style='margin-right:5px;'></i> Loading Task List...<p style='font-size:12px; margin-top:15px; margin-bottom:0px;'>" + $('#activeFiltersMessage').html() + "</p><hr style='margin-top:7px; margin-bottom:7px;' /><div style='font-size:12px; color:#999;'>Closing this window is ok. Tasks will continue to load and display when ready.</div>"
  });
  
  // START TASK LIST CREATION  
  if(!customUrl){
    var dataUrl = "";
    
    if(eval(Cookies.get('UseTaskColor'))){
      dataUrl += "/api/v1/task?fields=TaskId," + taskFieldsArray.join(',') + ",TaskColor" + "&" + activeTaskFilters + "&FindString=" + Cookies.get('filterFindString');
    }else{
      dataUrl += "/api/v1/task?fields=TaskId," + taskFieldsArray.join(',') + "&" + activeTaskFilters + "&FindString=" + Cookies.get('filterFindString');
    }
  }else{
    // for /api/v1/task, need to use TaskId but for /api/v1/fieldcaptions, use TaskNumber
    var dataUrl = customUrl + "&fields=TaskId," + taskFieldsArray.join(',');
  }
  
  var tblData = "";

  $.ajax({
    url: dataUrl,
    dataType: 'json',
    success: function(tasks){
      if(tasks == 'InvalidLogin'){
        logout();      
      }      
      
      if(!tasks.length){
        BootstrapDialog.closeAll();
        $("#tCount").html("0, <span style='font-style:italic; color:#ababab;'>nothing matched your search or current filters</span>");
      }else if(eval(Cookies.get('ShowAllTasks'))){
        $("#tCount").html(tasks.length);
      }else{
        $("#tCount").html(tasks.length + " (<a href='#' title='Change number of tasks to show' onclick=\"editOptions(); return false;\">Showing</a> up to " + Cookies.get('TaskLimit') + ")");
      }
      
      //if(tasks.length >= 200){
      //  $("#tCount").html(tasks.length + " (Showing 200)");
      //}else{
      //  $("#tCount").html(tasks.length);
      //}
      
      var tbl = "<div class='table-responsive'>";
          tbl += "<table class='table table-bordered table-condensed table-striped tablesorter table-hover'>";
          tbl += "  <thead>";
          tbl += "    <tr title='Click to sort (shift+click to multi-sort)'>";

      $.ajax({
        url: '/api/v1/fieldcaptions?fields=' + taskFieldsArray.join(',').replace('TaskId', 'TaskNumber'),
        dataType:'json',
        success: function(names){
          if(names == 'InvalidLogin'){
            logout();          
          }
                            
          $.each(taskFieldsArray, function(){
            if(this == "TaskId"){
              tbl += "<th>" + names['TaskNumber'] + "</th>";
            }else{
              tbl += "<th>" + names[this] + "</th>";
            }
          });
          
          tbl += "    </tr>";
          tbl += "  </thead>";
          tbl += "  <tbody>";
          
          //if(tasks.length >= 10000){
          //  if(confirm("This might take a few minutes to finish, hit Ok to stop and change Filters or hit Cancel to continue loading")){
          //    BootstrapDialog.closeAll();
          //    openBasicFilters();
          //    return;
          //  }
          //}
          
          var rowClickable = false;
          
          if($.inArray("TaskDescription", taskFieldsArray) != -1){
            rowClickable = false;
          }else{
            rowClickable = true;
          }
          
          //if(tasks.length >= 200){
          if(!eval(Cookies.get('ShowAllTasks'))){
            var limit = 0;
            
            if(tasks.length <= Cookies.get('TaskLimit')){
              limit = tasks.length;
            }else{
              limit = Cookies.get('TaskLimit');
            }
            
            for(var i = 0; i < limit; i++){
              if(rowClickable){
                if(eval(Cookies.get('UseTaskColor'))){
                  tbl += "<tr onclick=\"editTask('" + tasks[i]['TaskId'] + "','TaskManager');\" style='cursor:pointer; color:" + tasks[i].TaskColor + ";'>";
                }else{
                  tbl += "<tr onclick=\"editTask('" + tasks[i]['TaskId'] + "','TaskManager');\" style='cursor:pointer;'>";
                }
              }else{
                if(eval(Cookies.get('UseTaskColor'))){
                  tbl += "<tr style='color:" + tasks[i].TaskColor + ";'>";
                }else{
                  tbl += "<tr>";
                }
              }
                  
              $.each(taskFieldsArray, function(j){
                if(taskFieldsArray[j] == "TaskDescription"){
                  if(tasks[i]['TaskDescription'] == ""){
                    tbl += "<td><a href='#' onclick=\"editTask('" + tasks[i]['TaskId'] + "','TaskManager');return false;\" style='font-style:italic; font-size:12px;' title='click to edit'>(No Description)</a></td>";
                  }else{
                    tbl += "<td><a href='#' onclick=\"editTask('" + tasks[i]['TaskId'] + "','TaskManager');return false;\">" + tasks[i][taskFieldsArray[j]].replace(/&/g, '&#38').replace(/</g, '&lt').replace(/>/g, '&gt') + "</a></td>";
                  }
                }else if(taskFieldsArray[j] == "TaskTargetStart" || taskFieldsArray[j] == "TaskTargetFinish" || taskFieldsArray[j] == "TaskActualFinish"){
                  tbl += "<td>" + formatDate(tasks[i][taskFieldsArray[j]]) + "</td>";
                }else if(taskFieldsArray[j] == "TaskNotes"){
                  tbl += "<td>" + tasks[i][taskFieldsArray[j]] + "</td>";
                }else if(taskFieldsArray[j] == "TaskMarked"){
                  tbl += (eval(tasks[i][taskFieldsArray[j]].toLowerCase())) ? "<td style='text-align:center;'><span class='hidden'>1</span><input type='checkbox' name='tmkd' data-t-marked='" + tasks[i]['TaskId'] + "' checked='checked'></td>" : "<td style='text-align:center;'><span class='hidden'>0</span><input name='tmkd' data-t-marked='" + tasks[i]['TaskId'] + "' type='checkbox'></td>";
                // }else if(taskFieldsArray[j] == "JobAddress1"){
                //   tbl += "<td>" + tasks[i].JobAddress + "</td>";
                }else{
                  tbl += "<td><div>" + tasks[i][taskFieldsArray[j]] + "</div></td>";
                }
              });
                  
              tbl += "</tr>";
            }
          }else{
            $.each(tasks, function(i){
              if(rowClickable){
                if(eval(Cookies.get('UseTaskColor'))){
                  tbl += "<tr onclick=\"editTask('" + tasks[i]['TaskId'] + "','TaskManager');\" style='cursor:pointer; color:" + tasks[i].TaskColor + ";'>";
                }else{
                  tbl += "<tr onclick=\"editTask('" + tasks[i]['TaskId'] + "','TaskManager');\" style='cursor:pointer;'>";
                }
              }else{
                if(eval(Cookies.get('UseTaskColor'))){
                  tbl += "<tr style='color:" + tasks[i].TaskColor + ";'>";
                }else{
                  tbl += "<tr>";
                }
              }
   
              $.each(taskFieldsArray, function(j){
                if(taskFieldsArray[j] == "TaskDescription"){
                  if(tasks[i]['TaskDescription'] == ""){
                    tbl += "<td><a href='#' onclick=\"editTask('" + tasks[i]['TaskId'] + "','TaskManager');return false;\" style='font-style:italic; font-size:12px;' title='click to edit'>(No Description)</a></td>";
                  }else{
                    tbl += "<td><a href='#' onclick=\"editTask('" + tasks[i]['TaskId'] + "','TaskManager');return false;\">" + tasks[i][taskFieldsArray[j]].replace(/&/g, '&#38').replace(/</g, '&lt').replace(/>/g, '&gt') + "</a></td>";
                  }
                }else if(taskFieldsArray[j] == "TaskTargetStart" || taskFieldsArray[j] == "TaskTargetFinish" || taskFieldsArray[j] == "TaskActualFinish"){
                  tbl += "<td>" + formatDate(tasks[i][taskFieldsArray[j]]) + "</td>";
                }else if(taskFieldsArray[j] == "TaskNotes"){
                  tbl += "<td>" + tasks[i][taskFieldsArray[j]] + "</td>";
                }else if(taskFieldsArray[j] == "TaskMarked"){
                  tbl += (eval(tasks[i][taskFieldsArray[j]].toLowerCase())) ? "<td style='text-align:center;'><span class='hidden'>1</span><input type='checkbox' name='tmkd' data-t-marked='" + tasks[i]['TaskId'] + "' checked='checked'></td>" : "<td style='text-align:center;'><span class='hidden'>0</span><input name='tmkd' data-t-marked='" + tasks[i]['TaskId'] + "' type='checkbox'></td>";
                // }else if(taskFieldsArray[j] == "JobAddress1"){
                //   tbl += "<td>" + tasks[i].JobAddress + "</td>";
                }else{
                  tbl += "<td>" + tasks[i][taskFieldsArray[j]] + "</td>";
                }
              });
                  
              tbl += "</tr>";
            });
          }
          
          tbl += "  </tbody>";
          tbl += "</table>";
          tbl += "</div>";

          $("#tTaskList").html(tbl);
          // table has been created
          
          if(eval(Cookies.get("wordWrapForHeaderText"))){
            $("th").css("white-space", "normal");
          }else{
            $("th").css("white-space", "nowrap");
          }
        
          if(eval(Cookies.get("wordWrapForColumnText"))){
            $("td").css("white-space", "normal");
          }else{
            $("td").css("white-space", "nowrap");
          }
          
          if(!tasks.length){
            // no tasks so skip table sorting
          }else{
            $("#tTaskList table").tablesorter({
              sortList: eval(Cookies.get("TaskListSort"))
            }).bind("sortStart",function(){
              
            }).bind("sortEnd", function(data){            
              setCookie("TaskListSort", data.delegateTarget.config.sortList);
            });
            
            BootstrapDialog.closeAll();
            postEffects();
          }
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
    
      var tmp = document.createElement("DIV");
      
      $(tmp).html(jqXhr.responseText);
      tmp = $(tmp).text().trim();
      
      BootstrapDialog.closeAll();
      BootstrapDialog.alert({
        message: "There was a problem loading your data.  <br /><br />If your database is accessed over a network, you may need to <i>disable</i> the option to run Remote VirtualBoss as a system service.<hr />Server Response: <br /><br /><div>" + tmp + "</div>",
        type: BootstrapDialog.TYPE_WARNING
      });
    }   
  });
  // END TASK LIST CREATION 
}

function postEffects(){
  activateMarkedTriggers();
}

function activateMarkedTriggers(){
  $("input[name=tmkd]").change(function(){
    var status = $(this).prop("checked");
    var id = $(this).data("t-marked");
     
    if($(this).prop("checked")){
      $(this).prev().html("1");
    }else{
      $(this).prev().html("0");
    }
     
    $(this).parents("table").trigger("update");     
    
    $.ajax({
      url: '/api/v1/task/' + id + '?Marked=' + status,
      method: 'PUT',
      success: function(response){
        if(response == 'InvalidLogin'){
          logout();
        }
      },
      error: function(jqXHR, textStatus, errorThrown){
        if(jqXHR.responseText == 'InvalidLogin'){
          logout();
        }
      }
    });
  });
}

function getActiveTaskFilters(){
  var currentActiveFilters = Cookies.get("TaskFilters");

  return currentActiveFilters;
}

function setCookie(name, value){
  Cookies.set(name, value, {
    expires: 365
  });
}

function removeCookie(name){
  Cookies.remove(name);
}

function getTaskListFieldsToShowArray(){
  return Cookies.get("TaskListFieldsToShow").split(",");
}

function formatDate(d){
  if (!d) return "";
  // YYYY-MM-DD
  var t = d.split("-");
  return t[1] + "/" + t[2] + "/" + t[0];
}

function editListColumns(){
  //var captionUrl = "/api/v1/fieldcaptions?fields=" + allTaskFieldCaptionNames.join(',') + "," + allContactFieldCaptionNames.join(',') + "," + allJobFieldCaptionNames.join(',');
  var taskFieldNames, contactFieldNames, jobFieldNames;
  
  $.ajax({
    url: "/api/v1/fieldcaptions?fields=" + allTaskFieldCaptionNames.join(','),
    dataType:'json',
    success: function(names){
      if(names == 'InvalidLogin'){
        logout();
      }
      
      taskFieldNames = names;
      console.log(taskFieldNames);
      $.ajax({
        url: "/api/v1/fieldcaptions?fields=" + allContactFieldCaptionNames.join(','),
        dataType:'json',
        success: function(names){
          if(names == 'InvalidLogin'){
            logout();
          }
        
          contactFieldNames = names;
          
          $.ajax({
            url: "/api/v1/fieldcaptions?fields=" + allJobFieldCaptionNames.join(','),
            dataType:'json',
            success: function(names){
              if(names == 'InvalidLogin'){
                logout();
              }
            
              jobFieldNames = names;
            
              var msg = "";
                  msg += "<div class='row'>";
                  msg += " <div class='col-xs-12'>";
                  msg += "  <div class='checkbox'>";
                  msg += "    <label>";
                  msg += "      <input type='checkbox' id='toggleAllBox' onchange='toggleOptionBoxes();'> Check / Uncheck All";
                  msg += "    </label>";
                  msg += "  </div>";
                  msg += " </div>";
                  msg += "</div>";
                  msg += "<div id='taskListFieldSettingsToggle' class='row'>";
                  msg += "<form role='form' name='taskFieldsToShowForm'>";
        
              var fieldValues = getTaskListFieldsToShowArray(); // returns an array of all current active task list fields to appear on the task list. needed to tell which fields are currently 'active'
              
              msg += "<div class='col-xs-6 col-lg-4'>";
              msg += "<h4>Task Fields</h4>";
              
              $.each(taskFieldNames, function(i){
                msg += "<div class='checkbox'><label>";
                
                if($.inArray(i, fieldValues) != -1){
                  msg += "<input type='checkbox' checked='checked' name='" + i + "'> <span class='activeFieldColor'>" + this + "</span>";
                }else{
                  msg += "<input type='checkbox' name='" + i + "'> <span class='notActiveColor'>" + this + "</span>";
                }
                
                msg += "</label></div>";
              });
              
              msg += "</div>";
              msg += "<div class='col-xs-6 col-lg-4'>";
              msg += "<h4>Contact Fields</h4>";
              
              $.each(contactFieldNames, function(i){
                msg += "<div class='checkbox'><label>";
                
                if($.inArray(i, fieldValues) != -1){
                  msg += "<input type='checkbox' checked='checked' name='" + i + "'> <span class='activeFieldColor'>" + this + "</span>";
                }else{
                  msg += "<input type='checkbox' name='" + i + "'> <span class='notActiveColor'>" + this + "</span>";
                }
                
                msg += "</label></div>";
              });              
              
              msg += "</div>";
              msg += "<div class='col-xs-6 col-lg-4'>";
              msg += "<h4>Job Fields</h4>";
              
              $.each(jobFieldNames, function(i){
                msg += "<div class='checkbox'><label>";
                
                if($.inArray(i, fieldValues) != -1){
                  msg += "<input type='checkbox' checked='checked' name='" + i + "'> <span class='activeFieldColor'>" + this + "</span>";
                }else{
                  msg += "<input type='checkbox' name='" + i + "'> <span class='notActiveColor'>" + this + "</span>";
                }
                
                msg += "</label></div>";
              });              
              
              msg += "</div>";           
              msg += "</form>";
              msg += "</div>";
              
              BootstrapDialog.show({
                title: "Choose Fields To Show On Task List",
                message: msg,
                buttons:[{
                  label: "<span class='hidden-xs'>Restore </span>Defaults",
                  cssClass: "btn-warning",
                  action: function(dialogRef){
                    $.each($("form[name=taskFieldsToShowForm] input[type=checkbox]"), function(){
                      $(this).prop("checked", false);
                    });
                    
                    $.each(defaultTaskListFields[0].split(','), function(){
                      $("form[name=taskFieldsToShowForm] input[name=" + this + "]").prop("checked", true);
                    });
                  }
                },{
                  label: "Cancel",
                  action: function(dialogRef){
                    dialogRef.close();
                  }
                },{
                  label: "Save Changes",
                  cssClass: "btn-primary",
                  action: function(dialogRef){
                    var activeTaskFields = [];
                    var inputList = $("form[name=taskFieldsToShowForm]").serializeArray();
                    
                    if(inputList.length <= 0){
                      alert("You need to have at least 1 field active");
                      return false;
                    }
                    
                    $.each(inputList, function(){
                      activeTaskFields.push($(this).attr("name"));  
                    });

                    setCookie("TaskListFieldsToShow", activeTaskFields.join(','));
                    setCookie("TaskListSort", "[[0,0]]"); // resets table sort (solves issues when someone removes the sorted list)
                    createTaskList();
                    
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

function editFieldOrder(){
  var fields = getTaskListFieldsToShowArray();
  var msg = "";
  
  msg += "The top field shows up 1st on the Task List. The bottom field is last.  Drag and Drop the fields to change.";
  msg += "<hr />";
  msg += "<div>";
  msg += "<ul id='sortableFields'>";
  
  $.ajax({
    url: "/api/v1/fieldcaptions?fields=" + allTaskFieldCaptionNames.join(',') + "," + allJobFieldCaptionNames.join(',') + "," + allContactFieldCaptionNames.join(','),
    dataType:'json',
    success: function(names){
      if(names == 'InvalidLogin'){
        logout();
      }
    
      var currentSort = eval(Cookies.get("TaskListSort"));
      var originalSortArray = [];
      var originalSortArrayFieldNames = [];
      var originalSortArrayFieldDirection = [];
      
      $.each(currentSort, function(){        
        originalSortArray.push(this);
        originalSortArrayFieldNames.push(fields[this[0]]);
        originalSortArrayFieldDirection.push(this[1]);
      });

      $.each(fields, function(i){
        msg += "<li data-field-name='" + this + "'>" + names[this] + "</li>";
      });
      
      msg += "</ul>";
      msg += "</div>";
      
      BootstrapDialog.show({
        title: "Change Field Order",
        message: msg,
        onshown: function(){
          $("#sortableFields").sortable();
        },
        buttons:[{
          label: "Cancel",
          action: function(dialogRef){
            dialogRef.close();
          }
        },{
          label: "Save Field Order",
          cssClass: "btn-primary",
          action: function(dialogRef){
            
            var newFieldOrderArray = [];
            
            $.each($("#sortableFields li"), function(i){
              newFieldOrderArray.push($(this).data("field-name"));
            });
            
            var newFieldPositionArray = [];
            var sortString = "[";
            
            for(var i = 0; i < originalSortArrayFieldNames.length; i++){
              var newPosition = $.inArray(originalSortArrayFieldNames[i], newFieldOrderArray);
              newFieldPositionArray.push("[" + newPosition + "," + originalSortArrayFieldDirection[i] + "]");
              
            }
            
            sortString += newFieldPositionArray.join(',');
            sortString += "]";            
            
            setCookie("TaskListFieldsToShow", newFieldOrderArray.join(','));
            setCookie("TaskListSort", sortString);
            
            createTaskList();
            
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
}

function editOptions(){
  var msg = "";
      msg += "<form role='form' name='taskManagerOptionsForm'>";
      msg += " <div class='checkbox'>";
      msg += "  <label>";

      if(eval(Cookies.get('wordWrapForHeaderText'))){
        msg += "<input type='checkbox' id='wordWrapForHeaderText' checked='checked'> Use Wordwrap for column headers";
      }else{
        msg += "<input type='checkbox' id='wordWrapForHeaderText'> Use Wordwrap for column headers";
      }

      msg += "  </label>";
      msg += " </div>";
      msg += " <div class='checkbox'>";
      msg += "  <label>";

      if(eval(Cookies.get('wordWrapForColumnText'))){
        msg += "<input type='checkbox' id='wordWrapForColumnText' checked='checked'> Use Wordwrap for column text";
      }else{
        msg += "<input type='checkbox' id='wordWrapForColumnText'> Use Wordwrap for column text";
      }

      msg += "  </label>";
      msg += " </div>";
      msg += " <div class='checkbox'>";
      msg += "  <label>";
      
      if(eval(Cookies.get('UseTaskColor'))){
        msg += "<input id='taskColorOption' name='UseTaskColor' type='checkbox' checked='checked'> Use Active Color Profile For Tasks";
      }else{
        msg += "<input id='taskColorOption' name='UseTaskColor' type='checkbox'> Use Active Color Profile For Tasks";      
      }
      
      msg += "  </label>";
      msg += "</div>";
   
      msg += "<div class='checkbox'>";
      msg += "  <label>";
      
      if(eval(Cookies.get('ShowAllTasks'))){
        msg += "<input type='checkbox' onchange='taskLimitToggle();' name='ShowAllTasks' checked='checked' id='taskLimitOptionToggle'> Show All Tasks";
      }else{
        msg += "<input type='checkbox' onchange='taskLimitToggle();' name='ShowAllTasks' id='taskLimitOptionToggle'> Show All Tasks";      
      }
      
      msg += "  </label>";
      msg += "  <br />";
      msg += "  <div style='margin-left:20px; margin-top:5px;'>";      
      msg += "    Show up to ";
      
      if(eval(Cookies.get('ShowAllTasks'))){      
        msg += "<input type='text' name='TaskLimit' class='form-control' disabled='true' id='taskLimitOption' value='" + Cookies.get('TaskLimit') + "' style='max-width:75px; display:inline-block;'>";
      }else{
        msg += "<input type='text' name='TaskLimit' id='taskLimitOption' class='form-control' value='" + Cookies.get('TaskLimit') + "' style='max-width:75px; display:inline-block;'>";      
      }
      
      msg += "    Tasks";
      msg += "  </div>";
      msg += " </div>";
      msg += "</form>";
  
  BootstrapDialog.show({
    title: "Task Manager Options",
    message: msg,
    buttons:[{
      label: "Cancel",
      cssClass: "btn-default",
      action: function(dialogRef){
        dialogRef.close();
      }    
    },{
      label: "Save Changes",
      cssClass: "btn-primary",
      action: function(dialogRef){
        //var options = $("form[name=taskManagerOptionsForm]").serializeArray();
        
        //$.each(options, function(){
        //  if(this.name == "TaskLimit"){
        //    setCookie('ShowAllTasks', false);
        //    
        //  }
          
        //  setCookie(this.name, this.value);          
        //});
        
        setCookie('UseTaskColor', $("#taskColorOption").prop("checked"));
        
        if($("#taskLimitOptionToggle").prop("checked")){
          setCookie('ShowAllTasks', true);
        }else{
          setCookie('ShowAllTasks', false);
          setCookie('TaskLimit', $("#taskLimitOption").val());
        }
        
        setCookie('wordWrapForHeaderText', $("#wordWrapForHeaderText").prop("checked"));
        setCookie('wordWrapForColumnText', $("#wordWrapForColumnText").prop("checked"));
        
        dialogRef.close();
        createTaskList();
      }    
    }]
  });
}

function taskLimitToggle(){
  var state = $("#taskLimitOptionToggle").prop("checked");
  
  if(state){
    $("#taskLimitOption").attr("disabled", true); 
  }else{
    $("#taskLimitOption").attr("disabled", false);
  }
}

function toggleOptionBoxes(){
  var allBoxes = $("form[name=taskFieldsToShowForm] input[type=checkbox]");
  var state = $("#toggleAllBox").prop("checked");
  
  $.each(allBoxes, function(){
    if(state){
      $(this).prop("checked", true);
    }else{
      $(this).prop("checked", false);
    }
  });
}

function printSection(section){
  $("#" + section).printThis({
    importCSS: false,
    importStyle: false,
    loadCSS: ["/remotevb/css/printStyles.css"],
    pageTitle: "Remote VirtualBoss Task Manager"
  });
}










