var exec = require('cordova/exec');

var UpshotPlugin = {
        
    initialize: function(params, callback) {

        upshot.init( params, callback);            
    },

    getDeviceToken: function(success, error) {

        exec(success, error, 'UpshotPlugin', 'getDeviceToken',[]);
    }, 

    getPushPayload: function(success) {

        exec(success,null,'UpshotPlugin','getPushPayload',[]);
    },

    registerForPushWithForeground: function(arguments, status) {

    	exec(status, null, 'UpshotPlugin', 'registerForPushWithForeground', [arguments]);
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
        
        if(devicePlatform == 'iOS') {

            upshot.updateUserProfile({'apnsToken': {"token": deviceToken}});
        }  else if(devicePlatform == 'Android') {
            
            upshot.updateUserProfile({'gcmToken': {"token": deviceToken}});
        } 
    },

    pushClickEvent: function(payload) {

        upshot.pushClickEvent(JSON.parse(payload));
    },

    sendPushDetails: function(pushPayload, callback) {

        upshot.sendPushDetails(pushPayload, callback);
    },

    sendLocation: function(locationObj) {

        upshot.sendLocation(locationObj);
    },

    subscribePush: function() {

        upshot.subscribePush(); 
    }
}

module.exports = UpshotPlugin