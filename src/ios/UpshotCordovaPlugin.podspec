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
  spec.version      = "1.1.2"
  spec.summary      = "UpshotCordovaPlugin used for enhance push notifications."
  spec.description  = "UpshotCordovaPlugin used for enhance push notifications."
  spec.homepage     = "https://github.com/Upshot-AI/upshot-cordova.git"    
  spec.license      = { :type => "MIT", :file => "LICENSE" }
  spec.author             = { "Upshot.ai" => "developer@upshot.ai" }
  spec.platform     = :ios  
  spec.ios.deployment_target  = "10.0"
  spec.source       = { 
    :git => 'https://github.com/Upshot-AI/upshot-cordova.git',
    :tag =>  'v'+spec.version.to_s
  }
  spec.requires_arc = true
  spec.ios.vendored_frameworks = 'Framework/UpshotCordovaPlugin.xcframework'
  # spec.ios.preserve_paths = 'Framework/UpshotCordovaPlugin.xcframework'
  spec.pod_target_xcconfig = {
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64'
  }
  spec.user_target_xcconfig = { 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64' }
  # spec.source_files  = "iOS/*.{h,m}"
  # spec.vendored_frameworks = 'UpshotCordovaPlugin.framework'
  # spec.exclude_files = "Classes/Exclude"
  # spec.public_header_files = "Classes/**/*.h"
  # spec.resource  = "icon.png"
  # spec.resources = "Resources/*.png"
  # spec.framework  = "UpshotCordovaPlugin"
  # spec.frameworks = "SomeFramework", "AnotherFramework"

  # spec.library   = "iconv"
  # spec.libraries = "iconv", "xml2"


  # ――― Project Settings ――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #
  #
  #  If your library depends on compiler flags you can set them in the xcconfig hash
  #  where they will only apply to your library. If you depend on other Podspecs
  #  you can include multiple dependencies to ensure it works.

  # spec.requires_arc = true

  # spec.xcconfig = { "HEADER_SEARCH_PATHS" => "$(SDKROOT)/usr/include/libxml2" }
  # spec.dependency "JSONKit", "~> 1.4"

end
