var allTaskFieldCaptionNames    = ["TaskDescription","TaskFollows","FinishPlus","TaskTargetStart","TaskDuration","TaskTargetFinish","TaskActualFinish","TaskNumber","TaskOrder","TaskStatus","TaskRequested","TaskMarked","TaskNotes","TaskCustomField1","TaskCustomField2","TaskCustomField3","TaskCustomField4","TaskCustomField5","TaskCustomField6","TaskCustomList1","TaskCustomList2","TaskCustomList3","TaskCustomList4","TaskCustomList5","TaskCustomList6"];
var allJobFieldCaptionNames     = ["JobNumber","JobLot","JobOwnerName","JobSubdivision","JobLockBox","JobAddress","JobAddress2","JobCity","JobState","JobPostal","JobCountry","JobHomePhone","JobWorkPhone","JobCellPhone","JobFax","JobCompany","JobEmail","JobNotes","JobDirections","JobCustomField1","JobCustomField2","JobCustomField3","JobCustomField4","JobCustomField5","JobCustomField6","JobCustomList1","JobCustomList2","JobCustomList3","JobCustomList4","JobCustomList5","JobCustomList6"];
var allContactFieldCaptionNames = ["ContactCompany","ContactPerson","ContactProfession","ContactFirstName","ContactLastName","ContactSupervisor","ContactSpouse","ContactTaxID","ContactWebSite","ContactEmail","ContactFax","ContactWorkersCompDate","ContactInsuranceDate","ContactComments","ContactNotes","ContactPhones","ContactCustomField1","ContactCustomField2","ContactCustomField3","ContactCustomField4","ContactCustomField5","ContactCustomField6","ContactCustomList1","ContactCustomList2","ContactCustomList3","ContactCustomList4","ContactCustomList5","ContactCustomList6"];
/***************************************************************************************
	Initializes the Calendar after page is loaded
***************************************************************************************/
$(document).ready(function(){
  loadCalendarView();

  $("form[name=calendarSearchForm]").submit(function(event){   
    var phrase = $("#taskListSearchBox").val();

    loadCalendarView(encodeURIComponent(phrase));

    event.preventDefault();
  });
});
 
function loadCalendarView(searchPhrase){
   BootstrapDialog.show({
    title: "Loading Calendar, Please wait...",
    message: "<i class='fa fa-circle-o-notch fa-spin' style='margin-right:5px;'></i> Loading Calendar...<p style='font-size:12px; margin-top:7px;'>Closing this message is ok. The Calendar will continue to load and display when ready.</p></div>"
  });
  
  var date = new Date();
  var customUrl = '/api/v1/calendarfeed';
  
  if(searchPhrase){
    customUrl = '/api/v1/calendarfeed?FindString=' + searchPhrase;
  }
  
  $.ajax({
    url: customUrl,
    dataType:'json',
    success: function(tasks){
      if(tasks == 'InvalidLogin'){
        logout();
      }

	    $("#tTaskList").fullCalendar("destroy");
      $("#tCount").html(tasks.length);
      $("#tTaskList").fullCalendar({
        header: {
    		  left: 'prev,next today',
    		  center: 'title',
    		  right: 'month,basicWeek'
    		},
    		editable: true,
    		droppable: true,
    		dragOpacity: '.75',
    		drop: function(date, allDay) { 
    			var originalEventObject = $(this).data('eventObject');
    			var copiedEventObject = $.extend({}, originalEventObject);
    
    			copiedEventObject.start = date;
    			copiedEventObject.allDay = allDay;
    
    			$('#tTaskList').fullCalendar('renderEvent', copiedEventObject, true);
    			
    			if ($('#drop-remove').is(':checked')) {
    				$(this).remove();
    			}
    		},
    		defaultView: 'month',
    		startParam:'calStart',
    		endParam:'calEnd',
    		// Event Object must be formatted for FullCalendar to understand, uses fields like title, url, start, end, ect... So /api/v1/task can't be used.
    		// /api/v1/calendarfeed can be used, but we need a way to obtain the start and end dates for tasks.
    		events: tasks,
    		eventClick: function(task){
    			var d = $("#tTaskList").fullCalendar('getDate');
    			var CurrentDate = moment(d).format('YYYY-MM-DD');			
    			var view = $("#tTaskList").fullCalendar('getView');
    		  
    			setDateAndView(CurrentDate, view.name);
          editTask(task.TaskId, 'Calendar');
    			
    		  return false;
    		},
    		eventMouseover: function(task){
    		  //$(this).attr("title", task.title).tooltip();
    		},
    		//eventLimit: 3,
    		loading: function(isLoading, view){
    		  if(isLoading){
    		    $("#tCount").html("...");
    		  }else{
    		    // finished loading events
    		    $("#tCount").html(view.calendar.clientEvents().length);
    		  }
    		},
    		eventDrop: function(event, dayDelta, minuteDelta, allDay, revertFunc, jsEvent, ui, view){
    			var newStartDate = moment(event.start).format('YYYY-MM-DD');
    			var d = $("#tTaskList").fullCalendar('getDate');
    			var CurrentDate = moment(d).format('YYYY-MM-DD');			
    			var view = $("#tTaskList").fullCalendar('getView');
    		
    			setDateAndView(CurrentDate, view.name);
    			
    			$.ajax({							
    				url: "/api/v1/task?taskId=" + event.TaskId + "&Start=" + newStartDate,
					method: 'PUT',
    				success: function(response){
              if(response == 'InvalidLogin'){
                logout();
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
    	  viewDisplay: function(view) {
    			var d = $("#tTaskList").fullCalendar('getDate');
    			var CurrentDate = moment(d).format('YYYY-MM-DD');			
    		
    			setDateAndView(CurrentDate, view.name);
    	  },
    		eventResize: function(event, dayDelta, minuteDelta, revertFunc, jsEvent, ui, view){
      		if(event.end == null){
      			var newEndDate = moment(event.start).format('YYYY-MM-DD'); // in the case of event.end == null, we can assume the end date is the same as the start date (or the task has been changed to a 1 day duration).
      		}else{
      			var newEndDate = moment(event.end).format('YYYY-MM-DD');	
      		}
    			
      		var d = $("#tTaskList").fullCalendar('getDate');
      		var CurrentDate = moment(d).format('YYYY-MM-DD');
      		var view = $("#tTaskList").fullCalendar('getView');
      			
    		  setDateAndView(CurrentDate, view.name);
    			
      		$.ajax({							
      			url: "/api/v1/task?taskId=" + event.TaskId + "&End=" + newEndDate,
				method: 'PUT',
      			success: function(response){						
              if(response == 'InvalidLogin'){
                logout();
              }
      			},
      			error: function(jqXhr, textStatus, errorThrown){
              if(jqXhr.responseText == 'InvalidLogin'){
                logout();
              }

      			  alert(errorThrown);
      			}
        	});							
    	  }		
      });
      
      BootstrapDialog.closeAll();
    },
    error: function(jqXhr, textStatus, errorThrown){
      if(jqXhr.responseText == 'InvalidLogin'){
        logout();
      }

      BootstrapDialog.closeAll();
      alert(errorThrown);   
    }
  });
}

function setDateAndView(Date, View){
	Cookies.set("CalendarView", View, {
	  path: '/',
	  expires: 10
	});
	
	Cookies.set("CalendarDate", Date, {
	  path: '/',
	  expires: 10
	});
}

function setCookie(name, value){
  Cookies.set(name, value, {
    expires: 365
  });
}

function addEventToGoogleCalendar(){
  var body = "";
      body += "";
      
  BootstrapDialog.show({
    title: "Add Event to Google Calendar",
    message: body,
    buttons: [{
      label: "Add Event",
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
}


