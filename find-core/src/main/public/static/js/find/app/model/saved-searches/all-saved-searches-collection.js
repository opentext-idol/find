define([
    'backbone',
    'underscore'
], function(Backbone, _) {
    function AddModel (model) {
        this.add(model);
    }

    function RemoveModel (model) {
        this.remove(model);
    }

    function ResetCollection () {
        this.reset();
    }

    return Backbone.Collection.extend({
        loaded: false,
        queryCollectionLoaded: false,
        snapshotsCollectionLoaded: false,

        initialize: function(model, options){
            this.queryCollection = options.queryCollection;
            this.snapshotCollection = options.snapshotCollection;

            this.listenTo(this.queryCollection, 'add', AddModel);
            this.listenTo(this.snapshotCollection, 'add', AddModel);

            this.listenTo(this.queryCollection, 'remove', RemoveModel);
            this.listenTo(this.snapshotCollection, 'remove', RemoveModel);

            this.listenTo(this.queryCollection, 'reset', ResetCollection);

            this.listenTo(this.snapshotCollection, 'reset', ResetCollection);
        },

        modelId: function(attrs) {
            return attrs.type + attrs.id
        }

    });

});