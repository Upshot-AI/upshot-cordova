var exec = require('cordova/exec');


var UpshotPlugin = {
        
    initialize: function(params, callback) {
        exec(null, null, 'UpshotPlugin', 'registerPushChannels')
        this.addListeners();
        params["upshotNewInstall"] = !this.isUserExist();
        upshot.init( params, callback);    
        this.getDefaultAccountAndUserDetails();        
    },

    getDeviceToken: function(success, error) {

        exec(success, error, 'UpshotPlugin', 'getDeviceToken',[]);
    }, 

    getPushPayload: function(success) {

        exec(success,null,'UpshotPlugin','getPushPayload',[]);
    },

    getCarouselDeeplink: function(success) {
        exec(success,null,'UpshotPlugin','getCarouselDeeplink',[]);
    },

    getNotifications: function(loadMore, responseCallback) {
        upshot.getNotification(loadMore, responseCallback) 
    },

    registerForPushWithForeground: function(shouldEnable, status) {

    	exec(status, null, 'UpshotPlugin', 'registerForPushWithForeground', [shouldEnable]);
    },        

    updateUserProfile: function(params, callback) {

        upshot.updateUserProfile(params, callback);            
    },

    getCurrentProfile: function(fields) {

        return upshot.getCurrentProfile(fields);
    },

    getUserId: function() {

        return upshot.getUserId();
    },

    disableUser: function(shouldDisable, callback) {

        upshot.disableUser(shouldDisable, callback)
    },

    createPageViewEvent: function(pageName) {
        return upshot.createEvent('UpshotEventPageView', pageName);        
    }, 

    createCustomEvent: function(eventName, payload, isTimed) {
        return upshot.createEventByName(eventName, payload, isTimed);    
    },

    createAttributionEvent: function(payload) {

        return upshot.createAttributionEvent(payload)
    },

    setDataAndCloseEvent: function(eventId, params) {

        upshot.setDataAndCloseEvent(eventId, params)
    },

    closeEventForId: function(eventId) {

        upshot.closeEventForID(eventId)
    },

    getActivity: function(activityType, tagName, options) {

        upshot.getActivity(activityType, tagName, options);
    },   
    
    getActivityById: function(activityId) {
        
        upshot.getActivityById(activityId);
    },

    stopAd: function(divId) {
        upshot.stopAd(divId)
    },

    getBadges: function() {
       return upshot.getBadges();
    },

    fetchInboxInfo: function() {
        return upshot.fetchInboxInfo();
    },

    getRewardStatus: function(successCallback, failureCallback) {

        upshot.getRewardStatus(successCallback,failureCallback);
    },

    redeemRewards: function(programId, redeemValue, tag, transactionValue, successCallback, failureCallback) {

        upshot.redeemRewards(programId, redeemValue, tag, transactionValue, successCallback, failureCallback);
    },

    getRewardHistory: function(programId, transactionType, successCallback, failureCallback) {

        upshot.getRewardHistory(programId, transactionType, successCallback, failureCallback);
    },

    getRewardDetails: function(programId, successCallback, failureCallback) {

        upshot.getRewardDetails(programId, successCallback, failureCallback);
    },

    sendDeviceToken: function(deviceToken, devicePlatform) {
        
        if(devicePlatform.toLowerCase() == 'ios') {
            upshot.updateUserProfile({'apnsToken': {"token": deviceToken}});
        }  else if(devicePlatform.toLowerCase() == 'android') {
            
            upshot.updateUserProfile({'gcmToken': {"token": deviceToken}});
        } 
    },

    pushClickEvent: function(payload) {
        exec(null, null, 'UpshotPlugin', 'sendPushPayload', [payload]);        
        upshot.pushClickEvent(JSON.parse(payload));
    },    

    sendLocation: function(locationObj) {

        upshot.sendLocation(locationObj);
    },
     
    addListeners: function() {
        window.addEventListener('UpshotActivityShared', function(data) {
            exec(null, null, 'UpshotPlugin', 'shareCallback', [JSON.stringify(data.detail)]);
        }, false);
        window.addEventListener('UpshotActivityRedirectionCallback', function(data) {
            exec(null, null, 'UpshotPlugin', 'redirectionCallback', [JSON.stringify(data.detail)]);
        }, false);
    },

    isUserExist: function() {

        var upshotData = window.localStorage.getItem("upshotData");
        if (upshotData != undefined) {
            var upshotJsonData = JSON.parse(upshotData);
            var userId = upshotJsonData["upshotUserID"] ? upshotJsonData["upshotUserID"] : "";
            if (userId == "" || userId == undefined) {
                return false
            }
            return true
        } else {
            return false
        }        
    },

    getDefaultAccountAndUserDetails: function() {

        var upshotData = window.localStorage.getItem("upshotData");
        var upshotVersion = window.localStorage.getItem("upshotVersion");        
        
        var sdkVersion = "1.6.3"
        if(upshotVersion != undefined) {
            sdkVersion = upshotVersion;
        }
        
        if (upshotData != undefined) {

            var currentLocaleCode = "en_US";
            var upshotJsonData = JSON.parse(upshotData);
            var currentAppuid = upshotJsonData["upshotAppUID"];
            var localeCodeStr = upshotJsonData["upshotLocaleCode"];
            console.log("localeCodeStr" + localeCodeStr)
            if (localeCodeStr != undefined && localeCodeStr != '') {
                var localeObj = JSON.parse(localeCodeStr)
                if (currentAppuid == undefined || currentAppuid == '') {
                    currentAppuid = 'noAppuid293018'
                }            
                var localeCode = localeObj[currentAppuid]
                if(localeCode != undefined && localeCode != '') {
                    currentLocaleCode = localeCode
                }
            }

            var initParams = upshotJsonData["upshotInitParams"];
            if (initParams != undefined) {
                var initJsonParams = JSON.parse(initParams);
                var appId = initJsonParams["UpshotApplicationID"] ? initJsonParams["UpshotApplicationID"] : "";
                var ownerId = initJsonParams["UpshotApplicationOwnerID"] ? initJsonParams["UpshotApplicationOwnerID"] : "";
                var userId = upshotJsonData["upshotUserID"] ? upshotJsonData["upshotUserID"] : "";
                var appuid = upshotJsonData["upshotAppUID"] ? upshotJsonData["upshotAppUID"] : "";
                var sessionId = upshotJsonData["upshotSessionID"] ? upshotJsonData["upshotSessionID"] : "";
                var baseUrl = upshotJsonData["upshotAPIURL"] ? upshotJsonData["upshotAPIURL"] : ""
                if (!baseUrl.includes("https")){
                    baseUrl = "https://"+baseUrl;
                }
                var details = {"UpshotApplicationID": appId, "UpshotApplicationOwnerID": ownerId, "UpshotUserID": userId, "UpshotAppUID": appuid, "UpshotSessionID": sessionId, "UpshotBaseUrl":baseUrl, "UpshotVersion": sdkVersion, "upshotLocaleCode": currentLocaleCode};
                exec(null, null, 'UpshotPlugin', 'getDefaultAccountAndUserDetails', [JSON.stringify(details)]);       
            }
        }        
    }
}

module.exports = UpshotPlugin