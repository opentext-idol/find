/*-------------------------------------------
-- Description: Handle date ranges differently from numeric ranges

-- Previous Version: 11.4.0.6
-- Target Version: 11.5.0.0
--------------------------------------------*/

SET SCHEMA find;


CREATE TABLE search_numeric_ranges
(
  search_numeric_ranges_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  search_id BIGINT NOT NULL,
  field NVARCHAR(21844) NOT NULL,
  min DOUBLE NOT NULL,
  max DOUBLE NOT NULL
);

CREATE INDEX ix__search_numeric_ranges__search_id ON search_numeric_ranges (search_id);
ALTER TABLE search_numeric_ranges ADD CONSTRAINT fk__search_numeric_ranges__searches FOREIGN KEY (search_id) REFERENCES searches (search_id);


CREATE TABLE search_date_ranges
(
  search_date_ranges_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  search_id BIGINT NOT NULL,
  field NVARCHAR(21844) NOT NULL,
  min DATETIME NOT NULL,
  max DATETIME NOT NULL
);

CREATE INDEX ix__search_date_ranges__search_id ON search_date_ranges (search_id);
ALTER TABLE search_date_ranges ADD CONSTRAINT fk__search_date_ranges__searches FOREIGN KEY (search_id) REFERENCES searches (search_id);


INSERT INTO search_numeric_ranges (search_id, field, min, max)
SELECT search_id, field, min, max
FROM search_parametric_ranges
WHERE type = 1;

INSERT INTO search_date_ranges (search_id, field, min, max)
SELECT search_id, field, DATEADD('SECOND', FLOOR(min), DATE '1970-01-01'), DATEADD('SECOND', CEILING(max), DATE '1970-01-01')
FROM search_parametric_ranges
WHERE type = 0 AND min >= 0 AND min <= 2147483647 AND max >= 0 AND max <= 2147483647;

DROP TABLE search_parametric_ranges;
