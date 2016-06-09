/*-------------------------------------------
-- Description: Add parametric ranges table
  for handling numeric and date range restrictions

-- Previous Version: 11.1.0.2
-- Target Version: 11.1.0.3
--------------------------------------------*/

SET SCHEMA find;

CREATE TABLE search_parametric_ranges
(
  search_parametric_ranges_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  search_id BIGINT NOT NULL,
  field NVARCHAR(21844) NOT NULL,
  min BIGINT NOT NULL,
  max BIGINT NOT NULL,
  type INT NOT NULL
);

CREATE INDEX ix__search_parametric_ranges__search_id ON search_parametric_ranges (search_id);
ALTER TABLE search_parametric_ranges ADD CONSTRAINT fk__search_parametric_ranges__searches FOREIGN KEY (search_id) REFERENCES searches (search_id);
