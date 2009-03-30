var theClass = "";

/*
$(document).ready(
    function () {
        $('li.cluster').hide();
        $('li.cluster:first').show();
        theClass = $('li.cluster:first').attr('class');

        $('ol#clusterlist > li').click( function () {
            $("li[class='cluster "+theClass+"']").slideToggle("slow");
            theClass = $(this).attr('class');
            $("li[class='cluster "+theClass+"']").slideToggle("slow");
        } )
      
    }
);
*/

$(document).ready(
    function () {
        $('li.cluster').hide();
        $('li.cluster:first').show();

        $('ol#clusterlist > li').click(
            function () {
                $('li.cluster').hide();
                theClass = $(this).attr('class');
                $("li[class='cluster "+theClass+"']").slideToggle("slow");
            }
        )
    }
);
