
$(document).ready(function () {

// Slider de home.html
    $("#owl-big").owlCarousel({
        navigation: false, // Show next and prev buttons
        slideSpeed: 500,
        paginationSpeed: 800,
        singleItem: true

                // "singleItem:true" is a shortcut for:
                // items : 1, 
                // itemsDesktop : false,
                // itemsDesktopSmall : false,
                // itemsTablet: false,
                // itemsMobile : false

    });
    $("#owl-big").trigger('owl.play', 5500);
    
    $("#owl-small").owlCarousel({
        navigation: false, // Show next and prev buttons
        slideSpeed: 500,
        paginationSpeed: 800,
        singleItem: true

                // "singleItem:true" is a shortcut for:
                // items : 1, 
                // itemsDesktop : false,
                // itemsDesktopSmall : false,
                // itemsTablet: false,
                // itemsMobile : false

    });
    $("#owl-small").trigger('owl.play', 5500);

    $('#open-menu').click(function () {
        $(this).css({
            'display': 'none'
        });
        $('#close-menu').css({
            'display': 'block'
        });
        $('.menu').css({
            'height': 'auto'
        });

    });
    $('#close-menu, ul.menu>li>a').click(function () {
        var newWidth = $(window).width();
        if (newWidth > 650) {
            $('.menu').css({
                'height': 'auto'
            });
        } else {
            $('#close-menu').css({
                'display': 'none'
            });
            $('#open-menu').css({
                'display': 'block'
            });
            $('.menu').css({
                'height': '14px',
                'overflow': 'hidden'
            });
        }
        return false;
    });

// Activar el menú seleccionado cuando se navega por el submenú
    $("ul.dropdown").mouseover(function () {
        menuItem = $(this.parentNode).children(':first');
        menuItem.addClass('menuItemHover');
    }).mouseout(function () {
        menuItem.removeClass('menuItemHover');
    });
});
