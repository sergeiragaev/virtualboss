/**
 * sGantt v2.1
 * http://www.virtualboss.net
 *
 * Use ganttstyles.css for basic styling.
 * Use Moment.js (needed for some date calculations)
 * For event drag & drop, requires jQuery UI draggable.
 * For event resizing, requires jQuery UI resizable.
 *
 * 2017 Sean Whitescarver
 *
 * Last Updated: January 17th, 2017
 *
 **/
(function($){
 	var d = new Date();
 	var monthArray = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
 	var shortMonthArray = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
 	var veryShortMonthArray = ["J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"];
 	var curYear = d.getFullYear();
 	var curMonth = d.getMonth();
 	var monthName = monthArray[curMonth];
 	var daysInMonth = new Date(curYear, curMonth+1, 0).getDate();
	var events = new Array();	
 	var defaultStart = '';
 	var defaultEnd = '';

	/*************************************************************
	/	DEFAULT GANTT CHART SETTINGS
	/*************************************************************/
					           
  var settings = {
  	dayWidth     : 25,          // width in px
  	title        : '',          // text above task descriptions
  	jobTitle     : '',          // not used anymore
  	taskNumTitle : '',          // not used anymore
  	personTitle  : '',          // not used anymore
  	tStartTitle  : '',          // not used anymore
  	year         : curYear,			// current calendar year
    month        : curMonth,    // 0-11 - Jan-Dec
    time         : d.getTime(), // should change to use moment.js
    dateRange    : {
    	startDay: 1,			        // use todays date as default start (eventually)
    	endDay: daysInMonth,   	  // day of month to stop on (0-31)
    	numMonths: 1,				      // 1-12 - how many months to show
    	start: "",
    	end: ""
    },
    eventSource    : '',
    showJob        : true,
    showTstart     : true,
    showTaskNum    : false,
    advToolTips    : false,
    mobileOptimized: false,
    showPerson     : false,
    multiLineMode  : false,
    fixedColumns   : false,
    numColumns     : 0,
    makeScrollable : false,
    editable       : true,
    dayGrid        : true,
    showBarText    : false,
    primarySort    : '',
    secondarySort  : '',
    thisGuy        : '',
    columnOrder    : Cookies.get("GanttChartFieldsToShow"),
    showInnerText  : false
  };	
	
	/*************************************************************
	/	DEFAULT TASK SETTINGS
	/*************************************************************/
	
	var taskDefaults = {
		title	     : 'New Task',
		month	     : settings.month,
		year	     : settings.year,
		startDay   : 1,
		startMonth : 1,  				      // 1-12
		barLength  : 1,
		duration   : 1,
		color      : '',
		job        : '',
		taskNum    : '',
		person     : '',
		start      : defaultStart,		// start date  (yyyy-mm-dd)  needs to be in d.getTime() - use start = Date.parse("January 31, 2013"); turns string into formatted time.
		end        : defaultEnd,		  // end date	(yyyy-mm-dd)	 needs to be in d.getTime()
		url        : '',
		taskID     : '',
		taskNotes  : '(There are no notes for this task)',
		dateHolder : ''
	};
	
	// this snippet of code fixes a conflict between jquery ui tooltips and bootstrap tooltips. it's required to make the advanced tooltips work!
  var bootstrapTooltip = $.fn.tooltip.noConflict();
  $.fn.bootstrapTlp = bootstrapTooltip;

	/*************************************************************
	/	DEFAULT GANTT CHART LAYOUT
	/*************************************************************/
	
  $.fn.sGantt = function(options){
    var g = $.extend(settings, options);           
	    	g.numColumns = 0;
        g.dateRange.start = g.dateRange.start.replace(/-/g,"/");        
        g.dateRange.end = g.dateRange.end.replace(/-/g,"/");       
    
    setDateRange(g);
    setCookie("dayWidth", g.dayWidth); 
    setCookie("start", g.dateRange.start);
    setCookie("end", g.dateRange.end);
    
    g.thisGuy = this.selector; // used for referencing main gantt container

		loadOptions(g);

    return this.each(function(){
   	
   	$(this).addClass("sGantt");
		
		var selector = this;
		
		d.setFullYear(g.year);
		d.setMonth(g.month);
			
    var mainLayout = "";
        mainLayout += "<div class='btn-group' style='padding-bottom:5px; padding-top:3px;'>";
        mainLayout += "<button type='button' onclick='openGanttChartSettings();' class='btn btn-primary btn-xs'><i class='fa fa-cog'></i> Settings</button>";
   	    mainLayout += "   <button class='btn btn-primary btn-xs todayButton'><i class='fa fa-calendar' style='margin-right:3px;'></i> Today</button>";
   	    mainLayout += " </div>";
   		  mainLayout += " <table class='main' id='sGanttMain'>";
   		  mainLayout += "   <thead>";
   		  mainLayout += "     <tr>";
   			mainLayout += createHeaders(g);
   			
 			var tMonth = g.month;
 			var tYear = g.year;
 			//var daysInDateRange = getDaysInDateRange(g);
 			var a = moment(g.dateRange.start, 'YYYY/MM/DD');
 			var b = moment(g.dateRange.end, 'YYYY/MM/DD');
 			var c = b.diff(a, "days") + 1;
 			var daysInDateRange = c;
      
			// figure out todays date and, if found below, add a special marker for 'Go to Today' button scrolling
			var todaysDate = new Date();
			//var todaysDate = moment();
			//var todaysMonth = todaysDate.month();
			var todaysMonth = todaysDate.getMonth() + 1;
			var todaysDay = todaysDate.getDate();
			//var todaysDay = todaysDate.date();
			// todays date in this format (2014/08/25)
			var todaysYear = todaysDate.getFullYear();
			//var todaysYear = todaysDate.year();
			
			//var today = todaysDate.getFullYear() + '/' +
			//    (('' + todaysMonth).length < 2 ? '' : '') + todaysMonth + '/' +
			//    (('' + todaysDay).length < 2 ? '' : '') + todaysDay;			
			
			var today = moment().format('MM/DD/YYYY');

			for(var j = 0; j < g.dateRange.numMonths; j++){   			
				if(tMonth > 11){
					tMonth = 0; 
					tYear++;
				}
   				   				
   			//var numDays = getDaysInMonth(tYear, tMonth);
   			var numDays = moment(tYear + "-" + (tMonth+1), "YYYY-M").daysInMonth();

				mainLayout += "<th class='ruler'>";
				
				if(j == 0){
					mainLayout += "<div class='dates_table_first'>";
				}else{
					mainLayout += "<div class='dates_table'>";
				}
				
				// should output current month in the loop to the console
				// so tMonth+1 = current month (1-12)
				// tYear = current year
				// i = current day
				// formula should be something like tYear/tMonth/i
				
				if(daysInDateRange <= 1){
					mainLayout += "<div class='dtMonths'>" + veryShortMonthArray[tMonth] + "</div>";
				}else{
					if((numDays - g.dateRange.startDay < 5  && j == 0) || (j == g.dateRange.numMonths-1 && g.dateRange.endDay < 5)){
	   				if(g.dayWidth < 25){
	   					mainLayout += "<div class='dtMonths'></div>";
	   				}else{
						  mainLayout += "<div class='dtMonths' title='" + monthArray[tMonth] + " " + tYear + "'>" + veryShortMonthArray[tMonth] + "</div>";	
					  }
	   		  }else{	   		
        	  mainLayout += "<div class='dtMonths'>" + monthArray[tMonth] + " " + tYear + "</div>";	
	   		  }
			  }
   				
   		  var monthWidth = getWidth(numDays);
				
				/*************************************************************
				/	DRAW FIRST MONTH
				/*************************************************************/
				
				if(j == 0){
					monthWidth = getWidth(numDays-g.dateRange.startDay+1);
					
					if(g.dateRange.numMonths <= 1){					
						monthWidth = getWidth(g.dateRange.endDay-g.dateRange.startDay+1);
						numDays = g.dateRange.endDay;
					}

					mainLayout += "<ul style='width:" + monthWidth + "px;'>";
					
					for(var i = g.dateRange.startDay; i <= numDays; i++){
						if(g.dayWidth >= 15){
							if(tYear == todaysYear && tMonth == todaysMonth-1 && i == todaysDay){
								mainLayout += "<li class='dtDays Today'>" + i + "</li>";
							}else{
								mainLayout += "<li class='dtDays'>" + i + "</li>";
							}
						}else{
							mainLayout += "<li class='dtDays' style='height:12px;'></li>";
						}
					}
					
					mainLayout += "</ul></div></td>";	
				}
				
				/*************************************************************
				/	DRAW OTHER MONTHS
				/*************************************************************/	
					
				if(j != 0 && j != g.dateRange.numMonths - 1){
					mainLayout += "<ul style='width:" + monthWidth + "px;'>";
					
					for(var i = 1; i <= numDays; i++){
						if(g.dayWidth >= 15){
							if(tYear == todaysYear && tMonth == todaysMonth - 1 && i == todaysDay){
								mainLayout += "<li class='dtDays Today'>" + i + "</li>";
							}else{
								mainLayout += "<li class='dtDays'>" + i + "</li>";
							}
						}else{
							mainLayout += "<li class='dtDays' style='height:12px;'></li>";
						}
					}
					
					mainLayout += "</ul></div></td>";
				}
				
				/*************************************************************
				/	DRAW LAST MONTH
				/*************************************************************/	
					
				if(j == g.dateRange.numMonths-1 && j != 0){  
					monthWidth = getWidth(g.dateRange.endDay);
					mainLayout += "<ul style='width:" + monthWidth + "px;'>";
					
					for(var i = 1; i <= g.dateRange.endDay; i++){
						if(g.dayWidth >= 15){
							if(tYear == todaysYear && tMonth == todaysMonth - 1 && i == todaysDay){
								mainLayout += "<li class='dtDays Today'>" + i + "</li>";
							}else{
								mainLayout += "<li class='dtDays'>" + i + "</li>";
							}
						}else{
							mainLayout += "<li class='dtDays' style='height:12px;'></li>";
						}
					}
					
					mainLayout += "</ul></div></th>";
				}
				
				tMonth++;
			}

			mainLayout += "</tr></thead><tbody>";		
			
			/*************************************************************
			/	READ TASKS FROM JSON SOURCE
			/*************************************************************/	
        	
      var formattedStart = g.dateRange.start.replace(/\//g,"-"); // have to do this because firefox couldn't parse the date perfectly like everyone else!

			const sortParams = getSortParams(g.columnOrder.split(','));

			// var sourceURL = g.eventSource + "&end=" + Date.parse(g.dateRange.end) / 1000 + "&_=" + g.time + "&Sort1=" + g.primarySort + "&Sort2=" + g.secondarySort;
			var sourceURL = g.eventSource + "&end=" + Date.parse(g.dateRange.end) / 1000 + "&_=" + g.time + "&sort=" + sortParams;

			if(g.eventSource != ""){
				$.ajax({
					url: sourceURL + "&FindString=" + Cookies.get('filterFindString'),
					dataType: "json",
					error: function(e, status, error){
					  if(e.responseText == 'InvalidLogin'){
					    logout();
					  }
					  
						alert(error);
					},
					success: function(e){
					  if(e == 'InvalidLogin'){
					    logout();
					  }
					
						var t = e.content;
						var loadConfirm = true;
						
						if(t.length > 5000){
							//loadConfirm = confirm("Loading this Gantt Chart may take several minutes. To reduce load times, use the filter options to only show what you want to see. (" + t.length + " tasks)");
						}
						
						if(t.length == 0){
							BootstrapDialog.alert("There are no tasks to show");

							return;
						}
						
						if(loadConfirm){
							var dates = new Date(g.dateRange.start);
							var offsetDayGrid = dates.getDay();
				
							offsetDayGrid = ((offsetDayGrid) * 25) + offsetDayGrid;
							offsetDayGrid = "-"+offsetDayGrid+"px";
							
							if(g.multiLineMode){
								// do multi-line mode stuff here.
								mainLayout += createMultiTaskRow(g,"Task Description","Sean Whitescarver", "23 Allen Creek", "Task #", "Start");
								mainLayout += "<td class='dayGrid' colspan='"+g.dateRange.numMonths+"'>";
								
								for(var i in t){							
									var date = t[i].start.split("-");  // yyyy-mm-dd (with leading 0s)
									mainLayout += addMultiTaskLine({
										title      : t[i].title,
										startDay   : parseInt(date[2],10),
										startMonth : parseInt(date[1],10),
										startYear  : parseInt(date[0],10)%100,
										year       : date[0],
										color      : t[i].color,
										job        : t[i].job,
										duration   : t[i].duration,
										taskNum    : t[i].taskNum,
										person     : t[i].person,
										start      : t[i].start,
										end        : t[i].end,
										url        : t[i].url,
										taskID     : t[i].TaskId,
										taskNotes  : t[i].taskNotes
									});
								}
								
								mainLayout += "</td></tr></table>";
							}else{							  
							  $.each(t, function(i){
									var date = this.TaskTargetStart.split("-");  // yyyy-mm-dd (with leading 0s)
									
									mainLayout += addTask({
										title      : this.TaskDescription,
										startDay   : parseInt(date[2],10),
										startMonth : parseInt(date[1],10),
										startYear  : parseInt(date[0],10)%100,
										year       : date[0],
										color      : this.Color,
										job        : this.JobNumber,
										duration   : this.TaskDuration,
										taskNum    : this.TaskOrder,
										person     : this.ContactPerson,
										start      : this.TaskTargetStart,
										end        : this.TaskTargetFinish,
										taskID     : this.TaskId,
										taskNotes  : this.TaskNotes
									}, this);
									
									//if(i >= 100){
									//  return false;
									//}
									if(eval(Cookies.get('ShowAllTasks'))){
									  
									}else{
									  if(i >= (parseInt(Cookies.get('TaskLimit'), 10) - 1)){
									    return false;
									  }
									}
								});
								
								mainLayout += " </tbody>";
								mainLayout += "</table>";
							}
							
							// AFTER GANTT CHART AND TASKS ARE LOADED - APPLY
							$(selector).html(mainLayout);
							
							if(Cookies.get("GanttChartSort")){
							  
							}else{
							  setCookie("GanttChartSort", "[[1,0],[2,0]]");
							}

							initGanttChartSorting();

              // $("#sGanttMain").tablesorter({
              //   sortList: eval(Cookies.get("GanttChartSort"))
              // }).bind("sortStart",function(){
              //
              // }).bind("sortEnd", function(data){
              //   setCookie("GanttChartSort", data.delegateTarget.config.sortList);
              // });
							
              	
							$(".dtDays").css("width",g.dayWidth+"px");
														
							initOptions(selector);
							
							var custom_length = "0";
							
							if(t.length < 100){
							  custom_length = t.length;
							}else{
							  custom_length = "100";
							}

							updateTaskCount(e.page.totalElements);

							// // get number of tasks to show from task manager setting.
							// if(eval(Cookies.get('ShowAllTasks'))){
							//   $("#tCount").html("<a href='#' onclick=\"editOptions(); return false;\">" + t.length + "</a>");
							// }else{
							//   custom_length = Cookies.get('TaskLimit');
							//   $("#tCount").html("<a href='#' onclick=\"editOptions(); return false;\">Showing</a> " + custom_length + " out of " + t.length);
							// }
							//
							// //$("#tCount").html("Showing " + custom_length + " out of " + t.length);
							
							if(g.makeScrollable){
							  applyScrollableHeader(g);
							}
							
							if(g.mobileOptimized){
							  applyMobileOptimization(g);
							}else{
							  removeMobileOptimization(g);
							}
							
							if(g.editable){
							  makeEditable(g); 
							}
							
							$(".bar").click(function(){
							  editTask($(this).attr("id"), "GanttChart");
							});
							
							var $root = $('html, body');
							
							$(".scrollRight").click(function(){
						   		$root.animate({
    	    					scrollLeft: $($(this).attr('href')).offset().left-250
    							}, 500);
    							
    							var cTemp = $($(this).attr('href')).css("backgroundColor");
    							
    							$($(this).attr('href')).animate({
    								backgroundColor: "yellow"
    							},1000).animate({
    								backgroundColor: cTemp
    							},500).animate({
    								backgroundColor: "yellow"
    							},500).animate({
    								backgroundColor: cTemp
    							},500).animate({
    								backgroundColor: "yellow"
    							},500).animate({
    								backgroundColor: cTemp
    							},500);
    							
    							return false;		
							});
							
							$(".todayButton").click(function(){
								$root.animate({
									scrollLeft: $(".Today").offset().left-500
								}, 500);
							});
							
							/*if(g.editable){
								$(".bar").bind( "dragstart", function(event, ui){  // this is needed to make the revert option on the draggable elements 100% accurate.  it's a bug in jquery ui draggable.  This is a fix.
   									ui.originalPosition.top = $(this).position().top;
    								ui.originalPosition.left = $(this).position().left;
								});
							}*/
							
							// shows the resizing indicator on the left and right of the bars - doesn't work for some reason
							if(g.editable && !g.showInnerText){							
								$(".bar").mouseenter(function(){
									//$(this).children(".barLeft, .barRight").css("visibility","visible");
								}).mouseleave(function(){
									//$(this).children(".barLeft, .barRight").css("visibility","hidden");
								});
							}
							
							// THERE WERE DISPLAY PROBLEMS WITH THESE TOOLTIPS, FIX EVENTUALLY
							if(g.advToolTips){
								$(".bar").tooltip({
									tooltipClass: 'tTip',
									track: true,
									position: { 
										my: "left+20 top+15", 
										at: "right center" 
									},
									content: function(e, ui){										
										var view = "";
										
										$.ajax({
											url: "/api/v1/task/" + $(this).parent().attr("id"),
											dataType: "json",
											async: false,
											success: function(data){
											  if(data == 'InvalidLogin'){
											    logout();
											  }
											  
												view = "<div class='tipDesc'>"+ data.TaskDescription +"</div>";
												view += "<div class='ttWrap'>";
												view += "<div class='smallerTooltipText'>Start: " + moment(data.TaskTargetStart).format("MM/DD/YYYY") + "</div>";

    										var days = data.TaskDuration == 1 ? "day" : "days";

												view += "<div class='smallerTooltipText'>Duration: " + data.TaskDuration + " " + days + "</div>";
												view += "<div class='smallerTooltipText'>End: " + moment(data.TaskTargetFinish).format("MM/DD/YYYY") + "</div>";

												var notes = data.TaskNotes.replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/g,'<br />');

												if(data.TaskNotes != "" && data.TaskNotes != null && data.TaskNotes != " "){
													view += "<div style='padding-top:5px; padding-bottom:0px; margin-bottom:0px; font-size:12px; font-family:inherit;'>";
													view += "<span class='glyphicon glyphicon-comment'></span> Notes:</p><div class='tipNotes'>" + notes + "</div>";
												}
												
												view += "</div>";												
										  },
											error: function(e, status, err){
											  if(e.responseText == 'InvalidLogin'){
											    logout();
											  }
											
												console.log(err);
											} 										
										});
										
									  return view;									
                	}
								});
							}
																					
							$(".hMenu").click(function(){
                openGanttChartSettings();
							});
														
							// WHAT TO DO WHEN RESTORE DEFAULTS IS CLICKED
							$("#restoreDefaults").click(function(){
								$('.menuDrop input[type=checkbox]').each(function(){
									$(this).prop('checked',false);
								});
								$("#gridOption").prop("checked",true);
								$("#tStartOption").prop("checked",true);
								$("#toolTipOption").prop("checked",true);
								$("#jobOption").prop("checked",true);
								$("#barEditOption").prop("checked",true);
								$("#primarySort").val("JobNumber");
								$("#secondarySort").val("TaskTargetStart");
							});			
							
							if(g.dayGrid){
								$(".dayGrid").css("background-position",offsetDayGrid);
						  }else{
						    $(".dayGrid").css("background-image","none");
							}
							
							var cookies = getCookies();
							
							$("#primarySort").val(cookies['primarySort']);
							$("#secondarySort").val(cookies['secondarySort']);
							$(".loading").css("display","none");
							
							if(g.editable){
								$('body').ajaxStart(function() {
							    $(this).css({'cursor':'wait'})
								}).ajaxStop(function() {
							    $(this).css({'cursor':'default'})
								});
							}
							
							if(g.fixedColumns){
								$(".sGantt").css("width","100%");
								
								var table = $('#sGanttMain').DataTable({
						   		"scrollX": "100%",
						      "paging": false,
						      "bFilter": false,
						      "bInfo": false
						   	});
						   	
						   	new $.fn.dataTable.FixedColumns(table, {
						   		leftColumns: g.numColumns
						   	});
						  }else{
						  	$(".sGantt").css("width","auto");
						  }
						}
					}			
				});				
			}		
		});		   		
  };
    
	/*************************************************************
	/	ADDING TASKS
	/*************************************************************/
	
	function addTask(options, task){
		var t = $.extend(taskDefaults, options);
		var g = settings; 								

		t.dateHolder = t.start;		
		t.barLength = getBarLength(t.start, t.end);
		d.setFullYear(g.year);
		d.setMonth(g.month);
		
		var tOnBar = "";
		
		if(g.showBarText){
			tOnBar = getTextOnBar(g, t, task);
		}
		
		var lPos = getLeftPos(g, t);
		var bWidth = getBarWidth(t.barLength, g.dayWidth);		
		var newRow = createRow(g, t, task);
		
		if(g.advToolTips){
		  var notes = t.taskNotes;
		}else{
		  var notes = "";
		}
    
		if(g.dayWidth >= 15){  // IF DAY GRID IS TOO TINY TO DISPLAY BOTH NUMBERS, DO NOT SHOW THE DURATION ON THE TASK BAR
			newRow += "<td class='dayGrid' colspan='" + g.dateRange.numMonths + "'>";
			newRow += " <div class='barWrap'>";
			
			if(eval(Cookies.get('UseTaskColor'))){
  			// newRow += "<div class='bar' id='" + t.taskID + "' style='width:" + bWidth + "px; left:" + lPos + "px; background:" + task.Color + "; border:1px " + task.Color + " solid;'>";
				newRow += "<div class='bar' id='" + t.taskID + "' style='width:" + bWidth + "px; left:" + lPos + "px; background:" + task.Color + "; border:1px #000000 solid;'>";
			}else{
			  newRow += "<div class='bar' id='" + t.taskID + "' style='width:" + bWidth + "px; left:" + lPos + "px;'>";
			}
			
			newRow += "     <a title='" + notes + "'></a>";
			newRow += "     <div class='textOnBar'>" + tOnBar + "</div>";
			newRow += "     <div class='barLeft'></div>";
			newRow += "     <div class='barRight'></div>";			
			
			if(g.showInnerText){
			  newRow += "<span class='startInner' id='" + t.taskID + "_startInner'>" + t.startDay + "</span>";
			  newRow += "<span class='durInner' id='" + t.taskID + "_durInner'>" + parseInt(t.end.split('-')[2],10) + "</span>";
			}
			
			newRow += "</div>";
		}else{
			newRow += "<td class='dayGrid' colspan='" + g.dateRange.numMonths + "'>";
			newRow += " <div class='barWrap'>";

			if(eval(Cookies.get('UseTaskColor'))){
  			newRow += "<div class='bar' id='" + t.taskID + "' style='width:" + bWidth + "px; left:" + lPos + "px; background:" + task.Color + "; border:1px " + task.Color + " solid;'>";
			}else{
			  newRow += "<div class='bar' id='" + t.taskID + "' style='width:" + bWidth + "px; left:" + lPos + "px;'>";
			}

			newRow += "     <a href='" + t.url + "' title='" + t.taskNotes + "'></a>";
			newRow += "     <div class='textOnBar'>" + tOnBar + "</div>";
			newRow += "     <div class='barLeft'></div>";
			newRow += "     <div class='barRight'></div>";
			
			if(g.showInnerText){
			  newRow += "<span class='startInner' id='" + t.taskID + "_startInner'>" + t.startDay + "</span>";
			  newRow += "<span class='durInner'></span>";
			}
			
			newRow += "</div>";
		}
		
		if(g.fixedColumns){
			newRow += figureOutExtraColumns(g.dateRange.numMonths);
		}else{
			newRow += "</div></td></tr>";
		}

		return newRow;
		//return this;  
		// to restore chainability, use "return this"
	}

	function figureOutExtraColumns(numMonths){
		var extraRows = "</div></td>";
		
		for(var i = 1; i < numMonths; i++){
			extraRows += "<td class='extras'></td>";
		}
		
		extraRows += "</tr>";
		
		return extraRows;
	}

	function addMultiTaskLine(options){
		var t = $.extend(taskDefaults, options);
		var g = settings; 								
		
		t.dateHolder = t.start;		
		t.barLength = getBarLength(t.start, t.end);
		
		d.setFullYear(g.year);
		d.setMonth(g.month);
		
		var tOnBar = "";
		
		if(g.showBarText){
			tOnBar = getTextOnBar(g, t);
		}
		
		var lPos = getLeftPos(g, t);
		var bWidth = getBarWidth(t.barLength, g.dayWidth);
		
		if(g.dayWidth >= 15){  // IF DAY GRID IS TOO TINY TO DISPLAY BOTH NUMBERS, DO NOT SHOW THE DURATION ON THE TASK BAR
			var newRow = "<div class='barWrap' style='width:100%; position:absolute;'><div class='bar' id='" + t.taskID + "' style='width:" + bWidth + "px; left:" + lPos + "px; background:" + t.color + ";'><a href='" + t.url + "' title='" + t.taskNotes + "'></a><div class='textOnBar'>" + tOnBar + "</div><div class='barLeft'></div><div class='barRight'></div>";			
			
			if(g.showInnerText){
			  newRow += "<span class='startInner' id='" + t.taskID + "_startInner'>" + t.startDay + "</span><span class='durInner' id='" + t.taskID + "_durInner'>" + parseInt(t.end.split('-')[2],10) + "</span>";
	  	}
	  	
			newRow += "</div>";
		}else{
			var newRow = "<div class='barWrap' style='width:100%; position:absolute;'><div class='bar' id='" + t.taskID + "' style='width:" + bWidth + "px; left:" + lPos + "px; background:" + t.color + ";'><a href='" + t.url + "' title='" + t.taskNotes + "'></a><div class='textOnBar'>" + tOnBar + "</div><div class='barLeft'></div><div class='barRight'></div>";
			
			if(g.showInnerText){
			  newRow += "<span class='startInner' id='" + t.taskID + "_startInner'>" + t.startDay + "</span><span class='durInner'></span>";
			}
			
		  newRow += "</div>";
		}
		
		newRow += "</div>";
		  
		return newRow;
	}
	
	/*************************************************************
	/	RESIZE THE GANTT CHART BASED ON SLIDER
	/*************************************************************/
	
	$.fn.resize = function(options){
		var o = $.extend(settings, options);
		
		$(this.selector + " .dtDays").each(function(){
			$(this).css("width",o.dayWidth);
		});
		
		$(this.selector + " .ruler ul").each(function(){
			var days = $(this).children("li").size();
			var rulerWidth = getWidth(days);
			$(this).css("width",rulerWidth + "px");
		});

		return this;
	};

	/*************************************************************
	/	GANTT CHART AND TASK FUNCTIONS
	/*************************************************************/

	function makeEditable(g){
		var startPos;
		var durationChange;
		var cw, ow;
		
		$(".bar").each(function(){		
			$(this).draggable({	
				containment: 'parent',
				zIndex: 1,
				grid: [g.dayWidth+1, g.dayWidth+1],
				start: function(e, ui){
				  ui.helper.bind("click.prevent", function(e){
				    e.preventDefault();
				  });
				  
					if(g.advToolTips){
					  $(".bar").tooltip("disable");
					}

					if(g.showBarText){
					  $(this).children(".textOnBar").fadeOut('fast');
					}
				},
				stop: function(e, ui){
				  setTimeout(function(){
				    ui.helper.unbind("click.prevent");
				  }, 300);
				  
					if(g.advToolTips){
					  $(".bar").tooltip("enable");
					}
					
					if(g.showBarText){
					  $(this).children(".textOnBar").fadeIn('fast');
					}
					
					var posL = Math.round($(this).position().left);
					var start = Math.round((posL/(g.dayWidth+1)))+1;
					var duration = Math.round($(this).outerWidth()/(g.dayWidth+1));
					var nStartDate = moment(getNewStartDate(g, start)).format('YYYY-MM-DD');  // gets the new start date when dragging a bar
					var nEndDate = moment(getNewEndDate(g, start, duration)).format('YYYY-MM-DD');
					
					// changed from ui.position.left to posL because posL was using the actual current location.
					if(posL != Math.round(ui.originalPosition.left)){		
					  var id = $(this).attr("id");
						
						$.ajax({
							method: 'PUT',
							dataType: 'json',
							url: '/api/v1/task?taskId=' + id + '&Start=' + nStartDate + "&End=" + nEndDate,
							success: function(response){
							  if(response === 'InvalidLogin'){
							    logout();
							  }
							  
								var start = "",
								end = "",
								tid = "",
								duration = "";
								e = response.content;
								
								for(var i in e){
									start = e[i].TaskTargetStart;
									end = e[i].TaskTargetFinish;
									duration = e[i].TaskDuration;
									tid = e[i].TaskId;

									if(g.showTstart){
										var tstart = start.split('-');

										$("#"+tid+"_startInfo").html(tstart[1]+"/"+tstart[2]+"/"+tstart[0]%100);
									}
									
									if(g.showBarText){
										var tstart = start.split('-');
										var tend = end.split('-');
									
                  	$("#"+tid+"_barStart").html(tstart[1]+"/"+tstart[2]+"/"+tstart[0]%100);
										$("#"+tid+"_barDur").html(duration);
										$("#"+tid+"_barEnd").html(tend[1]+"/"+tend[2]+"/"+tend[0]%100);
									}
									
									if(g.showInnerText){
										$("#"+tid+"_startInner").html(parseInt(tstart[2],10));
										$("#"+tid+"_durInner").html(parseInt(tend[2],10));								
									}
											
									start = Date.parse(start.replace(/-/g,'/'))/1000;
    						  
    						  var leftPos = Math.round((start - (Date.parse(g.dateRange.start)/1000)) / 86400);							    	
 						    	    leftPos = ((leftPos) * g.dayWidth) + (leftPos + 1);
									
									var width = getBarLength(e[i].TaskTargetStart, e[i].TaskTargetFinish);
									    width = getBarWidth(width, g.dayWidth);
									
									$("#"+tid).css("left", leftPos+"px").css("width",width+"px");
									
									console.log(e[i]);
									console.log(getTextOnBar(g, 0, e[i]));
								}
							},
							error: function(jqXhr, status, error){
							  if(jqXhr.responseText == 'InvalidLogin'){
							    logout();
							  }
							}
						});
					}else{
						// task bar is back in the original left/left position. don't save.
					}					
				}
			}).resizable({
				axis        : 'x',
				containment : 'parent',
				handles     : 'e,w',
				minWidth    : g.dayWidth-2,  // subtract 2 if bars use borders.  otherwise, do not subtract anything
				grid        : [g.dayWidth+1,0],
				start       : function(e, ui){
				  $(".bar").bind("click.prevent", function(e){
				    e.preventDefault();
				  });
				  
				  ui.helper.bind("click.prevent", function(e){
				    e.preventDefault();
				  });

					if(g.advToolTips){
            $(".bar").tooltip("disable");
					}
				},
				stop: function(e, ui){				  
				  setTimeout(function(){
				    ui.helper.unbind("click.prevent");
				  }, 300);

					if(g.advToolTips){
					  $(".bar").tooltip("enable");
					}

					var duration = Math.round($(this).outerWidth()/(g.dayWidth+1)); // changed to use outerWidth() method instead of ui.size.width because it is way more accurate/consistent
					var ps = $(this).position().left;
					var st = Math.round((ps/(g.dayWidth+1)))+1;
					var nStartDate = moment(getNewStartDateResize(g, st)).format('YYYY-MM-DD'); // returns the new start date for when resizing a bar
					var nEndDate = moment(getNewEndDate(g, st, duration)).format('YYYY-MM-DD'); // returns the new end date
					var direction = $(this).data('ui-resizable').axis; // 'e' or 'w'
					var id = $(this).attr("id");
					var updateURL = "/api/v1/task?taskId=" + id + "&";
					var save = false;

					if(direction == "w"){
						updateURL += "Start=" + nStartDate;
					}
					
					if(direction == "e"){
						updateURL += "End=" + nEndDate;
					}

					if(!save){
						$.ajax({
							method: 'PUT',
							url: updateURL,
							dataType: 'json',
							success: function(response){
							  if(response === 'InvalidLogin'){
							    logout();
							  }

								var start = "";
								var end = "";
								var tid = "";
								var duration = "";
								e = response.content;

								for(var i in e){
									start = e[i].TaskTargetStart;
									end = e[i].TaskTargetFinish;
									duration = e[i].TaskDuration;
									tid = e[i].TaskId;
									
									if(g.showBarText){
										var tstart = start.split('-');
										var tend = end.split('-');
										
										$("#"+tid+"_barStart").html(tstart[1]+"/"+tstart[2]+"/"+tstart[0]%100);
										$("#"+tid+"_barDur").html(duration);
										$("#"+tid+"_barEnd").html(tend[1]+"/"+tend[2]+"/"+tend[0]%100);
									}
									
									if(g.showInnerText){
										$("#"+tid+"_startInner").html(parseInt(tstart[2],10));
										$("#"+tid+"_durInner").html(parseInt(tend[2],10));
									}
									
									start = Date.parse(start.replace(/-/g,'/'))/1000;
    						  
    						  var leftPos = Math.round((start - (Date.parse(g.dateRange.start)/1000)) / 86400);							    	
    						    	leftPos = ((leftPos) * g.dayWidth) + (leftPos + 1);
									
									var width = getBarLength(e[i].TaskTargetStart, e[i].TaskTargetFinish);
									    width = getBarWidth(width, g.dayWidth);
									
									$("#"+tid).css("left", leftPos+"px").css("width",width+"px");
								}
							},
							error: function(jqXhr, status, error){
							  if(jqXhr.responseText == 'InvalidLogin'){
							    logout();
							  }
							}
						});						
					}else{
						// revert the task to its original width and position.
						$(this).animate({
							left: ui.originalPosition.left, 
							width: ui.originalSize.width,
							done: function(){}
						},500);
					}
				}
			}).droppable({
				hoverClass: "preLinkHover",
				tolerance: 'touch',
				drop: function(e, ui){
					confirm("Link (" + $(ui.draggable).attr("id") + ") to (" + e.target.id + ")?");
				}
			});
		});
	}

	
	function getNewStartDate(g, start){
		var cStart = new Date(g.dateRange.start);
		    cStart.setDate(cStart.getDate() + start - 1);
		
		return cStart;	
	}

	function getNewStartDateResize(g, start){
		var cStart = new Date(g.dateRange.start);
		    cStart.setDate(cStart.getDate() + start - 1);
		
		return cStart;
	}

	function getNewEndDate(g, st, duration){
		var cStart = new Date(g.dateRange.start);
		    cStart.setDate(cStart.getDate() + st - 1);
		    cStart.setDate(cStart.getDate() + duration - 1);
		
		return cStart;
	}

	function getTextOnBar(g, t, task){
	  var barTextFields = Cookies.get('BarTextFields').split(',');	  
	  var text = [];

	  $.each(barTextFields, function(){
	    if(this == 'TaskTargetStart' || this == 'TaskTargetFinish'){
        text.push(moment(task[this]).format('MM/DD/YYYY'));
	    }else{
	      text.push(task[this]);
	    }
	  });
	   
	  text = text.join(',').replace(/,/g, ', ');

	  return text;
	}

	function getBarLength(start, end){
		start = Date.parse(start.replace(/-/g,'/'));
		end = Date.parse(end.replace(/-/g,'/'));
		
		var oneDay = 24*60*60*1000; // hours*minutes*seconds*milliseconds
		var tDur = Math.round(Math.abs((end - start) / oneDay));
		
		tDur++;
		
		return tDur;	
	}

	function getWidth(days){
		days = (days * settings.dayWidth) + days;
		
		return days;
	}
	
    function getDaysInMonth(year, month){
    	var tDate = new Date(year, month+1, 0).getDate();
    	
    	return tDate;
    }
    
    function getLeftPos(g, t){
    	t.start = Date.parse(t.start.replace(/-/g,'/'))/1000;
    	
    	var leftPos = Math.round((t.start - (Date.parse(g.dateRange.start)/1000)) / 86400);
    	leftPos = ((leftPos) * settings.dayWidth) + (leftPos + 1);

    	return leftPos;
    }
    
    function getBarWidth(dur, cw){
    	var barWidth = (dur * cw) + (dur - 3);  // SUBTRACT 3 IF BARS HAVE A 1PX BORDER, SUBTRACT 1 IF NO BORDER IS USED
    	
    	return barWidth;
    }
    
    function createHeaders(g){
    	var header = "";
    	var cOrder = g.columnOrder.split(',');

    	$.each(cOrder, function(){
    	  header += "<th class='tTitle'><div class='" + this + "'>" + g.fieldNames[this] + "</div></th>";
    	});

    	return header;
    }
    
    function createRow(g, t, task){
    	var row = "<tr>";
    	var cOrder = g.columnOrder.split(',');

      $.each(cOrder, function(){
        if(this == "TaskDescription"){
          row += "<td class='taskDescription'>";
          row += "  <div class='" + this + "'>";
          row += "    <a href='#" + task.TaskId + "' class='scrollRight' title='Scroll over to view this task'><i class='fa fa-chevron-right'></i> ";
          row += "    <a href='#' onclick=\"editTask(\'" + task['TaskId'] + "\',\'GanttChart\');\">" + task['TaskDescription'] + "</a>";
          row += "  </div>";
          row += "</td>";
        }else if(this == "TaskTargetStart" || this == "TaskTargetFinish"){
          row += "<td class='taskDescription'><div class='" + this + "'>" + moment(task[this]).format('MM/DD/YYYY') + "</div></td>";
        }else{
          row += "<td class='taskDescription'><div class='" + this + "'>" + task[this] + "</div></td>";
        }        
    	});
      
		  return row;
    }   
     
    function createMultiTaskRow(g,description,person,job,tasknum,start){
    	var row = "<tr>";
    	var cOrder = g.columnOrder.split(',');

      for(var i in cOrder){
    		switch(cOrder[i]){
    			case "sortTask":
  					row += "<td class='taskDescription'><div class='tDescription'>"+description+"</a></div></td>";
    			break;
    			case "sortJob":
   			   	if(g.showJob){
  						row += "<td class='taskDescription'><div class='tJob'>"+job+"</div></td>";
    				}
    			break;
    			case "sortTaskNum":
			    	if(g.showTaskNum){
  						row += "<td class='taskDescription'><div class='tTaskNum'>"+tasknum+"</div></td>";
			    	}		
    			break;
    			case "sortPerson":
			    	if(g.showPerson){
  						row += "<td class='taskDescription'><div class='tPerson'>"+person+"</div></td>";
			    	}  				
    			break;
    			case "sortStart":
			    	if(g.showTstart){

			    	}		
    			break;
    		}
    	}
    		
		  return row;
    }
      
    function setDateRange(g){
    	g.dateRange.startDay = moment(g.dateRange.start, 'YYYY/MM/DD').date();
    	g.dateRange.endDay = moment(g.dateRange.end, 'YYYY/MM/DD').date();
    	g.month = moment(g.dateRange.start, 'YYYY/MM/DD').month();
    	g.year = moment(g.dateRange.start, 'YYYY/MM/DD').year();
      g.dateRange.numMonths = getMonthsToShow(g);
    }
    
    function getDaysInDateRange(g){
    	var oneDay = 24*60*60*1000;	
    	
    	return Math.round(((Date.parse(g.dateRange.end)-Date.parse(g.dateRange.start))/oneDay))+1;
    }
       
    function getMonthsToShow(g){
      var a = moment(g.dateRange.start, 'YYYY/MM/DD');
      var b = moment(g.dateRange.end, 'YYYY/MM/DD');
      var c = b.diff(a, "months") + 2;

		  return c;
    }
    	
    function getCookies(){
    	var cks = new Object();
    	var ckList = document.cookie.split("; ");
    	
    	for (var i=0; i < ckList.length; i++){
    	  var ck = ckList[i].split("=");      
    	  cks[ck[0]] = unescape(ck[1]);
    	}
    	
	    return cks;
	  }
    
	  // HAMBURGER MENU
    function createHOptions(g){
    }
    
  function loadOptions(g){
    var cookies = getCookies();

    if(!Cookies.get('dayGrid')){
      setCookie('dayGrid', g.dayGrid);
    }else{
     	g.dayGrid = eval(Cookies.get('dayGrid'));
    }

		if(!Cookies.get('makeScrollable')){
		  setCookie('makeScrollable', g.makeScrollable);
		}else{
			g.makeScrollable = eval(Cookies.get('makeScrollable'));
		}
		    
    if(!Cookies.get('showBarText')){
      setCookie('showBarText', g.showBarText);
    }else{    
  		g.showBarText = eval(Cookies.get('showBarText'));
    }
    
   	if(!Cookies.get('advToolTips')){
   	  setCookie('advToolTips', g.advToolTips);
   	}else{
   		g.advToolTips = eval(Cookies.get('advToolTips'));
   	}

   	if(!Cookies.get('barIsEditable')){
   		setCookie('barIsEditable', g.editable);
   	}else{
   		g.editable = eval(Cookies.get('barIsEditable'));
   	}
    	
   	if(!Cookies.get('GanttChartFieldsToShow')){
   		setCookie('columnOrder', defaultGanttChartFields);
   	}else{
   		g.columnOrder = Cookies.get('GanttChartFieldsToShow');
   	}
   	
   	if(!Cookies.get('showInnerText')){
   		setCookie('showInnerText', g.showInnerText);
   	}else{
   		g.showInnerText = eval(Cookies.get('showInnerText'));
   	}
   	
   	if(!Cookies.get('mobileOptimized')){
   	  setCookie('mobileOptimized', g.mobileOptimized);
   	}else{
   	  g.mobileOptimized = eval(Cookies.get('mobileOptimized'));
   	}
  }
    
  function initOptions(gantt){
  	var cookies = getCookies();
				
		if(cookies['optionState'] == "open"){
			$(".opts").toggle();
			$("#settingsArrow").removeClass("settingsArrowDown").addClass("settingsArrowUp");
		}else{
			$("#settingsArrow").removeClass("settingsArrowUp").addClass("settingsArrowDown");
		}
		
		$("#colSort").sortable({
			axis:"x",
			placeholder: "ui-state-highlight",
			stop: function(e, ui){
			  // do something here that updates the position of the gantt chart columns. 
			}
		}).disableSelection();
		
		$("#applyGanttSettings").click(function(){
			setCookie("multiLineMode",$("#multiLineOption").is(":checked"));
			setCookie("fixedColumns", $("#fixedColumnsOption").is(":checked"));
			setCookie("dayGrid",$("#gridOption").is(":checked"));
			setCookie("showPerson",$("#personOption").is(":checked"));
			setCookie("makeScrollable",$("#scrollableOption").is(":checked"));
			setCookie("advToolTips",$("#toolTipOption").is(":checked"));
			setCookie("showJob",$("#jobOption").is(":checked"));
			setCookie("showTaskNum",$("#tNumOption").is(":checked"));
			setCookie("showBarText",$("#barTextOption").is(":checked"));
			setCookie("showTstart",$("#tStartOption").is(":checked"));
			setCookie("primarySort",$("#primarySort").val());
			setCookie("secondarySort",$("#secondarySort").val());
			setCookie("barIsEditable",$("#barEditOption").is(":checked"));
			setCookie("showInnerText",$("#innerTextOption").is(":checked"));

			var sortIDs = $("#colSort").sortable("toArray");
						
			$(gantt).sGantt();
			
			return false;
		});        
  }
 
  function applyMobileOptimization(g){
    $(".sGanttWrap").css("display", "block");    
  }
  
  function removeMobileOptimization(g){
    $(".sGanttWrap").css("display", "inline-block");
  }
   
  function applyScrollableHeader(g){
  	var cOrder = g.columnOrder.split(',');
  	var tdWidth = "", jWidth = "", tnWidth = "", pWidth = "", tsWidth = "";
  	    
    var dimensions = [];
    
    $.each(cOrder, function(i){
      dimensions.push([{
        name: cOrder[i],
        width: $('.' + this).width() + parseInt(i, 10)
      }]);
    });
    		
		var dayGridWidth = $(".dayGrid").width();
							
		$("thead > tr, tbody").addClass("makeScrollable");
		
		$.each(dimensions, function(){
		  $("." + this[0].name).css("width", this[0].width);
		});
		
		$(".dayGrid").css("width",dayGridWidth);
  }
}(jQuery));

function openGanttChartSettings(){
  var body = "<form role='form' name='ganttChartSettingsForm'>";
      body += "<div class='row'>";
      body += " <div class='col-xs-12 col-lg-6'>";
      body += "   <div class='checkbox'>";
      body += "     <label>";
      
      if(eval(Cookies.get('dayGrid'))){
        body += "<input type='checkbox' checked='checked' name='dayGrid'> Show Day Grid";
      }else{
        body += "<input type='checkbox' name='dayGrid'> Show Day Grid";      
      }
      
      body += "     </label>";
      body += "   </div>";
      body += "   <div class='checkbox'>";
      body += "     <label>";
      
      if(eval(Cookies.get('makeScrollable'))){
        body += "<input type='checkbox' name='makeScrollable' checked='checked'> Fixed Header";
      }else{
        body += "<input type='checkbox' name='makeScrollable'> Fixed Header";
      }
      
      body += "     </label>";
      body += "   </div>";
      body += "   <div class='checkbox'>";
      body += "     <label>";
      
      if(eval(Cookies.get('showInnerText'))){
        body += "<input type='checkbox' name='showInnerText' checked='checked'> Show Dates In Bar";
      }else{
        body += "<input type='checkbox' name='showInnerText'> Show Dates In Bar";
      }
      
      body += "     </label>";
      body += "   </div>";
      body += " </div>";
      body += " <div class='col-xs-12 col-lg-6'>";
      body += "   <div class='checkbox'>";
      body += "     <label>";

      if(eval(Cookies.get('advToolTips'))){
        body += "<input type='checkbox' name='advToolTips' checked='checked'> Show Advanced Tooltips";
      }else{
        body += "<input type='checkbox' name='advToolTips'> Show Advanced Tooltips";
      }

      body += "     </label>";
      body += "   </div>";
      body += "   <div class='checkbox'>";
      body += "     <label>";
      
      if(eval(Cookies.get('showBarText'))){
        body += "<input type='checkbox' name='showBarText' checked='checked'> Show Bar Text";
      }else{
        body += "<input type='checkbox' name='showBarText'> Show Bar Text";
      }
      
      body += "     </label> <a style='font-size:12px;' href='#' onclick=\"editBarTextOptions(); return false;\">[edit bar text]</a>";
      body += "   </div>";
      body += "   <div class='checkbox'>";
      body += "     <label>";
      
      if(eval(Cookies.get('barIsEditable'))){
        body += "<input type='checkbox' name='barIsEditable' checked='checked'> Enable Drag + Drop Editing";
      }else{
        body += "<input type='checkbox' name='barIsEditable'> Enable Drag + Drop Editing";
      }
      
      body += "     </label>";
      body += "   </div>";
      body += " </div>";
      body += "</div>";
      
      body += "<div class='row'>";
      body += " <div class='col-xs-12'>";
      body += "   <div class='checkbox'>";
      body += "     <label>";
      
      if(eval(Cookies.get('mobileOptimized'))){
        body += "<input type='checkbox' name='mobileOptimized' checked='checked'> Mobile Friendly View <small style='color:#777;'>(Recommended For Phones & Tablets)</small>";
      }else{
        body += "<input type='checkbox' name='mobileOptimized'> Mobile Friendly View (Recommended For Phones & Tablets)";
      }
      
      body += "     </label>";
      body += "   </div>";
      body += "   <div class='checkbox'>";
      body += "     <label>";
      
      if(eval(Cookies.get('UseTaskColor'))){
        body += "<input name='UseTaskColor' type='checkbox' checked='checked'> Use Active Color Profile For Task Bars";
      }else{
        body += "<input name='UseTaskColor' type='checkbox'> Use Active Color Profile For Task Bars";
      }
      
      body += "     </label>";
      body += "   </div>";
      body += " </div>";
      body += "</div>";
      
      body += "<hr />";
      body += "<strong>Enable/Disable Fields</strong>";
      body += "<div><a href='#' onclick=\"editGanttChartColumns(); return false;\">Change Fields</a></div>";
      body += "<hr />";
      body += "<strong>Column Order</strong>";
      body += "<div><a href='#' onclick=\"editGanttFieldOrder(); return false;\">Change Field Order</a></div>";      
      body += "</form>";
      
  BootstrapDialog.show({
    title: "Gantt Chart Settings",
    message: body,
    buttons:[{
      label: "<span class='hidden-xs'>Restore </span>Defaults",
      cssClass: "btn-warning",
      action: function(dialogRef){
        BootstrapDialog.confirm("Are you sure you want to reset the Gantt Chart back to default settings?", function(result){
          if(result){
            setCookie('makeScrollable', false);
            setCookie('advToolTips', true);
            setCookie('mobileOptimized', false);
            setCookie('dayGrid', true);
            setCookie('barIsEditable', true);
            setCookie('showInnerText', false);
            setCookie('showBarText', true);
            setCookie('GanttChartFieldsToShow', defaultGanttChartFields);
            setCookie('GanttChartSort', '[[1,0],[2,0]]');
            setCookie('UseTaskColor', false);
            setCookie('BarTextFields', defaultBarTextFields.join(','));
            
            dialogRef.close();
            
            //$('#Gantt').sGantt();
            loadGanttChart();
          }else{

          }
        });
      }
    },{
      label: "Cancel",
      action: function(dialogRef){
        dialogRef.close();
      }
    },{
      label: "Save<span class='hidden-xs'> Changes</span>",
      cssClass: "btn-primary",
      action: function(dialogRef){
        var ganttSettingsData = $("form[name=ganttChartSettingsForm]").serialize().split('&');
        var activeSettings = [];
        
        $.each(ganttSettingsData, function(){
          activeSettings.push(this.split('=')[0]);
        });
        
        activateNewGanttChartSettings(activeSettings, 'dayGrid');
        activateNewGanttChartSettings(activeSettings, 'makeScrollable');        
        activateNewGanttChartSettings(activeSettings, 'advToolTips');        
        activateNewGanttChartSettings(activeSettings, 'showBarText');
        activateNewGanttChartSettings(activeSettings, 'barIsEditable');
        activateNewGanttChartSettings(activeSettings, 'showInnerText');
        activateNewGanttChartSettings(activeSettings, 'mobileOptimized');
  		  activateNewGanttChartSettings(activeSettings, 'UseTaskColor');
  						
  			//$("#Gantt").sGantt();
  			loadGanttChart();
  			
        dialogRef.close();
      }
    }]
  });  
}

function activateNewGanttChartSettings(settings, name){
  if($.inArray(name, settings) != -1){
    setCookie(name, true);
    
    if(name == "mobileOptimized"){
      // disable other options that don't work well with mobile screens.
      //setCookie('advToolTips', false);
      setCookie('barIsEditable', false);
    }
    
  }else{
    setCookie(name, false);
  }
}

function editBarTextOptions(){
  var msg = "<form role='form' name='barTextFieldsForm'>";

  $.ajax({
    url: "/api/v1/fieldcaptions?fields=" + allTaskFieldCaptionNames.join(',') + "," + allJobFieldCaptionNames.join(',') + "," + allContactFieldCaptionNames.join(','),
    dataType:'json',
    success: function(names){
      if(names == 'InvalidLogin'){
        logout();
      }
      
      var fields = Cookies.get('BarTextFields').split(',');
      
      msg += "<h4>Fields to Display</h4>";
      msg += "<div class='checkbox'>";
      msg += "  <label>";
      
      if($.inArray('TaskDescription', fields) != -1){
        msg += "<input name='TaskDescription' type='checkbox' checked='checked'> " + names['TaskDescription'];
      }else{
        msg += "<input name='TaskDescription' type='checkbox'> " + names['TaskDescription'];
      }
      
      msg += "  </label>";
      msg += "</div>";
      msg += "<div class='checkbox'>";
      msg += "  <label>";
      
      if($.inArray('ContactPerson', fields) != -1){
        msg += "<input name='ContactPerson' type='checkbox' checked='checked'> " + names['ContactPerson'];
      }else{
        msg += "<input name='ContactPerson' type='checkbox'> " + names['ContactPerson'];
      }
      
      msg += "  </label>";
      msg += "</div>";
      msg += "<div class='checkbox'>";
      msg += "  <label>";
      
      if($.inArray('ContactCompany', fields) != -1){
        msg += "<input name='ContactCompany' type='checkbox' checked='checked'> " + names['ContactCompany'];
      }else{
        msg += "<input name='ContactCompany' type='checkbox'> " + names['ContactCompany'];
      }
      
      msg += "  </label>";
      msg += "</div>";
      msg += "<div class='checkbox'>";
      msg += "  <label>";
      
      if($.inArray('JobNumber', fields) != -1){
        msg += "<input name='JobNumber' type='checkbox' checked='checked'> " + names['JobNumber'];
      }else{
        msg += "<input name='JobNumber' type='checkbox'> " + names['JobNumber'];
      }
      
      msg += "  </label>";
      msg += "</div>";
      msg += "<div class='checkbox'>";
      msg += "  <label>";
      
      if($.inArray('TaskTargetStart', fields) != -1){
        msg += "<input name='TaskTargetStart' type='checkbox' checked='checked'> " + names['TaskTargetStart'];
      }else{
        msg += "<input name='TaskTargetStart' type='checkbox'> " + names['TaskTargetStart'];
      }
      
      msg += "  </label>";
      msg += "</div>";
      msg += "<div class='checkbox'>";
      msg += "  <label>";
      
      if($.inArray('TaskDuration', fields) != -1){
        msg += "<input name='TaskDuration' type='checkbox' checked='checked'> " + names['TaskDuration'];
      }else{
        msg += "<input name='TaskDuration' type='checkbox'> " + names['TaskDuration'];
      }
      
      msg += "  </label>";
      msg += "</div>";
      msg += "<div class='checkbox'>";
      msg += "  <label>";
      
      if($.inArray('TaskTargetFinish', fields) != -1){
        msg += "<input name='TaskTargetFinish' type='checkbox' checked='checked'> " + names['TaskTargetFinish'];
      }else{
        msg += "<input name='TaskTargetFinish' type='checkbox'> " + names['TaskTargetFinish'];
      }
      
      msg += "  </label>";
      msg += "</div>";
      msg += "</form>";
      msg += "<form role='form' name='barTextOrderForm'>";
      msg += "<hr />";
      msg += "<h4>Bar Text Order</h4>";
      msg += "<div style='color:#666; font-size:12px;'>Drag and drop fields to change the order. The top field appears 1st.</div><br />";
      msg += "<div>";
      msg += "<ul id='sortableFields'>";
                  
      $.each(fields, function(i){
        msg += "<li data-field-name='" + this + "'>" + names[this] + "</li>";
      });
          
      msg += "</ul>";
      msg += "</div>";
      msg += "</form>";
      
      BootstrapDialog.show({
        title: 'Edit Bar Text',
        message: msg,
        onshown: function(){
          $("#sortableFields").sortable();
          $('form[name=barTextFieldsForm] input').change(function(){
            if($(this).prop("checked")){
              var fieldToAdd = "<li data-field-name='" + $(this).attr('name') + "'>" + names[$(this).attr('name')] + "</li>";

              $('#sortableFields').append(fieldToAdd);
              $("#sortableFields").sortable();
            }else{
              $('#sortableFields li[data-field-name=' + $(this).attr("name") + ']').remove();  
              $("#sortableFields").sortable();
            }
          });
        },
        buttons: [{
          label: '<span class="hidden-xs">Restore </span>Defaults',
          cssClass: 'btn-warning',
          action: function(){
            var defaultFields = defaultBarTextFields;
            
            $('#sortableFields').html(''); 
            
            $('form[name=barTextFieldsForm] input').each(function(){
              if($.inArray($(this).attr('name'), defaultFields) != -1){
                $(this).prop('checked', true).change();
              }else{
                $(this).prop('checked', false).change();
              }
            });
            
            $("#sortableFields").sortable();
            
            setCookie('BarTextFields', defaultBarTextFields.join(','));
          }
        },{
          label: 'Cancel',
          cssClass: 'btn-default',
          action: function(dialogRef){
            dialogRef.close();
          }    
        },{
          label: 'Save<span class="hidden-xs"> Changes</span>',
          cssClass: 'btn-primary',
          action: function(dialogRef){
            var barFieldsSerializedArray = $('form[name=barTextFieldsForm]').serializeArray();
            var barFieldsToShowArray = [];
            
            $.each(barFieldsSerializedArray, function(){
              barFieldsToShowArray.push(this.name);
            });
            
            var barFieldsToShowString = barFieldsToShowArray.join(',');        
            var newBarTextOrderArray = [];
            
            $.each($("#sortableFields li"), function(i){
              newBarTextOrderArray.push($(this).data("field-name"));
            });
            
            setCookie('BarTextFields', newBarTextOrderArray.join(','));
            
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

function updateTaskCount(total) {
	if (!total) {
		$("#tCount").html("0, <span style='font-style:italic; color:#ababab;'>nothing matched your search or current filters</span>");
	} else if (eval(Cookies.get('ShowAllTasks'))) {
		$("#tCount").html("<a href='#' onclick=\"editOptions(); return false;\">" + total + "</a>");
	} else {
		$("#tCount").html(total + " (<a href='#' title='Change number of tasks to show' onclick=\"editOptions(); return false;\">Showing</a> up to " + Cookies.get('TaskLimit') + ")");
	}
}

function initGanttChartSorting() {
	$("#sGanttMain").tablesorter({
		sortList: eval(Cookies.get("GanttChartSort"))
	}).bind("sortStart", function () {

	}).bind("sortEnd", function (data) {
		setCookie("GanttChartSort", data.delegateTarget.config.sortList);
		currentPage = 1;
		loadGanttChart();
	});
}
















