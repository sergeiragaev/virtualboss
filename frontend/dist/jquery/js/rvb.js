$(document).ready(function(){
	
	var cookies = getCookies();
	if(cookies['filterStatus'] == 'open'){
		$("#tabs").toggle("fade",{},200).css("display","inline-block");
	}else{
	
	}
	$("#toggleFilters").click(function(){
		if($("#tabs").is(":visible")){
			$("#tabs").toggle("fade",{},200);
			setCookie("filterStatus","closed",30);
		}else{
			$("#tabs").toggle("fade",{},200).css("display","inline-block");
			setCookie("filterStatus","open",30);
		}
	});
	
});

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