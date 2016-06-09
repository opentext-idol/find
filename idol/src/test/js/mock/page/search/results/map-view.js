define([
    'js-testing/backbone-mock-factory'
], function(mockFactory) {

    var layer = jasmine.createSpyObj('layer', ['addLayer', 'clearLayers', 'getLayers']);
    var createLayerSpy = jasmine.createSpy('createLayer').and.returnValue(layer);
    var getMarkerSpy = jasmine.createSpy('getMarker').and.returnValue('testMarker');
    return mockFactory.getView(['getDivIconCreateFunction', 'getIcon', 'loaded', 'addLayer'], {
        createLayer: createLayerSpy,
        getMarker: getMarkerSpy
    });

});
