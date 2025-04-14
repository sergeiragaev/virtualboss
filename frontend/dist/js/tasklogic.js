/***********************************************************************************************/
const defaultTaskListFields = ["TaskOrder,TaskDescription,JobNumber,TaskTargetStart,TaskDuration,TaskTargetFinish,TaskStatus"];
let tasksPerPage = 20;
let tCurrentPage = 1;
let tTotalPages = 1;

const defaultTaskFilters = "IsActive=on";

const allTaskFieldCaptionNames = ["TaskDescription", "TaskTargetStart", "TaskDuration", "TaskTargetFinish", "TaskActualFinish", "TaskOrder", "TaskStatus", "TaskNumber", "TaskFollows", "FinishPlus", "TaskRequested", "TaskMarked", "TaskNotes", "TaskCustomField1", "TaskCustomField2", "TaskCustomField3", "TaskCustomField4", "TaskCustomField5", "TaskCustomField6", "TaskCustomList1", "TaskCustomList2", "TaskCustomList3", "TaskCustomList4", "TaskCustomList5", "TaskCustomList6"];
const allJobFieldCaptionNames = ["JobNumber", "JobLot", "JobOwnerName", "JobSubdivision", "JobLockBox", "JobAddress1", "JobAddress2", "JobCity", "JobState", "JobPostal", "JobCountry", "JobHomePhone", "JobWorkPhone", "JobCellPhone", "JobFax", "JobCompany", "JobEmail", "JobNotes", "JobDirections", "JobCustomField1", "JobCustomField2", "JobCustomField3", "JobCustomField4", "JobCustomField5", "JobCustomField6", "JobCustomList1", "JobCustomList2", "JobCustomList3", "JobCustomList4", "JobCustomList5", "JobCustomList6"];
const allContactFieldCaptionNames = ["ContactCompany", "ContactPerson", "ContactProfession", "ContactFirstName", "ContactLastName", "ContactSupervisor", "ContactSpouse", "ContactTaxID", "ContactWebSite", "ContactEmail", "ContactFax", "ContactWorkersCompDate", "ContactInsuranceDate", "ContactComments", "ContactNotes", "ContactPhones", "ContactCustomField1", "ContactCustomField2", "ContactCustomField3", "ContactCustomField4", "ContactCustomField5", "ContactCustomField6", "ContactCustomList1", "ContactCustomList2", "ContactCustomList3", "ContactCustomList4", "ContactCustomList5", "ContactCustomList6"];

/***********************************************************************************************/
$(document).ready(function () {
  initTaskManager();
});

function initTaskManager() {
  loadCookies();
  loadTaskSettings();
  loadTaskFilters();
  setupEventHandlers();
  createTaskList();
}

/***********************************************************************************************/
function createTaskList(customUrl) {
  const taskFieldsArray = getTaskListFieldsToShowArray();
  const activeTaskFilters = getActiveTaskFilters();
  const sortParams = getSortParams(taskFieldsArray);

  showLoadingModal();

  if (eval(Cookies.get('ShowAllTasks'))) tasksPerPage = 10000;

  const requestParams = {
    page: tCurrentPage,
    limit: tasksPerPage,
    fields: taskFieldsArray.join(','),
    findString: Cookies.get('filterFindString') || '',
    sort: sortParams
  };

  const dataUrl = buildDataUrl(customUrl, requestParams, activeTaskFilters);

  $.ajax({
    url: dataUrl,
    dataType: 'json',
    success: (response) => handleSuccess(response, taskFieldsArray),
    error: handleRequestError
  });

  setFiltersMessage(activeTaskFilters, 'TaskManager');

  BootstrapDialog.closeAll();
  postEffects();
}

function buildDataUrl(customUrl, params, activeTaskFilters) {
  if (customUrl) {
    const urlParts = customUrl.split('?');
    const existingParams = new URLSearchParams(urlParts[1] || '');

    Object.entries(params).forEach(([key, value]) => {
      if (value) existingParams.set(key, value);
    });

    return `${urlParts[0]}?${existingParams.toString()}&${activeTaskFilters}`;
  }

  const baseUrl = '/api/v1/task';
  const queryParams = new URLSearchParams(params);
  return `${baseUrl}?${queryParams.toString()}&${activeTaskFilters}`;
}

function handleSuccess(response, taskFieldsArray) {
  if (response === 'InvalidLogin') return logout();

  const tasks = response.content || [];
  tTotalPages = response.page.totalPages;

  updatePagination();

  $.ajax({
    url: '/api/v1/fieldcaptions?fields=' + taskFieldsArray.join(',').replace('TaskId', 'TaskNumber'),
    dataType: 'json',
    success: function (names) {
      if (names == 'InvalidLogin') {
        logout();
      }
      renderTaskTable(tasks, taskFieldsArray, names);
    }
  });

  updateTaskCount(response.page.totalElements);
  BootstrapDialog.closeAll();
}

/***********************************************************************************************/
function loadCookies() {
  tCurrentPage = parseInt(Cookies.get('tCurrentPage')) || 1;
}

function getSortParams(taskFieldsArray) {
  const savedSort = JSON.parse(Cookies.get("TaskListSort") || '[]');
  return savedSort.map(([fieldIndex, direction]) => {
    const field = taskFieldsArray[fieldIndex];
    return `${field}:${direction === 0 ? 'asc' : 'desc'}`;
  }).join(',');
}

function updatePagination() {
  $('#tPageInfo').text(`Page ${tCurrentPage} of ${tTotalPages}`);
  $('#tPrevPage').prop('disabled', tCurrentPage === 1);
  $('#tNextPage').prop('disabled', tCurrentPage >= tTotalPages);
  Cookies.set('tCurrentPage', tCurrentPage);
}

function setupEventHandlers() {
  $('#tPrevPage').click(() => changePage(-1));
  $('#tNextPage').click(() => changePage(1));

  $("form[name='taskListSearchForm']").submit(function (e) {
    e.preventDefault();
    tCurrentPage = 1;
    const phrase = $("#taskListSearchBox").val().replace(/["'&?,;]/g, "");
    Cookies.set('filterFindString', phrase);
    createTaskList();
  });
}

function changePage(delta) {
  tCurrentPage = Math.max(1, tCurrentPage + delta);
  createTaskList();
}


/***********************************************************************************************/
function renderTaskTable(tasks, taskFieldsArray, names) {

  let html = '<div class="table-responsive">';
  html += '<table class="table table-bordered table-condensed table-striped tablesorter table-hover">';

  html += '<thead><tr>';
  taskFieldsArray.forEach(field => {
    html += `<th>${getFieldCaption(names[field])}</th>`;
  });
  html += '</tr></thead>';

  html += '<tbody>';
  tasks.forEach(task => {
    html += '<tr>';
    taskFieldsArray.forEach(field => {
      html += `${formatTaskCell(task, field)}`;
    });
    html += '</tr>';
  });
  html += '</tbody></table></div>';

  $('#tTaskList').html(html);
  initTableSorting();
}

function initTableSorting() {
  $("#tTaskList table").tablesorter({
    sortList: eval(Cookies.get("TaskListSort"))
  }).bind("sortStart", function () {

  }).bind("sortEnd", function (data) {
    setCookie("TaskListSort", data.delegateTarget.config.sortList);
    tCurrentPage = 1;
    createTaskList();
  });
}

/***********************************************************************************************/
function showLoadingModal() {
  BootstrapDialog.show({
    title: "Loading Tasks...",
    message: '<i class="fa fa-circle-o-notch fa-spin"></i> Loading...'
  });
}

function updateTaskCount(total) {
  if (!total) {
    $("#tCount").html("0, <span style='font-style:italic; color:#ababab;'>nothing matched your search or current filters</span>");
  } else if (eval(Cookies.get('ShowAllTasks'))) {
    $("#tCount").html("<a href='#' onclick=\"editOptions(); return false;\">" + total + "</a>");
  } else {
    $("#tCount").html(total + " (<a href='#' title='Change number of tasks to show' onclick=\"editOptions(); return false;\">Showing</a> up to " + Cookies.get('TaskLimit') + ")");
  }
}

/***********************************************************************************************/
function getTaskListFieldsToShowArray() {
  return Cookies.get("TaskListFieldsToShow")
    ? Cookies.get("TaskListFieldsToShow").split(',')
    : [...defaultTaskListFields];
}

function getActiveTaskFilters() {
  return Cookies.get("TaskFilters") || '';
}

function getFieldCaption(field) {
  if (field === "TaskId") {
    field = "TaskNumber";
  }
  return field.replace(/([A-Z])/g, ' $1').trim();
}

function formatTaskCell(task, field) {
  switch (field) {
    case 'TaskDescription':
      let caption = task[field] || '<i>(No Description)</i>';
      return "<td><a href='#' onclick=\"editTask('" + task['TaskId'] + "','TaskManager');return false;\">" + caption.replace(/&/g, '&#38').replace(/</g, '&lt').replace(/>/g, '&gt') + "</a></td>";
    case 'TaskTargetStart':
    case 'TaskTargetFinish':
      return "<td>" + formatDate(task[field]) + "</td>";
    case 'TaskMarked':
      return `<td style="text-align: center"><input type="checkbox" ${task[field] ? 'checked' : ''} name='tmkd' data-t-marked=${task['TaskId']}></td>`;
    default:
      return "<td>" + task[field] || '' + "</td>";
  }
}

function formatDate(dateString) {
  if (!dateString) return '';
  const [year, month, day] = dateString.split('-');
  return `${month}/${day}/${year}`;
}

function getActiveFiltersMessage() {
  const activeFilters = Cookies.get("TaskFilters");
  return activeFilters ? `Active filters: ${activeFilters}` : '';
}

function handleRequestError(jqXhr) {
  BootstrapDialog.closeAll();
  console.error('Request failed:', jqXhr.responseText);
  BootstrapDialog.alert({
    title: 'Error',
    message: 'Failed to load tasks. Please try again later.'
  });
}

function loadTaskSettings() {

  if (!Cookies.get("TaskLimit")) {
    setCookie("TaskLimit", tasksPerPage);
  } else {
    tasksPerPage = parseInt(Cookies.get("TaskLimit"), 10);
  }

  if (!Cookies.get("TaskListFieldsToShow")) {
    setCookie("TaskListFieldsToShow", defaultTaskListFields.join(','));
  }

  if (!Cookies.get("TaskListSort")) {
    setCookie("TaskListSort", "[[2,0],[0,0]]");
  }

  if (!Cookies.get("TaskFiltersJobAutoLoad")) {
    setCookie("TaskFiltersJobAutoLoad", true);
  }


  if (!Cookies.get("ShowAllTasks")) {
    setCookie("ShowAllTasks", false);
  }

  if (!Cookies.get("TaskFiltersContactAutoLoad")) {
    setCookie("TaskFiltersContactAutoLoad", true);
  }

  if (!Cookies.get("UseTaskColor")) {
    setCookie("UseTaskColor", false);
  }

  // set task custom field options

  if (!Cookies.get("ShowTaskCustomField1")) {
    setCookie("ShowTaskCustomField1", false);
  }

  if (!Cookies.get("ShowTaskCustomField2")) {
    setCookie("ShowTaskCustomField2", false);
  }

  if (!Cookies.get("ShowTaskCustomField3")) {
    setCookie("ShowTaskCustomField3", false);
  }

  if (!Cookies.get("ShowTaskCustomField4")) {
    setCookie("ShowTaskCustomField4", false);
  }

  if (!Cookies.get("ShowTaskCustomField5")) {
    setCookie("ShowTaskCustomField5", false);
  }

  if (!Cookies.get("ShowTaskCustomField6")) {
    setCookie("ShowTaskCustomField6", false);
  }

  // task custom list options

  if (!Cookies.get("ShowTaskCustomList1")) {
    setCookie("ShowTaskCustomList1", false);
  }

  if (!Cookies.get("ShowTaskCustomList2")) {
    setCookie("ShowTaskCustomList2", false);
  }

  if (!Cookies.get("ShowTaskCustomList3")) {
    setCookie("ShowTaskCustomList3", false);
  }

  if (!Cookies.get("ShowTaskCustomList4")) {
    setCookie("ShowTaskCustomList4", false);
  }

  if (!Cookies.get("ShowTaskCustomList5")) {
    setCookie("ShowTaskCustomList5", false);
  }

  if (!Cookies.get("ShowTaskCustomList6")) {
    setCookie("ShowTaskCustomList6", false);
  }

  // word wrap options

  if (!Cookies.get("wordWrapForHeaderText")) {
    setCookie("wordWrapForHeaderText", false);
  }

  if (!Cookies.get("wordWrapForColumnText")) {
    setCookie("wordWrapForColumnText", false);
  }

  if (!Cookies.get("wordWrapForHeaderText")) {
    setCookie("wordWrapForHeaderText", false);
  }

  // $("form[name=taskListSearchForm]").submit(function (event) {
  //   event.preventDefault();
  //   var phrase = $("#taskListSearchBox").val().replace(/["'&?,;]/g, "");
  //
  //   setCookie("taskListFindString", phrase);
  //   createTaskList("/api/v1/task?FindString=" + encodeURIComponent(phrase));
  // });
}

function loadTaskFilters() {
  if (!Cookies.get("TaskFilters")) {
    setCookie("TaskFilters", defaultTaskFilters);
  }

  if (!Cookies.get("filterFindString")) {
    setCookie("filterFindString", "");
  }

  $("#taskListSearchBox").val(Cookies.get('filterFindString'));
}

function setCookie(name, value) {
  Cookies.set(name, value, {
    expires: 365
  });
}

function postEffects() {
  activateMarkedTriggers();
}

let isUpdateInProgress = false;

function activateMarkedTriggers() {
  const taskFieldsArray = getTaskListFieldsToShowArray();
  const sortParams = getSortParams(taskFieldsArray);

  $(document).off('change', 'input[name=tmkd]').on('change', 'input[name=tmkd]', function () {
    if (isUpdateInProgress) return;

    const $checkbox = $(this);
    const status = $checkbox.prop("checked");
    const id = $checkbox.data("t-marked");

    if (!id) {
      console.error('Task ID not found');
      return;
    }

    isUpdateInProgress = true;

    $checkbox.prev().html(status ? "1" : "0");

    $.ajax({
      url: `/api/v1/task/${id}?Marked=${status}`,
      method: 'PUT',
      success: function (response) {
        console.log('Update successful:', response);
        if (sortParams.indexOf("TaskMarked") === -1) {
          $checkbox.prop('checked', status);
        } else {
          createTaskList();
        }
      },
      error: function (jqXHR) {
        console.error('Update failed:', jqXHR.responseText);
        $checkbox.prop('checked', !status);
      },
      complete: function () {
        isUpdateInProgress = false;
      }
    });
  });
}

function editListColumns() {
  var taskFieldNames, contactFieldNames, jobFieldNames;

  $.ajax({
    url: "/api/v1/fieldcaptions?fields=" + allTaskFieldCaptionNames.join(','),
    dataType: 'json',
    success: function (names) {
      if (names == 'InvalidLogin') {
        logout();
      }

      taskFieldNames = names;
      console.log(taskFieldNames);
      $.ajax({
        url: "/api/v1/fieldcaptions?fields=" + allContactFieldCaptionNames.join(','),
        dataType: 'json',
        success: function (names) {
          if (names == 'InvalidLogin') {
            logout();
          }

          contactFieldNames = names;

          $.ajax({
            url: "/api/v1/fieldcaptions?fields=" + allJobFieldCaptionNames.join(','),
            dataType: 'json',
            success: function (names) {
              if (names == 'InvalidLogin') {
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

              $.each(taskFieldNames, function (i) {
                msg += "<div class='checkbox'><label>";

                if ($.inArray(i, fieldValues) != -1) {
                  msg += "<input type='checkbox' checked='checked' name='" + i + "'> <span class='activeFieldColor'>" + this + "</span>";
                } else {
                  msg += "<input type='checkbox' name='" + i + "'> <span class='notActiveColor'>" + this + "</span>";
                }

                msg += "</label></div>";
              });

              msg += "</div>";
              msg += "<div class='col-xs-6 col-lg-4'>";
              msg += "<h4>Contact Fields</h4>";

              $.each(contactFieldNames, function (i) {
                msg += "<div class='checkbox'><label>";

                if ($.inArray(i, fieldValues) != -1) {
                  msg += "<input type='checkbox' checked='checked' name='" + i + "'> <span class='activeFieldColor'>" + this + "</span>";
                } else {
                  msg += "<input type='checkbox' name='" + i + "'> <span class='notActiveColor'>" + this + "</span>";
                }

                msg += "</label></div>";
              });

              msg += "</div>";
              msg += "<div class='col-xs-6 col-lg-4'>";
              msg += "<h4>Job Fields</h4>";

              $.each(jobFieldNames, function (i) {
                msg += "<div class='checkbox'><label>";

                if ($.inArray(i, fieldValues) !== -1) {
                  msg += "<input type='checkbox' checked='checked' name='" + i + "'> <span class='activeFieldColor'>" + this + "</span>";
                } else {
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
                buttons: [{
                  label: "<span class='hidden-xs'>Restore </span>Defaults",
                  cssClass: "btn-warning",
                  action: function (dialogRef) {
                    $.each($("form[name=taskFieldsToShowForm] input[type=checkbox]"), function () {
                      $(this).prop("checked", false);
                    });

                    $.each(defaultTaskListFields[0].split(','), function () {
                      $("form[name=taskFieldsToShowForm] input[name=" + this + "]").prop("checked", true);
                    });
                  }
                }, {
                  label: "Cancel",
                  action: function (dialogRef) {
                    dialogRef.close();
                  }
                }, {
                  label: "Save Changes",
                  cssClass: "btn-primary",
                  action: function (dialogRef) {
                    var activeTaskFields = [];
                    var inputList = $("form[name=taskFieldsToShowForm]").serializeArray();

                    if (inputList.length <= 0) {
                      alert("You need to have at least 1 field active");
                      return false;
                    }

                    $.each(inputList, function () {
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
            error: function (jqXhr, textStatus, errorThrown) {
              if (jqXhr.responseText == 'InvalidLogin') {
                logout();
              }

              alert(errorThrown);
            }
          });
        },
        error: function (jqXhr, textStatus, errorThrown) {
          if (jqXhr.responseText == 'InvalidLogin') {
            logout();
          }

          alert(errorThrown);
        }
      });
    },
    error: function (jqXhr, textStatus, errorThrown) {
      if (jqXhr.responseText == 'InvalidLogin') {
        logout();
      }

      alert(errorThrown);
    }
  });
}

function editFieldOrder() {
  var fields = getTaskListFieldsToShowArray();
  var msg = "";

  msg += "The top field shows up 1st on the Task List. The bottom field is last.  Drag and Drop the fields to change.";
  msg += "<hr />";
  msg += "<div>";
  msg += "<ul id='sortableFields'>";

  $.ajax({
    url: "/api/v1/fieldcaptions?fields=" + allTaskFieldCaptionNames.join(',') + "," + allJobFieldCaptionNames.join(',') + "," + allContactFieldCaptionNames.join(','),
    dataType: 'json',
    success: function (names) {
      if (names == 'InvalidLogin') {
        logout();
      }

      var currentSort = eval(Cookies.get("TaskListSort"));
      var originalSortArray = [];
      var originalSortArrayFieldNames = [];
      var originalSortArrayFieldDirection = [];

      $.each(currentSort, function () {
        originalSortArray.push(this);
        originalSortArrayFieldNames.push(fields[this[0]]);
        originalSortArrayFieldDirection.push(this[1]);
      });

      $.each(fields, function (i) {
        msg += "<li data-field-name='" + this + "'>" + names[this] + "</li>";
      });

      msg += "</ul>";
      msg += "</div>";

      BootstrapDialog.show({
        title: "Change Field Order",
        message: msg,
        onshown: function () {
          $("#sortableFields").sortable();
        },
        buttons: [{
          label: "Cancel",
          action: function (dialogRef) {
            dialogRef.close();
          }
        }, {
          label: "Save Field Order",
          cssClass: "btn-primary",
          action: function (dialogRef) {

            var newFieldOrderArray = [];

            $.each($("#sortableFields li"), function (i) {
              newFieldOrderArray.push($(this).data("field-name"));
            });

            var newFieldPositionArray = [];
            var sortString = "[";

            for (var i = 0; i < originalSortArrayFieldNames.length; i++) {
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
    error: function (jqXhr, status, error) {
      if (jqXhr.responseText == 'InvalidLogin') {
        logout();
      }

      alert(error);
    }
  });
}

function editOptions() {
  var msg = "";
  msg += "<form role='form' name='taskManagerOptionsForm'>";
  msg += " <div class='checkbox'>";
  msg += "  <label>";

  if (eval(Cookies.get('wordWrapForHeaderText'))) {
    msg += "<input type='checkbox' id='wordWrapForHeaderText' checked='checked'> Use Wordwrap for column headers";
  } else {
    msg += "<input type='checkbox' id='wordWrapForHeaderText'> Use Wordwrap for column headers";
  }

  msg += "  </label>";
  msg += " </div>";
  msg += " <div class='checkbox'>";
  msg += "  <label>";

  if (eval(Cookies.get('wordWrapForColumnText'))) {
    msg += "<input type='checkbox' id='wordWrapForColumnText' checked='checked'> Use Wordwrap for column text";
  } else {
    msg += "<input type='checkbox' id='wordWrapForColumnText'> Use Wordwrap for column text";
  }

  msg += "  </label>";
  msg += " </div>";
  msg += " <div class='checkbox'>";
  msg += "  <label>";

  if (eval(Cookies.get('UseTaskColor'))) {
    msg += "<input id='taskColorOption' name='UseTaskColor' type='checkbox' checked='checked'> Use Active Color Profile For Tasks";
  } else {
    msg += "<input id='taskColorOption' name='UseTaskColor' type='checkbox'> Use Active Color Profile For Tasks";
  }

  msg += "  </label>";
  msg += "</div>";

  msg += "<div class='checkbox'>";
  msg += "  <label>";

  if (eval(Cookies.get('ShowAllTasks'))) {
    msg += "<input type='checkbox' onchange='taskLimitToggle();' name='ShowAllTasks' checked='checked' id='taskLimitOptionToggle'> Show All Tasks";
  } else {
    msg += "<input type='checkbox' onchange='taskLimitToggle();' name='ShowAllTasks' id='taskLimitOptionToggle'> Show All Tasks";
  }

  msg += "  </label>";
  msg += "  <br />";
  msg += "  <div style='margin-left:20px; margin-top:5px;'>";
  msg += "    Show up to ";

  if (eval(Cookies.get('ShowAllTasks'))) {
    msg += "<input type='text' name='TaskLimit' class='form-control' disabled='true' id='taskLimitOption' value='" + Cookies.get('TaskLimit') + "' style='max-width:75px; display:inline-block;'>";
  } else {
    msg += "<input type='text' name='TaskLimit' id='taskLimitOption' class='form-control' value='" + Cookies.get('TaskLimit') + "' style='max-width:75px; display:inline-block;'>";
  }

  msg += "    Tasks";
  msg += "  </div>";
  msg += " </div>";
  msg += "</form>";

  BootstrapDialog.show({
    title: "Task Manager Options",
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
        //var options = $("form[name=taskManagerOptionsForm]").serializeArray();

        //$.each(options, function(){
        //  if(this.name == "TaskLimit"){
        //    setCookie('ShowAllTasks', false);
        //
        //  }

        //  setCookie(this.name, this.value);
        //});

        setCookie('UseTaskColor', $("#taskColorOption").prop("checked"));

        if ($("#taskLimitOptionToggle").prop("checked")) {
          setCookie('ShowAllTasks', true);
          tCurrentPage = 1;
        } else {
          setCookie('ShowAllTasks', false);
          setCookie('TaskLimit', $("#taskLimitOption").val());
          tasksPerPage = $("#taskLimitOption").val();
          tCurrentPage = 1;
        }

        setCookie('wordWrapForHeaderText', $("#wordWrapForHeaderText").prop("checked"));
        setCookie('wordWrapForColumnText', $("#wordWrapForColumnText").prop("checked"));

        dialogRef.close();
        createTaskList();
      }
    }]
  });
}

function taskLimitToggle() {
  var state = $("#taskLimitOptionToggle").prop("checked");

  if (state) {
    $("#taskLimitOption").attr("disabled", true);
  } else {
    $("#taskLimitOption").attr("disabled", false);
  }
}

function toggleOptionBoxes() {
  var allBoxes = $("form[name=taskFieldsToShowForm] input[type=checkbox]");
  var state = $("#toggleAllBox").prop("checked");

  $.each(allBoxes, function () {
    if (state) {
      $(this).prop("checked", true);
    } else {
      $(this).prop("checked", false);
    }
  });
}

function printSection(section) {
  $("#" + section).printThis({
    importCSS: false,
    importStyle: false,
    loadCSS: ["/remotevb/css/printStyles.css"],
    pageTitle: "Remote VirtualBoss Task Manager"
  });
}