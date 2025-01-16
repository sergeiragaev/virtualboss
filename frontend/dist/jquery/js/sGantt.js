/**
 * sGantt v1.5
 * http://virtualboss.com
 *
 * Use styles.css for basic styling.
 * Use jsDate.js (needed for some date calculations)
 * For event drag & drop, requires jQuery UI draggable.
 * For event resizing, requires jQuery UI resizable.
 *
 * 2014 Sean Whitescarver
 *
 * Last Updated: Thurs May. 22nd, 2014
 *
 **/
(function($){
	
 	var d = new Date();
 	var monthArray = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
 	var shortMonthArray = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];	
 	var curYear = d.getFullYear();
 	var curMonth = d.getMonth();
 	var monthName = monthArray[curMonth];
 	var daysInMonth = new Date(curYear, curMonth+1, 0).getDate();
	var events = new Array();	
	
	var defaultStart = d.getFullYear() + "-" + (curMonth+1) + "-" + d.getDate(); 	
 	var defaultEnd = d.getFullYear() + "-" + (curMonth+1) + "-" + d.getDate();
 		
	/*************************************************************
	/	DEFAULT GANTT CHART SETTINGS
	/*************************************************************/
					           
    var settings = {
   		dayWidth       : 25,  				// width in px
   		title          : 'Task Descriptions', // text above task descriptions
   		jobTitle       : 'Job #',
   		taskNumTitle   : 'Task #',
   		personTitle    : 'Contact',
   		tStartTitle    : 'Target Start',
   		year           : curYear,  			// current calendar year
       	month          : curMonth,				// 0-11 - Jan-Dec
       	time           : d.getTime(),
       	dateRange      : {
       		startDay   : 1,					// use todays date as default start (eventually)
       		endDay     : daysInMonth,  		// day of month to stop on (0-31)
       		numMonths  : 1,					// 1-12 - how many months to show
       		start      : "2013-03-01",
       		end        : "2013-09-30"
       	},
       	eventSource    : '',
       	showJob        : true,
       	showTstart     : true,
       	showTaskNum    : false,
       	advToolTips    : true,
       	showPerson     : false,
       	multiLineMode  : false,
       	fixedColumns   : false,
       	numColumns     : 0,
       	makeScrollable : false,
       	editable       : true,
       	dayGrid        : true,
       	showBarText    : false,
       	primarySort    : 'JobNumber',
       	secondarySort  : 'TaskTargetStart',
       	thisGuy        : '',
       	columnOrder    : 'sortTask,sortJob,sortTaskNum,sortPerson,sortStart',
       	showInnerText  : false
    };	
	
	/*************************************************************
	/	DEFAULT TASK SETTINGS
	/*************************************************************/
	
	var taskDefaults = {
		title	   : 'New Task',
		month	   : settings.month,
		year	   : settings.year,
		startDay   : 1,
		startMonth : 1,  				// 1-12
		barLength  : 1,
		duration   : 1,
		color      : '',
		job        : '',
		taskNum    : '',
		person     : '',
		start      : defaultStart,		// start date  (yyyy-mm-dd)  needs to be in d.getTime() - use start = Date.parse("January 31, 2013"); turns string into formatted time.
		end        : defaultEnd,		// end date	(yyyy-mm-dd)	 needs to be in d.getTime()
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
        setCookie("dayWidth", g.dayWidth, 1); 
        setCookie("start", g.dateRange.start, 1);
        setCookie("end", g.dateRange.end, 1);
        g.thisGuy = this.selector; // used for referencing main gantt container

		loadOptions(g);        

   		return this.each(function(){
   		
   			$(this).addClass("sGantt");
   			$(this).html("<span class='loading'>Loading Gantt Chart.  This may take a few moments.  <img alt='loading...' src='images/loading.gif'></span>"); 			
			var selector = this;
			d.setFullYear(g.year);
			d.setMonth(g.month);
			
			var pSortText = "";
			var sSortText = "";
			switch(g.primarySort){
				case "TaskDescription":
					pSortText = g.title;
				break;
				case "TaskTargetStart":
					pSortText = g.tStartTitle;
				break;
				case "TaskNumber":
					pSortText = g.taskNumTitle;
				break;
				case "JobNumber":
					pSortText = g.jobTitle;
				break;
				case "ContactPerson":
					pSortText = g.personTitle;
				break;
			}
			switch(g.secondarySort){
				case "TaskDescription":
					sSortText = g.title;
				break;
				case "TaskTargetStart":
					sSortText = g.tStartTitle;
				break;
				case "TaskNumber":
					sSortText = g.taskNumTitle;
				break;
				case "JobNumber":
					sSortText = g.jobTitle;
				break;
				case "ContactPerson":
					sSortText = g.personTitle;
				break;
			}				
			
			//var gOptions = createOptions(g);
			var hOptions = createHOptions(g);
					
			// HAMBURGER
			var hamburger = "";
			hamburger += "	<div class='menuWrap'>";
			hamburger += "		<img alt='' class='hMenu' id='hamMenu' src='images/hamburger.png' title='Options Menu'>";
			hamburger += "		<div class='menuDrop' id='menuD'>";
			hamburger += "			<div class='menuNotch' id='notch'></div>";
			hamburger += hOptions;
			hamburger += "		</div>";
			hamburger += "	</div>";
			hamburger += "";
			// END HAMBURGER
			
			// old menu
   			//var mainLayout = "<div class='options'><img alt='' src='images/options.png' style='width:12px;'> <a href='#' onclick='return false;'>Gantt Chart Options</a> <div id='settingsArrow'> </div> <span class='tCount'>Tasks: <span class='optionsText' id='taskCount'></span>, Primary Sort: <span class='optionsText'>"+pSortText+"</span>, Secondary Sort: <span class='optionsText'>"+sSortText+"</span></span></div><div class='opts'>"+gOptions+"</div><table class='main'><thead><tr>";
   			
   			// new hamburger menu
   			var mainLayout = "<div class='options'>"+ hamburger +"</div><div class='gttLine'> <span class='tCount'>Tasks: <span class='optionsText' id='taskCount'></span>, Primary Sort: <span class='optionsText'>"+pSortText+"</span>, Secondary Sort: <span class='optionsText'>"+sSortText+"</span></span> <button style='border-radius:3px;' class='btn btn-success btn-xs todayButton' value='Go to Today'><span class='glyphicon glyphicon-calendar'></span> Go to Today</button></div><table class='main' id='sGanttMain'><thead><tr>";
   			   			
   			mainLayout += createHeaders(g);
   			
   			var tMonth = g.month;
   			var tYear = g.year;
   			var daysInDateRange = getDaysInDateRange(g);

			// figure out todays date and, if found below, add a special marker for 'Go to Today' button scrolling
			var todaysDate = new Date();
			
			var todaysMonth = todaysDate.getMonth()+1;
			var todaysDay = todaysDate.getDate();
			// todays date in this format (2014/08/25)
			var todaysYear = todaysDate.getFullYear();
			var today = todaysDate.getFullYear() + '/' +
			    ((''+todaysMonth).length<2 ? '' : '') + todaysMonth + '/' +
			    ((''+todaysDay).length<2 ? '' : '') + todaysDay;			

			for(var j = 0; j < g.dateRange.numMonths; j++){   			
								
				if(tMonth > 11){
					tMonth = 0; 
					tYear++;		
				}
   				   				
   				var numDays = getDaysInMonth(tYear, tMonth);
				mainLayout += "<th class='ruler'>";
				
				if(j == 0){
					mainLayout += "<div class='dates_table_first'>";
				}else{
					mainLayout += "<div class='dates_table'>";
				}
				
				// should output current month in the loop to the console
				// so tMonth+1 = current month (1-12)
				// tYear = current year (2014)
				// i = current day
				// formula should be something like tYear/tMonth/i
				
				if(daysInDateRange <= 1){
					mainLayout += "<div class='dtMonths'>"+shortMonthArray[tMonth]+"</div>";
				}else{
					if((numDays - g.dateRange.startDay < 5  && j == 0) || (j == g.dateRange.numMonths-1 && g.dateRange.endDay < 5)){
	   					if(g.dayWidth < 25){
	   						mainLayout += "<div class='dtMonths'></div>";
	   					}else{
							mainLayout += "<div class='dtMonths'>"+shortMonthArray[tMonth]+"</div>";	
						}
	   				}else{
	   					mainLayout += "<div class='dtMonths'>"+monthArray[tMonth]+" "+tYear+"</div>";	
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

					mainLayout += "<ul style='width:"+monthWidth+"px;'>";
					for(var i = g.dateRange.startDay; i <= numDays; i++){
						if(g.dayWidth >= 15){
							if(tYear == todaysYear && tMonth == todaysMonth-1 && i == todaysDay){
								mainLayout += "<li class='dtDays Today'>"+i+"</li>";
							}else{
								mainLayout += "<li class='dtDays'>"+i+"</li>";
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
					
				if(j != 0 && j != g.dateRange.numMonths-1){
					mainLayout += "<ul style='width:"+monthWidth+"px;'>";
					for(var i = 1; i <= numDays; i++){
						if(g.dayWidth >= 15){
							//console.log(tYear+"/"+tMonth+"/"+i+" ("+todaysYear+"/"+todaysMonth+"/"+todaysDay+")");
							if(tYear == todaysYear && tMonth == todaysMonth-1 && i == todaysDay){
								mainLayout += "<li class='dtDays Today'>"+i+"</li>";
							}else{
								mainLayout += "<li class='dtDays'>"+i+"</li>";
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
					mainLayout += "<ul style='width:"+monthWidth+"px;'>";
					for(var i = 1; i <= g.dateRange.endDay; i++){
						if(g.dayWidth >= 15){
							if(tYear == todaysYear && tMonth == todaysMonth-1 && i == todaysDay){
								mainLayout += "<li class='dtDays Today'>"+i+"</li>";
							}else{
								mainLayout += "<li class='dtDays'>"+i+"</li>";
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
			var sourceURL = g.eventSource+"?start="+Date.parse(formattedStart)/1000+"&end="+Date.parse(g.dateRange.end)/1000+"&_="+g.time+"&Sort1="+g.primarySort+"&Sort2="+g.secondarySort;

			if(g.eventSource != ""){
				$.ajax({
					url: sourceURL,
					async: true,
					dataType: "json",
					error: function(e,status, error){
						alert(error);
						console.log(e.responseText);
					},
					success: function(e){
						var t = e;
						var loadConfirm = true;
						if(t.length > 5000){
							loadConfirm = confirm("Loading this Gantt Chart may take several minutes. To reduce load times, use the filter options to only show what you want to see. (" + t.length + " tasks)");
						}
						
						if(t.length == 0){
							alert("There are no tasks for this filter");
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
								for(var i in t){							
									var date = t[i].start.split("-");  // yyyy-mm-dd (with leading 0s)
									mainLayout += addTask({
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
								mainLayout += "</tbody></table>";
							}
							
							// AFTER GANTT CHART AND TASKS ARE LOADED - APPLY
							
							$(selector).html(mainLayout);		
														
							$(".dtDays").css("width",g.dayWidth+"px");
														
							initOptions(selector);
							
							$("#taskCount").html(t.length);
							
							if(g.makeScrollable){
							  applyScrollableHeader(g);
							}
							
							if(g.editable){
							  makeEditable(g); 
							}
							
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
														
							if(g.advToolTips){
								$(".bar").tooltip({
									tooltipClass : 'tTip',
									track        : true,
									position     : { 
										my: "left+20 top+15", 
										at: "right center" 
									},
									content      : function(e, ui){										
										var view = "";
										$.ajax({
											url      : "/api/v1/FetchTask?id="+$(this).parent().attr("id"),
											dataType : "json",
											async    : false,
											success  : function(data){
												
												view = "<div class='tipDesc'>"+ data[0].TaskDescription +"</div>";
												view += "<div class='ttWrap'>";
												var td = data[0].start.split("-");
												var sd = new Date(td[0]+"/"+td[1]+"/"+td[2]++); // need to add +1 to the day (td[2]) value or else it reads one day behind when converting to a new Date object.
												
												var n = sd.toDateString();
												
												view += "<div class='smallerTooltipText'>Start: "+ n +"</div>";
												
												var dur = data[0].duration;
												var days = dur == 1 ? "day" : "days";
												
												view += "<div class='smallerTooltipText'>Duration: " + data[0].duration + " " + days + "</div>";
												
												td = data[0].end.split("-");
												sd = new Date(td[0]+"/"+td[1]+"/"+td[2]++);
												n = sd.toDateString();
												
												view += "<div class='smallerTooltipText'>End: "+ n +"</div>";
												var notes = data[0].taskNotes.replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/g,'<br />');
																								
												if(data[0].taskNotes != "" && data[0].taskNotes != null && data[0].taskNotes != " "){
													view += "<div style='padding-top:5px; padding-bottom:0px; margin-bottom:0px; font-size:12px; font-family:inherit;'>";
													view += "<span class='glyphicon glyphicon-comment'></span> Notes:</p><div class='tipNotes'>"+ notes +"</div>";
												}
												view += "</div>";
										  },
											error   : function(e, status, err){
												console.log(err);
											} 										
										});
										return view; 								
                	}
								});
							}
							
							
							
							// HAMBURGER MENU LOGIC
							
							$(".hMenu").click(function(){
								if($(this).hasClass("hMenuActive")){
									$(this).removeClass("hMenuActive");
									$(".menuDrop").toggle("fade",{ duration: 200 });
								}else{
									$(this).addClass("hMenuActive");
									$(".menuDrop").toggle("fade",{ duration: 200 });
								}
							});
							
							$(".dropList").on("click", "li", function (e) {
						   		var input = $(this).children("input");
						    	$(input).prop("checked", !$(input).prop("checked")); // proper way to check a checkbox
							});
						
							$(".dropList li :checkbox").click(function (e) {
							    e.stopPropagation();
							});
							
							$(document).mouseup(function(e){
							    var c1 = $(".hMenu");
							    var c2 = $(".menuDrop");
							    var c3 = $("input[type=checkbox]");
							    if(c1.has(e.target).length === 0 && e.target.id != "notch" && e.target.id != "hamMenu" && e.target.id != "menuD" && e.target.tagName != "LI" && e.target.tagName != "INPUT" && e.target.tagName != "BUTTON" && e.target.tagName != "UL" && e.target.tagName != "SELECT" && e.target.tagName != "OPTION" && e.target.tagName != "TD" && e.target.className != "optionBlock" && e.target.className != "colOrderTitle" && e.target.className != "colSortBlock" && e.target.tagName != "HR"){
									if(c1.hasClass("hMenuActive")){
										c1.removeClass("hMenuActive");
										c2.toggle("fade",{ duration: 200 });
										console.log(e.target.className);
									}        
							    }  
							});
							
							// END HAMBURGER MENU LOGIC
							
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
								$(".dayGrid").css("background-position",offsetDayGrid);}else{$(".dayGrid").css("background-image","none");
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
	
	function addTask(options){
								
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
		
		var newRow = createRow(g,t);
		
		if(g.dayWidth >= 15){  // IF DAY GRID IS TOO TINY TO DISPLAY BOTH NUMBERS, DO NOT SHOW THE DURATION ON THE TASK BAR
			newRow += "<td class='dayGrid' colspan='"+g.dateRange.numMonths+"'><div class='barWrap'><div class='bar' id='"+t.taskID+"' style='width:"+bWidth+"px; left:"+lPos+"px; background:"+t.color+";'><a href='"+t.url+"' title='"+t.taskNotes+"'></a><div class='textOnBar'>"+tOnBar+"</div><div class='barLeft'></div><div class='barRight'></div>";			
			if(g.showInnerText){newRow+="<span class='startInner' id='"+t.taskID+"_startInner'>"+t.startDay+"</span><span class='durInner' id='"+t.taskID+"_durInner'>"+parseInt(t.end.split('-')[2],10)+"</span>";}
			newRow += "</div>";
		}else{
			newRow += "<td class='dayGrid' colspan='"+g.dateRange.numMonths+"'><div class='barWrap'><div class='bar' id='"+t.taskID+"' style='width:"+bWidth+"px; left:"+lPos+"px; background:"+t.color+";'><a href='"+t.url+"' title='"+t.taskNotes+"'></a><div class='textOnBar'>"+tOnBar+"</div><div class='barLeft'></div><div class='barRight'></div>";
			if(g.showInnerText){newRow+="<span class='startInner' id='"+t.taskID+"_startInner'>"+t.startDay+"</span><span class='durInner'></span>";}
			newRow += "</div>";
		}
		
		if(g.fixedColumns){
			//newRow += "</div></td> <td class='extras'></td> <td class='extras'></td> <td class='extras'></td></tr>";
			newRow += figureOutExtraColumns(g.dateRange.numMonths);
		}else{
			newRow += "</div></td></tr>";
		}

		return newRow;
		//return this;  // to restore chainability, use "return this"
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
			var newRow = "<div class='barWrap' style='width:100%; position:absolute;'><div class='bar' id='"+t.taskID+"' style='width:"+bWidth+"px; left:"+lPos+"px; background:"+t.color+";'><a href='"+t.url+"' title='"+t.taskNotes+"'></a><div class='textOnBar'>"+tOnBar+"</div><div class='barLeft'></div><div class='barRight'></div>";			
			if(g.showInnerText){newRow+="<span class='startInner' id='"+t.taskID+"_startInner'>"+t.startDay+"</span><span class='durInner' id='"+t.taskID+"_durInner'>"+parseInt(t.end.split('-')[2],10)+"</span>";}
			newRow += "</div>";
		}else{
			var newRow = "<div class='barWrap' style='width:100%; position:absolute;'><div class='bar' id='"+t.taskID+"' style='width:"+bWidth+"px; left:"+lPos+"px; background:"+t.color+";'><a href='"+t.url+"' title='"+t.taskNotes+"'></a><div class='textOnBar'>"+tOnBar+"</div><div class='barLeft'></div><div class='barRight'></div>";
			if(g.showInnerText){newRow+="<span class='startInner' id='"+t.taskID+"_startInner'>"+t.startDay+"</span><span class='durInner'></span>";}
			newRow += "</div>";
		}
		

		newRow += "</div>";
		return newRow;
		
		
		//return this;  // to restore chainability, use "return this"
	}
	
	/*************************************************************
	/	RESIZE THE GANTT CHART BASED ON SLIDER
	/*************************************************************/
	
	$.fn.resize = function(options){
		var o = $.extend(settings, options);
		
		$(this.selector+" .dtDays").each(function(){
			$(this).css("width",o.dayWidth);
		});
		
		$(this.selector+" .ruler ul").each(function(){
			var days = $(this).children("li").size();
			var rulerWidth = getWidth(days);
			$(this).css("width",rulerWidth+"px");
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
				//containment : g.thisGuy,
				containment : 'parent',
				zIndex      : 1,
				/*revert      : function(e){
				    $(this).data("uiDraggable").originalPosition = {
		                top : 4,
		                left : Math.round(startPos.left),
		            };	
		            // detects whether the task bar was dragged along the y axis.  Triggers the revert.  We assume this action is in response to an attempted task link. (or dragged too far left)
		            // this does not fire if the dragged task bar was dropped on a valid target.
		            if($(this).position().top < 4 || $(this).position().top > 4 || $(this).position().left < 1){
		            	return true;
		            }else{
		             	return e;
		            }
				},*/
				grid        : [g.dayWidth+1, g.dayWidth+1],
				start       : function(e, ui){
					if(g.advToolTips){$(".bar").tooltip("disable");}
					//startPos = ui.helper.position();
					if(g.showBarText){$(this).children(".textOnBar").fadeOut('fast');}
				},
				stop        : function(e, ui){
					if(g.advToolTips){$(".bar").tooltip("enable");}
					if(g.showBarText){$(this).children(".textOnBar").fadeIn('fast');}
					var posL = Math.round($(this).position().left);
					var start = Math.round((posL/(g.dayWidth+1)))+1;
					var duration = Math.round($(this).outerWidth()/(g.dayWidth+1));

					var nStartDate = getNewStartDate(g,start);  			// gets the new start date when dragging a bar
					var nEndDate = getNewEndDate(g, start, duration);	
					// changed from ui.position.left to posL because posL was using the actual current location.
					if(posL != Math.round(ui.originalPosition.left)){		// save task
						var id = $(this).attr("id");
						$.ajax({
							async   : true,
							dataType: 'json',
							url     : '/api/v1/ganttupdate?taskId='+id+'&Start='+Format(nStartDate, 'yyyy-mm-dd')+"&End="+Format(nEndDate, 'yyyy-mm-dd'),
							success : function(e){
								var start = "",
								end = "",
								tid = "",
								duration = "";
								
								for(var i in e){
									start = e[i].start;
									end = e[i].end;
									duration = e[i].duration;
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
									
									var width = getBarLength(e[i].start, e[i].end);
									width = getBarWidth(width, g.dayWidth);
									$("#"+tid).css("left", leftPos+"px").css("width",width+"px");
									
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
					if(g.advToolTips){
            $(".bar").tooltip("disable");
					}
				},
				stop        : function(e, ui){
					if(g.advToolTips){
					  $(".bar").tooltip("enable");
					}

					var duration = Math.round($(this).outerWidth()/(g.dayWidth+1)); // changed to use outerWidth() method instead of ui.size.width because it is way more accurate/consistent
					var ps = $(this).position().left;
					var st = Math.round((ps/(g.dayWidth+1)))+1;
					var nStartDate = getNewStartDateResize(g, st); // returns the new start date for when resizing a bar
					var nEndDate = getNewEndDate(g, st, duration); // returns the new end date
					
					var direction = $(this).data('ui-resizable').axis; // 'e' or 'w'

					var id = $(this).attr("id");
					var updateURL = "/api/v1/ganttupdate?taskId="+id+"&";
					var save = false;

					if(direction == "w"){
						updateURL += "Start="+Format(nStartDate, 'yyyy-mm-dd');
					}else{
						updateURL += "End="+Format(nEndDate, 'yyyy-mm-dd');
					}
					if(!save){
						// save the task
						$.ajax({
							async   : true,
							url     : updateURL,
							dataType: 'json',
							success : function(e){
								
								var start = "",
								end = "",
								tid = "",
								duration = "";
								
								for(var i in e){
									
									start = e[i].start;
									end = e[i].end;
									duration = e[i].duration;
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
									
									//$("#"+tid).children(".dragDate").html(duration);
									
									start = Date.parse(start.replace(/-/g,'/'))/1000;
    						    	var leftPos = Math.round((start - (Date.parse(g.dateRange.start)/1000)) / 86400);							    	
    						    	leftPos = ((leftPos) * g.dayWidth) + (leftPos + 1);
									
									var width = getBarLength(e[i].start, e[i].end);
									width = getBarWidth(width, g.dayWidth);
									$("#"+tid).css("left", leftPos+"px").css("width",width+"px");
									
								}
							}
						});						
					}else{
						// revert the task to its original width and position.
						$(this).animate({
							left: ui.originalPosition.left, 
							width: ui.originalSize.width,
							done: function(){
							}
						},500);
					}
				}
			}).droppable({
				hoverClass: "preLinkHover",
				tolerance : 'touch',
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

	function getTextOnBar(g, t){
		var sfDate = t.start.split("-");
		var efDate = t.end.split("-");
		sfDate = sfDate[1] + "/" + sfDate[2] + "/" + parseInt(sfDate[0],10)%100;
		efDate = efDate[1] + "/" + efDate[2] + "/" + parseInt(efDate[0],10)%100;

		var text = "";
    	var cOrder = g.columnOrder.split(',');

       	for(var i in cOrder){
    		switch(cOrder[i]){
    			case "sortTask":
					text += t.title;
					if(i < cOrder.length-1){text+=", ";}
    			break;
    			case "sortJob":
    			   	if(g.showJob){
						text += "job:"+t.job;
						if(i < cOrder.length-1){text+=", ";}
    				}
    			break;
    			case "sortTaskNum":
			    	if(g.showTaskNum){
						text += "task#:"+t.taskNum;
						if(i < cOrder.length-1){text+=", ";}
			    	}		
    			break;
    			case "sortPerson":
			    	if(g.showPerson){
						text += "contact:"+t.person;
						if(i < cOrder.length-1){text+=", ";}
			    	}  				
    			break;
    			case "sortStart":
			    	if(g.showTstart){
						var tstart = t.dateHolder.split("-");
						text += "start:<span id='"+t.taskID+"_barStart'>" + sfDate + "</span>, dur:<span id='"+t.taskID+"_barDur'>" + t.duration + "</span>, end:<span id='"+t.taskID+"_barEnd'>" + efDate + "</span>";
						if(i < cOrder.length-1){text+=", ";}
			    	}		
    			break;
    		}
    	}
		
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
    	
       	for(var i in cOrder){
    		switch(cOrder[i]){
    			case "sortTask":
    				header += "<th class='tTitle' style='text-align:left;'><div id='taskHeader'>"+g.title+"</div></th>";
					g.numColumns++;					
    			break;
    			case "sortJob":
    			   	if(g.showJob){
    					header += "<th class='tTitle'><div id='jobHeader'>"+g.jobTitle+"</div></th>";
    					g.numColumns++;
    				}
    			break;
    			case "sortTaskNum":
			    	if(g.showTaskNum){
			    		header += "<th class='tTitle'><div id='tnHeader'>"+g.taskNumTitle+"</div></th>";
			    		g.numColumns++;
			    	}		
    			break;
    			case "sortPerson":
			    	if(g.showPerson){
			    		header += "<th class='tTitle' style='text-align:left;'><div id='personHeader'>"+g.personTitle+"</div></th>";
			    		g.numColumns++;
			    	}  				
    			break;
    			case "sortStart":
			    	if(g.showTstart){
			    		header += "<th class='tTitle'><div id='tsHeader'>"+g.tStartTitle+"</div></th>";
			    		g.numColumns++;
			    	}		
    			break;
    		}
    	}

    	return header;
    }
    
    function createRow(g,t){
    
    	var row = "<tr>";
    	var cOrder = g.columnOrder.split(',');

       	for(var i in cOrder){
    		switch(cOrder[i]){
    			case "sortTask":
					row += "<td class='taskDescription'><div class='tDescription'><a href='#"+t.taskID+"' class='scrollRight' title='Scroll over to view this task'><span class='glyphicon glyphicon-forward'></span><img style='display:none;' src='images/scrollright.png' class='jumpImage'></a><a href='"+t.url+"'>"+t.title+"</a></div></td>";
    			break;
    			case "sortJob":
    			   	if(g.showJob){
						row += "<td class='taskDescription'><div class='tJob'>"+t.job+"</div></td>";
    				}
    			break;
    			case "sortTaskNum":
			    	if(g.showTaskNum){
						row += "<td class='taskDescription'><div class='tTaskNum'>"+t.taskNum+"</div></td>";
			    	}		
    			break;
    			case "sortPerson":
			    	if(g.showPerson){
						row += "<td class='taskDescription'><div class='tPerson'>"+t.person+"</div></td>";
			    	}  				
    			break;
    			case "sortStart":
			    	if(g.showTstart){
						var tstart = t.dateHolder.split("-");
						row += "<td class='taskDescription'><div id='"+t.taskID+"_startInfo' class='tStart'>"+tstart[1]+"/"+tstart[2]+"/"+tstart[0]%100+"</div></td>";
			    	}		
    			break;
    		}
    	}
    		
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
						//var tstart = t.dateHolder.split("-");
						//row += "<td class='taskDescription'><div id='"+t.taskID+"_startInfo' class='tStart'>"+tstart[1]+"/"+tstart[2]+"/"+tstart[0]%100+"</div></td>";
			    	}		
    			break;
    		}
    	}
    		
		return row;
		
    }
      
    function setDateRange(g){
	    var dRangeS = g.dateRange.start.split("/");
        var dRangeE = g.dateRange.end.split("/");
        g.dateRange.startDay = parseInt(dRangeS[2],10);
        g.dateRange.endDay = parseInt(dRangeE[2],10);
        g.month = parseInt(dRangeS[1],10)-1;
    	g.year = parseInt(dRangeS[0],10);	
        g.dateRange.numMonths = getMonthsToShow(g);
    }
    
    function getDaysInDateRange(g){
    	var oneDay = 24*60*60*1000;	
    	
    	return Math.round(((Date.parse(g.dateRange.end)-Date.parse(g.dateRange.start))/oneDay))+1;
    }
       
    function getMonthsToShow(g){
    	var ms = DateDiff("m",g.dateRange.start, g.dateRange.end);
       	ms++;

		return ms;  
    }
    
    function setCookie(c_name, value, exdays){
		var exdate = new Date();
		exdate.setDate(exdate.getDate() + exdays);
		var c_value = escape(value) + ((exdays==null) ? "" : "; expires="+exdate.toUTCString());
		document.cookie = c_name + "=" + c_value;
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
    	var oString = "<div class='optionBlock'><ul class='dropList'>";
    	if(g.dayGrid){
       		oString += "<li><input type='checkbox' id='gridOption' checked='checked'> Show Day Grid</li>";
    	}else{
    		oString += "<li><input type='checkbox' id='gridOption'> Show Day Grid</li>";
    	}
    	if(g.showJob){
    		oString += "<li><input type='checkbox' id='jobOption' checked='checked'> Show "+g.jobTitle+"</li>";
    	}else{
    		oString += "<li><input type='checkbox' id='jobOption'> Show "+g.jobTitle+"</li>";
    	}
    	if(g.showPerson){
    		oString += "<li><input type='checkbox' id='personOption' checked='checked'> Show "+g.personTitle+"</li>";
    	}else{
    		oString += "<li><input type='checkbox' id='personOption'> Show "+g.personTitle+"</li>";
    	}
    	if(g.makeScrollable){
    		oString += "<li><input type='checkbox' id='scrollableOption' title='Makes the Gantt Chart scrollable with a fixed header - disable to greatly improve load times' checked='checked'> Fixed Header</li>";
    		//oString += "<div style='padding-left:30px;'>Height: <input type='text' id='fixedGanttHeight' value='530'> px</div>";
    	}else{
    		oString += "<li><input type='checkbox' id='scrollableOption' title='Makes the Gantt Chart scrollable with a fixed header - disable to greatly improve load times'> Fixed Header</li>";
    		//oString += "<div style='padding-left:30px;'>Height: <input type='text' id='fixedGanttHeight' readonly='readonly' value='530'> px</div>";
    	}
    	if(g.showInnerText){
    		oString += "<li><input type='checkbox' id='innerTextOption' checked='checked' title='Shows the start and end dates inside the task bar'> Show dates in bar</li>";
    	}else{
    		oString += "<li><input type='checkbox' id='innerTextOption' title='Shows the start and end dates inside the task bar'> Show dates in bar</li>";
    	}

		/* MULTI-LINE MODE (NOT FINISHED - WORK WAS LOST...)
    	if(g.multiLineMode){
    		oString += "<li><input type='checkbox' id='multiLineOption' checked='checked' title='Shows multiple tasks per line for each person'> Multi-Line Mode</li></ul></div>";
    	}else{
    		oString += "<li><input type='checkbox' id='multiLineOption' title='Shows multiple tasks per line for each person'> Multi-Line Mode</li></ul></div>";
    	}
		
		// FIXED COLUMNS - IS WORKING FOR THE MOST PART.
		
		if(g.fixedColumns){
			oString += "<div class='optionBlock'><ul class='dropList'><li><input type='checkbox' id='fixedColumnsOption' title='Fix the columns on the left side of the screen when scrolling left to right' checked='checked'> Fix Columns When Scrolling </li>";
		}else{
			oString += "<div class='optionBlock'><ul class='dropList'><li><input type='checkbox' id='fixedColumnsOption' title='Fix the columns on the left side of the screen when scrolling left to right'> Fix Columns When Scrolling </li>";
		}
		*/
		
		oString += "</ul></div><div class='optionBlock'><ul class='dropList'>"; // remove this when adding back the multiline and fixed columns
		
    	if(g.advToolTips){
    		oString += "<li><input type='checkbox' id='toolTipOption' title='Show more detailed task information when hovering over a task bar' checked='checked'> Show Advanced Tooltips </li>";
    	}else{
    		oString += "<li><input type='checkbox' id='toolTipOption' title='Show more detailed task information when hovering over a task bar'> Show Advanced Tooltips </li>";
    	}    		
    	if(g.showTstart){
    		oString += "<li><input type='checkbox' id='tStartOption' checked='checked'> Show "+g.tStartTitle+"</li>";
    	}else{
    		oString += "<li><input type='checkbox' id='tStartOption'> Show "+g.tStartTitle+"</li>";
    	}
    	if(g.showTaskNum){
    		oString += "<li><input type='checkbox' id='tNumOption' checked='checked'> Show "+g.taskNumTitle+"</li>";
    	}else{
    		oString += "<li><input type='checkbox' id='tNumOption'> Show "+g.taskNumTitle+"</li>";
    	}
    	if(g.showBarText){
	    	oString += "<li><input type='checkbox' id='barTextOption' checked='checked'> Show text next to bar</li>";
    	}else{
    		oString += "<li><input type='checkbox' id='barTextOption'> Show text next to bar</li>";
    	}
		if(g.editable){	
			oString += "<li><input type='checkbox' title='Enables drag and drop for task bars - uncheck to increase performance' id='barEditOption' checked='checked'> Edit task bars with drag and drop</li></ul></div>";
		}else{
			oString += "<li><input type='checkbox' title='Enables drag and drop for task bars - uncheck to increase performance' id='barEditOption'> Edit task bars with drag and drop</li></ul></div>";
		}
		oString += "<hr style='margin-bottom:0px; padding-bottom:0px;' /><div class='optionBlock'><table class='sortTable'><tr><td>Primary Sort</td><td>Secondary Sort</td></tr><tr><td style='padding-right:15px;'><select id='primarySort' class='form-control'><option value='TaskDescription'>"+g.title+"</option><option value='TaskTargetStart'>"+g.tStartTitle+"</option><option value='TaskNumber'>"+g.taskNumTitle+"</option><option value='JobNumber'>"+g.jobTitle+"</option><option value='ContactPerson'>"+g.personTitle+"</option></select></td><td><select class='form-control' id='secondarySort'><option value='TaskDescription'>"+g.title+"</option><option value='TaskTargetStart'>"+g.tStartTitle+"</option><option value='TaskNumber'>"+g.taskNumTitle+"</option><option value='JobNumber'>"+g.jobTitle+"</option><option value='ContactPerson'>"+g.personTitle+"</option></select></td></tr></table></div>";

    	
    	oString += "<div class='optionBlock' style='white-space:nowrap;'><div class='colSortBlock'><div class='colOrderTitle'>Column Order (drag box left or right to change the column order)</div>";
    	oString += "<ul id='colSort'>";
    	
    	var cOrder = g.columnOrder.split(',');
    	
    	for(var i in cOrder){	
    		switch(cOrder[i]){
    			case "sortTask":
    				oString += "<li id='sortTask'>"+g.title+"</li>";	
    			break;
    			case "sortJob":
    				oString += "<li id='sortJob'>"+g.jobTitle+"</li>";
    			break;
    			case "sortTaskNum":
    				oString += "<li id='sortTaskNum'>"+g.taskNumTitle+"</li>";
    			break;
    			case "sortPerson":
    				oString += "<li id='sortPerson'>"+g.personTitle+"</li>";
    			break;
    			case "sortStart":
    				oString += "<li id='sortStart'>"+g.tStartTitle+"</li>";
    			break;
    		}
    	}
    		
    	oString += "</ul></div><hr /><button id='applyGanttSettings' class='btn btn-primary btn-sm' value='Apply Settings'><span class='glyphicon glyphicon-ok'></span>  Apply Settings</button> <button class='btn btn-primary btn-sm' id='restoreDefaults' value='Restore Defaults'><span class='glyphicon glyphicon-refresh'></span> Restore Defaults</button></div>";
    	return oString;
    }
    
    function loadOptions(g){
        var cookies = getCookies();
        
        g.multiLineMode = (cookies['multiLineMode']) ? (cookies['multiLineMode'] == "true") : false;
        
        //if(cookies['multiLineMode']){
        //	g.multiLineMode = (cookies['multiLineMode'] == "true");
        //}
        if(cookies['fixedColumns']){
        	g.fixedColumns = (cookies['fixedColumns'] == "true");
        }
        if(cookies['dayGrid']){
        	g.dayGrid = (cookies['dayGrid'] == "true");
        }
		if(cookies['showPerson']){
			g.showPerson = (cookies['showPerson'] == "true");
		}
		if(cookies['makeScrollable']){
			g.makeScrollable = (cookies['makeScrollable'] == "true");
		}
		if(cookies['showJob']){
			g.showJob = (cookies['showJob'] == "true");
		}
		if(cookies['showTaskNum']){
			g.showTaskNum = (cookies['showTaskNum'] == "true");
		}
    	if(cookies['showBarText']){
    		g.showBarText = (cookies['showBarText'] == "true");
    	}
    	if(cookies['showTstart']){
    		g.showTstart = (cookies['showTstart'] == "true");
    	}
    	if(cookies['primarySort']){
    		g.primarySort = cookies['primarySort'];
    	}else{
    		setCookie("primarySort",g.primarySort,30);
    	}
    	if(cookies['advToolTips']){
    		g.advToolTips = (cookies['advToolTips'] == "true");
    	}
    	if(cookies['secondarySort']){
    		g.secondarySort = cookies['secondarySort'];
    	}else{
    		setCookie("secondarySort",g.secondarySort,30);
    	}
    	if(cookies['barIsEditable']){
    		g.editable = (cookies['barIsEditable'] == "true");
    	}else{
    		setCookie("barIsEditable",g.editable,30);
    	}
    	if(cookies['columnOrder']){
    		g.columnOrder = cookies['columnOrder'];
    	}else{
    		setCookie("columnOrder",g.columnOrder,30);
    	}
    	if(cookies['showInnerText']){
    		g.showInnerText = (cookies['showInnerText'] == "true");
    	}else{
    		setCookie("showInnerText",g.showInnerText,30);
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
			setCookie("multiLineMode",$("#multiLineOption").is(":checked"),30);
			setCookie("fixedColumns", $("#fixedColumnsOption").is(":checked"),30);
			setCookie("dayGrid",$("#gridOption").is(":checked"),30);
			setCookie("showPerson",$("#personOption").is(":checked"),30);
			setCookie("makeScrollable",$("#scrollableOption").is(":checked"),30);
			setCookie("advToolTips",$("#toolTipOption").is(":checked"),30);
			setCookie("showJob",$("#jobOption").is(":checked"),30);
			setCookie("showTaskNum",$("#tNumOption").is(":checked"),30);
			setCookie("showBarText",$("#barTextOption").is(":checked"),30);
			setCookie("showTstart",$("#tStartOption").is(":checked"),30);
			setCookie("primarySort",$("#primarySort").val(),30);
			setCookie("secondarySort",$("#secondarySort").val(),30);
			setCookie("barIsEditable",$("#barEditOption").is(":checked"),30);
			setCookie("showInnerText",$("#innerTextOption").is(":checked"),30);
			var sortIDs = $("#colSort").sortable("toArray");
			setCookie("columnOrder",sortIDs,30);
						
			$(gantt).sGantt();
			return false;
		});        
    }
    
    function applyScrollableHeader(g){

    	var cOrder = g.columnOrder.split(',');
    	var tdWidth = "", jWidth = "", tnWidth = "", pWidth = "", tsWidth = "";
    	
       	for(var i in cOrder){
    		switch(cOrder[i]){
    			case "sortTask":
					tdWidth = $("#taskHeader").width()+parseInt(i,10);
    			break;
    			case "sortJob":
    			   	if(g.showJob){
						jWidth = $("#jobHeader").width()+parseInt(i,10);
    				}
    			break;
    			case "sortTaskNum":
			    	if(g.showTaskNum){
						tnWidth = $("#tnHeader").width()+parseInt(i,10);
			    	}		
    			break;
    			case "sortPerson":
			    	if(g.showPerson){
						pWidth = $("#personHeader").width()+parseInt(i,10);
			    	}  				
    			break;
    			case "sortStart":
			    	if(g.showTstart){
						tsWidth = $("#tsHeader").width()+parseInt(i,10);
			    	}		
    			break;
    		}
    	}
    		
		var dayGridWidth = $(".dayGrid").width();
							
		$("thead > tr, tbody").addClass("makeScrollable");
		$("#taskHeader, .tDescription").css("width",tdWidth);
		$("#jobHeader, .tJob").css("width",jWidth);
		$("#tnHeader, .tTaskNum").css("width",tnWidth);
		$("#personHeader, .tPerson").css("width",pWidth);
		$("#tsHeader, .tStart").css("width",tsWidth);
		$(".dayGrid").css("width",dayGridWidth);
    	
    }
    
}(jQuery));





















