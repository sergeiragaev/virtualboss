/***********************************************************************************************/
var defaultJobFieldsToShow = "JobNumber,JobOwnerName,JobEmail,JobNotes";
var defaultJobFilters = ""; // empty is all active jobs
/***********************************************************************************************/
// use array.join(',') to convert an array to comma separated string.
var allJobFieldCaptionNames = ["JobNumber","JobLot","JobOwnerName","JobSubdivision","JobLockBox","JobAddress1","JobAddress2","JobCity","JobState","JobPostal","JobCountry","JobHomePhone","JobWorkPhone","JobCellPhone","JobFax","JobCompany","JobEmail","JobNotes","JobDirections","JobCustomField1","JobCustomField2","JobCustomField3","JobCustomField4","JobCustomField5","JobCustomField6","JobCustomList1","JobCustomList2","JobCustomList3","JobCustomList4","JobCustomList5","JobCustomList6"];
/***********************************************************************************************/

$(document).ready(function(){
  loadJobSettings();
  loadJobFilters();
  createJobList();
});

/***********************************************************************************************/

function loadJobSettings(){
  if(!Cookies.get("JobListFieldsToShow")){
    setCookie("JobListFieldsToShow", defaultJobFieldsToShow);
  }
  
  if(!Cookies.get("JobListSort")){
    setCookie("JobListSort", "[[0,0]]");
  }

  // set Job custom field options
  
  if(!Cookies.get("ShowJobCustomField1")){
    setCookie("ShowJobCustomField1", false);
  }
  
  if(!Cookies.get("ShowJobCustomField2")){
    setCookie("ShowJobCustomField2", false);
  }
  
  if(!Cookies.get("ShowJobCustomField3")){
    setCookie("ShowJobCustomField3", false);
  }
  
  if(!Cookies.get("ShowJobCustomField4")){
    setCookie("ShowJobCustomField4", false);
  }
  
  if(!Cookies.get("ShowJobCustomField5")){
    setCookie("ShowJobCustomField5", false);
  }
  
  if(!Cookies.get("ShowJobCustomField6")){
    setCookie("ShowJobCustomField6", false);
  }
  
  // Job custom list options
  
  if(!Cookies.get("ShowJobCustomList1")){
    setCookie("ShowJobCustomList1", false);
  }
  
  if(!Cookies.get("ShowJobCustomList2")){
    setCookie("ShowJobCustomList2", false);
  }
  
  if(!Cookies.get("ShowJobCustomList3")){
    setCookie("ShowJobCustomList3", false);
  }
  
  if(!Cookies.get("ShowJobCustomList4")){
    setCookie("ShowJobCustomList4", false);
  }
  
  if(!Cookies.get("ShowJobCustomList5")){
    setCookie("ShowJobCustomList5", false);
  }
  
  if(!Cookies.get("ShowJobCustomList6")){
    setCookie("ShowJobCustomList6", false);
  }
  
  $("form[name=jobListSearchForm]").submit(function(event){   
    var phrase = $("#jobListSearchBox").val();

    createJobList("/api/v1/job?FindString=" + encodeURIComponent(phrase));

    event.preventDefault();
  });
}

function loadJobFilters(){
  if(!Cookies.get("JobFilters")){
    setCookie("JobFilters", defaultJobFilters);
  }
}

function createJobList(customUrl){
  var jobFieldsArray = getJobFieldsToShowArray();
  var jobFieldsString = Cookies.get("JobListFieldsToShow");
  var activeJobFilters = getActiveJobFilters();

  BootstrapDialog.show({
    title: "Loading Jobs, Please wait...",
    message: "<i class='fa fa-circle-o-notch fa-spin'></i> Loading Job List..."
  });

  // START JOB LIST CREATION  
  if(!customUrl){
    var dataUrl = "/api/v1/job?fields=JobId," + jobFieldsArray.join(',');
    if(activeJobFilters){
      dataUrl += "&" + activeJobFilters;
    }
  }else{
    var dataUrl = customUrl + "&fields=JobId," + jobFieldsArray.join(',');
  }
  
  var tblData = "";

  $.ajax({
    url: dataUrl,
    dataType: 'JSON',
    success: function(jobs){
      if(jobs == 'InvalidLogin'){
        logout();
      }

      if(!jobs.length){
        BootstrapDialog.closeAll();
        BootstrapDialog.show({
          title: "No Results",
          message: "Your Search Returned No Results"
        });
        
        return;
      }
      
      if(jobs.length >= 200){
        $("#jCount").html(jobs.length + " (Showing 200 out of " + jobs.length + ") You can use the Search feature to get more focused results.");
      }else{
        $("#jCount").html(jobs.length);
      }
      
      var tbl = "<div class='table-responsive'>";
          tbl += "<table class='table table-bordered table-condensed table-striped tablesorter table-hover'>";
          tbl += "  <thead>";
          tbl += "    <tr>";
    
      $.ajax({      
        url: '/api/v1/fieldcaptions?fields=JobId,' + allJobFieldCaptionNames.join(","),
        dataType:'json',
        success: function(names){
          if(names == 'InvalidLogin'){
            logout();
          }
    
          $.each(jobFieldsArray, function(i){
            tbl += "<th>" + names[this] + "</th>";
          });

          tbl += "    </tr>";
          tbl += "  </thead>";
          tbl += "  <tbody>";

          $.each(jobs, function(i){
            tbl += "<tr onclick=\"editJob('" + jobs[i]['JobId'] + "');\" style='cursor:pointer;'>";

            $.each(jobFieldsArray, function(j){
              if(this == "JobNumber"){
                tbl += "<td><a href='#' onclick=\"return false;\">" + jobs[i][this] + "</a></td>";
              }else if(this == "JobNotes"){
                tbl += "<td>" + jobs[i][this]; //.replace(/\\n/g, '<br />') + "</td>";
              // }else if(this == "JobAddress1"){
              //   tbl += "<td>" + jobs[i]['JobAddress'] + "</td>";
              }else{
                tbl += "<td>" + jobs[i][this] + "</td>";
              }
            });

            tbl += "</tr>";
          });

          tbl += "  </tbody>";
          tbl += "</table>";
          tbl += "</div>";

          $("#jJobList").html(tbl);

          $("#jJobList table").tablesorter({
            sortList: eval(Cookies.get("JobListSort"))
          }).bind("sortStart",function(){
          }).bind("sortEnd", function(data){
            setCookie("JobListSort", data.delegateTarget.config.sortList);
          });

          BootstrapDialog.closeAll();
          postEffects();
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

      //alert(errorThrown);
      BootstrapDialog.closeAll();
      $("#jMain").append("<h4><small style='color:red;'><i class='fa fa-exclamation-circle'></i> " + errorThrown + "</small></h4>");
    }
  });
  // END JOB LIST CREATION
}
/**********************************************************************************************/

function postEffects(){
  // place for additional effects (if any)
}

function getActiveJobFilters(){
  return Cookies.get("JobFilters");
}

function setCookie(name, value){
  Cookies.set(name, value, {
    expires: 365
  });
}

function removeCookie(name){
  Cookies.remove(name);
}

function getJobFieldsToShowArray(){
  return Cookies.get("JobListFieldsToShow").split(",");
}

function formatDate(d){
  var t = d.split("-");

  return t[1] + "/" + t[2] + "/" + t[0];
}

function getJobListFieldsToShowArray(){
  return Cookies.get("JobListFieldsToShow").split(",");
}

function editJobListColumns(){
  var jobFieldNames;

  $.ajax({
    url: "/api/v1/fieldcaptions?fields=" + allJobFieldCaptionNames.join(','),
    dataType:'json',
    success: function(names){
      if(names == 'InvalidLogin'){
        logout();
      }

      jobFieldNames = names;

      var msg = "<div id='jobListFieldSettingsToggle' class='row'>";
          msg += "<form role='form' name='jobFieldsToShowForm'>";

      var fieldValues = getJobListFieldsToShowArray();

      msg += "<div class='col-xs-12'>";
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
      msg += "        <input type='checkbox' id='checkAllJobFieldBoxes' onchange='toggleAllJobFields();'> Check / Uncheck All";
      msg += "      </label>";
      msg += "    </div>";
      msg += "  </div>";
      msg += "</div>";

      BootstrapDialog.show({
        title: "Choose Fields To Show On Job List",
        message: msg,
        buttons:[{
          label: "Defaults",
          cssClass: "btn-warning",
          action: function(){
            $.each($("form[name=jobFieldsToShowForm] input[type=checkbox]"), function(){
              $(this).prop("checked", false);
            });

            $.each(defaultJobFieldsToShow.split(','), function(){
              $("form[name=jobFieldsToShowForm] input[name=" + this + "]").prop("checked", true);
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
            var inputList = $("form[name=jobFieldsToShowForm]").serializeArray();

            if(inputList.length <= 0){
              alert("You need to have at least 1 field active");
              return false;
            }

            $.each(inputList, function(){
              activeTaskFields.push($(this).attr("name"));
            });

            setCookie("JobListFieldsToShow", activeTaskFields.join(','));
            setCookie("JobListSort", "[[0,0]]"); // resets table sort (solves issues when someone removes the sorted list)
            createJobList();

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

function toggleAllJobFields(){
  var state = $("#checkAllJobFieldBoxes").prop("checked");

  $.each($("form[name=jobFieldsToShowForm] input[type=checkbox]"), function(){
    if(state){
      $(this).prop("checked", true);
    }else{
      $(this).prop("checked", false);
    }
  });
}

function editJobFieldOrder(){
  var fields = getJobListFieldsToShowArray();
  var msg = "";

  msg += "The top field shows up 1st on the Job List. The bottom field is last.  Drag and Drop the fields to change.";
  msg += "<hr />";
  msg += "<div>";
  msg += "<ul id='sortableFields'>";

  $.ajax({
    url: "/api/v1/fieldcaptions?fields=" + allJobFieldCaptionNames.join(','),
    dataType:'json',
    success: function(names){
      if(names == 'InvalidLogin'){
        logout();
      }

      var currentSort = eval(Cookies.get("JobListSort"));
      var originalSortArray = [];
      var originalSortArrayFieldNames = [];
      var originalSortArrayFieldDirection = [];

      $.each(currentSort, function(){
        originalSortArray.push(this);
        originalSortArrayFieldNames.push(fields[this[0]]);
        originalSortArrayFieldDirection.push(this[1]);
      });

      $.each(fields, function(i){
        msg += "<li data-field-name='" + this + "'>" + names[0][this] + "</li>";
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
            
            setCookie("JobListFieldsToShow", newFieldOrderArray.join(','));
            setCookie("JobListSort", sortString);
            
            createJobList();
            
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





