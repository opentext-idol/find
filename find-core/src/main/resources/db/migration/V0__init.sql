CREATE SCHEMA find;

SET SCHEMA find;

CREATE TABLE searches
(
  search_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  search_type_id INT NOT NULL,
  title NVARCHAR(1000) NOT NULL,
  query_text NVARCHAR(21844) NOT NULL,
  start_date DATETIME,
  end_date DATETIME,
  total_results INT,
  created_date DATETIME NOT NULL,
  modified_date DATETIME NOT NULL,
  active BIT NOT NULL
);

CREATE TABLE users
(
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  name NVARCHAR(21844),
  domain VARCHAR(255),
  user_store VARCHAR(255),
  uuid UUID
);

CREATE INDEX ix__searches__user_id ON searches (user_id);
ALTER TABLE searches ADD CONSTRAINT fk__searches__users FOREIGN KEY (user_id) REFERENCES users (user_id);

CREATE TABLE search_type
(
  search_type_id INT AUTO_INCREMENT PRIMARY KEY,
  description VARCHAR(100) NOT NULL
);

CREATE INDEX ix__searches__search_type_id ON searches (search_type_id);
ALTER TABLE searches ADD CONSTRAINT fk__searches__search_type FOREIGN KEY (search_type_id) REFERENCES search_type (search_type_id);

CREATE TABLE search_parametric_values
(
  search_parametric_values_id INT AUTO_INCREMENT PRIMARY KEY,
  search_id INT NOT NULL,
  parametric_value_id INT NOT NULL
);

CREATE TABLE parametric_values
(
  parametric_value_id INT AUTO_INCREMENT PRIMARY KEY,
  field NVARCHAR(21844) NOT NULL,
  value NVARCHAR(21844) NOT NULL
);

CREATE INDEX ix__search_parametric_values__search_id ON search_parametric_values (search_id);
CREATE INDEX ix__search_parametric_values__parametric_value_id ON search_parametric_values (parametric_value_id);
ALTER TABLE search_parametric_values ADD CONSTRAINT fk__search_parametric_values__searches FOREIGN KEY (search_id) REFERENCES searches (search_id);
ALTER TABLE search_parametric_values ADD CONSTRAINT fk__search_parametric_values__parametric_values FOREIGN KEY (parametric_value_id) REFERENCES parametric_values (parametric_value_id);

CREATE TABLE search_indexes
(
  search_index_id INT AUTO_INCREMENT PRIMARY KEY,
  search_id INT NOT NULL,
  index_id INT NOT NULL
);

CREATE TABLE indexes
(
  index_id INT AUTO_INCREMENT PRIMARY KEY,
  name NVARCHAR(21844) NOT NULL,
  domain VARCHAR(255)
);

CREATE INDEX ix__search_indexes__search_id ON search_indexes (search_id);
CREATE INDEX ix__search_indexes__index_id ON search_indexes (index_id);
ALTER TABLE search_indexes ADD CONSTRAINT fk__search_indexes__searches FOREIGN KEY (search_id) REFERENCES searches (search_id);
ALTER TABLE search_indexes ADD CONSTRAINT fk__search_indexes__indexes FOREIGN KEY (index_id) REFERENCES indexes (index_id);

CREATE TABLE search_stored_state
(
  search_stored_state_id INT AUTO_INCREMENT PRIMARY KEY,
  search_id INT NOT NULL,
  state_token VARCHAR(100) NOT NULL
);

CREATE INDEX ix__search_stored_state__search_id ON search_stored_state (search_id);
ALTER TABLE search_stored_state ADD CONSTRAINT fk__search_stored_state__searches FOREIGN KEY (search_id) REFERENCES searches (search_id);
