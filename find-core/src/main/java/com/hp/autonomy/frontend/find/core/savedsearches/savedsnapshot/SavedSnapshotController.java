package com.hp.autonomy.frontend.find.core.savedsearches.savedsnapshot;

import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping(SavedSnapshotController.PATH)
public abstract class SavedSnapshotController {
    public static final String PATH = "/api/public/saved-snapshot";

    //autowire document service, get state toke for snapshot restrictions, build from stuff inside savedsnapshot s. Add state toke to the snapshot s.
    protected final DocumentsService documentsService;

    protected final SavedSnapshotService service;

    @Autowired
    public SavedSnapshotController(final SavedSnapshotService service, final DocumentsService<ResourceIdentifier, HodSearchResult, HodErrorException> documentsService) {
        this.service = service;
        this.documentsService = documentsService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Set<SavedSnapshot> getAll() {
        return service.getAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public SavedSnapshot create(
            @RequestBody final SavedSnapshot snapshot
    ) throws Exception {
        return service.create(snapshot);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PATCH)
    public SavedSnapshot update(
            @PathVariable("id") final long id,
            @RequestBody final SavedSnapshot snapshot
    ) {
        return service.update(
                new SavedSnapshot.Builder(snapshot).setId(id).build()
        );
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void delete(
            @PathVariable("id") final long id
    ) {
        service.deleteById(id);
    }
}
