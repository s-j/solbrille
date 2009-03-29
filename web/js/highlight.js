var theClass = "";

$(document).ready(
    function () {
        $('li.cluster').hide();

        $('ol#clusterlist > li').click( function () {
            $("li[class='cluster "+theClass+"']").slideToggle("slow")
            theClass = $(this).attr('class');
            $("li[class='cluster "+theClass+"']").slideToggle("slow")
        } )
      
    }
);
