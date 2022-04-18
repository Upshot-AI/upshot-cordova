#
#  Be sure to run `pod spec lint UpshotCordovaPlugin.podspec' to ensure this is a
#  valid spec and to remove all comments including this before submitting the spec.
#
#  To learn more about Podspec attributes see https://guides.cocoapods.org/syntax/podspec.html
#  To see working Podspecs in the CocoaPods repo see https://github.com/CocoaPods/Specs/
#

Pod::Spec.new do |spec|

  # ―――  Spec Metadata  ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #
  #
  #  These will help people to find your library, and whilst it
  #  can feel like a chore to fill in it's definitely to your advantage. The
  #  summary should be tweet-length, and the description more in depth.
  #

  spec.name         = "UpshotCordovaPlugin"
  spec.version      = "1.1.1"
  spec.summary      = "UpshotCordovaPlugin used for enhance push notifications."
  spec.description  = "Upshot.ai is a analytics and customer engagement platform. This framework helps you capture analytics, track events, send smart notifications and in-app messages to users."
  spec.homepage     = "http://www.upshot.ai/"    
  spec.documentation_url = 'http://www.upshot.ai/documentation/sdk/Cordova/'
  spec.social_media_url   = 'https://twitter.com/upshot_ai'

  spec.platform     = :ios
  spec.ios.deployment_target  = "9.0"
  spec.license      = { :type => "MIT", :file => "LICENSE" }
  spec.author        = { "Upshot" => "developer@upshot.ai" }
  
  spec.source       = { 
    :git => 'https://github.com/Upshot-AI/upshot-cordova.git',
    :tag =>  'v'+spec.version.to_s
  }
  
  spec.ios.vendored_frameworks = 'Framework/UpshotCordovaPlugin.xcframework'
  spec.ios.preserve_paths = 'Framework/UpshotCordovaPlugin.xcframework'
  spec.requires_arc = true
  spec.pod_target_xcconfig = {
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64'
  }
  spec.user_target_xcconfig = { 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64' }
  

end
