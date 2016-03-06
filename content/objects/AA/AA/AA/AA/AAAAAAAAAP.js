// make our own namespace...
if (typeof $SL == "undefined")
    var $SL = {};
else
    alert( "$SL namespace conflict!");


// the object that holds data about a page...
$SL.PAGE = {
    IMAGES: {}   // property names are image ID; value is an object with the image metadata...
};


// initialize the app (called after all HTML is loaded)...
$SL.init = function() {

    // fix up appearance of top bar items...
    var dn = $("#displayName");
    var si = $("#settingsImg");
    var um = $("#unauthMenu");
    var am = $("#authMenu");
    si.height( dn.height() *.6 );
    si.css( "padding-top", (dn.height() - si.height()) / 2 );
    um.css( "padding-top", 6 + (dn.height() - um.height()) / 2);
    am.css( "padding-top", 6 + (dn.height() - am.height()) / 2);

    {{if(not(user.isAuthenticated))}}
        // set up the sign in and sign up dialogs...
        $("#signIn").on( "click", function() {
            var signIn = new $SL.SignIn();
            signIn.show();
        });
        $("#signUp").on( "click", function() {
            var signUp = new $SL.SignUp();
            signUp.show();
        });
    {{end}}
    {{if(user.isAuthenticated)}}
        // set up the sign out dialog...
        $("#signOut").on( "click", function() {
            var signOut = new $SL.SignOut();
            signOut.show();
        });
    {{end}}


    // initialize any images we have...
    $SL.initImages();

};

$SL.initImages = function() {

    // our line size and spacing, based on a 16pt font size...
    var lh = 1.2 * 16;

    // get our image divs...
    var imageIDs = {};
    var imgs = $(".SL_IMAGE");
    imgs.each( function() {

        var img = $(this);

        // get our image ids...
        var iids = img.attr("iids" ).split(',');
        var mult = iids.length > 1;

        // compute the height and position of this element...
        var isRight = (img.attr("ipos" ).toLowerCase() == 'r');
        var h = Math.floor(img.attr("ih") * lh - 5);  // subtract a few pixels to give us a small margin for error...

        // count how many we've loaded...
        var imgsLoaded = 0;

        // start loading all the images and building our image info structure...
        // we need to load them ALL before showing anything, as we don't know the width...
        var imgsInfo = [];
        for(var i = 0; i < iids.length; i++) {
            var iid = iids[i];

            // get an image object and start loading it...
            var newImg = new Image();
            newImg.onload = onImageLoad;
            newImg.src = '/' + iid + '?h=' + h;

            // stuff it away in our info structure...
            var imgInfo = {};
            imgInfo.img = newImg;
            imgInfo.iid = iid;
            imgsInfo.push( imgInfo );

            imageIDs[iid] = true;
        }

        // start with display index of zero and a horizontal position of zero...
        var indx = 0;
        var hpos = 0;

        function onImageLoad() {
            imgsLoaded++;
            if( imgsLoaded >= iids.length )
                showImage();
        }

        function showImage() {

            // figure out the widest image we've got...
            var w = 0;
            for( var i = 0; i < imgsInfo.length; i++ ) {
                var imgInfo = imgsInfo[i];
                var iiw = Math.floor(imgInfo.img.width * h / imgInfo.img.height);
                if( iiw > w ) w = iiw;
            }

            // if our width is more than 250% of the height, we need to scroll it (and limit the width)...
            var isScrolling = false;
            if( w > 1.5 * h ) {
                w = Math.floor(1.5 * h);
                isScrolling = true;
            }

            // insert the HTML we need to support this image thumbnail...
            img.append('<div class="lWing"><canvas></canvas></div><div class="iHolder"></div><div class="rWing"><canvas></canvas></div>' +
                '<div class="imgOver"><div class="imgPlus">+</div><div class="imgInfo"><img src="AAAAAAAAARA"></div></div>');

            img.addClass( isRight ? "R" : "L" );

            var lw = img.find('.lWing');
            var rw = img.find('.rWing');
            var ih = img.find('.iHolder');

            if( mult ) {
                lw.height( h );
                rw.height( h );
                lw.css( "display", "inline-block" );
                rw.css( "display", "inline-block" );

                var can = lw.children('canvas')[0];
                can.width = 12;
                can.height = h;
                var c2 = can.getContext('2d');
                c2.fillStyle = '#FF8000';
                c2.beginPath();
                c2.moveTo(lw.width(), 0);
                c2.lineTo(lw.width(), lw.height());
                c2.lineTo(0, lw.height()/2);
                c2.closePath();
                c2.fill();

                can = rw.children('canvas')[0];
                can.width = 12;
                can.height = h;
                c2 = can.getContext('2d');
                c2.fillStyle = '#FF8000';
                c2.beginPath();
                c2.moveTo(0, 0);
                c2.lineTo(0, rw.height());
                c2.lineTo(rw.width(), rw.height()/2);
                c2.closePath();
                c2.fill();

                lw.on( "click", prevPic );
                rw.on( "click", nextPic );

                // these prevent double-click on the arrows from selecting text...
                lw.mousedown(function(e){ e.preventDefault(); });
                rw.mousedown(function(e){ e.preventDefault(); });
            }

            ih.width( w );

            var ii = $(imgsInfo[indx].img);
            ii.height( h );
            ih.append( ii );

            // delay a little to allow the rendering to finish...
            setTimeout( posIt, 500 );

            function posIt() {
                var io = img.find( '.imgOver' );
                var p = ii.offset();
                io.css( "top", p.top );
                io.css( "left", p.left );
                io.height( ii.height() );
                io.width( Math.min(ii.width(), ih.width()) );
                var ip = io.children( '.imgPlus' );
                ip.css( "font-size", ih.height() * .4 );
                ip.css( "margin-top", ih.height() * .25 );
                var iif = io.children( ".imgInfo" );
                var ig = iif.find( "img" );
                ig.height( ih.height() * .3 );

                iif.off("click").on( "click", onInfo );
                ip.off("click").on( "click", onPlus );

                function onInfo( event ) {
                    var balloon = new $SL.Balloon();
                    balloon.show( ii[0], $SL.getImageInfo(imgsInfo[indx].iid) );
                    event.preventDefault();
                    event.stopPropagation();
                }

                function onPlus( event ) {

                    // find the first ancestor that is either a post or the body...
                    var parent = img;
                    while( (parent.prop('tagName') != 'BODY') && !parent.hasClass('SL_POST') )
                        parent = parent.parent();

                    // now find all the images and accumulate their IDs...
                    var vimgs = parent.find('.SL_IMAGE');
                    var vids = [];
                    vimgs.each( function() {
                        vids = vids.concat( $( this ).attr( "iids" ).split( ',' ) );
                    });

                    // finally we're ready to put up the viewer...
                    var viewer = new $SL.PhotoViewer( imgsInfo[indx].iid, vids );
                    viewer.show();

                    event.preventDefault();
                    event.stopPropagation();
                }
            }

            function nextPic( event ) {
                indx++;
                if( indx >= imgsInfo.length ) indx = 0;
                changePic();
                event.preventDefault();
                event.stopPropagation();
            }

            function prevPic( event ) {
                indx--;
                if( indx < 0) indx = imgsInfo.length - 1;
                changePic();
                event.preventDefault();
                event.stopPropagation();
            }

            function changePic() {
                ii = $(imgsInfo[indx].img);
                ii.height( h );
                ih.children('img' ).remove();
                ih.append( ii );
                posIt();
            }
        }
    });

    // now request the metadata...
    var iids = [];
    for( var iid in imageIDs ) iids.push(iid);
    var req = { images: iids };
    $.post( "AAAAAAAAA1A", JSON.stringify( req ), response, "json" );

    function response( data ) {
        $SL.PAGE.IMAGES = data.images;
    }
};

// iid is the string image ID (which is the key to the image information record)...
$SL.getImageInfo = function( iid ) {

    var ii = $SL.PAGE.IMAGES[iid];
    if( !ii ) return "No information available.";

    var result = '';

    if( ii.title )             result += '<p class="iiTitle">'                           + ii.title             + '</p>';
    if( ii.description )       result += '<p class="iiDescription">'                     + ii.description       + '</p>';
    if( ii.credit )            result += '<p class="iiCredit">Credit: '                  + ii.credit            + '</p>';
    if( ii.source )            result += '<p class="iiSource">Source: '                  + ii.source            + '</p>';
    if( ii.where )             result += '<p class="iiLocation">Location: '              + ii.where             + '</p>';
    if( ii.when )              result += '<p class="iiWhen">When: '                      + ii.when              + '</p>';
    if( ii.cameraSettings )    result += '<p class="iiSettings">Camera settings: '       + ii.cameraSettings    + '</p>';
    if( ii.cameraOrientation ) result += '<p class="iiOrientation">Camera orientation: ' + ii.cameraOrientation + '</p>';

    result += '<p class="iiInfo">This image is available in its <a target="_blank" href="' + iid + '">full resolution</a>: ' + $SL.commaize(ii.width)
        + 'w x ' + $SL.commaize(ii.height) + 'h (' + $SL.commaize(ii.size) + ' bytes).</p>';

    return result;
};

// put commas in the given integer...
$SL.commaize = function( number ) {
    var i = "" + number;
    var j = (j = i.length) > 3 ? j % 3 : 0;
    return (j ? i.substr(0, j) + ',' : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + ',');

};

// attach info event handlers...
$SL.setupInfo = function( base ) {

    var searchBase = (!!base) ? base : $(document);
    searchBase.find(".info").on("click", function(event) {

        // save our DOM reference...
        var that = this;

        // request info from key...
        var req = {};
        req.key = this.getAttribute('key');
        $.post( "AAAAAAAAAUA", JSON.stringify( req ), response,  "json");


        function response( data ) {

            if( !data.info ) return;

            var balloon = new $SL.Balloon();
            balloon.show( that, data.info );
        }
    });
};


// photo viewr class...
$SL.PhotoViewer = function( iid, ids ) {

    this.current = iid;
    this.html = '<div class="viewer"><table class="viewerTable">' +
        '<tr class="topRow"><td class="photo"><div class="picHolder"></div></td><td class="info"><div class="infoHolder"></div></td></tr>' +
        '<tr class="thumbRow"><td class="thumbs" colspan="2"></td></tr>' +
        '</table><div class="closer">&#x2612;</div></div>';
    this.fader = new $SL.Fader(1.0);

    this.show = function() {

        this.fader.show();
        var body = $('body');
        body.append(this.html);
        body.on("keydown", this, onKeydown);

        $('div.closer' ).on("click", this, this.hide);

        // make the viewer occupy the entire screen...
        var table = $('table.viewerTable');
        table.height( $(window).height() );
        table.width( $(window).width() );

        // figure out the height of our full-size photos...
        this.h = $('td.photo').height() - 20;  // the 20 pixels is to make room for the scroll bar...

        // build our info array and thumbs html...
        pics = [];
        index = 0;
        var thumbs = $('td.thumbs');
        for( var i = 0; i < ids.length; i++ ) {

            var info = {};
            info.iid = ids[i];
            var img = new Image();
            img.src = '/' + info.iid + '?h=' + this.h;
            info.img = img;
            var jImg = $(img);
            jImg.on("load", this, setHeight );
            pics.push( info );

            var thumb = new Image();
            thumb.src = '/' + info.iid + '?h=50';
            thumb.id = 'thumb'+i;
            var jThumb = $(thumb);
            jThumb.on("click", i, onClick);
            thumbs.append( jThumb );

            if( this.current == ids[i])
                index = i;
        }

        choose( index );

        function choose( newIndex ) {
            $('#thumb' + index).removeClass('selected');
            index = newIndex;
            $('#thumb' + index).addClass('selected');
            $('div.picHolder img' ).remove();
            $('div.picHolder' ).append(pics[index].img);

            $('div.infoHolder').html($SL.getImageInfo(pics[index].iid));
        }

        function onClick( event ) {
            choose( event.data );
        }

        function setHeight( event ) {
            this.height = Math.min( event.data.h, this.height * 2 );
        }

        function onKeydown( event ) {
            var that = event.data;

            switch( event.which ) {

                case 9: // tab key (which could be shifted for backtab)...
                    if( event.shiftKey )
                        choose( (pics.length + index - 1) % pics.length );
                    else
                        choose( (index + 1) % pics.length );
                    choose(index);
                    break;

                case 27:  // escape key...
                    that.hide(event);
                    break;

                case 37: // left arrow key...
                    choose( (pics.length + index - 1) % pics.length );
                    break;

                case 39: // right arrow key...
                    choose( (index + 1) % pics.length );
                    break;

                default:
                    return true;
            }
            return false;
        }
    };

    this.hide = function( event ) {
        var that = event.data;
        that.fader.hide();
        $('div.viewer').remove();
        $('body').off("keydown");
    };
};


// balloon class...
$SL.Balloon = function() {

    this.prefix = '<div id="balloon"><div id="balloonStopper"><canvas id="balloonCanvas"></div><div id="balloonText">';
    this.suffix = '</div></div>';

    $("#balloon").remove();

    // show the balloon (target is DOM object being explained, or object with "area" attribute, text is explanatory HTML)...
    this.show = function( target, text ) {

        // insert the html...
        var html = this.prefix + text + this.suffix;
        $("body" ).append( html );

        // event handlers to dismiss the balloon...
        $("#balloon").on("click", this, this.hide);
        $(document).on("keydown", this, this.hide );

        // some basic things we'll need...
        var canvas = $("#balloonCanvas")[0];
        var bs = $("#balloonStopper");
        var t = $("#balloonText");

        // get the area being consumed...
        var w = t.width();
        var h = t.height();
        var a = w * h;

        // now normalize it to (roughly) a square, with a minimum width of 120px...
        w = Math.sqrt(a);
        t.width( Math.max(120, w) );

        // get the actual outer size, post-normalization...
        w = t.outerWidth();
        h = t.outerHeight();

        // get the area of our target (actually, slightly bigger than our target)...
        var tgt = target.getAttribute("area") ? $("#" + target.getAttribute("area")) : $(target);
        var tgt_l = tgt.offset().left - 3;
        var tgt_t = tgt.offset().top - 3;
        var tgt_w = tgt.width() + 6;
        var tgt_h = tgt.height() + 6;
        var tgt_lc = tgt_l + tgt_w/2;
        var tgt_tc = tgt_t + tgt_h/2;

        // figure out where we have room for this thing...
        var tgt_r = Math.sqrt( Math.pow((tgt_w/2),2) + Math.pow((tgt_h/2),2) );
        var r = Math.sqrt( Math.pow((w/2),2) + Math.pow((h/2),2) );
        var mo = 25;  // minimum space between balloon and target, in pixels...

        // will we fit at our preferred location (above left)?
        var p = getTrial(225*Math.PI/180);
        if( !p.ok ) {
            // how about above right?
            p = getTrial(315*Math.PI/180);
            if( !p.ok ) {
                // ok, lower left?
                p = getTrial(135*Math.PI/180);
                if( !p.ok ) {
                    // geez, how about lower right?
                    p = getTrial(45*Math.PI/180);
                    if( !p.ok ) {
                        // ok, screw it - cram it directly above or below, wherever we have the most room...
                        if( tgt_t >= (bs.height() - (tgt_t + tgt_h)) ) {
                            p.ct = tgt_tc - (10 + (tgt_h + h)/2);
                            p.cl = tgt_lc;
                            p.left = tgt_lc - w/2;
                            p.top = tgt_t - (10 + h);
                        }
                        else {
                            p.ct = tgt_tc + 10 + (tgt_h + h)/2;
                            p.cl = tgt_lc;
                            p.left = tgt_lc - w/2;
                            p.top = tgt_t + tgt_h + 10;
                        }
                    }
                }
            }
        }

        // now position it where we want it...
        t.css("top", p.top);
        t.css("left", p.left);

        // highlight the target...
        canvas.width = bs.width();
        canvas.height = bs.height();
        var ctx = canvas.getContext("2d");
        ctx.fillStyle = 'rgba(0,250,0,0.2)';
        ctx.fillRect(tgt_l, tgt_t, tgt_w, tgt_h);

        // paint a line from text to target, center-to-center...
        ctx.strokeStyle = 'rgba(0,250,0,0.2)';
        ctx.lineWidth = 5;
        ctx.beginPath();
        ctx.moveTo(tgt_lc, tgt_tc);
        ctx.lineTo( p.cl, p.ct);
        ctx.stroke();

        // returns trial position for balloon text at the given angle from the target; ok property is true if it will fit...
        function getTrial( angle ) {
            var result = {};
            var tr = mo + tgt_r + r;
            result.ct = tgt_tc + Math.sin(angle) * tr;
            result.cl = tgt_lc + Math.cos(angle) * tr;
            result.left = result.cl - w/2;
            result.top = result.ct - h/2;
            result.ok = (result.top >= 0) && (result.left >= 0) && (result.top + h <= bs.height()) && (result.left + w <= bs.width());
            return result;
        }
    };


    // hide the balloon...
    this.hide = function() {
        $(document ).off("keydown");
        $("#balloon").remove();
    };
};


// background fader class...
$SL.Fader = function( opacity ) {

    this.opa = !!opacity ? opacity : 0.6;

    if( !$("#fader").length )
        $("body" ).append('<div id="fader"></div>');

    this.show = function() {
        var fader = $("#fader");
        fader.on("click", false);  // prevent mouse clicks from affecting faded-out stuff...
        fader.fadeTo(500, this.opa);

    };


    this.hide = function() {
        $("#fader").fadeOut(500);
    };
};


// modal dialog class...
$SL.Dialog = function( context, _inner, onCancel, onOK ) {

    this.context = context;
    this.inner = _inner;
    this.onCancel = onCancel;
    this.onOK = onOK;
    this.prefix = '<div id="modalDialog"><div id="modalDialogInner">';
    this.suffix = '<div id="modalDialogButtons"></div></div></div>';
    this.fader = new $SL.Fader();
    this.inputs = [];
    this.currentFocus = 0;
    this.onInput = null;
    this.onFocus = null;

    $("#modalDialog" ).remove();

    // given a list of jQuery objects that are inputs, registers keyboard events and handles tabbing (in given order)...
    // if no inputs are present, still handles the buttons...
    this.setInputs = function( inputs ) {

        // save our inputs and bind event handlers...
        for( var i = 0; i < arguments.length; i++ ) {
            var input = arguments[i];
            this.inputs.push(input);
            input.on( "keydown", this, this.onKeydown );
            input.on( "input", this, this.onInternalInput );
            input.on( "focus", this, this.onInternalFocus );
        }

        // bind keydown to the body, in case we have a dialog with only buttons...
        $('body').on( "keydown", this, this.onKeydown );

        this.setFocus(0);
    };

    this.setFocus = function( newFocus ) {
        if( this.inputs.length == 0 ) return;
        this.currentFocus = newFocus;
        this.inputs[this.currentFocus].focus();
    };

    this.onKeydown = function( event ) {
        var that = event.data;
        var e;

        switch( event.which ) {

            case 9: // tab key (which could be shifted for backtab)...
                if( event.shiftKey )
                    that.setFocus.call(that, (that.currentFocus == 0) ? that.inputs.length - 1 : that.currentFocus - 1 );
                else
                    that.setFocus.call(that, (that.currentFocus >= that.inputs.length - 1) ? 0 : that.currentFocus + 1 );
                break;

            case 13:  // enter/return key...
                if( that.getOK().hasClass("default") ) {

                    // simulate a click on the ok button...
                    e = jQuery.Event( "click" );
                    e.data = that;
                    that.getOK().triggerHandler( e );
                }
                else
                    return true;
                break;

            case 27:  // escape key...

                // simulate a click on the cancel button...
                e = jQuery.Event( "click" );
                e.data = that;
                that.getCancel().triggerHandler( e );
                break;

            default:
                return true;
        }
        return false;
    };

    this.onInternalInput = function( event ) {
        var that = event.data;
        if( that.onInput )
            that.onInput.call(that.context, event);
        return false;
    };

    this.onInternalFocus = function( event ) {
        var that = event.data;
        if( that.onFocus )
            that.onFocus.call(that.context, event);
        return false;
    };

    // the width function (optional) returns the desired width of the pop-up's interior
    this.show = function( widthFunction ) {
        var html = this.prefix + this.inner + this.suffix;
        $("body").append(html);
        if( this.onOK ) {
            $("#modalDialogButtons").append('<div id="modalDialogOK" class="disabled">OK</div>');
            $("#modalDialogOK").on( "click", this, this.internalOnOK );
        }
        if( this.onCancel ) {
            $("#modalDialogButtons").append('<div id="modalDialogCancel" class="disabled">Cancel</div>');
            $("#modalDialogCancel").on( "click", this, this.internalOnCancel );
        }
        this.fader.show();
        var d = $("#modalDialog");
        d.css( "display", "block" );
        if( widthFunction )
            d.width( widthFunction() + 20 );
        var h = d.height();
        var w = d.width();
        d.css( "margin-top", -h/2 );
        d.css( "margin-left", -w/2 );
        d.height( h );
        d.width( w );
    };


    this.changeButtonState = function( button, state ) {
        $("#modalDialog" + button ).removeClass().addClass(state);
    };


    this.internalOnCancel = function( event ) {
        var that = event.data;
        if( $("#modalDialogCancel" ).hasClass("disabled") ) return true;
        if(that.onCancel ) that.onCancel.call(that.context);
        $("#modalDialog" ).css("display", "none");
        that.fader.hide();
        $("body").off("keydown");
        return false;
    };


    this.internalOnOK = function( event ) {
        var that = event.data;
        if( $("#modalDialogOK" ).hasClass("disabled") ) return true;
        if( that.onOK ) that.onOK.call(that.context);
        $("#modalDialog" ).css("display", "none");
        that.fader.hide();
        $("body").off("keydown");
        return false;
    };


    this.disableOK = function() {
        this.changeButtonState("OK", "disabled");
    };


    this.enableOK = function() {
        this.changeButtonState("OK", "enabled");
    };


    this.enableCancel = function() {
        this.changeButtonState("Cancel", "enabled");
    };


    this.defaultOK = function() {
        this.changeButtonState("OK", "default");
    };


    this.getOK = function() {
        return $("#modalDialogOK");
    };


    this.getCancel = function() {
        return $("#modalDialogCancel");
    };
};


// message dialog class...
$SL.Message = function( msg, isAlert ) {

    this.html = (isAlert ? '<div class="alert"><img src="AAAAAAAAASA"></div><br/>' : '') + '<div class="message">' + msg + '</div>';

    this.show = function() {
        this.dialog = new $SL.Dialog( this, this.html, null, this.onOK );
        this.dialog.show();
        this.dialog.defaultOK();
    };

    this.onOK = function() {};
};


// sign-in dialog class...
$SL.SignIn = function() {

    this.html =
        '<table id="signIn" class="dialogGuts">' +
          '<tr><td colspan="3"><b>Sign In</b> to the <i>{{blog.displayName}}</i> blog.</td></tr>' +
          '<tr id="usernameRow">' +
            '<td class="label">Username:</td>' +
            '<td><input id="username" class="text" type="text" size="30"></td>' +
            '<td><img id="usernameInfo" class="info" key="username" area="username" src="AAAAAAAAARA"></td>' +
          '</tr>' +
          '<tr id="passwordRow">' +
            '<td class="label">Password:</td>' +
            '<td><input id="password" class="text" type="password" size="30"></td>' +
            '<td><img id="passwordInfo" class="info" key="password" area="password" src="AAAAAAAAARA"></td>' +
          '</tr>' +
          '<tr id="rememberMeRow">' +
            '<td class="label">Remember Me:</td>' +
            '<td><input id="rememberMe" class="checkbox" type="checkbox"></td>' +
            '<td><img id="rememberMeInfo" class="info" key="rememberMe" area="rememberMe" src="AAAAAAAAARA"></td>' +
          '</tr>' +
        '</table>';


    this.show = function() {
        this.dialog = new $SL.Dialog( this, this.html, this.onCancel, this.onOK );
        this.dialog.show( function() {
            return $("table#signIn").width();
        });
        this.username = $("#username");
        this.password = $("#password");
        this.rememberMe = $("#rememberMe");
        this.dialog.setInputs( this.username, this.password, this.rememberMe );
        this.dialog.onInput = this.onInput;
        $SL.setupInfo( $("table#signIn"));
        this.dialog.enableCancel();
    };


    this.onInput = function() {
        var ok = this.dialog.getOK();
        if( this.username.val() && this.password.val() && !ok.hasClass("default") )
            this.dialog.defaultOK();
        if( !(this.username.val() && this.password.val()) && !ok.hasClass("disabled") )
            this.dialog.disableOK();
        return false;
    };

    this.onCancel = function() {
    };

    this.onOK = function() {
        var req ={};
        req.user = this.username.val();
        req.password = this.password.val();
        req.rememberMe = this.rememberMe.is(':checked');
        $.post( "AAAAAAAAAGB",

            JSON.stringify( req ),

            function( data ) {
                if( data.success ) {
                    window.location.href = "/";
                    window.location.reload();
                }
                else {
                    setTimeout( function() {var msg = new $SL.Message( data.reason, true ); msg.show();}, 500);
                }
            },

            "json");
    };
};


// sign-up dialog class...
        $SL.SignUp = function() {

            this.html =
                '<b>Sign Up</b> as a member of the <i>{{blog.displayName}}</i> blog community.<br/><br/>By signing up, you\'ll be able to ' +
                'comment on blog posts, and personalize your experience on the blog.  Click the information icons below for details.' +
                '<table id="signUpTable" class="dialogGuts">' +
                '<tr id="emailRow">' +
                '<td class="label">Your personal email address:</td>' +
                '<td><input id="email" class="text" type="text" size="30"></td>' +
                '<td><img id="emailInfo" class="info" key="emailSignUp" area="email" src="AAAAAAAAARA"></td>' +
                '</tr>' +
                '<tr id="emailPublicRow">' +
                '<td class="label">Make your email visible?</td>' +
                '<td><input id="emailPublic" class="checkbox" type="checkbox"></td>' +
                '<td><img id="emailPublicInfo" class="info" key="emailPublicSignUp" area="emailPublic" src="AAAAAAAAARA"></td>' +
                '</tr>' +
                '<tr id="usernameRow">' +
                '<td class="label">Username:</td>' +
                '<td><input id="username" class="text" type="text" size="30"></td>' +
                '<td><img id="usernameInfo" class="info" key="usernameSignUp" area="username" src="AAAAAAAAARA"></td>' +
                '</tr>' +
                '<tr id="password1Row">' +
                '<td class="label">Password:</td>' +
                '<td><input id="password1" class="text" type="password" size="30"></td>' +
                '<td id="password1TD"><img id="passwordInfo" class="info" key="passwordSignUp" area="password1" src="AAAAAAAAARA">' +
                '<div id="passwordStrength"><canvas id="passwordMeter"></canvas></div></td>' +
                '</tr>' +
                '<tr id="password2Row">' +
                '<td class="label">Enter&nbsp;password&nbsp;again:</td>' +
                '<td><input id="password2" class="text" type="password" size="30"></td>' +
                '<td><div id="passwordMatch"></div></td>' +
                '</tr>' +
                '<tr id="handleRow">' +
                '<td class="label">Handle:</td>' +
                '<td><input id="handle" class="text" type="text" size="30"></td>' +
                '<td><img id="handleInfo" class="info" key="handleSignUp" area="handle" src="AAAAAAAAARA"></td>' +
                '</tr>' +
                '<tr id="firstNameRow">' +
                '<td class="label">First name:</td>' +
                '<td><input id="firstName" class="text" type="text" size="30"></td>' +
                '<td><img id="firstNameInfo" class="info" key="firstNameSignUp" area="firstName" src="AAAAAAAAARA"></td>' +
                '</tr>' +
                '<tr id="lastNameRow">' +
                '<td class="label">Last name:</td>' +
                '<td><input id="lastName" class="text" type="password" size="30"></td>' +
                '<td><img id="lastNameInfo" class="info" key="lastNameSignUp" area="lastName" src="AAAAAAAAARA"></td>' +
                '</tr>' +
                '<tr id="namePublicRow">' +
                '<td class="label">Make your real name visible?</td>' +
                '<td><input id="namePublic" class="checkbox" type="checkbox"></td>' +
                '<td><img id="namePublicInfo" class="info" key="namePublicSignUp" area="namePublic" src="AAAAAAAAARA"></td>' +
                '</tr>' +
                '</table>';


            this.show = function() {
                this.dialog = new $SL.Dialog( this, this.html, this.onCancel, this.onOK );
                this.dialog.show( function() {
                    return $("#signUpTable").width();
                } );
                this.email       = $("#email"       );
                this.emailPublic = $("#emailPublic" );
                this.username    = $("#username"    );
                this.password1   = $("#password1"   );
                this.password2   = $("#password2"   );
                this.handle      = $("#handle"      );
                this.firstName   = $("#firstName"   );
                this.lastName    = $("#lastName"    );
                this.namePublic  = $("#namePublic"  );
                this.dialog.setInputs( this.email, this.emailPublic, this.username, this.password1, this.password2, this.handle, this.firstName,
                    this.lastName, this.namePublic );
                this.dialog.onInput = this.onInput;
                this.dialog.onFocus = this.onFocus;
                $SL.setupInfo( $("table#signUpTable"));
                this.dialog.enableCancel();

                $SL.passwordMeter( $("#passwordMeter"), 0 );
            };


            this.onInput = function( event ) {

                // if we've just modified the password, update its strength...
                if( event.currentTarget.id == "password1" ) {
                    var strength = $SL.passwordStrength( this.password1.val() );
                    $SL.passwordMeter( $("#passwordMeter"), strength );
                }

                return false;
            };

            this.onFocus = function( event ) {

                // if we have an email address, but no username, default the email address to the username...
                if( this.email.val() && !this.username.val() )
                    this.username.val( this.email.val() );

                return false;
            };

            this.onCancel = function() {};

            this.onOK = function() {
                var req ={};
                req.user = this.username.val();
                req.password = this.password.val();
                req.rememberMe = this.rememberMe.is(':checked');
                $.post( "AAAAAAAAAGB",

                    JSON.stringify( req ),

                    function( data ) {
                        if( data.success ) {
                            window.location.href = "/";
                            window.location.reload();
                        }
                        else {
                            setTimeout( function() {var msg = new $SL.Message( data.reason, true ); msg.show();}, 500);
                        }
                    },

                    "json");
            };
        };

// sign-out dialog class...
$SL.SignOut = function() {

    this.html =
        'Sign out as a user on this blog?';


    this.show = function() {
        this.dialog = new $SL.Dialog( this, this.html, this.onCancel, this.onOK );
        this.dialog.show();
        this.dialog.enableCancel();
        this.dialog.enableOK();
        this.dialog.defaultOK();
    };

    this.onCancel = function() {
    };

    this.onOK = function() {
        var req ={};
        req.signOut = true;
        $.post( "AAAAAAAABUB",

            JSON.stringify( req ),

            function( data ) {
                window.location.href = "/";
                window.location.reload();
            },

            "json");
    };
};


// displays a password meter, 50w x 15h, on the given canvas...
$SL.passwordMeter = function( canvas, score ) {

    var okThresh = 0.4;
    var goodThresh = 0.7;

    canvas[0].width = 50;
    canvas[0].height = 15;

    // put an outline around it...
    canvas.css("outline", "1px solid black");

    var ctx = canvas[0].getContext("2d");

    // first we paint a white background...
    ctx.fillStyle = "#FFFFFF";
    ctx.fillRect( 0, 0, 50, 15 );

    // and then a colored (red, orange, or green) bar...
    var len = Math.max( 3, Math.floor( 1 + score * 49.999));
    ctx.fillStyle = (score < okThresh ) ? "#FF0000" : (score < goodThresh) ? "#FF8000" : "#00FF00";
    ctx.fillRect( 0, 0, len, 15);

    // finally, we put in some helpful text...
    ctx.fillStyle = "#000000";
    var text = (score < okThresh) ? "BAD" : (score < goodThresh) ? "OK" : "GOOD!";
    var hpos = (score < okThresh) ? 25 : (score < goodThresh) ? 30 : 2;
    ctx.font = "12px Arial";
    ctx.fillText( text, hpos, 12 );
};


// password strength calculator (0 = worst; 1.0 = best possible)...
$SL.passwordStrength = function( password ) {

    var ctrl = 0;
    var numb = 1;
    var alup = 2;
    var allw = 3;
    var punc = 4;
    var othr = 5;
    var counts = [0,0,0,0,0,0];

    // see what kind of characters we have...
    for(var i = 0; i < password.length; i++ ) {
        var c = password.charCodeAt(i);
        if( c < 32 )
            counts[ctrl]++;
        else if( c < 48 )
            counts[punc]++;
        else if( c < 48 )
            counts[numb]++;
        else if( c < 65 )
            counts[punc]++;
        else if( c < 91 )
            counts[alup]++;
        else if( c < 97 )
            counts[punc]++;
        else if( c < 123 )
            counts[allw]++;
        else if( c < 127 )
            counts[punc]++;
        else if( c < 128 )
            counts[ctrl]++;
        else
            counts[othr]++;
    }

    // how many kinds do we have represented, and how distributed?
    var cats = 0;
    for( i = 0; i < 6; i++ ) if( counts[i] > 0 ) cats++;
    var avg = 1 / cats;
    var chi2 = 0;
    for( i = 0; i < 6; i++ ) if( counts[i] > 0 ) chi2 += Math.pow((counts[i]/password.length - avg), 2);

    // now we score based on distribution, categories, and overall length...
    return Math.min( 1.0, (1 - chi2) * (password.length / 12) * (cats/4) );
};