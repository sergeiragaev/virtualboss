function logout(){
  BootstrapDialog.confirm({
    title: "<i class='fa fa-sign-out'></i> Confirm Logging Out",
    message: "Are you sure you want to log out?",
    type: BootstrapDialog.TYPE_DEFAULT,
    btnOKLabel: "Yes, Log me out",
    btnOKClass: "btn-primary",
    callback: function(result){
      if(result){
        Cookies.remove('RVBLoginName', {
          path: '/'
        });
        Cookies.remove('RVBLoginPwd', {
          path: '/'
        });
        
        var parameters = location.href.split("/").slice(-1);
        location.href='login.htm?URL=' + parameters;      
      }else{
        
      }
    }
  });
}