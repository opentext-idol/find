CREATE SCHEMA find;

SET SCHEMA find;

CREATE TABLE searches
(
  search_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  search_type INT NOT NULL,
  title NVARCHAR(1000) NOT NULL,
  query_text NVARCHAR(21844) NOT NULL,
  start_date DATETIME,
  end_date DATETIME,
  total_results INT,
  created_date DATETIME NOT NULL,
  modified_date DATETIME NOT NULL,
  date_range_type INT,
  last_fetched_new_date DATETIME,
  active BIT NOT NULL
);

CREATE INDEX ix__searches__search_type ON searches (search_type);

CREATE TABLE users
(
  user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  domain VARCHAR(255),
  user_store VARCHAR(255),
  uuid UUID,
  uid BIGINT
);

CREATE INDEX ix__searches__user_id ON searches (user_id);
ALTER TABLE searches ADD CONSTRAINT fk__searches__users FOREIGN KEY (user_id) REFERENCES users (user_id);

CREATE TABLE search_parametric_values
(
  search_parametric_values_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  search_id BIGINT NOT NULL,
  field NVARCHAR(21844) NOT NULL,
  value NVARCHAR(21844) NOT NULL
);

CREATE INDEX ix__search_parametric_values__search_id ON search_parametric_values (search_id);
ALTER TABLE search_parametric_values ADD CONSTRAINT fk__search_parametric_values__searches FOREIGN KEY (search_id) REFERENCES searches (search_id);

CREATE TABLE search_indexes
(
  search_index_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  search_id BIGINT NOT NULL,
  name NVARCHAR(21844) NOT NULL,
  domain VARCHAR(255)
);

CREATE INDEX ix__search_indexes__search_id ON search_indexes (search_id);
ALTER TABLE search_indexes ADD CONSTRAINT fk__search_indexes__searches FOREIGN KEY (search_id) REFERENCES searches (search_id);

CREATE TABLE search_stored_state
(
  search_stored_state_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  search_id BIGINT NOT NULL,
  state_token VARCHAR(100) NOT NULL
);

CREATE INDEX ix__search_stored_state__search_id ON search_stored_state (search_id);
ALTER TABLE search_stored_state ADD CONSTRAINT fk__search_stored_state__searches FOREIGN KEY (search_id) REFERENCES searches (search_id);

CREATE TABLE search_concept_cluster_phrases
(
  search_concept_cluster_phrase_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  search_id BIGINT NOT NULL,
  phrase NVARCHAR(21844) NOT NULL,
  primary_phrase BIT NOT NULL,
  cluster_id INT NOT NULL
);

CREATE INDEX ix__search_concept_cluster_phrases__search_id ON search_concept_cluster_phrases (search_id);
ALTER TABLE search_concept_cluster_phrases ADD CONSTRAINT fk__search_concept_cluster_phrases__searches FOREIGN KEY (search_id) REFERENCES searches (search_id);
