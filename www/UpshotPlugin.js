var exec = require('cordova/exec');

var UpshotPlugin = {
        
    initialize: function(options, callback) {

        upshot.init( options, callback);            
    },

    getDeviceToken: function(success, error) {

        exec(success, error, 'UpshotPlugin', 'getDeviceToken',[]);
    }, 

    getPushClickDetails: function(success) {

        exec(success,null,'UpshotPlugin','getPushClickDetails',[]);
    },

    registerForPushWithForeground: function(arguments, status) {

    	exec(status, null, 'UpshotPlugin', 'registerForPushWithForeground', [arguments]);
    },        

    updateUserProfile: function(userDetails, callback) {

        upshot.updateUserProfile(userDetails, callback);            
    },

    sendUserLogoutInfo: function(callback) {
        upshot.updateUserProfile({'appuid': ''}, callback);        
    },

    getCurrentUserDetails: function(fields) {

        return upshot.getCurrentProfile(fields);
    },

    getUpshotUserId: function() {

        return upshot.getUserId();
    },

    disableUser: function(shouldDisable, callback) {

        upshot.disableUser(shouldDisable, callback)
    },

    emailOptout: function(shouldOptout) {

        upshot.updateUserProfile({'emailOptout': shouldOptout})        
    },

    smsOptout: function(shouldOptout) {

        upshot.updateUserProfile({'smsOptout': shouldOptout})        
    },

    pushOptout: function(shouldOptout) {

        upshot.updateUserProfile({'pushOptout': shouldOptout})        
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

    getActivityWithTag: function(activityType, tagName, options) {

        upshot.getActivity(activityType, tagName, options);
    },  

    getAnyActivityWithTag: function(tagName, options) {

        upshot.getActivity('UpshotActivityTypeAny', tagName, options);
    },   
    
    getActivityWithActivityId: function(activityId) {
        
        upshot.getActivityById(activityId);
    },

    getUserBadges: function() {
       return upshot.getBadges();
    },

    fetchInbox: function() {
        return upshot.fetchInboxInfo();
    },

    getRewardsList: function(successCallback, failureCallback) {

        upshot.getRewardStatus(successCallback,failureCallback);
    },

    redeemRewards: function(programId, redeemValue, tag, transactionValue, successCallback, failureCallback) {

        upshot.redeemRewards(programId, redeemValue, tag, transactionValue, successCallback, failureCallback);
    },

    getRewardTransactionhistory: function(programId, transactionType, successCallback, failureCallback) {

        upshot.getRewardHistory(programId, transactionType, successCallback, failureCallback);
    },

    getRewardRuleDetails: function(programId, successCallback, failureCallback) {

        upshot.getRewardDetails(programId, successCallback, failureCallback);
    },

    sendDeviceToken: function(deviceToken, devicePlatform) {
        
        if(devicePlatform == 'iOS') {

            upshot.updateUserProfile({'apnsToken': {"token": deviceToken}});
        }  else if(devicePlatform == 'Android') {
            
            upshot.updateUserProfile({'gcmToken': {"token": deviceToken}});
        } 
    },

    sendPushClickDetails: function(payload) {

        upshot.pushClickEvent(JSON.parse(payload));
    },

    sendCustomPush: function(pushPayload, callback) {

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