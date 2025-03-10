var globalNames = [];
/**********************************************************************************************/
function editNewJob(){
  $.ajax({
    url: '/api/v1/fieldcaptions?fields=' + allJobFieldCaptionNames.join(","),
    dataType: 'json',
    success: function(names){
      if(names == 'InvalidLogin'){
        logout();
      }
    
      globalNames = names;
      showNewJobEditScreen(names);
    },
    error: function(jqXhr, textStatus, errorThrown){
      if(jqXhr.responseText == 'InvalidLogin'){
        logout();
      }    
    
      BootstrapDialog.show({
        title: "Error Opening New Job",
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

function showNewJobEditScreen(names){
  var body = "";
      body += "<form role='form' method='POST' name='details'>";
      
      // JOB NAME
      body += " <div class='form-group'>";
      body += names.JobNumber;
      body += "   <input name='Number' class='form-control' value=''>";
      body += " </div>";
      
      /*
      // LOT #
      body += "<div class='row form-group'>";
      body += " <div class='col-xs-6 col-lg-4'>";
      body += names.JobLot;
      body += "   <input name='Lot' class='form-control' value=''>";
      body += " </div>";
      
      // SUBDIVISION
      body += " <div class='col-xs-6 col-lg-4'>";
      body += names.JobSubdivision;
      body += "   <input name='Subdivision' class='form-control' value=''>";
      body += " </div>";
      
      // LOCK BOX COMBO
      body += " <div class='col-xs-12 col-lg-4'>";
      body += names.JobLockBox;
      body += "   <input name='Lock_Box_Combo' class='form-control' value=''>";
      body += " </div>";
      body += "</div>";
            
      // OWNERS NAME
      body += "<div class='row form-group'>";
      body += " <div class='col-xs-6 col-lg-4'>";
      body += names.JobOwnerName;
      body += "   <input name='Owners_Name' class='form-control' value=''>";
      body += " </div>";
      
      // COMPANY NAME
      body += " <div class='col-xs-6 col-lg-4'>";
      body += names.JobCompany;
      body += "   <input name='Company_Name' class='form-control' value=''>";
      body += " </div>";
      
      // EMAIL
      body += " <div class='col-xs-6 col-lg-4'>";
      body += names.JobEmail;
      body += "   <input name='Email' class='form-control' value=''>";
      body += " </div>";
      body += "</div>";
      
      // DIRECTIONS TO JOB SITE
      body += "<div class='form-group'>";
      body += names.JobDirections;
      body += " <textarea name='Direction_to_job_site' class='form-control'></textarea>";
      body += "</div>";
      
      // JOB NOTES
      body += "<div class='form-group'>";
      body += names.JobNotes;
      body += " <textarea name='Notes' class='form-control'></textarea>";
      body += "</div>";
      */
      body += "</form>";
      body += "Please provide a name for this new " + names.JobNumber; // + ".  You can edit the job once it is created";
      
  // Job EDIT POP UP
  BootstrapDialog.show({
    title: "<div>New " + names.JobNumber + " Details</div>",
    message: body,
    //draggable: true,
    size: BootstrapDialog.SIZE_WIDE,
    buttons: [{
      label:'Close',
      cssClass:'btn-default',
      action: function(dialogRef){
        dialogRef.close();
      }    
    },{ 
      label: 'Save New ' + names.JobNumber,
      cssClass:'btn-primary',
      action: function(dialogRef){
        if($("input[name=Number]").val().trim() == ""){
          BootstrapDialog.show({
            title: "Missing " + names.JobNumber,
            message: "Please give this new " + names.JobNumber + " a name",
            buttons:[{
              label: "Ok",
              cssClass: 'btn-primary',
              action: function(dialogRef){
                dialogRef.close();
                //$("input[name=JobNo]").focus();
              }
            }]  
          });
          
          return;
        }
        
        var formData = $("form[name=details]").serialize();
        var jobNum = escape($("input[name=JobNumber]").val());
        
        $.ajax({
          url: "/api/v1/job", // + formData,
          data: formData,
          method: 'POST',
          success: function(response){
            if(response == 'InvalidLogin'){
              logout();
            }
            
            // if(response.trim() != ""){
            //   // job name must already exist
            //   BootstrapDialog.show({
            //     type: BootstrapDialog.TYPE_WARNING,
            //     title: "Duplicate " + names.JobNumber,
            //     message: "There is already a " + names.JobNumber + " with that name.",
            //     buttons: [{
            //       label: "Ok",
            //       cssClass: "btn-primary",
            //       action: function(dialogWarn){
            //         dialogWarn.close();
            //       }
            //     }]
            //   });
            // }else{
              dialogRef.close();
              editJob(response.JobId);
              // createJobList();
            // }
          },
          error: function(jqXhr, textStatus, errorThrown){
            if(jqXhr.responseText == 'InvalidLogin'){
              logout();
            }

            BootstrapDialog.show({
              title: "Error Creating New Job",
              message: jqXhr.responseJSON.message, //errorThrown,
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
    }] 
  });
}
/**********************************************************************************************/
