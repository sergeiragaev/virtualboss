/***********************************************************************************************/
const defaultContactFieldsToShow = "ContactPerson,ContactEmail,ContactCompany";
const defaultContactFilters = "";
const allContactFieldCaptionNames = ["ContactCompany", "ContactPerson", "ContactProfession", "ContactFirstName", "ContactLastName", "ContactSupervisor", "ContactSpouse", "ContactTaxID", "ContactWebSite", "ContactEmail", "ContactAddresses", "ContactWorkersCompDate", "ContactInsuranceDate", "ContactComments", "ContactNotes", "ContactPhones", "ContactCustomField1", "ContactCustomField2", "ContactCustomField3", "ContactCustomField4", "ContactCustomField5", "ContactCustomField6", "ContactCustomList1", "ContactCustomList2", "ContactCustomList3", "ContactCustomList4", "ContactCustomList5", "ContactCustomList6"];

let contactsPerPage = 20;
let cCurrentPage = 1;
let cTotalPages = 1;
/***********************************************************************************************/
$(document).ready(function(){
  loadContactSettings();
  loadContactFilters();
  setupEventHandlers();
  createContactList();
});

/***********************************************************************************************/

function loadContactSettings(){
  if(!Cookies.get("ContactFieldsToShow")){
    setCookie("ContactFieldsToShow", defaultContactFieldsToShow);
  }
  
  if(!Cookies.get("ContactListSort")){
    setCookie("ContactListSort", "[[0,0]]");
  }

  if (!Cookies.get("ShowAllContacts")) {
    setCookie("ShowAllContacts", false);
  }

  if (!Cookies.get("ContactLimit")) {
    setCookie("ContactLimit", contactsPerPage);
  } else {
    contactsPerPage = parseInt(Cookies.get("ContactLimit"), 10);
  }

  cCurrentPage = parseInt(Cookies.get('cCurrentPage')) || 1;

  // set Contact custom field options
  
  if(!Cookies.get("ShowContactCustomField1")){
    setCookie("ShowContactCustomField1", false);
  }
  
  if(!Cookies.get("ShowContactCustomField2")){
    setCookie("ShowContactCustomField2", false);
  }
  
  if(!Cookies.get("ShowContactCustomField3")){
    setCookie("ShowContactCustomField3", false);
  }
  
  if(!Cookies.get("ShowContactCustomField4")){
    setCookie("ShowContactCustomField4", false);
  }
  
  if(!Cookies.get("ShowContactCustomField5")){
    setCookie("ShowContactCustomField5", false);
  }
  
  if(!Cookies.get("ShowContactCustomField6")){
    setCookie("ShowContactCustomField6", false);
  }
  
  // Contact custom list options
  
  if(!Cookies.get("ShowContactCustomList1")){
    setCookie("ShowContactCustomList1", false);
  }
  
  if(!Cookies.get("ShowContactCustomList2")){
    setCookie("ShowContactCustomList2", false);
  }
  
  if(!Cookies.get("ShowContactCustomList3")){
    setCookie("ShowContactCustomList3", false);
  }
  
  if(!Cookies.get("ShowContactCustomList4")){
    setCookie("ShowContactCustomList4", false);
  }
  
  if(!Cookies.get("ShowContactCustomList5")){
    setCookie("ShowContactCustomList5", false);
  }
  
  if(!Cookies.get("ShowContactCustomList6")){
    setCookie("ShowContactCustomList6", false);
  }

  if (!Cookies.get("UseContactColor")) {
    setCookie("UseContactColor", false);
  }
}

function loadContactFilters(){
  if(!Cookies.get("ContactFilters")){
    setCookie("ContactFilters", defaultContactFilters);
  }

  if (!Cookies.get("contactListFindString")) {
    setCookie("contactListFindString", "");
  }

  $("#contactListSearchBox").val(Cookies.get('contactListFindString'));
}

function createContactList(customUrl){
  let dataUrl;
  const contactFieldsArray = getContactFieldsToShowArray();
  let contactFieldsString = Cookies.get("ContactFieldsToShow");
  const activeContactFilters = getActiveContactFilters();
  const sortParams = getSortParams(contactFieldsArray);

  BootstrapDialog.show({
    title: "Loading Contacts, Please wait...",
    message: "<i class='fa fa-circle-o-notch fa-spin'></i> Loading Contacts..."
  });

  if(eval(Cookies.get('UseContactColor'))) {
    contactFieldsString += ",Color";
  }

  // START CONTACT LIST CREATION  
  if(!customUrl){
    dataUrl = "/api/v1/contact?fields=ContactId," + contactFieldsString;
    dataUrl += "&" + activeContactFilters;
  }else{
    dataUrl = customUrl + "&fields=ContactId," + contactFieldsString;
  }

  if (eval(Cookies.get('ShowAllContacts'))) contactsPerPage = 10000;

  let findString = Cookies.get('contactListFindString') || '';
  dataUrl += '&page=' + cCurrentPage + '&limit=' + contactsPerPage + '&sort=' + sortParams + '&findString=' + findString;

  $.ajax({
    url: dataUrl,
    dataType: 'json',
    success: function(contacts){
      if(contacts == 'InvalidLogin'){
        logout();
      }

      cTotalPages = contacts.page.totalPages;

      updatePagination();

      if(!contacts.page.totalElements){
        BootstrapDialog.closeAll();
        BootstrapDialog.show({
          title: "No Results",
          message: "Your Search Returned No Results"
        });
        
        return;
      }

      updateContactCount(contacts.page.totalElements);

      var tbl = "<div class='table-responsive'>";
          tbl += "<table class='table table-bordered table-condensed table-striped tablesorter table-hover'>";
          tbl += "  <thead>";
          tbl += "    <tr>";
    
      $.ajax({      
        url: '/api/v1/fieldcaptions?fields=ContactId,' + allContactFieldCaptionNames.join(","),
        dataType:'json',
        success: function(names){
          if(names == 'InvalidLogin'){
            logout();
          }
    
          $.each(contactFieldsArray, function(i){
            tbl += "<th>" + names[this] + "</th>";
          });
        
          tbl += "    </tr>";
          tbl += "  </thead>";
          tbl += "  <tbody>";
              
          $.each(contacts.content, function(i){
            var emptyContact = true;
            
            for(var n in contactFieldsArray){
              if(contacts.content[i][contactFieldsArray[n]] != ""){
                emptyContact = false;
              }
            }
            
            if(emptyContact){
              return;
            }

            if(eval(Cookies.get('UseContactColor'))) {
              tbl += "<tr onclick=\"editContact('" + contacts.content[i]['ContactId'] + "');\" style='cursor:pointer; color:" + contacts.content[i]['Color'] + ";'>";
            } else {
              tbl += "<tr onclick=\"editContact('" + contacts.content[i]['ContactId'] + "');\" style='cursor:pointer;'>";
            }
            $.each(contactFieldsArray, function(j){             
              if(this == "ContactPerson"){
                if(eval(Cookies.get('UseContactColor'))) {
                  tbl += "<td>" + contacts.content[i][this] + "</td>";
                } else {
                  tbl += "<td><a href='#' onclick=\"return false;\">" + contacts.content[i][this] + "</a></td>";
                }
              }else if(this == "ContactNotes"){
                tbl += "<td>" + contacts.content[i][this]; //.replace(/\\n/g, '<br />') + "</td>";
              }else if(contactFieldsArray[j] == "ContactWorkersCompDate" || contactFieldsArray[j] == "ContactInsuranceDate"){
                tbl += "<td>" + formatDate(contacts.content[i][contactFieldsArray[j]]) + "</td>";
              }else {
                tbl += "<td>" + contacts.content[i][this] + "</td>";
              }
            });
                
            tbl += "</tr>";
          });
                        
          tbl += "  </tbody>";
          tbl += "</table>";
          tbl += "</div>";
          
          $("#cContactList").html(tbl);
          initTableSorting();
          
          BootstrapDialog.closeAll();
          postEffects();
        },
        error: function(jqXhr, textStatus, errorThrown){
          if(jqXhr.responseText == 'InvalidLogin'){
            logout();
          }
    
          BootstrapDialog.alert({
            title: "Error",
            message: jqXhr.responseText
          });
        }
      });
    },
    error: function(jqXhr, textStatus, errorThrown){
      if(jqXhr.responseText == 'InvalidLogin'){
        logout();
      }
      
      BootstrapDialog.alert({
        title: "Error",
        message: jqXhr.responseText
      });
    }   
  });
  // END CONTACT LIST CREATION 
}

/**********************************************************************************************/

function postEffects(){

}

function getActiveContactFilters(){
  return Cookies.get("ContactFilters");
}

function setCookie(name, value){
  Cookies.set(name, value, {
    expires: 365
  });
}

function removeCookie(name){
  Cookies.remove(name);
}

function getContactFieldsToShowArray(){
  //console.log(Cookies.get("ContactFieldsToShow").split(","));
  return Cookies.get("ContactFieldsToShow").split(",");
}

function formatDate(d){
  if (!d) return "";
  var t = d.split("-");
  return t[1] + "/" + t[2] + "/" + t[0];
}

function editContactListColumns(){
  var contactFieldNames;
  
  $.ajax({
    url: "/api/v1/fieldcaptions?fields=" + allContactFieldCaptionNames.join(','),
    dataType:'json',
    success: function(names){
      if(names == 'InvalidLogin'){
        logout();
      }

      contactFieldNames = names;

      var msg = "<div class='row'>";
          msg += "<form role='form' name='contactFieldsToShowForm'>";
        
      var fieldValues = getContactFieldsToShowArray(); 
                    
          msg += "<div class='col-xs-12'>";
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
      msg += "</form>";
      msg += "</div>";
      msg += "<div class='row'>";
      msg += "  <div class='col-xs-12'>";
      msg += "    <div class='checkbox'>";
      msg += "      <label>";
      msg += "        <input type='checkbox' id='checkAllContactFieldBoxes' onchange='toggleAllContactFields();'> Check / Uncheck All";
      msg += "      </label>";
      msg += "    </div>";
      msg += "  </div>";
      msg += "</div>";

      BootstrapDialog.show({
        title: "Choose Fields To Show On Contact List",
        message: msg,
        buttons:[{
          label: "Defaults",
          cssClass: "btn-warning",
          action: function(){
            $.each($("form[name=contactFieldsToShowForm] input[type=checkbox]"), function(){
              $(this).prop("checked", false);
            });
            
            $.each(defaultContactFieldsToShow.split(','), function(){
              $("form[name=contactFieldsToShowForm] input[name=" + this + "]").prop("checked", true);
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
            var activeContactFields = [];
            var inputList = $("form[name=contactFieldsToShowForm]").serializeArray();
            
            if(inputList.length <= 0){
              alert("You need to have at least 1 field active");
              return false;
            }
                    
            $.each(inputList, function(){
              activeContactFields.push($(this).attr("name"));  
            });

            setCookie("ContactFieldsToShow", activeContactFields.join(','));
            setCookie("ContactListSort", "[[0,0]]"); // resets table sort (solves issues when someone removes the sorted list)
            createContactList();
                    
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

function toggleAllContactFields(){
  var state = $("#checkAllContactFieldBoxes").prop("checked");
  
  $.each($("form[name=contactFieldsToShowForm] input[type=checkbox]"), function(){
    if(state){
      $(this).prop("checked", true);      
    }else{
      $(this).prop("checked", false);
    }
  });
}

function editContactFieldOrder(){
  var fields = getContactListFieldsToShowArray();
  var msg = "";
  
  msg += "The top field shows up 1st on the Contact List. The bottom field is last.  Drag and Drop the fields to change.";
  msg += "<hr />";
  msg += "<div>";
  msg += "<ul id='sortableFields'>";
  
  $.ajax({
    url: "/api/v1/fieldcaptions?fields=" + allContactFieldCaptionNames.join(','),
    dataType:'json',
    success: function(names){
      if(names == 'InvalidLogin'){
        logout();
      }

      var currentSort = eval(Cookies.get("ContactListSort"));
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
            
            setCookie("ContactFieldsToShow", newFieldOrderArray.join(','));
            setCookie("ContactListSort", sortString);
            
            createContactList();
            
            dialogRef.close();
          }
        },{
          label: "Cancel",
          action: function(dialogRef){
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

function getContactListFieldsToShowArray(){
  return Cookies.get("ContactFieldsToShow").split(",");
}

function setupEventHandlers() {
  $('#cPrevPage').click(() => changePage(-1));
  $('#cNextPage').click(() => changePage(1));

  $("form[name='contactListSearchForm']").submit(function (e) {
    e.preventDefault();
    cCurrentPage = 1;
    const phrase = $("#contactListSearchBox").val().replace(/["'&?,;]/g, "");
    Cookies.set('contactListFindString', phrase);
    createContactList();
  });
}

function changePage(delta) {
  cCurrentPage = Math.max(1, cCurrentPage + delta);
  createContactList();
}

function updateContactCount(total) {
  if (!total) {
    $("#cCount").html("0, <span style='font-style:italic; color:#ababab;'>nothing matched your search or current filters</span>");
  } else if (eval(Cookies.get('ShowAllContacts'))) {
    $("#cCount").html("<a href='#' onclick=\"editOptions(); return false;\">" + total + "</a>");
  } else {
    $("#cCount").html(total + " (<a href='#' title='Change number of contacts to show' onclick=\"editOptions(); return false;\">Showing</a> up to " + Cookies.get('ContactLimit') + ")");
  }
}

function editOptions() {
  var msg = "";
  msg += "<form role='form' name='contactManagerOptionsForm'>";

  msg += " <div class='checkbox'>";
  msg += "  <label>";

  if (eval(Cookies.get('UseContactColor'))) {
    msg += "<input id='contactColorOption' name='UseContactColor' type='checkbox' checked='checked'> Use Active Color Profile For Contacts";
  } else {
    msg += "<input id='contactColorOption' name='UseContactColor' type='checkbox'> Use Active Color Profile For Contacts";
  }

  msg += "  </label>";
  msg += "</div>";
  
  msg += "<div class='checkbox'>";
  msg += "  <label>";

  if (eval(Cookies.get('ShowAllContacts'))) {
    msg += "<input type='checkbox' onchange='contactLimitToggle();' name='ShowAllContacts' checked='checked' id='contactLimitOptionToggle'> Show All Contacts";
  } else {
    msg += "<input type='checkbox' onchange='contactLimitToggle();' name='ShowAllContacts' id='contactLimitOptionToggle'> Show All Contacts";
  }

  msg += "  </label>";
  msg += "  <br />";
  msg += "  <div style='margin-left:20px; margin-top:5px;'>";
  msg += "    Show up to ";

  if (eval(Cookies.get('ShowAllContacts'))) {
    msg += "<input type='text' name='ContactLimit' class='form-control' disabled='true' id='contactLimitOption' value='" + Cookies.get('ContactLimit') + "' style='max-width:75px; display:inline-block;'>";
  } else {
    msg += "<input type='text' name='ContactLimit' id='contactLimitOption' class='form-control' value='" + Cookies.get('ContactLimit') + "' style='max-width:75px; display:inline-block;'>";
  }

  msg += "    Contacts";
  msg += "  </div>";
  msg += " </div>";
  msg += "</form>";

  BootstrapDialog.show({
    title: "Contact Manager Options",
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

        setCookie('UseContactColor', $("#contactColorOption").prop("checked"));

        if ($("#contactLimitOptionToggle").prop("checked")) {
          setCookie('ShowAllContacts', true);
          cCurrentPage = 1;
        } else {
          setCookie('ShowAllContacts', false);
          setCookie('ContactLimit', $("#contactLimitOption").val());
          contactsPerPage = $("#contactLimitOption").val();
          cCurrentPage = 1;
        }

        dialogRef.close();
        createContactList();
      }
    }]
  });
}

function contactLimitToggle() {
  var state = $("#contactLimitOptionToggle").prop("checked");

  if (state) {
    $("#contactLimitOption").attr("disabled", true);
  } else {
    $("#contactLimitOption").attr("disabled", false);
  }
}

function updatePagination() {
  $('#cPageInfo').text(`Page ${cCurrentPage} of ${cTotalPages}`);
  $('#cPrevPage').prop('disabled', cCurrentPage === 1);
  $('#cNextPage').prop('disabled', cCurrentPage >= cTotalPages);
  Cookies.set('cCurrentPage', cCurrentPage);
}

function getSortParams(contactFieldsArray) {
  const savedSort = JSON.parse(Cookies.get("ContactListSort") || '[]');
  return savedSort.map(([fieldIndex, direction]) => {
    const field = contactFieldsArray[fieldIndex];
    return `${field}:${direction === 0 ? 'asc' : 'desc'}`;
  }).join(',');
}

function initTableSorting() {
  $("#cContactList table").tablesorter({
    sortList: eval(Cookies.get("ContactListSort"))
  }).bind("sortStart", function () {

  }).bind("sortEnd", function (data) {
    setCookie("ContactListSort", data.delegateTarget.config.sortList);
    cCurrentPage = 1;
    createContactList();
  });
}
