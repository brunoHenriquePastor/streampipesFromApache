angular
    .module('streamPipesApp')
    .directive('iframeAutoSize', function() {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            element.on('load', function() {
                console.log(element[0]);
                var iFrameHeight = element[0].contentWindow.document.body.scrollHeight + 'px';
                element.css('height', iFrameHeight);
            });
        }
    }})
    .directive('collapsible', function() {
    	return {
    		restrict : 'E',
    		templateUrl : 'modules/sensors/directives/collapsible.tmpl.html',
    		transclude: true,
    		scope : {
    			list : "=list",
    			index: "=index",
    			disabled: "=disabled",
    			removable: "=removable",
    			titleLabel: "=titleLabel",
    			collapsible: "=collapsible",
    			subtitle: "="
    		},
    		
    		controller: function($scope, $element) {
    			
    			$scope.hide = true;
    			
    			$scope.toggleVisibility = function() {
    				$scope.hide = !$scope.hide;
    			}
    			
    			$scope.removeProperty = function(list, index) {
    				list.splice(index, 1);
    			}
       		}
    	}
    }).directive('advancedSettings', function() {
    	return {
    		restrict : 'E',
    		templateUrl : 'modules/sensors/directives/advanced-settings.tmpl.html',
    		transclude: true,
    		scope : {

    		},
    		
    		controller: function($scope, $element) {
    			
    			$scope.visible = false;
    			
    			$scope.showLabel = function() {
    				return $scope.visible == true ? "Hide" : "Show";
    			}

    			$scope.advancedSettingsVisible = function() {
    				return $scope.visible;
    			}

    			$scope.toggleAdvancedSettingsVisibility = function() {
    				$scope.visible = !$scope.visible;
    			}
    			
       		}
    	}
    }).directive('nagPrism', ['$compile', function($compile) {
        return {
            restrict: 'A',
            transclude: true,
            scope: {
              source: '@'
            },
            link: function(scope, element, attrs, controller, transclude) {
                scope.$watch('source', function(v) {
                  element.find("code").html(v);

                  Prism.highlightElement(element.find("code")[0]);
                });

                transclude(function(clone) {
                  if (clone.html() !== undefined) {
                    element.find("code").html(clone.html());
                    $compile(element.contents())(scope.$parent);
                  }
                });
            },
            template: "<code></code>"
        };
    }]);



