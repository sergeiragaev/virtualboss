/***********************************************************************************************/
const defaultJobFieldsToShow = "JobNumber,ContactPerson,JobEmail,JobNotes";
const defaultJobFilters = ""; // empty is all active jobs
/***********************************************************************************************/
// use array.join(',') to convert an array to comma separated string.
const allJobFieldCaptionNames = ["JobNumber", "JobLot", "ContactPerson", "JobSubdivision", "JobLockBox", "ContactAddresses", "ContactPhones", "ContactCompany", "JobEmail", "JobNotes", "JobDirections", "JobCustomField1", "JobCustomField2", "JobCustomField3", "JobCustomField4", "JobCustomField5", "JobCustomField6", "JobCustomList1", "JobCustomList2", "JobCustomList3", "JobCustomList4", "JobCustomList5", "JobCustomList6"];
/***********************************************************************************************/
let jobsPerPage = 20;
let jCurrentPage = 1;
let jTotalPages = 1;
/***********************************************************************************************/

$(document).ready(function(){
  loadJobSettings();
  loadJobFilters();
  setupEventHandlers();
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

  if (!Cookies.get("ShowAllJobs")) {
    setCookie("ShowAllJobs", false);
  }

  if (!Cookies.get("JobLimit")) {
    setCookie("JobLimit", jobsPerPage);
  } else {
    jobsPerPage = parseInt(Cookies.get("JobLimit"), 10);
  }

  jCurrentPage = parseInt(Cookies.get('jCurrentPage')) || 1;

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
  
  // $("form[name=jobListSearchForm]").submit(function(event){
  //   var phrase = $("#jobListSearchBox").val();
  //
  //   createJobList("/api/v1/job?FindString=" + encodeURIComponent(phrase));
  //
  //   event.preventDefault();
  // });
  if (!Cookies.get("UseJobColor")) {
    setCookie("UseJobColor", false);
  }
}

function loadJobFilters(){
  if(!Cookies.get("JobFilters")){
    setCookie("JobFilters", defaultJobFilters);
  }
  if (!Cookies.get("jobListFindString")) {
    setCookie("jobListFindString", "");
  }

  $("#jobListSearchBox").val(Cookies.get('jobListFindString'));
}

function createJobList(customUrl){
  let dataUrl;
  const jobFieldsArray = getJobFieldsToShowArray();
  const activeJobFilters = getActiveJobFilters();
  const sortParams = getSortParams(jobFieldsArray);

  BootstrapDialog.show({
    title: "Loading Jobs, Please wait...",
    message: "<i class='fa fa-circle-o-notch fa-spin'></i> Loading Job List..."
  });

  if(eval(Cookies.get('UseJobColor'))) {
    jobFieldsArray.push("Color");
  }
  
  // START JOB LIST CREATION  
  if(!customUrl){
    dataUrl = "/api/v1/job?fields=JobId," + jobFieldsArray.join(',');
    if(activeJobFilters){
      dataUrl += "&" + activeJobFilters;
    }
  }else{
    dataUrl = customUrl + "&fields=JobId," + jobFieldsArray.join(',');
  }

  if (eval(Cookies.get('ShowAllJobs'))) jobsPerPage = 10000;

  let findString = Cookies.get('jobListFindString') || '';
  dataUrl += '&page=' + jCurrentPage + '&limit=' + jobsPerPage + '&sort=' + sortParams + '&findString=' + findString;

  $.ajax({
    url: dataUrl,
    dataType: 'JSON',
    success: function(jobs){
      if(jobs == 'InvalidLogin'){
        logout();
      }

      jTotalPages = jobs.page.totalPages;

      updatePagination();

      if(!jobs.page.totalElements){
        BootstrapDialog.closeAll();
        BootstrapDialog.show({
          title: "No Results",
          message: "Your Search Returned No Results"
        });
        
        return;
      }

      updateJobCount(jobs.page.totalElements);

      // if(jobs.content.length >= 200){
      //   $("#jCount").html(jobs.content.length + " (Showing 200 out of " + jobs.content.length + ") You can use the Search feature to get more focused results.");
      // }else{
      //   $("#jCount").html(jobs.content.length);
      // }
      
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

          if(eval(Cookies.get('UseJobColor'))) {
            jobFieldsArray.pop();
          }
          
          
          $.each(jobFieldsArray, function(i){
            tbl += "<th>" + names[this] + "</th>";
          });

          tbl += "    </tr>";
          tbl += "  </thead>";
          tbl += "  <tbody>";

          $.each(jobs.content, function(i){
            if(eval(Cookies.get('UseJobColor'))) {
              tbl += "<tr onclick=\"editJob('" + jobs.content[i]['JobId'] + "');\" style='cursor:pointer; color:" + jobs.content[i]['Color'] + ";'>";
            } else {
              tbl += "<tr onclick=\"editJob('" + jobs.content[i]['JobId'] + "');\" style='cursor:pointer;'>";
            }

            $.each(jobFieldsArray, function(j){
              if(this == "JobNumber"){
                if(eval(Cookies.get('UseJobColor'))) {
                  tbl += "<td>" + jobs.content[i][this] + "</td>";
                } else {
                  tbl += "<td><a href='#' onclick=\"return false;\">" + jobs.content[i][this] + "</a></td>";
                }
              }else if(this == "JobNotes"){
                tbl += "<td>" + jobs.content[i][this]; //.replace(/\\n/g, '<br />') + "</td>";
              // }else if(this == "JobAddress1"){
              //   tbl += "<td>" + jobs[i]['JobAddress'] + "</td>";
              }else{
                tbl += "<td>" + jobs.content[i][this] + "</td>";
              }
            });

            tbl += "</tr>";
          });

          tbl += "  </tbody>";
          tbl += "</table>";
          tbl += "</div>";

          $("#jJobList").html(tbl);
          initTableSorting();

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

function updatePagination() {
  $('#jPageInfo').text(`Page ${jCurrentPage} of ${jTotalPages}`);
  $('#jPrevPage').prop('disabled', jCurrentPage === 1);
  $('#jNextPage').prop('disabled', jCurrentPage >= jTotalPages);
  Cookies.set('jCurrentPage', jCurrentPage);
}

function updateJobCount(total) {
  if (!total) {
    $("#jCount").html("0, <span style='font-style:italic; color:#ababab;'>nothing matched your search or current filters</span>");
  } else if (eval(Cookies.get('ShowAllJobs'))) {
    $("#jCount").html("<a href='#' onclick=\"editOptions(); return false;\">" + total + "</a>");
  } else {
    $("#jCount").html(total + " (<a href='#' title='Change number of jobs to show' onclick=\"editOptions(); return false;\">Showing</a> up to " + Cookies.get('JobLimit') + ")");
  }
}

function editOptions() {
  let msg = "";
  msg += "<form role='form' name='jobManagerOptionsForm'>";

  msg += " <div class='checkbox'>";
  msg += "  <label>";

  if (eval(Cookies.get('UseJobColor'))) {
    msg += "<input id='jobColorOption' name='UseJobColor' type='checkbox' checked='checked'> Use Active Color Profile For Jobs";
  } else {
    msg += "<input id='jobColorOption' name='UseJobColor' type='checkbox'> Use Active Color Profile For Jobs";
  }

  msg += "  </label>";
  msg += "</div>";


  msg += "<div class='checkbox'>";
  msg += "  <label>";

  if (eval(Cookies.get('ShowAllJobs'))) {
    msg += "<input type='checkbox' onchange='jobLimitToggle();' name='ShowAllJobs' checked='checked' id='jobLimitOptionToggle'> Show All Jobs";
  } else {
    msg += "<input type='checkbox' onchange='jobLimitToggle();' name='ShowAllJobs' id='jobLimitOptionToggle'> Show All Jobs";
  }

  msg += "  </label>";
  msg += "  <br />";
  msg += "  <div style='margin-left:20px; margin-top:5px;'>";
  msg += "    Show up to ";

  if (eval(Cookies.get('ShowAllJobs'))) {
    msg += "<input type='text' name='JobLimit' class='form-control' disabled='true' id='jobLimitOption' value='" + Cookies.get('JobLimit') + "' style='max-width:75px; display:inline-block;'>";
  } else {
    msg += "<input type='text' name='JobLimit' id='jobLimitOption' class='form-control' value='" + Cookies.get('JobLimit') + "' style='max-width:75px; display:inline-block;'>";
  }

  msg += "    Jobs";
  msg += "  </div>";
  msg += " </div>";
  msg += "</form>";

  BootstrapDialog.show({
    title: "Job Manager Options",
    message: msg,
    buttons: [{
      label: "Cancel",
      cssClass: "btn-default",
      action: function (dialogRef) {
        dialogRef.close();
      }
    }, {
      label: "Save Changes",
      cssClass: "btn-primary",
      action: function (dialogRef) {

        setCookie('UseJobColor', $("#jobColorOption").prop("checked"));
        
        if ($("#jobLimitOptionToggle").prop("checked")) {
          setCookie('ShowAllJobs', true);
          jCurrentPage = 1;
        } else {
          setCookie('ShowAllJobs', false);
          setCookie('JobLimit', $("#jobLimitOption").val());
          jobsPerPage = $("#jobLimitOption").val();
          jCurrentPage = 1;
        }

        dialogRef.close();
        createJobList();
      }
    }]
  });
}

function jobLimitToggle() {
  var state = $("#jobLimitOptionToggle").prop("checked");

  if (state) {
    $("#jobLimitOption").attr("disabled", true);
  } else {
    $("#jobLimitOption").attr("disabled", false);
  }
}

function setupEventHandlers() {
  $('#jPrevPage').click(() => changePage(-1));
  $('#jNextPage').click(() => changePage(1));

  $("form[name='jobListSearchForm']").submit(function (e) {
    e.preventDefault();
    jCurrentPage = 1;
    const phrase = $("#jobListSearchBox").val().replace(/["'&?,;]/g, "");
    Cookies.set('jobListFindString', phrase);
    createJobList();
  });
}

function changePage(delta) {
  jCurrentPage = Math.max(1, jCurrentPage + delta);
  createJobList();
}

function getSortParams(jobFieldsArray) {
  const savedSort = JSON.parse(Cookies.get("JobListSort") || '[]');
  return savedSort.map(([fieldIndex, direction]) => {
    const field = jobFieldsArray[fieldIndex];
    return `${field}:${direction === 0 ? 'asc' : 'desc'}`;
  }).join(',');
}

function initTableSorting() {
  $("#jJobList table").tablesorter({
    sortList: eval(Cookies.get("JobListSort"))
  }).bind("sortStart", function () {

  }).bind("sortEnd", function (data) {
    setCookie("JobListSort", data.delegateTarget.config.sortList);
    jCurrentPage = 1;
    createJobList();
  });
}


