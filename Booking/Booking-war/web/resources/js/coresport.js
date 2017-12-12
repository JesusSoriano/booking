
$(document).ready(function () {
    
// Show/hide top menu sections on resize
    $(window).resize(function () {
        var newWidth = $(window).width();
        if (newWidth > 963) {
            $('#nav').css({
                'height': 'auto'
            });
        } else {
            $('#close-menu').css({
                'display': 'none'
            });
            $('#open-menu').css({
                'display': 'block'
            });
            $('#nav').css({
                'height': '10px',
                'overflow': 'hidden'
            });
        }
    });


    // Show/hide top menu sections on click
    $('#open-menu').click(function () {
        $(this).css({
            'display': 'none'
        });
        $('#close-menu').css({
            'display': 'block'
        });
        $('#nav').css({
            'height': 'auto'
        });

    });
    $('#close-menu').click(function () {
        var newWidth = $(window).width();
        if (newWidth > 963) {
            $('#nav').css({
                'height': 'auto'
            });
        } else {
            $('#close-menu').css({
                'display': 'none'
            });
            $('#open-menu').css({
                'display': 'block'
            });
            $('#nav').css({
                'height': '10px',
                'overflow': 'hidden'
            });
        }
        return false;
    });
        $('ul#nav>li.dropdown-li>a').click(function () {
        $(this).parent().children('ul.dropdown').toggle();
    });
});
