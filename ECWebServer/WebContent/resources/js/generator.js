$(document).ready(function() {

  // change the content of the form when selecting different type:
  $('#typeSelector').change(function(e) {
    var type = $(this).val();
    var mainForm = $('#mainFormBody');
    if(type=='Device') {
      mainForm.empty().append("<tr><td class='firstColumn'>Device ID</td><td class='secondColumn'><input name='contentText'/></td></tr>");
    } else if(type=='URL') {
      mainForm.empty().append("<tr><td class='firstColumn'>URL</td><td class='secondColumn'><input name='contentText'/></td></tr>");
    } else if(type=='JSON') {
      var html = "<tr><td class='firstColumn'>Device ID</td><td class='secondColumn'><input name='deviceID'/></td></tr>"
                +"<tr><td class='firstColumn'>Base URL</td><td class='secondColumn'><input name='baseURL'/></td></tr>"
                +"<tr><td class='firstColumn'>Quantity</td><td class='secondColumn'><input type='number' name='quantity' min='0'/></td></tr>";
      mainForm.empty().append(html);
    } else if(type=='Setting') {
      var html = "<tr><td class='firstColumn'>Web Server</td><td class='secondColumn'><input name='webserver'/></td></tr>"
      		    +"<tr><td class='firstColumn'>Quantity</td><td class='secondColumn'><input type='number' name='quantity' min='0'/></td></tr>";
      mainForm.empty().append(html);
    } else if(type=='Plain') {
      mainForm.empty().append("<tr><td class='firstColumn'>Plain Text</td><td class='secondColumn'><textarea name='contentText' rows='5'></textarea></td></tr>");
    }
  });

  // after clicking the "generate" button:
  $('#btnGenerate').bind("click",function() {
            var flag = true;
            var $form = $('#generatorForm');
            
            //
            var text = $form.find("input[name='contentText']").val();
            var type = $('#typeSelector').val();
            if(type=='JSON') { // JSON format data
              text = ""; //"{'deviceID':'device_id','webserver':'url','direct':boolean}";
              var did = $("input[name='deviceID']").val();
              var baseurl = $("input[name='baseURL']").val();
              var quantity = $("input[name='quantity']").val();
              if (did != '' && did != 'undefinided' && did != null) {
                text += "'deviceID':'" + did + "',";
              } else {
                alert('Please input Device ID!');
                flag = false;
              }
              if (baseurl != '' && baseurl != 'undefinided' && baseurl != null) {
                text += "'webserver':'" + baseurl + "',";
              }
              if (quantity != '' && quantity != 'undefinided' && quantity != null && quantity != '0') {
                text += "'quantity':" + quantity + ",";
              }
              text = "{" + text + "'direct':false}";
            } else if(type=='Setting') { // Setting
              text = "SETTINGS={";
              var webserver = $("input[name='webserver']").val();
              var quantity = $("input[name='quantity']").val();
              if (webserver != '' && webserver != 'undefinided' && webserver != null) {
                text = "SETTINGS={'webserver':'" + webserver + "'";
              } else {
                webserver = '';
              }
              if (quantity != '' && quantity != 'undefinided' && quantity != null && quantity != '0') {
                if (webserver == '') {
                  text = "SETTINGS={'quantity':" + quantity + "";
                } else {
                  text += ",'quantity':" + quantity + "";
                }
              }
              text += "}";
            } else if(type=='Plain') { // Plain text
              text = $form.find("textarea[name='contentText']").val();
            } else {
              if (text == '' || text == 'undefinided' || text == null) {
                alert('Please input deivce ID or URL!');
                flag = false;
              }
            }
            
            if (flag) {
              // attributes of image
              var size = $("[name='qrcodeSize']").val();
              var level = $("[name='qrcodeLevel']").val();
              
              // request URL
              var url = $form.attr("action");
              url += "?qrs=" + size + "&amp;qrl=" + level;
              url += "&amp;qrt=" + text;
              
              // update image
              var content = "<img src=\"" + url + "\" style=''/>";
              $("#qrimageresult").empty().append(content);
            } else {
              ;
            }
  });

  /*
   * $("#generatorForm").submit(function(event) { // Stop form from submitting
   * normally event.preventDefault(); // Get some values from elements on the
   * page: var $form = $(this); var text =
   * $form.find("textarea[name='contentText']").val(); var url =
   * $form.attr("action");
   * 
   * var content = "<img
   * src='http://localhost:8088/ECWebServer/qrgenerator?qrt=test&amp;qrs=200&amp;qrl=L'
   * style=''/>"; $("#qrimageresult").empty().append(content); // Send the data
   * using post var posting = $.post(url, { contentText : text }); // Put the
   * results in a div posting.done(function(data) { var content = "<img
   * src='http://localhost:8088/ECWebServer/qrgenerator?qrt=test&amp;qrs=200&amp;qrl=L'
   * style=''/>"; $("#qrimageresult").empty().append(content); }); });
   */

});
