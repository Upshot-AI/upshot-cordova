var exec = require("cordova/exec");

var UpshotPlugin = {
  /**
   * This function is used to initialize the Upshot.ai
   * @param {*} params provide initialize Options as a Json object
   * @param {*} callback function which provides the init callback
   */
  initialize: function (params, callback) {
    this.addListeners();
    params["upshotNewInstall"] = !this.isUserExist();
    this.getDeviceDetails((response) => {
      upshot.setDeviceInfo(response);
    });
    upshot.init(params, callback);
    this.getDefaultAccountAndUserDetails();
  },

  getDeviceDetails: function (response) {
    cordova.exec(response, null, "UpshotPlugin", "getDeviceDetails", []);
  },

  /**
   * This function is used to get device token from Upshot.ai
   * @param {*} success will get token
   * @param {*} error
   */
  getDeviceToken: function (success, error) {
    cordova.exec(success, error, "UpshotPlugin", "getDeviceToken", []);
  },

  /**
   * This function is used to get PushClick payload from Upshot.ai
   * @param {*} success
   * @param {*} error
   */
  getPushPayload: function (success) {
    cordova.exec(success, null, "UpshotPlugin", "getPushPayload", []);
  },

  /**
   * This function is used to get Carousel Push Deeplink from Upshot.ai
   * @param {*} success
   */
  getCarouselDeeplink: function (success) {
    cordova.exec(success, null, "UpshotPlugin", "getCarouselDeeplink", []);
  },

  /**
   * This function is used to get list of push notifications for the user.
   * @param {Boolean} loadMore
   * * @param {Number} limit
   * @param {function} responseCallback
   */
  getNotifications: function (loadMore, limit, responseCallback) {
    upshot.getNotification(loadMore, limit, responseCallback);
  },

  /**
   * This function is used to get list of push notifications for the user.
   * @param {Number} inboxType
   * * @param {Number} limit
   * @param {function} responseCallback
   */
  getUnreadNotificationsCount: function (inboxType, limit, responseCallback) {
    upshot.getUnreadCount(inboxType, limit, false, responseCallback);
  },

  /**
   * This function is used to get list of push notifications for the user.
   * @param {Number} inboxType
   * @param {Number} limit
   * @param {Boolean} enableLoadMore
   * @param {Boolean} readNotifications
   * @param {Boolean} displayTime
   * @param {Boolean} displayMsgCount
   * @param {String} divId
   */
  showInboxList: function (
    inboxType,
    limit,
    enableLoadMore,
    readNotifications,
    displayTime,
    displayMsgCount,
    divId
  ) {
    upshot.showInboxList(
      inboxType,
      limit,
      enableLoadMore,
      readNotifications,
      displayTime,
      displayMsgCount,
      divId
    );
  },

  /**
   * This function is used to allow push notifcation show while app is in foreground.
   *
   */
  registerForPushNotifications: function () {
    cordova.exec(
      null,
      null,
      "UpshotPlugin",
      "registerForPushNotifications",
      []
    );
  },

  /**
   * This function is used to send userDetails to Upshot.ai
   * @param {*} params
   * @param {*} callback
   */
  updateUserProfile: function (params, callback) {
    upshot.updateUserProfile(params, callback);
  },

  /**
   * This function is used to get userDetails from Upshot.ai
   * @param {*} params
   * @param {*} callback
   */
  getCurrentProfile: function (fields) {
    return upshot.getCurrentProfile(fields);
  },

  /**
   * This function is used to get UserId generated by Upshot.ai
   * @param {*} params
   * @param {*} callback
   */
  getUserId: function () {
    return upshot.getUserId();
  },

  /**
   * This function is used to Delete User Record from Upshot.ai
   * @param {*} params
   * @param {*} callback
   */
  disableUser: function (callback) {
    upshot.disableUser(callback);
  },

  /**
   * This function is used to Create PageView / ScreenView Event
   * @param {*} params
   * @param {*} callback
   */
  createPageViewEvent: function (pageName) {
    return upshot.createEvent("UpshotEventPageView", pageName);
  },

  /**
   * This function is used to Create Custom Events
   * @param {*} params
   * @param {*} callback
   */
  createCustomEvent: function (eventName, payload, isTimed) {
    return upshot.createEventByName(eventName, payload, isTimed);
  },

  /**
   * This function is used to Create Attribution Event
   * @param {*} params
   * @param {*} callback
   */
  createAttributionEvent: function (payload) {
    return upshot.createAttributionEvent(payload);
  },

  /**
   * This function is used to Set And Close the Custom Event w.r.t eventId.
   * @param {*} params
   * @param {*} callback
   */
  setDataAndCloseEvent: function (eventId, params) {
    upshot.setDataAndCloseEvent(eventId, params);
  },

  /**
   * This function is used to Close the Custom Event w.r.t eventId.
   * @param {*} params
   * @param {*} callback
   */
  closeEventForId: function (eventId) {
    upshot.closeEventForID(eventId);
  },

  /**
   * This function is used to Get Upshot.ai Action on top of the application w.r.t Tag name.
   * @param {*} params
   * @param {*} callback
   */
  getActivity: function (activityType, tagName, options) {
    upshot.getActivity(activityType, tagName, options);
  },

  /**
   * This function is used to Get Upshot.ai Action on top of the application w.r.t ActivityID name.
   * @param {*} params
   * @param {*} callback
   */
  getActivityById: function (activityId) {
    upshot.getActivityById(activityId);
  },

  /**
   * This function is used to Stop the AD w.r.t divID.
   * @param {*} params
   * @param {*} callback
   */
  stopAd: function (divId) {
    upshot.stopAd(divId);
  },

  /**
   * This function is used to Get List of Achived and Yet to Achived Badges from Upshot.ai.
   * @param {*} params
   * @param {*} callback
   */
  getBadges: function () {
    return upshot.getBadges();
  },

  /**
   * This function is used to Get List of Campaign Actions which are added to Inbox from Upshot.ai.
   * @param {*} params
   * @param {*} callback
   */
  fetchInboxInfo: function () {
    return upshot.fetchInboxInfo();
  },

  /**
   * This function is used to Get the List of Qualified Reward Programs to the User.
   * @param {*} params
   * @param {*} callback
   */
  getRewardStatus: function (successCallback, failureCallback) {
    upshot.getRewardStatus(successCallback, failureCallback);
  },

  /**
   * This function is used to Update redeem reward points to Upshot.ai.
   * @param {*} params
   * @param {*} callback
   */
  redeemRewards: function (
    programId,
    redeemValue,
    tag,
    transactionValue,
    successCallback,
    failureCallback
  ) {
    upshot.redeemRewards(
      programId,
      redeemValue,
      tag,
      transactionValue,
      successCallback,
      failureCallback
    );
  },

  /**
   * This function is used to get the reward transaction history for the given program from Upshot.ai.
   * @param {*} params
   * @param {*} callback
   */
  getRewardHistory: function (
    programId,
    transactionType,
    successCallback,
    failureCallback
  ) {
    upshot.getRewardHistory(
      programId,
      transactionType,
      successCallback,
      failureCallback
    );
  },

  /**
   * This function is used to get the list of rules to achive the reward ponints for the given program from Upshot.ai.
   * @param {*} params
   * @param {*} callback
   */
  getRewardDetails: function (programId, successCallback, failureCallback) {
    upshot.getRewardDetails(programId, successCallback, failureCallback);
  },

  /**
   * This function is used to pass device token to Upshot.ai.
   * @param {*} params
   * @param {*} callback
   */
  sendDeviceToken: function (deviceToken, devicePlatform) {
    if (devicePlatform.toLowerCase() == "ios") {
      upshot.updateUserProfile({ apnsToken: { token: deviceToken } });
    } else if (devicePlatform.toLowerCase() == "android") {
      upshot.updateUserProfile({ gcmToken: { token: deviceToken } });
    }
  },

  /**
   * This function is used to Send push click event to Upshot.ai.
   * @param {*} params
   * @param {*} callback
   */
  pushClickEvent: function (payload) {
    cordova.exec(null, null, "UpshotPlugin", "sendPushPayload", [payload]);
    upshot.pushClickEvent(JSON.parse(payload));
  },

  /**
   * This function is used to Send Location Details to Upshot.ai.
   * @param {*} params
   * @param {*} callback
   */
  sendLocation: function (locationObj) {
    upshot.sendLocation(locationObj);
  },

  /**
   * This function is used to fetch the Streak Info.
   * @param {*} successCallback
   * * @param {*} failureCallback
   */
  getStreakInfo: function (successCallback, failureCallback) {
    upshot.getStreak(successCallback, failureCallback);
  },

  displayNotification: function (payload) {
    cordova.exec(null, null, "UpshotPlugin", "displayNotification", [payload]);
  },

  addListeners: function () {
    window.addEventListener(
      "UpshotActivityShared",
      function (data) {
        cordova.exec(null, null, "UpshotPlugin", "shareCallback", [
          JSON.stringify(data.detail),
        ]);
      },
      false
    );
    window.addEventListener(
      "UpshotActivityRedirectionCallback",
      function (data) {
        cordova.exec(null, null, "UpshotPlugin", "redirectionCallback", [
          JSON.stringify(data.detail),
        ]);
      },
      false
    );
    window.addEventListener(
      "UpshotActivityRatingCallback",
      function (data) {
        console.log(
          "UpshotActivityRatingCallback--------------- "[
            JSON.stringify(data.detail)
          ]
        );
        cordova.exec(
          null,
          null,
          "UpshotPlugin",
          "ratingStoreRedirectionCallback",
          [JSON.stringify(data.detail)]
        );
      },
      false
    );
  },

  isUserExist: function () {
    var upshotData = window.localStorage.getItem("upshotData");
    if (upshotData != undefined) {
      var upshotJsonData = JSON.parse(upshotData);
      var userId = upshotJsonData["upshotUserID"]
        ? upshotJsonData["upshotUserID"]
        : "";
      if (userId == "" || userId == undefined) {
        return false;
      }
      return true;
    } else {
      return false;
    }
  },

  getJSONString(jsonObject) {
    return JSON.stringify(jsonObject);
  },
  getDefaultAccountAndUserDetails: function () {
    var upshotData = window.localStorage.getItem("upshotData");
    var upshotVersion = window.localStorage.getItem("upshotVersion");

    var sdkVersion = "1.6.3";
    if (upshotVersion != undefined) {
      sdkVersion = upshotVersion;
    }

    if (upshotData != undefined) {
      var currentLocaleCode = "en_US";
      var upshotJsonData = JSON.parse(upshotData);
      var currentAppuid = upshotJsonData["upshotAppUID"];
      var localeCodeStr = upshotJsonData["upshotLocaleCode"];
      console.log("localeCodeStr" + localeCodeStr);
      if (localeCodeStr != undefined && localeCodeStr != "") {
        var localeObj = JSON.parse(localeCodeStr);
        if (currentAppuid == undefined || currentAppuid == "") {
          currentAppuid = "noAppuid293018";
        }
        var localeCode = localeObj[currentAppuid];
        if (localeCode != undefined && localeCode != "") {
          currentLocaleCode = localeCode;
        }
      }

      var initParams = upshotJsonData["upshotInitParams"];
      if (initParams != undefined) {
        var initJsonParams = JSON.parse(initParams);
        var appId = initJsonParams["UpshotApplicationID"]
          ? initJsonParams["UpshotApplicationID"]
          : "";
        var ownerId = initJsonParams["UpshotApplicationOwnerID"]
          ? initJsonParams["UpshotApplicationOwnerID"]
          : "";
        var userId = upshotJsonData["upshotUserID"]
          ? upshotJsonData["upshotUserID"]
          : "";
        var appuid = upshotJsonData["upshotAppUID"]
          ? upshotJsonData["upshotAppUID"]
          : "";
        var sessionId = upshotJsonData["upshotSessionID"]
          ? upshotJsonData["upshotSessionID"]
          : "";
        var baseUrl = upshotJsonData["upshotAPIURL"]
          ? upshotJsonData["upshotAPIURL"]
          : "";
        if (!baseUrl.includes("https")) {
          baseUrl = "https://" + baseUrl;
        }
        var details = {
          UpshotApplicationID: appId,
          UpshotApplicationOwnerID: ownerId,
          UpshotUserID: userId,
          UpshotAppUID: appuid,
          UpshotSessionID: sessionId,
          UpshotBaseUrl: baseUrl,
          UpshotVersion: sdkVersion,
          upshotLocaleCode: currentLocaleCode,
        };
        cordova.exec(
          null,
          null,
          "UpshotPlugin",
          "getDefaultAccountAndUserDetails",
          [JSON.stringify(details)]
        );
      }
    }
  },
};

module.exports = UpshotPlugin;
