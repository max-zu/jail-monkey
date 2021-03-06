require "json"
package = JSON.parse(File.read(File.join(__dir__, '/package.json')))

Pod::Spec.new do |s|
  s.name = package['name']
  s.version = package['version']
  s.summary = package['description']
  s.description = package['description']
  s.homepage = package['homepage']
  s.license = package['license']
  s.author = package['author']
  s.source = { :git => 'https://github.com/max-zu/jail-monkey.git' }

  s.platform = :ios, '9.0'
  s.ios.deployment_target = '9.0'

  s.source_files = "JailMonkey/*.{h,m}"

  s.dependency 'React'
end
