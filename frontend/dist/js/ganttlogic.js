Date.prototype.getJulian = function() {
  return Math.floor((this / 86400000) - (this.getTimezoneOffset()/1440) + 2440587.5);
}

/***********************************************************************************************/
// use array.join(',') to convert an array to comma separated string.
var defaultTaskListFields        = ["TaskDescription,TaskTargetStart,TaskDuration,TaskTargetFinish,TaskActualFinish,TaskOrder,TaskStatus,JobNumber"];
var defaultGanttChartFields      = "TaskDescription,JobNumber,TaskTargetStart";
var defaultBarTextFields         = ["TaskDescription", "TaskTargetStart"];
var defaultBarTextFieldOrder     = ["TaskDescription", "TaskTargetStart"];
var allTaskFieldCaptionNames     = ["TaskDescription","TaskTargetStart","TaskDuration","TaskTargetFinish","TaskActualFinish","TaskOrder","TaskStatus","TaskNumber","TaskFollows","FinishPlus","TaskRequested","TaskMarked","TaskNotes","TaskCustomField1","TaskCustomField2","TaskCustomField3","TaskCustomField4","TaskCustomField5","TaskCustomField6","TaskCustomList1","TaskCustomList2","TaskCustomList3","TaskCustomList4","TaskCustomList5","TaskCustomList6"];
var allJobFieldCaptionNames      = ["JobNumber","JobLot","JobOwnerName","JobSubdivision","JobLockBox","JobAddress","JobAddress2","JobCity","JobState","JobPostal","JobCountry","JobHomePhone","JobWorkPhone","JobCellPhone","JobFax","JobCompany","JobEmail","JobNotes","JobDirections","JobCustomField1","JobCustomField2","JobCustomField3","JobCustomField4","JobCustomField5","JobCustomField6","JobCustomList1","JobCustomList2","JobCustomList3","JobCustomList4","JobCustomList5","JobCustomList6"];
var allContactFieldCaptionNames  = ["ContactCompany","ContactPerson","ContactProfession","ContactFirstName","ContactLastName","ContactSupervisor","ContactSpouse","ContactTaxID","ContactWebSite","ContactEmail","ContactFax","ContactWorkersCompDate","ContactInsuranceDate","ContactComments","ContactNotes","ContactPhones","ContactCustomField1","ContactCustomField2","ContactCustomField3","ContactCustomField4","ContactCustomField5","ContactCustomField6","ContactCustomList1","ContactCustomList2","ContactCustomList3","ContactCustomList4","ContactCustomList5","ContactCustomList6"];
var defaultTaskFilters = "IsActive=on"; 
var globalNames = [];
/***********************************************************************************************/

$(document).ready(function(){
  loadTaskSettings();  
  loadGanttChart();  
});

function loadGanttChart(customUrl){
  var ganttFieldsArray = getGanttChartFieldsToShowArray();
  var activeTaskFilters = getActiveTaskFilters();

  if(ganttFieldsArray[0].length <= 0){
    ganttFieldsArray = ["TaskDescription"]; // There should always be at least one field.
    setCookie("GanttChartFieldsToShow", "TaskDescription");
  }
  
  setFiltersMessage(activeTaskFilters, 'GanttChart');
  
  BootstrapDialog.show({
    title: "Loading Tasks, Please wait...",
    message: "<i class='fa fa-circle-o-notch fa-spin' style='margin-right:5px;'></i> Loading Task List...<p style='font-size:12px; margin-top:15px; margin-bottom:0px;'>" + $('#activeFiltersMessage').html() + "</p><hr style='margin-top:7px; margin-bottom:7px;' /><div style='font-size:12px; color:#999;'>Closing this window is ok. Tasks will continue to load and display when ready.</div>"
  });
  
  if($.inArray("TaskTargetStart", ganttFieldsArray) == -1){
    ganttFieldsArray.push("TaskTargetStart");
  }
  
  if($.inArray("TaskTargetFinish", ganttFieldsArray) == -1){
    ganttFieldsArray.push("TaskTargetFinish");
  }
  
  // START TASK LIST CREATION  
  if(!customUrl){
    var dataUrl = "";
    
    if(eval(Cookies.get('UseTaskColor'))){
      if(eval(Cookies.get('showBarText'))){
        dataUrl += "/api/v1/task?fields=TaskId," + ganttFieldsArray.join(',') + ",TaskColor," + Cookies.get('BarTextFields') + "&" + activeTaskFilters + "&FindString=" + Cookies.get('filterFindString');
      }else{
        dataUrl += "/api/v1/task?fields=TaskId," + ganttFieldsArray.join(',') + ",TaskColor" + "&" + activeTaskFilters + "&FindString=" + Cookies.get('filterFindString');
      }
    }else{
      if(eval(Cookies.get('showBarText'))){
        dataUrl += "/api/v1/task?fields=TaskId," + ganttFieldsArray.join(',') + "," + Cookies.get('BarTextFields') + "&" + activeTaskFilters;
      }else{
        dataUrl += "/api/v1/task?fields=TaskId," + ganttFieldsArray.join(',') + "&" + activeTaskFilters + "&FindString=" + Cookies.get('filterFindString');
      }
    }
  }else{
    // for /api/v1/task, need to use TaskId but for /api/v1/fieldcaptions, use TaskNumber
    var dataUrl = customUrl + "&fields=TaskId," + ganttFieldsArray.join(',');
  }

  $.ajax({
    url: '/api/v1/fieldcaptions?fields=' + allTaskFieldCaptionNames.join(',') + ',' + allJobFieldCaptionNames.join(',') + ',' + allContactFieldCaptionNames.join(','),
    dataType:'json',
    success: function(names){
      if(names == 'InvalidLogin'){
        logout();
      }

      globalNames = names;

      $.ajax({
        url: '/api/v1/task?fields=TaskTargetStart,TaskTargetFinish&' + activeTaskFilters + '&FindString=' + Cookies.get('filterFindString'),
        dataType:'json',
        success: function(tasks){
          if(tasks == 'InvalidLogin'){
            logout();
          }

          var startDatesArray = [];
          var endDatesArray = [];
          
          $.each(tasks, function(){
            startDatesArray.push(this.TaskTargetStart);
            endDatesArray.push(this.TaskTargetFinish);
          });
          
          startDatesArray.sort();
          endDatesArray.sort();
          
          if(tasks.length == 0){
            var trueStart = moment().format('YYYY-MM-DD');
            var trueEnd = moment().add(4, 'months').format('YYYY-MM-DD');
          }else{
            var trueStart = startDatesArray[0];
            var trueEnd = moment(endDatesArray.pop(), 'YYYY-MM-DD').add(1, 'w').format('YYYY-MM-DD');
          }

          $("#Gantt").sGantt({
          	eventSource: dataUrl,
          	dateRange: {
          		start: trueStart,
          		end  : trueEnd
          	},
          	dayGrid     : true,
          	showPerson  : true,
          	showJob     : true,
          	showBarText : true,
          	title       : names.TaskDescription,
            jobTitle    : names.JobNumber,
            taskNumTitle: names.TaskOrder,
            personTitle : names.ContactPerson,
            tStartTitle : names.TaskTargetStart,
            fieldNames  : globalNames 
          });
    
          BootstrapDialog.closeAll();
        },
        error: function(jqXhr, status, error){
          if(jqXhr.responseText == 'InvalidLogin'){
            logout();
          }
    
          BootstrapDialog.closeAll();
          BootstrapDialog.alert(error);
        }
      });
      
    },
    error: function(jqXhr, status, error){
      if(jqXhr.responseText == 'InvalidLogin'){
        logout();
      }

      BootstrapDialog.closeAll();
      alert(error);
    }
  });    
}

function loadTaskSettings(){
  if(!Cookies.get("GanttChartFieldsToShow")){
    setCookie("GanttChartFieldsToShow", defaultGanttChartFields);
    setCookie("GanttChartSort", "[[1,0]]");
  }

  if(!Cookies.get("GanttChartSort")){
    setCookie("GanttChartSort", "[[1,0]]");
  }

  if(!Cookies.get("mobileOptimized")){
    setCookie("mobileOptimized", true);
  }

  if(!Cookies.get("TaskFiltersJobAutoLoad")){
    setCookie("TaskFiltersJobAutoLoad", true);
  }
  
  if(!Cookies.get("TaskFiltersContactAutoLoad")){
    setCookie("TaskFiltersContactAutoLoad", true);
  }
  
  if(!Cookies.get('UseTaskColor')){
    setCookie('UseTaskColor', true);
  }

  if(!Cookies.get("BarTextFields")){
    setCookie("BarTextFields", defaultBarTextFields.join(','));
  }
    
  $("form[name=ganttChartSearchForm]").submit(function(event){   
    event.preventDefault();
    var phrase = $("#ganttChartSearchBox").val().replace(/["'&?,;]/g, "");

    setCookie("filterFindString", phrase);
    
    loadGanttChart("/api/v1/task?FindString=" + encodeURIComponent(phrase));
  });

  if(!Cookies.get("filterFindString")){
    setCookie("filterFindString", "");
  }
  
  $("#ganttChartSearchBox").val(Cookies.get('filterFindString'));
}

function loadTaskFilters(){
  if(!Cookies.get("TaskFilters")){
    setCookie("TaskFilters", defaultTaskFilters);
  }

  if(!Cookies.get("filterFindString")){
    setCookie("filterFindString", "");
  }  
}

function getActiveTaskFilters(){
  return Cookies.get("TaskFilters");
}

function getGanttChartFieldsToShowArray(){
  return Cookies.get("GanttChartFieldsToShow").split(",");
}

function setCookie(name, value){
  Cookies.set(name, value, {
    expires: 365
  });
}

function getGanttChartFieldsToShowArray(){
  return Cookies.get("GanttChartFieldsToShow").split(",");
}

function editGanttChartColumns(){
  var taskFieldNames, contactFieldNames, jobFieldNames;
  
  $.ajax({
    url: "/api/v1/fieldcaptions?fields=" + allTaskFieldCaptionNames.join(','),
    dataType:'json',
    success: function(names){
      if(names == 'InvalidLogin'){
        logout();
      }

      taskFieldNames = names;
      
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
            
              var msg = "<div id='taskListFieldSettingsToggle' class='row'>";
                  msg += "<form role='form' name='ganttFieldsToShowForm'>";
        
              var fieldValues = getGanttChartFieldsToShowArray(); // returns an array of all current active task list fields to appear on the task list. needed to tell which fields are currently 'active'
              
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
              msg += "<div class='row'>";
              msg += "  <div class='col-xs-12'>";
              msg += "    <div class='checkbox'>";
              msg += "      <label>";
              msg += "        <input type='checkbox' id='checkAllGanttFieldBoxes' onchange='toggleAllGanttFields();'> Check / Uncheck All";
              msg += "      </label>";
              msg += "    </div>";
              msg += "  </div>";
              msg += "</div>";  
                    
              BootstrapDialog.show({
                title: "Choose Fields To Show On Gantt Chart",
                message: msg,
                buttons:[{
                  label: "Defaults",
                  cssClass: "btn-warning",
                  action: function(){
                    $.each($("form[name=ganttFieldsToShowForm] input[type=checkbox]"), function(){
                      $(this).prop("checked", false);
                    });
                    
                    $.each(defaultGanttChartFields.split(','), function(){
                      $("form[name=ganttFieldsToShowForm] input[name=" + this + "]").prop("checked", true);
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
                    var activeGanttChartFields = [];
                    var inputList = $("form[name=ganttFieldsToShowForm]").serializeArray();
                    
                    if(inputList.length <= 0){
                      alert("You need to have at least 1 field active");
                      return false;
                    }
                    
                    $.each(inputList, function(){
                      activeGanttChartFields.push($(this).attr("name"));  
                    });

                    setCookie("GanttChartFieldsToShow", activeGanttChartFields.join(','));
                    
                    if(inputList.Length == 1){
                      setCookie("GanttChartSort", "[[0,0]]"); // resets table sort (solves issues when someone removes the sorted list)
                    }else{
                      setCookie("GanttChartSort", "[[1,0]]");                    
                    }
                    
                    loadGanttChart();
                    
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
 
function toggleAllGanttFields(){
  var state = $("#checkAllGanttFieldBoxes").prop("checked");
  
  $.each($("form[name=ganttFieldsToShowForm] input[type=checkbox]"), function(){
    if(state){
      $(this).prop("checked", true);      
    }else{
      $(this).prop("checked", false);
    }
  });
}

function editGanttFieldOrder(){
  var fields = getGanttChartFieldsToShowArray();
  var msg = "";
  
  msg += "The top field shows up 1st on the Gantt Chart. The bottom field is last.  Drag and Drop the fields to change.";
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

      var currentSort = eval(Cookies.get("GanttChartSort"));
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
        title: "Change Column Order",
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
          label: "Save",
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
            
            setCookie("GanttChartFieldsToShow", newFieldOrderArray.join(','));
            setCookie("GanttChartSort", sortString);
            
            loadGanttChart();
            
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
        //createTaskList();
        loadGanttChart();
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

function printSection(section){
  $("#" + section).printThis({
    importCSS: false,
    importStyle: false,
    //printDelay: 5000,
    loadCSS: ["/remotevb/css/mainstyles.css","/remotevb/css/taskstyles.css"],
    pageTitle: "Remote VirtualBoss Gantt Chart",
    removeScripts: true
  });
}
