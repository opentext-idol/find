define([
    'backbone',
    'underscore'
], function(Backbone, _) {
    function addModel (model) {
        this.add(model);
    }

    function removeModel (model) {
        this.remove(model);
    }

    return Backbone.Collection.extend({
        loaded: false,
        queryCollectionLoaded: false,
        snapshotsCollectionLoaded: false,

        initialize: function(model, options){
            this.queryCollection = options.queryCollection;
            this.snapshotCollection = options.snapshotCollection;

            this.listenTo(this.queryCollection, 'add', addModel);
            this.listenTo(this.snapshotCollection, 'add', addModel);

            this.listenTo(this.queryCollection, 'remove', removeModel);
            this.listenTo(this.snapshotCollection, 'remove', removeModel);
        },

        modelId: function(attrs) {
            return attrs.type + attrs.id
        }

    });

});