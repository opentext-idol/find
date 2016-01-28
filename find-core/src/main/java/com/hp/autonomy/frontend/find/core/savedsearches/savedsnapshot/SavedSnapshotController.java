package com.hp.autonomy.frontend.find.core.savedsearches.savedsnapshot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping(SavedSnapshotController.PATH)
public class SavedSnapshotController {
    public static final String PATH = "/api/public/saved-snapshot";

    private final SavedSnapshotService service;

    @Autowired
    public SavedSnapshotController(final SavedSnapshotService service) {
        this.service = service;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Set<SavedSnapshot> getAll() {
        return service.getAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public SavedSnapshot create(
            @RequestBody final SavedSnapshot query
    ) {
        return service.create(query);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PATCH)
    public SavedSnapshot update(
            @PathVariable("id") final long id,
            @RequestBody final SavedSnapshot query
    ) {
        return service.update(
                new SavedSnapshot.Builder(query).setId(id).build()
        );
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void delete(
            @PathVariable("id") final long id
    ) {
        service.deleteById(id);
    }
}
