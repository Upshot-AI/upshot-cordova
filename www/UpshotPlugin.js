var exec = require('cordova/exec');


var UpshotPlugin = {
        
    initialize: function(params, callback) {

        this.addListeners();
        params["upshotNewInstall"] = !this.isUserExist();
        upshot.init( params, callback);            
    },

    getDeviceToken: function(success, error) {

        exec(success, error, 'UpshotPlugin', 'getDeviceToken',[]);
    }, 

    getPushPayload: function(success) {

        exec(success,null,'UpshotPlugin','getPushPayload',[]);
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
    }
}

module.exports = UpshotPlugin