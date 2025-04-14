/******************************************************************/
function openBasicFilters(view){
$.ajax({
  url: '/api/v1/fieldcaptions?fields=TaskMarked,ContactPerson,JobNumber',
  dataType:'json',
  success: function(names){
    if(names == 'InvalidLogin'){
      logout();
    }
    
  if(Cookies.get("TaskFilters")){
    var filtersString = Cookies.get("TaskFilters");
  }else{
    var filtersString = defaultTaskFilters;
  }

  var filtersObj = JSON.parse('{"' + decodeURI(filtersString).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g,'":"') + '"}');
  
  // Status
  var msg = "";
      msg += "<form role='form' name='basicFiltersForm'>";
      msg += "<h4 style='margin-bottom:0px;'>Filter By Status</h4>";
      msg += "<div class='row'>";
      msg += "  <div class='col-xs-3'>";
      msg += "    <div class='checkbox'>";
      msg += "      <label class='statusActive'>";
   
  // Active
  if(filtersObj.IsActive){
    msg += "<input type='checkbox' name='IsActive' checked='checked'> Active";
  }else{
    msg += "<input type='checkbox' name='IsActive'> Active";
  }
  
  msg += "  </label>";
  msg += "</div>";
  msg += "</div>";
  msg += "<div class='col-xs-3'>";
  msg += "  <div class='checkbox'>";
  msg += "    <label class='statusDone'>";

  // Done
  if(filtersObj.IsDone){
    msg += "<input type='checkbox' name='IsDone' checked='checked'> Done";
  }else{
    msg += "<input type='checkbox' name='IsDone'> Done";
  }
  
  msg += "  </label>";
  msg += "</div>";
  msg += "</div>";
  msg += "<div class='col-xs-3'>";
  msg += "  <div class='checkbox'>";
  msg += "    <label>";
  
  // Marked
  if(filtersObj.IsMarked){
    msg += "<input type='checkbox' name='IsMarked' checked='checked'> " + names['TaskMarked'];
  }else{
    msg += "<input type='checkbox' name='IsMarked'> " + names['TaskMarked'];
  }
  
  msg += "  </label>";
  msg += "</div>";
  msg += "</div>";
  msg += "</div>";
  //msg += "<br />";
  
  //msg += "<h4>Filter By Group</h4>";
    
  msg += "<hr style='margin-top:5px;' />";  
    
  msg += "<h4>Filter By " + names['ContactPerson'] + " <i id='filtersContactSpinIcon' style='margin-left:10px; font-size:14px; font-weight:normal; color:#0066ff;' class='fa fa-circle-o-notch fa-spin hidden'></i></h4>";
  
  // Filter  By Contact
  msg += "<div class='row'>";
  msg += "  <div class='col-xs- 12 col-lg-8'>";
  msg += "    <div class='input-group'>";
  msg += "      <input class='form-control' value='' placeholder=\"Search " + names.ContactPerson + " List\" id='filtersContactSearch'>";
  msg += "      <span class='input-group-btn'><button type='button' id='filtersContactListSearchBtn' class='btn btn-default'>Search...</button></span>";
  msg += "    </div>";
  msg += "  </div>";
  msg += "</div>";
  msg += "<div class='input-group' style='margin-top:7px;'>";
  msg += "  <span class='input-group-btn'><button class='btn btn-default' type='button' onclick=\"$('#cFilter').val('');\" title='Reset Filter'><i class='fa fa-eraser'></i></button></span>";
  msg += "  <select class='form-control' name='ContactIds' id='cFilter'><option value=''>Choose " + names.ContactPerson + "</option></select>";
  msg += "</div>";
  msg += "  <div class='checkbox'>";
  msg += "    <label title='Can cause performance issues when dealing with thousands of records'>";
  
  if(Cookies.get("TaskFiltersContactAutoLoad") === "true"){
    msg += "<input type='checkbox' checked='checked' id='filtersContactAutoLoad'> Load all Contacts";
  }else{
    msg += "<input type='checkbox' id='filtersContactAutoLoad'> Load all Contacts";
  }
  
  msg += "    </label>";
  msg += "<button class='btn btn-primary' style='float:right;' type='button' onclick=\"applyFilters('" + view + "');\">Apply Filter</button>"; 
  msg += "  </div>";
      
  // Filter By Job       
  msg += "<h4 style='margin-top:15px;'>Filter By " + names['JobNumber'] + " <i style='color:#0066ff; margin-left:10px; font-size:14px; font-weight:normal;' class='fa fa-circle-o-notch fa-spin hidden' id='filtersJobSpinIcon'></i></h4>";
  msg += "<div class='row'>";
  msg += "  <div class='col-xs-12 col-lg-8'>";
  msg += "    <div class='input-group'>";
  msg += "      <input class='form-control' value='' placeholder=\"Search " + names.JobNumber + " List\" id='filtersJobSearch'>";
  msg += "      <span class='input-group-btn'><button type='button' id='filtersJobListSearchBtn' class='btn btn-default'>Search...</button></span>";
  msg += "    </div>";
  msg += "  </div>";
  msg += "</div>";
  msg += "<div class='input-group' style='margin-top:7px;'>";
  msg += "  <span class='input-group-btn'><button onclick=\"$('#jFilter').val('');\" type='button' class='btn btn-default' title='Reset Filter'><i class='fa fa-eraser'></i></button></span>";
  msg += "  <select class='form-control' name='JobIds' id='jFilter'><option value=''>Choose " + names.JobNumber + "</option></select>";
  msg += "</div>";
  msg += "  <div class='checkbox'>";
  msg += "    <label title='Can cause performance issues when dealing with thousands of records'>";
  
  if(Cookies.get("TaskFiltersJobAutoLoad") === "true"){
    msg += "<input type='checkbox' checked='checked' id='filtersJobAutoLoad'> Load all Jobs";
  }else{
    msg += "<input type='checkbox' id='filtersJobAutoLoad'> Load all Jobs";
  }
  
  msg += "    </label>";
  msg += "<button class='btn btn-primary' style='float:right;' type='button' onclick=\"applyFilters('" + view + "');\">Apply Filter</button>"; 
  msg += "  </div>";
    
  msg += "<hr />";  
    
    
  msg += "<h4>Filter By Date</h4>";
  msg += "<div class='form-group checkbox'>";
  msg += "  <label style='padding-left:0px;'>";
  
  if(!filtersObj.DateType){
    msg += "<input onchange=\"toggleDateRange('off');\" id='dateRangeOff' type='radio' name='IsDateRange' checked='checked'> Show All Tasks Regardless of Date";
  }else{
    msg += "<input onchange=\"toggleDateRange('off');\" id='dateRangeOff' type='radio' name='IsDateRange'> Show All Tasks Regardless of Date";
  }
  
  msg += "  </label>";
  msg += "</div>";
  msg += "<div class='form-group checkbox'>";
  msg += "  <label style='padding-left:0px;'>";
  
  // Show Tasks With  
  if(filtersObj.DateType){
    msg += "<input onchange=\"toggleDateRange('on');\" type='radio' name='IsDateRange' checked='checked'> Show Tasks with";
  }else{
    msg += "<input onchange=\"toggleDateRange('on');\" type='radio' name='IsDateRange'> Show Tasks with";
  }
  
  msg += "  </label>";
  msg += "  <div class='row' style='margin-top:5px;'>";
  msg += "    <div class='col-xs-6'>";
  
  // Target Start
  if(filtersObj.DateType){
    msg += "  <select  name='DateType' class='form-control'>";
  }else{
    msg += "  <select disabled='true' name='DateType' class='form-control'>";
  }
  
  msg += "    <option value='1'>Target Start</option>";
  msg += "    <option value='2'>Target Finish</option>";
  msg += "    <option value='3'>Actual Finish</option>";
  msg += "    <option value='4'>Any Part of Date Range</option>";
  msg += "  </select>";
  msg += "    </div>";
  msg += "    <div class='col-xs-6'>";
  
  if(filtersObj.DateType){
    msg += "  <select name='DateCriteria' class='form-control'>";
  }else{
    msg += "  <select disabled='true' name='DateCriteria' class='form-control'>";
  }
  
  msg += "    <option value='1'>On or Before</option>";
  msg += "    <option value='2'>On or After</option>";
  msg += "    <option value='3'>Exactly Equals</option>";
  msg += "  </select>";
  msg += "    </div>";
  msg += "  </div>";
  
  msg += "    <div class='checkbox'>";
  msg += "      <label style='padding-left:0px;'>";

  // Today
  if(filtersObj.DateType){
    msg += "<input type='radio' name='DateRange' value='1'> Today <span id='todayText' style='color:#000;'>(" + moment(new Date()).format('MM/DD/YYYY') + ")</span>";
  }else{
    msg += "<input type='radio' checked='checked' disabled='true' name='DateRange' value='1'> Today <span id='todayText' style='color:#c0c0c0;'>(" + moment(new Date()).format('MM/DD/YYYY') + ")</span>";
  }
  
  msg += "      </label>";
  msg += "    </div>";
  
  msg += "    <div class='checkbox'>";
  msg += "      <label style='padding-left:0px;'>";
  
  // This Date
  if(filtersObj.DateType){
    msg += "  <input type='radio' name='DateRange' value='5'> This Date:";
    msg += "  <input type='text' style='margin-top:5px;' class='form-control' disabled='true' name='ThisDate' value='" + moment(new Date()).format("MM/DD/YYYY") + "'>";
    msg += "  </label>";
  }else{
    msg += "  <input type='radio' name='DateRange' disabled='true' value='5'> This Date";
    msg += "  <input type='text' style='margin-top:5px;' class='form-control' disabled='true' name='ThisDate' value='" + moment(new Date()).format("MM/DD/YYYY") + "'>";
    msg += "  </label>";
  }
  
  msg += "    </div>";  
  msg += "    <div class='checkbox'>";
  msg += "      <label style='padding-left:0px;'>";
  
  // Range
  if(filtersObj.DateType){
    msg += "<input type='radio' name='DateRange' value='4'> Date Range";
  }else{
    msg += "<input type='radio' name='DateRange' disabled='true' value='4'> Date Range";
  }
  
  msg += "      </label>";
  msg += "    </div>";
  
  msg += "  <div class='row'>";
  msg += "    <div class='col-xs-6'>";
  msg += "      <div class='input-group date'>";
  msg += "        <div class='input-group-addon'>";
  msg += "          From";
  msg += "        </div>";
  
  // From (Range)
  if(filtersObj.DateFrom){
    msg += "<input name='DateFrom' disabled='true' value='" + moment(new Date()).format('MM/DD/YYYY') + "' class='form-control' />";
  }else{
    msg += "<input name='DateFrom' disabled='true' value='" + moment(new Date()).format('MM/DD/YYYY') + "' class='form-control' />";
  }
  
  msg += "      </div>";
  msg += "    </div>";
  msg += "    <div class='col-xs-6'>";
  msg += "      <div class='input-group date'>";
  msg += "        <div class='input-group-addon'>";
  msg += "          To";
  msg += "        </div>";
    
  // To (Range)
  if(filtersObj.DateTo){
    msg += "<input name='DateTo' disabled='true' value='" + filtersObj.DateTo + "' class='form-control' />";
  }else{
    msg += "<input name='DateTo' disabled='true' value='" + moment(new Date()).format("MM/DD/YYYY") + "' class='form-control' />";
  }
  
      
  msg += "      </div>";
  msg += "    </div>";
  msg += "  </div>";
  msg += " </div>";
  msg += "</div>";  
  //msg += "<br />";
  msg += "</form>";
              
  BootstrapDialog.show({
    title: "Basic Filter Options",
    //closable: false,
    message: msg,
    onshown: function(){
      $("#filtersContactAutoLoad").change(function(){
        setCookie("TaskFiltersContactAutoLoad", $(this).prop("checked"));
      });
      
      $("#filtersJobAutoLoad").change(function(){
        setCookie("TaskFiltersJobAutoLoad", $(this).prop("checked"));
      });
      
      if(Cookies.get("TaskFiltersContactAutoLoad") === "true"){
    	  $("#filtersContactSpinIcon").removeClass("hidden");

    	  $.ajax({
    	    url: '/api/v1/contact?fields=ContactId,ContactPerson,ContactCompany',
    	    dataType:'json',
    	    success: function(contacts){
            if(contacts == 'InvalidLogin'){
              logout();
            }
      
    	      var options = "<option value=''>Choose " + names.ContactPerson + "</option>";
    	      
    	      $.each(contacts.content, function(){
    	        if(this.ContactPerson.trim() == "" && this.ContactCompany.trim() == ""){}else{
    	          options += "<option value='" + this.ContactId + "'>" + this.ContactPerson + " (" + this.ContactCompany + ")</option>";
    	        }
    	      });
    	      
    	      $("#cFilter").html(options);  	      
    	      $("#filtersContactSpinIcon").addClass("hidden");
    	    },
    	    error:function(jqXhr, status, error){
            if(jqXhr.responseText == 'InvalidLogin'){
              logout();
            }
      
    	      alert(error);
    	    }
    	  });
    	}
    	
    	if(Cookies.get("TaskFiltersJobAutoLoad") === "true"){
    	  $("#filtersJobSpinIcon").removeClass("hidden");
    	  
    	  $.ajax({
    	    url: '/api/v1/job?fields=JobId,JobNumber',
    	    dataType:'json',
    	    success: function(jobs){
    	      if(jobs == 'InvalidLogin'){
    	        logout();
    	      }
    	      
    	      var options = "<option value=''>Choose " + names.JobNumber + "</option>";
    	      
    	      $.each(jobs.content, function(){
    	        if(this.JobNumber.trim() == ""){}else{
    	          options += "<option value='" + this.JobId + "'>" + this.JobNumber + "</option>";
    	        }
    	      });
    	      
    	      $("#jFilter").html(options);
    	      $("#filtersJobSpinIcon").addClass("hidden");
    	    },
    	    error:function(jqXhr, status, error){
    	      if(jqXhr.responseText == 'InvalidLogin'){
    	        logout();
    	      }
    	      
    	      alert(error);
    	    }
    	  });
  	  }
  	  
  	  var timer;
          		
          		$("#filtersContactSearch").keyup(function(){
          			clearTimeout(timer);
          			
          			if($(this).val()){                  
                  var phrase = $("#filtersContactSearch").val();
          				timer = setTimeout(function(){
                    $("#filtersContactSpinIcon").removeClass("hidden");
                    $.ajax({
                      url: '/api/v1/contact?fields=ContactId,ContactPerson,ContactCompany&FindString=' + phrase,
                      dataType:'json',
                      success: function(filteredContacts){
                        if(filteredContacts == 'InvalidLogin'){
                          logout();
                        }

                        var d = "";
        
                        $.each(filteredContacts.content, function(){
                          d += "<option value='" + this.ContactId + "'>" + this.ContactPerson + " (" + this.ContactCompany + ")</option>";
                        });
                        
                        $("#cFilter").html(d);
                        $("#filtersContactSpinIcon").addClass("hidden");
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
          		
          		$("#filtersJobSearch").keyup(function(){
          			clearTimeout(timer);
          			
          			if($(this).val()){                  
                  var phrase = $("#filtersJobSearch").val();
          				timer = setTimeout(function(){
                    $("#filtersJobSpinIcon").removeClass("hidden");
                    $.ajax({
                      url: '/api/v1/job?fields=JobId,JobNumber&FindString=' + phrase,
                      dataType:'json',
                      success: function(filteredJobs){
                        if(filteredJobs == 'InvalidLogin'){
                          logout();
                        }
                  
                        var d = "";
                        
                        $.each(filteredJobs.content, function(){
                          d += "<option value='" + this.JobId + "'>" + this.JobNumber + "</option>";
                        });
                        
                        $("#jFilter").html(d);
                        $("#filtersJobSpinIcon").addClass("hidden");
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
          		
              $("#filtersContactListSearchBtn").click(function(){
                $("#filtersContactSpinIcon").removeClass("hidden");
                
                var phrase = $("#filtersContactSearch").val();
                
                $.ajax({
                  url: '/api/v1/contact?fields=ContactId,ContactPerson,ContactCompany&FindString=' + phrase,
                  dataType:'json',
                  success: function(filteredContacts){
                    if(filteredContacts == 'InvalidLogin'){
                      logout();
                    }

                    var d = "";
                    
                    $.each(filteredContacts.content, function(){
                      d += "<option value='" + this.ContactId + "'>" + this.ContactPerson + " (" + this.ContactCompany + ")</option>";
                    });
                    
                    $("#cFilter").html(d);
                    $("#filtersContactSpinIcon").addClass("hidden");
                  },
                  error: function(jqXhr, status, error){
                    if(jqXhr.responseText == 'InvalidLogin'){
                      logout();
                    }
              
                    alert(error);
                  }
                });
              });
              
              $("#filtersJobListSearchBtn").click(function(){
                $("#filtersJobSpinIcon").removeClass("hidden");
                
                var phrase = $("#filtersJobSearch").val();
                
                $.ajax({
                  url: '/api/v1/job?fields=JobId,JobNumber&FindString=' + phrase,
                  dataType:'json',
                  success: function(filteredJobs){
                    if(filteredJobs == 'InvalidLogin'){
                      logout();
                    }
              
                    var d = "";
                    
                    $.each(filteredJobs.content, function(){
                      d += "<option value='" + this.JobId + "'>" + this.JobNumber + "</option>";
                    });
                    
                    $("#jFilter").html(d);
                    $("#filtersJobSpinIcon").addClass("hidden");
                  },
                  error: function(jqXhr, status, error){
                    if(jqXhr.responseText == 'InvalidLogin'){
                      logout();
                    }
              
                    alert(error);
                  }
                });
              });

              $("input[name=ThisDate], input[name=DateFrom], input[name=DateTo]").datepicker({
                autoclose:true,
                todayBtn: true,
                todayHighlight: true,
                //orientation: 'bottom',
                daysOfWeekDisabled: [0,6],
                format: 'mm/dd/yyyy'         
              });

              $("input:radio[name=DateRange]").change(function(){
                switch($(this).val()){
                  case '1':
                    $("input[name=DateFrom], input[name=DateTo], input[name=ThisDate]").prop("disabled",true);
                  break;
                  case '4':
                    $("input[name=DateFrom], input[name=DateTo]").prop("disabled",false);                    
                    $("input[name=ThisDate]").prop("disabled",true);
                    $("select[name=DateCriteria]").val(3);
                  break;
                  case '5':
                    $("input[name=ThisDate]").prop("disabled",false);
                    $("input[name=DateFrom], input[name=DateTo]").prop("disabled",true);
                  break;
                }
              });

              if(filtersObj.filtersContactSearch){
                //$("input[name=filtersContactSearch]").val(decodeURIComponent(filtersObj.filtersContactSearch).replace('+',' '));
              }
              
              if(filtersObj.ContactIds){
                if(Cookies.get("TaskFiltersContactAutoLoad") === "true"){
                  var dataUrl = '/api/v1/contact?fields=ContactId,ContactPerson,ContactCompany';
                }else{
                  var dataUrl = '/api/v1/contact/' + filtersObj.ContactIds;
                }
                
                $.ajax({
                  url: dataUrl,
                  dataType:'json',
                  success: function(c){
                    if(c == 'InvalidLogin'){
                      logout();
                    }
              
                    if(Cookies.get("TaskFiltersContactAutoLoad") === "true"){
                      var options = "";
                          options += "<option value=''>Choose " + names.ContactPerson + "</option>";
                      $.each(c.content, function(){
                        if(filtersObj.ContactIds == this.ContactId){
                          options += "<option value='" + this.ContactId + "' selected='selected'>" + this.ContactPerson + " (" + this.ContactCompany + ")</option>";
                        }else{
                          options += "<option value='" + this.ContactId + "'>" + this.ContactPerson + " (" + this.ContactCompany + ")</option>";
                        }
                      });
                    }else{
                      var options = "";
                          options += "<option value=''>Choose " + names.ContactPerson + "</option>";
                          options += "<option value='" + c[0].ContactId + "' selected='selected'>" + c[0].ContactPerson + " (" + c[0].ContactCompany + ")</option>";
                    }
                    
                    $("#cFilter").html(options);
                  },
                  error: function(jqXhr, status, error){
                    if(jqXhr.responseText == 'InvalidLogin'){
                      logout();
                    }
              
                    alert(error);
                  }
                });              
              }
              
              if(filtersObj.filtersJobSearch){
                //$("input[name=filtersJobSearch]").val(decodeURIComponent(filtersObj.filtersJobSearch).replace('+',' '));
              }

              if(filtersObj.JobIds){
                if(Cookies.get("TaskFiltersJobAutoLoad") === "true"){
                  var dataUrl = '/api/v1/job?fields=JobId,JobNumber';
                }else{
                  var dataUrl = '/api/v1/job/' + filtersObj.JobIds;
                }
                
                $.ajax({
                  url: dataUrl,
                  dataType:'json',
                  success: function(j){
                    if(j == 'InvalidLogin'){
                      logout();
                    }
              
                    if(Cookies.get("TaskFiltersJobAutoLoad") === "true"){
                      var options = "";
                          options += "<option value=''>Choose " + names.JobNumber + "</option>";
                      $.each(j.content, function(){
                        if(filtersObj.JobIds == this.JobId){
                          options += "<option value='" + this.JobId + "' selected='selected'>" + this.JobNumber + "</option>";
                        }else{
                          options += "<option value='" + this.JobId + "'>" + this.JobNumber + "</option>";
                        }
                      });
                    }else{
                      var options = "";
                          options += "<option value=''>Choose " + names.JobNumber + "</option>";
                          options += "<option value='" + j[0].JobId + "' selected='selected'>" + j[0].JobNumber + "</option>";
                    }
                    
                    $("#jFilter").html(options);
                  },
                  error: function(jqXhr, status, error){
                    if(jqXhr.responseText == 'InvalidLogin'){
                      logout();
                    }
              
                    alert(error);
                  }
                });              
              }
              
              if(filtersObj.DateType){
                $("select[name=DateType]").val(filtersObj.DateType);
                $("select[name=DateCriteria]").val(filtersObj.DateCriteria);
                
                if(filtersObj.DateFrom){
                  $("input[name=DateFrom]").val(decodeURIComponent(filtersObj.DateFrom));
                }
                
                if(filtersObj.DateTo){
                  $("input[name=DateTo]").val(decodeURIComponent(filtersObj.DateTo));
                }
                
                $("input:radio[name=DateRange]").val([filtersObj.DateRange]);
              
                if(filtersObj.ThisDate){
                  $("input[name=ThisDate]").val(Cookies.get("filterThisDateGreg"));
                }
                
                if(filtersObj.DateFrom){
                  $("input[name=DateFrom]").val(Cookies.get("filterDateFromGreg"));
                }
                
                if(filtersObj.DateTo){
                  $("input[name=DateTo]").val(Cookies.get("filterDateToGreg"));
                }
              
                switch(filtersObj.DateRange){
                  case '4':
                    $("input[name=DateFrom], input[name=DateTo]").prop("disabled",false);
                  break;
                  case '5':
                    $("input[name=ThisDate]").prop("disabled",false);
                  break;
                }
              }
            },
            buttons: [{
              label: "Reset<span class='hidden-xs'> Filters</span>",
              icon: "fa fa-ban",
              cssClass: "btn-warning",
              action: function(dialogRef){
                removeFilters(view);
              }
            },{
              label: "Cancel",
              action: function(dialog){
                dialog.close();
              }
            },{
              label: "Apply<span class='hidden-xs'> Filter</span>",
              cssClass: "btn-primary",
              action: function(dialog){
                applyFilters(view);
                dialog.close();
              }
            }]
          });
  
    },
    error:function(jqXhr, textStatus, errorThrown){
      if(jqXhr.responseText == 'InvalidLogin'){
        logout();
      }
      
      alert("Error Retrieving Field Names: " + errorThrown);
    }
  });
}

/******************************************************************/

function applyFilters(view){
  // APPLYING FILTERS
  if(!$("input[name=IsActive]").prop("checked") && !$("input[name=IsDone]").prop("checked")){
    $("input[name=IsActive]").prop("checked",true);
  }
                
  if($("#dateRangeOff").prop("checked")){
    $("#dateRangeOff").prop("disabled",true);
    $("input[name=IsDateRange]").prop("disabled", true);
  }else{
    var dRange = $("input:radio[name=DateRange]:checked").val();
    
    switch(dRange){
      case "1":
        $("input[name=ThisDate]").prop("disabled",false).val(moment(new Date()).format("MM/DD/YYYY"));
      break;
    }
                  
    Cookies.set("filterThisDateGreg", $('input[name=ThisDate]').val(), { expires: 30, path: '/' });
    Cookies.set("filterDateFromGreg", $('input[name=DateFrom]').val(), { expires: 30, path: '/' });
    Cookies.set("filterDateToGreg", $('input[name=DateTo]').val(), { expires: 30, path: '/' });

    var thisDate = $("input[name=ThisDate]").val().split("/");
    var thisDateObj = {
      "month": thisDate[0],
      "day": thisDate[1],
      "year": thisDate[2]
    };

    var formattedThisDate = thisDateObj.year + "-" + thisDateObj.month + "-" + thisDateObj.day;
    var formattedDateFrom = $("input[name=DateFrom]").val().split("/");
        formattedDateFrom = formattedDateFrom[2] + "-" + formattedDateFrom[0] + "-" + formattedDateFrom[1];
    var formattedDateTo = $("input[name=DateTo]").val().split("/");
        formattedDateTo = formattedDateTo[2] + "-" + formattedDateTo[0] + "-" + formattedDateTo[1];
                      
    $("input[name=ThisDate]").val(formattedThisDate);
    $("input[name=DateFrom]").val(formattedDateFrom);
    $("input[name=DateTo]").val(formattedDateTo);
  }
                
  if($("#cFilter").val() == ""){
    $("#cFilter").prop("disabled",true);
  }else{
    Cookies.set("filterContact", $("#cFilter option:selected").text(), {
      expires: 30,
      path: '/'
    });
  }
                
  if($("#jFilter").val() == ""){
    $("#jFilter").prop("disabled",true);
  }else{
    Cookies.set("filterJob", $("#jFilter option:selected").text(), {
      expires: 30,
      path: '/'
    });
  }
                
  var activeFilters = $("form[name=basicFiltersForm]").serialize();

  if(activeFilters){
    setCookie("TaskFilters", activeFilters);
  }else{
    setCookie("TaskFilters", defaultTaskFilters);        
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
}

function toggleDateRange(status){
  switch(status.toLowerCase()){
    case "on":
      $("select[name=DateType], select[name=DateCriteria], input[name=DateRange]").prop("disabled",false);     
      $("input:radio[name=DateRange]").val([1]);
    break;
    case "off":
      $("select[name=DateType], select[name=DateCriteria], input[name=DateFrom], input[name=DateTo], input[name=DateRange], input[name=ThisDate]").prop("disabled",true);
    break;
  }
}

function openJobFilter(){
  $.ajax({
    url: "/api/v1/task",
    async: false,
    dataType: "json",
    success: function(jobIds){
      if(jobIds == 'InvalidLogin'){
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
  
  var msg = "";
      msg += "Job IDs have been retrieved.";
  
  BootstrapDialog.show({
    title: "Filter By Job",
    message: msg,
    buttons: [{
      label: "Apply Filter",
      cssClass: "btn-primary",
      action: function(dialog){
        
        dialog.close();
      }
    },{
      label: "Cancel",
      action: function(dialog){
        dialog.close();
      }
    }]
  });
}

/******************************************************************/

function openContactFilter(){
  $.ajax({
    url: "/api/v1/task",
    async: false,
    dataType: "json",
    success: function(jobIds){
      if(jobIds == 'InvalidLogin'){
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
  
  var msg = "";
      msg += "Job IDs have been retrieved.";
  
  BootstrapDialog.show({
    title: "Filter By Job",
    message: msg,
    buttons: [{
      label: "Apply Filter",
      cssClass: "btn-primary",
      action: function(dialog){
        
        dialog.close();
      }
    },{
      label: "Cancel",
      action: function(dialog){
        dialog.close();
      }
    }]
  });
}

/******************************************************************/

function openDateFilter(){
  
  var msg = "";
      msg += "<h5>Custom Date Range</h5>";
      msg += "<form role='form' name='filterByDateRangeForm'>";
      msg += "<div class='row'>";
      msg += "  <div class='col-xs-12 col-md-6'>";
      msg += "    <span style='font-size:12px;'>From</span> <input placeholder='mm/dd/yyyy' type='date' class='form-control' name='filterDateFrom'>";
      msg += "    <span style='font-size:12px;'>To</span> <input placeholder='mm/dd/yyyy' type='date' class='form-control' name='filterDateTo'>";
      msg += "  </div>";
      msg += "</div>";    
      msg += "</form>";
            
  BootstrapDialog.show({
    title: "Filter By Date Range",
    message: msg,
    buttons: [{
      label: "Apply Filter",
      cssClass: "btn-primary",
      action: function(dialog){
        var dates = $("form[name=filterByDateRangeForm]").serializeArray();
        //var customUrl = "/api/v1/task?IsDateRange=TRUE&";
      
        //createTaskList(customUrl);
        //dialog.close();
      }
    },{
      label: "Cancel",
      action: function(dialog){
        dialog.close();
      }
    }]
  });
}

/**********************************************************************/

function getJobData(fields){
  var jd;
  var u = "/api/v1/job?fields=" + fields;

  $.ajax({
    url: u,
    dataType: 'json',
    success: function(data){
      if(data == 'InvalidLogin'){
        logout();
      }

      jd = data;

      return jd;
    },
    error: function(jqXhr, textStatus, errorThrown){
      if(jqXhr.responseText == 'InvalidLogin'){
        logout();
      }

      alert(errorThrown);
    }
  }); 
}

function getContactData(fields){
  var jd;
  var u = "/api/v1/contact?fields=" + fields;

  $.ajax({
    url: u,
    dataType: 'json',
    success: function(data){
      if(data == 'InvalidLogin'){
        logout();
      }

      cd = data;

      return jd;
    },
    error: function(jqXhr, textStatus, errorThrown){
      if(jqXhr.responseText == 'InvalidLogin'){
        logout();
      }

      alert(errorThrown);
    }
  });
}

function removeFilters(view){
  Cookies.set("TaskFilters", defaultTaskFilters, {
    expires: 30,
    path: '/'
  });
  
  Cookies.set('filterFindString', '', { expires: 30, path: '/' });
  $("#taskListSearchBox, #ganttChartSearchBox").val(Cookies.get('filterFindString'));
  
  switch(view){
    case "TaskManager":
      createTaskList();
    break;
    case "Calendar":
      loadCalendar();    
    break;
    case "GanttChart":
      loadGanttChart();
    break;
  }
}

function setFiltersMessage(filters, view){
  if(!filters){
    filters = defaultTaskFilters;
  }
  
  var filtersArray = filters.split("&");
  var fMsg = "<i class='fa fa-filter'></i>: <a href='#' onclick=\"openBasicFilters('" + view + "'); return false;\" title='Current Active Filters'>";
    
  $.each(filtersArray, function(i){
    var filterName = this.split("=")[0];
    switch(filterName){
      case "IsActive":
        fMsg += "Show Active";
        if(i >= filtersArray.length - 1){}else{ fMsg += ", "; }
      break;
      case "IsDone":
        fMsg += "Show Done";
        if(i >= filtersArray.length - 1){}else{ fMsg += ", "; }
      break;
      case "IsMarked":
        fMsg += "Marked";
        if(i >= filtersArray.length - 1){}else{ fMsg += ", "; }
      break;
      case "ThisDate":
        fMsg += "<i class='fa fa-calendar'></i> " + Cookies.get('filterThisDateGreg');
        if(i >= filtersArray.length - 1){}else{ fMsg += ", "; }
      break;
      case "DateFrom":
        fMsg += "<i class='fa fa-calendar'></i> " + Cookies.get("filterDateFromGreg") + " - ";
        //if(i >= filtersArray.length - 1){}else{ fMsg += ", "; }
      break;
      case "DateTo":
        fMsg += Cookies.get("filterDateToGreg");
        if(i >= filtersArray.length - 1){}else{ fMsg += ", "; }
      break;
      case "ContactIds":
        fMsg += "<i class='fa fa-user'></i> " + Cookies.get("filterContact");
        if(i >= filtersArray.length - 1){}else{ fMsg += ", "; }
      break;
      case "JobIds":
        fMsg += "<i class='fa fa-book'></i> " + Cookies.get("filterJob");
        if(i >= filtersArray.length - 1){}else{ fMsg += ", "; }
      break;
    }

  });
  
  fMsg += "</a>";
  
  if(Cookies.get('filterFindString') !== ''){
    fMsg += ", Search Phrase: <span style='color:#777;'>" + Cookies.get('filterFindString') + "</span>";
  }
  
  $("#activeFiltersMessage").html(fMsg);
}











