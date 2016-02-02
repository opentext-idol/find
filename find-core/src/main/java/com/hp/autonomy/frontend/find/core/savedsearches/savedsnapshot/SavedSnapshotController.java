package com.hp.autonomy.frontend.find.core.savedsearches.savedsnapshot;

import com.hp.autonomy.aci.content.fieldtext.FieldText;
import com.hp.autonomy.aci.content.fieldtext.MATCH;
import com.hp.autonomy.frontend.find.core.savedsearches.FieldAndValue;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(SavedSnapshotController.PATH)
public abstract class SavedSnapshotController<S extends Serializable, R extends SearchResult, E extends Exception> {
    public static final String PATH = "/api/public/saved-snapshot";

    //autowire document service, get state toke for snapshot restrictions, build from stuff inside savedsnapshot s. Add state toke to the snapshot s.
    protected final DocumentsService<S, R, E> documentsService;

    protected final SavedSnapshotService service;

    @Autowired
    public SavedSnapshotController(final SavedSnapshotService service, final DocumentsService<S, R, E> documentsService) {
        this.service = service;
        this.documentsService = documentsService;
    }

    protected abstract String getStateToken(final SavedSnapshot snapshot) throws E;

    @RequestMapping(method = RequestMethod.GET)
    public Set<SavedSnapshot> getAll() {
        return service.getAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public SavedSnapshot create(
            @RequestBody final SavedSnapshot snapshot
    ) throws E {

        return service.create(
                new SavedSnapshot.Builder(snapshot)
                        .setStateToken(Collections.singletonList(getStateToken(snapshot)))
                        .build()
        );
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PATCH)
    public SavedSnapshot update(
            @PathVariable("id") final long id,
            @RequestBody final SavedSnapshot snapshot
    ) throws E {
        return service.update(
                new SavedSnapshot.Builder(snapshot)
                        .setStateToken(Collections.singletonList(getStateToken(snapshot)))
                        .setId(id)
                        .build()
        );
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void delete(
            @PathVariable("id") final long id
    ) {
        service.deleteById(id);
    }

    protected String getQueryText(final SavedSnapshot snapshot) {
        return snapshot.getQueryText() + StringUtils.join(snapshot.getRelatedConcepts(), " AND ");
    }

    protected String getFieldText(final Set<FieldAndValue> fieldAndValues) {
        final List<FieldText> matchNodes = new ArrayList<>();

        for(FieldAndValue fieldAndValue: fieldAndValues) {
            matchNodes.add(new MATCH(fieldAndValue.getField(), new String[]{fieldAndValue.getValue()}));
        }

        if(matchNodes.size() == 0) return "";

        final FieldText fieldtext = matchNodes.get(0);
        for(int ii = 1; ii <= matchNodes.size();) {
            fieldtext.AND(matchNodes.get(ii));
        }

        return fieldtext.toString();
    }
}
