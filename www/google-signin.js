var exec = require('cordova/exec');

var GoogleSignIn = {
  login: function (clientId, successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'GoogleSignIn', 'login', [clientId]);
  },
  logout: function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'GoogleSignIn', 'logout', []);
  }
};

module.exports = GoogleSignIn;
