$(document).ready(function () {
  "use strict";

  /*--------------------------------------
    One Page Navigation
    --------------------------------------*/
  $("#one-page-nav").onePageNav({
    currentClass: "active",
  });

  // $.localScroll();

  $(".fucking-scroll").click(function () {
    var clickedId = $(this).attr("href");
    $("html, body").animate({ scrollTop: $(clickedId).offset().top }, 1000);
    return false;
  });

  $(".unlock-premium").click(function () {
    $('.nav-tabs a[href="#premium"]').tab("show");
    return false;
  });

  //WOW
  var wow = new WOW({
    mobile: false,
  });
  wow.init();

  /*-------------------------------------
    Mobile Navigation
    -------------------------------------*/
  function mobileNavigatin() {
    if ($(window).width() < 992) {
      $("body").on("click", function (e) {
        if (
          $(".navbar-collapse").is(":visible") &&
          $(".navbar-toggle").is(":visible") &&
          !$(e.target).is(".dropdown")
        ) {
          $(".navbar-collapse").collapse("hide");
        }
      });

      $(".dropdown").unbind("click");
      $(".dropdown-menu").slideUp();
      $(".dropdown").on("click", function (sub) {
        sub.stopPropagation();
        $(this).children(".dropdown-menu").slideToggle();
        $(this).toggleClass("dropped");
      });
    } else {
      $(".dropdown-menu").css("display", "block");
    }
  }

  mobileNavigatin();

  /*--------------------------------------
    Plyr Video
    --------------------------------------*/
  plyr.setup();

  /*--------------------------------------
    Screenshot Carousel
    --------------------------------------*/
  $("#screenshot-carousel").owlCarousel({
    items: 5,
    itemsDesktop: [1199, 5],
    itemsDesktopSmall: [991, 3],
    itemsTablet: [767, 1],
    itemsMobile: [479, 1],
    slideSpeed: 200,
    autoPlay: 3000,
    stopOnHover: true,
    navigation: false,
    pagination: true,
  });

  /*--------------------------------------
    Testimonial Carousel
    --------------------------------------*/
  if ($("#testimonial-carousel").length > 0) {
    $("#testimonial-carousel").owlCarousel({
      singleItem: true,
      slideSpeed: 200,
      autoPlay: 3000,
      stopOnHover: true,
      navigation: true,
      navigationText: [
        '<i class="fa fa-angle-left"></i>',
        '<i class="fa fa-angle-right"></i>',
      ],
      pagination: false,
    });
  }

  /*--------------------------------------
    Magnific Popup
    --------------------------------------*/
  $(".image-large").magnificPopup({
    type: "image",
    gallery: {
      enabled: true,
    },
  });
  $(".video-play").magnificPopup({
    type: "iframe",
  });
  $.extend(true, $.magnificPopup.defaults, {
    iframe: {
      patterns: {
        youtube: {
          index: "youtube.com/",
          id: "v=",
          src: "http://www.youtube.com/embed/%id%?autoplay=1",
        },
      },
    },
  });

  /*-----------------------------------
    Contact Form
    -----------------------------------*/
  // Function for email address validation
  function isValidEmail(emailAddress) {
    var pattern = new RegExp(
      /^(("[\w-\s]+")|([\w-]+(?:\.[\w-]+)*)|("[\w-\s]+")([\w-]+(?:\.[\w-]+)*))(@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$)|(@\[?((25[0-5]\.|2[0-4][0-9]\.|1[0-9]{2}\.|[0-9]{1,2}\.))((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\.){2}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\]?$)/i
    );

    return pattern.test(emailAddress);
  }
  $("#contactForm").on("submit", function (e) {
    e.preventDefault();
    var data = {
      name: $("#name").val(),
      email: $("#email").val(),
      subject: $("#subject").val(),
      message: $("#message").val(),
    };

    if (
      isValidEmail(data["email"]) &&
      data["message"].length > 1 &&
      data["name"].length > 1 &&
      data["subject"].length > 1
    ) {
      $.ajax({
        type: "POST",
        url: "sendmail.php",
        data: data,
        success: function () {
          $("#contactForm .input-success").delay(500).fadeIn(1000);
          $("#contactForm .input-error").fadeOut(500);
        },
      });
    } else {
      $("#contactForm .input-error").delay(500).fadeIn(1000);
      $("#contactForm .input-success").fadeOut(500);
    }

    return false;
  });

  /*-----------------------------------
    Subscription
    -----------------------------------*/
  $(".subscription").ajaxChimp({
    callback: mailchimpResponse,
    url:
      "http://codepassenger.us10.list-manage.com/subscribe/post?u=6b2e008d85f125cf2eb2b40e9&id=6083876991", // Replace your mailchimp post url inside double quote "".
  });

  function mailchimpResponse(resp) {
    if (resp.result === "success") {
      $(".newsletter-success").html(resp.msg).fadeIn().delay(3000).fadeOut();
    } else if (resp.result === "error") {
      $(".newsletter-error").html(resp.msg).fadeIn().delay(3000).fadeOut();
    }
  }

  //Steller Parallax
  $(window).stellar({
    responsive: true,
    positionProperty: "position",
    horizontalScrolling: false,
  });

  //Window resize events
  $(window).on("resize orientationchange", function () {
    mobileNavigatin();
  });
});

/*--------------------------------------
Preloader    
--------------------------------------*/
$(window).on("load", function () {
  $("#loader-wrap").delay(200).fadeOut();
});
