$(document).ready(function () {
    'use strict';

    function initialize() {
        $(".google-map").each(function (index) {

            //Taking data attribute from map wrapper
            var mapLat = parseFloat($(this).data('lat'));
            var mapLng = parseFloat($(this).data('lng'));
            var mapZoom = parseInt($(this).data('zoom'));

            //Processing wrapper data attribute to coordinate
            var mapOptions = {
                center: {
                    lat: mapLat,
                    lng: mapLng
                },
                zoom: mapZoom,
                scrollwheel: false,
                styles: [
                    {
                        "featureType": "all",
                        "elementType": "labels.text.fill",
                        "stylers": [
                            {
                                "saturation": 36
            },
                            {
                                "color": "#000000"
            },
                            {
                                "lightness": 40
            }
        ]
    },
                    {
                        "featureType": "all",
                        "elementType": "labels.text.stroke",
                        "stylers": [
                            {
                                "visibility": "on"
            },
                            {
                                "color": "#000000"
            },
                            {
                                "lightness": 16
            }
        ]
    },
                    {
                        "featureType": "all",
                        "elementType": "labels.icon",
                        "stylers": [
                            {
                                "visibility": "off"
            }
        ]
    },
                    {
                        "featureType": "administrative",
                        "elementType": "geometry.fill",
                        "stylers": [
                            {
                                "color": "#000000"
            },
                            {
                                "lightness": 20
            }
        ]
    },
                    {
                        "featureType": "administrative",
                        "elementType": "geometry.stroke",
                        "stylers": [
                            {
                                "color": "#000000"
            },
                            {
                                "lightness": 17
            },
                            {
                                "weight": 1.2
            }
        ]
    },
                    {
                        "featureType": "landscape",
                        "elementType": "geometry",
                        "stylers": [
                            {
                                "color": "#000000"
            },
                            {
                                "lightness": 20
            }
        ]
    },
                    {
                        "featureType": "poi",
                        "elementType": "geometry",
                        "stylers": [
                            {
                                "color": "#000000"
            },
                            {
                                "lightness": 21
            }
        ]
    },
                    {
                        "featureType": "road.highway",
                        "elementType": "geometry.fill",
                        "stylers": [
                            {
                                "color": "#000000"
            },
                            {
                                "lightness": 17
            }
        ]
    },
                    {
                        "featureType": "road.highway",
                        "elementType": "geometry.stroke",
                        "stylers": [
                            {
                                "color": "#000000"
            },
                            {
                                "lightness": 29
            },
                            {
                                "weight": 0.2
            }
        ]
    },
                    {
                        "featureType": "road.arterial",
                        "elementType": "geometry",
                        "stylers": [
                            {
                                "color": "#000000"
            },
                            {
                                "lightness": 18
            }
        ]
    },
                    {
                        "featureType": "road.local",
                        "elementType": "geometry",
                        "stylers": [
                            {
                                "color": "#000000"
            },
                            {
                                "lightness": 16
            }
        ]
    },
                    {
                        "featureType": "transit",
                        "elementType": "geometry",
                        "stylers": [
                            {
                                "color": "#000000"
            },
                            {
                                "lightness": 19
            }
        ]
    },
                    {
                        "featureType": "water",
                        "elementType": "geometry",
                        "stylers": [
                            {
                                "color": "#000000"
            },
                            {
                                "lightness": 17
            }
        ]
    }
]
            };

            //Initiating map
            var map = new google.maps.Map(this, mapOptions);

            //Map Marker
            var marker = new google.maps.Marker({
                position: new google.maps.LatLng(mapLat, mapLng),
                map: map,
                icon: 'images/map-marker.png'
            });
        });
    }
    google.maps.event.addDomListener(window, 'load', initialize);
});
