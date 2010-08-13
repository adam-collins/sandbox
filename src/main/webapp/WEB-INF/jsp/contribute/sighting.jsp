<%-- 
    Document   : sighting
    Created on : Aug 6, 2010, 5:19:21 PM
    Author     : "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="/common/taglibs.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<c:set var="googleKey" scope="request"><ala:propertyLoader bundle="biocache" property="googleKey"/></c:set>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta charset="UTF-8" >
        <title>Contribute a Sighting</title>
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/date.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery.datePicker.js"></script>
        <link rel="stylesheet" type="text/css" media="screen" href="${pageContext.request.contextPath}/static/css/datePicker.css" />
        <script type="text/javascript" src="http://www.google.com/jsapi?key=${googleKey}"></script>
        <style type="text/css" >
            #mapCanvas {
                width: 315px;
                height: 315px;
            }
        </style>
        <script type="text/javascript">
            // Load Google maps via AJAX API
            google.load("maps", "3", {other_params:"sensor=false"});

            var geocoder, zoom;

//            google.setOnLoadCallback(function() {
//                geocoder = new google.maps.Geocoder();
//            });

            function geocodePosition(pos) {
                geocoder.geocode({
                    latLng: pos
                }, function(responses) {
                    if (responses && responses.length > 0) {
                        updateMarkerAddress(responses[0].formatted_address);
                    } else {
                        updateMarkerAddress('Cannot determine address at this location.');
                    }
                });
            }

            function updateMarkerStatus(str) {
                document.getElementById('markerStatus').innerHTML = str;
            }

            function updateMarkerPosition(latLng) {
                document.getElementById('info').innerHTML = [
                    latLng.lat(),
                    latLng.lng()
                ].join(', ');
                $('#markerLatitude').html(latLng.lat());
                $('#sightingLatitude').val(latLng.lat());
                $('#markerLongitude').html(latLng.lng());
                $('#sightingLongitude').val(latLng.lng());
            }

            function updateMarkerAddress(str) {
                document.getElementById('markerAddress').innerHTML = str;
                $('#sightingLocation').val(str);
            }

            function initialize() {
                var lat = $('input#latitude').val();
                var lng = $('input#longitude').val();
                var latLng = new google.maps.LatLng(lat, lng);
                var map = new google.maps.Map(document.getElementById('mapCanvas'), {
                    zoom: zoom,
                    center: latLng,
                    mapTypeId: google.maps.MapTypeId.HYBRID
                });
                var marker = new google.maps.Marker({
                    position: latLng,
                    title: 'Sighting',
                    map: map,
                    draggable: true
                });

                // Update current position info.
                updateMarkerPosition(latLng);
                geocodePosition(latLng);

                // Add dragging event listeners.
                google.maps.event.addListener(marker, 'dragstart', function() {
                    updateMarkerAddress('Dragging...');
                });

                google.maps.event.addListener(marker, 'drag', function() {
                    updateMarkerStatus('Dragging...');
                    updateMarkerPosition(marker.getPosition());
                });

                google.maps.event.addListener(marker, 'dragend', function() {
                    updateMarkerStatus('Drag ended');
                    geocodePosition(marker.getPosition());
                });
            }

            // Onload handler to fire off the app.
            //google.maps.event.addDomListener(window, 'load', initialize);
            /**
             * Try to get a lat/long using HTML5 geoloation API
             */
            function attemptGeolocation() {
                // HTML5 GeoLocation
                if (navigator && navigator.geolocation) {
                    //alert("trying to get coords with navigator.geolocation...");  
                    function getPostion(position) {  
                        //alert('coords: '+position.coords.latitude+','+position.coords.longitude);
                        $('#latitude').val(position.coords.latitude);
                        $('#longitude').val(position.coords.longitude);
                        zoom = 15;
                        //codeAddress(true);
                        initialize();
                    }
                    navigator.geolocation.getCurrentPosition(getPostion);
                } else if (google.loader && google.loader.ClientLocation) {
                    // Google AJAX API fallback GeoLocation
                    //alert("getting coords using google geolocation");
                    $('#latitude').val(google.loader.ClientLocation.latitude);
                    $('#longitude').val(google.loader.ClientLocation.longitude);
                    zoom = 11;
                   // codeAddress(true);
                   initialize();
                } else {
                    //alert("Client geolocation failed");
                    //codeAddress();
                    zoom = 10;
                    initialize();
                }
            }

            /**
             * Document onLoad event using JQuery
             */
            $(document).ready(function() {
                // geocoding
                geocoder = new google.maps.Geocoder();
                attemptGeolocation();

                $('#sightingDate').datePicker({startDate:'01/01/1996'}).val(new Date().asString()).trigger('change');
            });
        </script>
    </head>
    <body>
        <div id="header">
            <div id="breadcrumb">
                <a href="http://test.ala.org.au">Home</a>
                <a href="http://test.ala.org.au/explore">Contribute</a>
                Contribute a Sighting
            </div>
            <h1>Contribute a Sighting</h1>
        </div>
        
            <c:choose>
                <c:when test="${!empty pageContext.request.remoteUser}"><%-- User is logged in --%>
                    <c:if test="${not empty taxonConcept}">
                        <form name="sighting" id="sighting" action="" method="GET">
                            <div id="column-one">
                                <div class="section">
                                    <div style="float: right; padding-right: 15px" id="images"><img src="${taxonConcept.imageThumbnailUrl}" height="85px" alt="species thumbnail"/></div>
                                    <fieldset id="sightingInfo">
                                        <p><label>Species Name</label> <alatag:formatSciName name="${taxonConcept.scientificName}" rankId="${taxonConcept.rankId}"/>
                                            (${taxonConcept.commonName})
                                            <input type="hidden" name="guid" id="sightingGuid" value="${param['guid']}"/>
                                            </p>
                                            <p><label for="date">Date</label>
                                                <input type="text" id="sightingDate" name="date" size="20" value=""/>
                                                <span>(DD-MM-YYYY)</span>
                                            </p>
                                            <p><label for="number">Number observed</label>
                                            <%--<input type="text" id="sightingNumber" name="number" size="20" value="1"/>--%>
                                            <select id="sightingNumber" name="number">
                                                <option ></option>
                                            </select>
                                            </p>
                                            <p><label for="location">Location</label>
                                            <span id="markerAddress"></span>
                                            <input type="hidden" id="sightingLocation" name="location" size="20" value=""/>
                                            </p>
                                            <p><label for="latitude">Latitude</label>
                                            <span id="markerLatitude"></span>
                                            <input type="hidden" id="sightingLatitude" name="latitude" size="20" value=""/>
                                            </p>
                                            <p><label for="longitude">Longitude</label>
                                            <span id="markerLongitude"></span>
                                            <input type="hidden" id="sightingLongitude" name="longitude" size="20" value=""/>
                                            </p>
                                            <p><label for="notes">Notes</label>
                                            <textarea id="sightingNotes" name="notes" cols="40" rows="5"></textarea>
                                            </p>
                                            <p><label for=""></label>
                                            <input type="submit" id="sightingSubmit" value="Next"/>
                                            </p>
                                    </fieldset>
                                    
                                </div>
                            </div>
                            <div id="column-two">
                                <div class="section">

                                        <div id="sightingAddress">
                                            <label for="address">Enter sighting location or address:</label>
                                            <input name="address" id="address" size="40" value="${address}"/>
                                            <input id="locationSearch" type="button" value="Search"/>
                                            <input type="hidden" name="latitude" id="latitude" value="${latitude}"/>
                                            <input type="hidden" name="longitude" id="longitude" value="${longitude}"/>
                                            <input type="hidden" name="location" id="location" value="${location}"/>
                                        </div>
                                        <div id="mapCanvas"></div>
                                        <div id="markerAddress"></div>
                                        <div id="markerStatus" style="display: none"></div>
                                        <div id="info"></div>

                                </div>
                            </div>
                        </form>
                    </c:if>
                    <c:if test="${not empty error}">
                        <div class="section">${error}</div>
                    </c:if>
                </c:when>
                <c:otherwise><%-- User is NOT logged in --%>
                    <c:set var="queryString" value="${pageContext.request.queryString}"/>
                    <c:choose>
                        <c:when test="${empty queryString}">
                            <c:set var="requestUrl" value="${pageContext.request.requestURL}"/>
                        </c:when>
                        <c:otherwise>
                            <c:set var="requestUrl" value="${pageContext.request.requestURL}?${fn:replace(queryString, '+', '%2B')}"/>
                        </c:otherwise>
                    </c:choose>
                    <div style="border-top: 1px solid #DDD; margin-top: 10px">&nbsp;</div>
                    <div>You are not logged in. <ala:loginLogoutLink returnUrlPath="${requestUrl}"/></div>
                </c:otherwise>
            </c:choose>
    </body>
</html>