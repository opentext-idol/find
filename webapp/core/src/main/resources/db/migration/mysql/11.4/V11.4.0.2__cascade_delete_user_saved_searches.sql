/*-------------------------------------------
-- Description: Add ON DELETE CASCADE to
   foreign key constraints

-- Previous Version: 11.4.0.1
-- Target Version: 11.4.0.2
--------------------------------------------*/

USE find;

ALTER TABLE searches DROP FOREIGN KEY fk__searches__users;
ALTER TABLE searches ADD CONSTRAINT fk__searches__users FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE;

ALTER TABLE search_parametric_values DROP FOREIGN KEY fk__search_parametric_values__searches;
ALTER TABLE search_parametric_values ADD CONSTRAINT fk__search_parametric_values__searches FOREIGN KEY (search_id) REFERENCES searches (search_id) ON DELETE CASCADE;

ALTER TABLE search_parametric_ranges DROP FOREIGN KEY fk__search_parametric_ranges__searches;
ALTER TABLE search_parametric_ranges ADD CONSTRAINT fk__search_parametric_ranges__searches FOREIGN KEY (search_id) REFERENCES searches (search_id) ON DELETE CASCADE;

ALTER TABLE search_indexes DROP FOREIGN KEY fk__search_indexes__searches;
ALTER TABLE search_indexes ADD CONSTRAINT fk__search_indexes__searches FOREIGN KEY (search_id) REFERENCES searches (search_id) ON DELETE CASCADE;

ALTER TABLE search_stored_state DROP FOREIGN KEY fk__search_stored_state__searches;
ALTER TABLE search_stored_state ADD CONSTRAINT fk__search_stored_state__searches FOREIGN KEY (search_id) REFERENCES searches (search_id) ON DELETE CASCADE;

ALTER TABLE search_concept_cluster_phrases DROP FOREIGN KEY fk__search_concept_cluster_phrases__searches;
ALTER TABLE search_concept_cluster_phrases ADD CONSTRAINT fk__search_concept_cluster_phrases__searches FOREIGN KEY (search_id) REFERENCES searches (search_id) ON DELETE CASCADE;

ALTER TABLE shared_to_users DROP FOREIGN KEY fk__shared_to_users__searches;
ALTER TABLE shared_to_users ADD CONSTRAINT fk__shared_to_users__searches FOREIGN KEY (search_id) REFERENCES searches (search_id) ON DELETE CASCADE;

ALTER TABLE shared_to_users DROP FOREIGN KEY fk__shared_to_users__users;
ALTER TABLE shared_to_users ADD CONSTRAINT fk__shared_to_users__users FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE;