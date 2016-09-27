/*-------------------------------------------
-- Description: Add concept search; now all search
  terms are concepts and query text can be removed

-- Previous Version: 11.1.0.4
-- Target Version: 11.2.0.0
--------------------------------------------*/

USE find;

-- Add quotes to old related concepts, as we no longer want to add quotes to all concepts when generating query text
UPDATE search_concept_cluster_phrases
SET search_concept_cluster_phrases.phrase = CONCAT('"', search_concept_cluster_phrases.phrase, '"');

-- Treat query text as context
-- it will always be primary because there is no associated cluster
-- we can safely use -1 for the cluster id as we generate it in the code from 0 incrementing upwards
-- we no longer store '*' as a concept, so we want to exclude that from the initial migration step
INSERT INTO search_concept_cluster_phrases (search_id, phrase, primary_phrase, cluster_id)
SELECT search_id, query_text, TRUE, -1
FROM searches
WHERE query_text <> '*';

ALTER TABLE searches DROP COLUMN query_text;