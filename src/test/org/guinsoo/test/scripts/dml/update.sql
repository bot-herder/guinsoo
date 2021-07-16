-- Copyright 2021 Gunsioo Group. Multiple-Licensed under the MPL 2.0,
-- and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
-- Initial Developer: Gunsioo Group
--

CREATE TABLE TEST(A INT, B INT);
> ok

INSERT INTO TEST VALUES (1, 2);
> update count: 1

UPDATE TEST SET (A, B) = (3, 4);
> update count: 1

SELECT * FROM TEST;
> A B
> - -
> 3 4
> rows: 1

UPDATE TEST SET (B) = 5;
> update count: 1

SELECT B FROM TEST;
>> 5

UPDATE TEST SET (B) = ROW (6);
> update count: 1

SELECT B FROM TEST;
>> 6

UPDATE TEST SET (B) = (7);
> update count: 1

SELECT B FROM TEST;
>> 7

UPDATE TEST SET (B) = (2, 3);
> exception COLUMN_COUNT_DOES_NOT_MATCH

-- TODO
-- UPDATE TEST SET (A, B) = ARRAY[3, 4];
-- > exception COLUMN_COUNT_DOES_NOT_MATCH

EXPLAIN UPDATE TEST SET (A) = ROW(3), B = 4;
>> UPDATE "PUBLIC"."TEST" /* PUBLIC.TEST.tableScan */ SET "A" = 3, "B" = 4

EXPLAIN UPDATE TEST SET A = 3, (B) = 4;
>> UPDATE "PUBLIC"."TEST" /* PUBLIC.TEST.tableScan */ SET "A" = 3, "B" = 4

UPDATE TEST SET (A, B) = (1, 2), (B, A) = (2, 1);
> exception DUPLICATE_COLUMN_NAME_1

UPDATE TEST SET (A) = A * 3;
> update count: 1

DROP TABLE TEST;
> ok

CREATE TABLE TEST(ID INT) AS VALUES 100;
> ok

-- _ROWID_ modifications are not allowed
UPDATE TEST SET _ROWID_ = 2 WHERE ID = 100;
> exception SYNTAX_ERROR_2

DROP TABLE TEST;
> ok

CREATE TABLE TEST(A INT, B INT GENERATED ALWAYS AS (A + 1));
> ok

INSERT INTO TEST(A) VALUES 1;
> update count: 1

UPDATE TEST SET A = 2, B = DEFAULT;
> update count: 1

TABLE TEST;
> A B
> - -
> 2 3
> rows: 1

DROP TABLE TEST;
> ok

CREATE TABLE TEST(A INT, B INT GENERATED ALWAYS AS (A + 1));
> ok

INSERT INTO TEST(A) VALUES 1;
> update count: 1

UPDATE TEST SET B = 1;
> exception GENERATED_COLUMN_CANNOT_BE_ASSIGNED_1

UPDATE TEST SET B = DEFAULT;
> update count: 1

DROP TABLE TEST;
> ok

CREATE TABLE TEST(ID INT PRIMARY KEY, A INT, B INT, C INT, D INT, E INT, F INT) AS VALUES (1, 1, 1, 1, 1, 1, 1);
> ok

EXPLAIN UPDATE TEST SET
    (F, C, A) = (SELECT 2, 3, 4 FROM TEST FETCH FIRST ROW ONLY),
    (B, E) = (SELECT 5, 6 FROM TEST FETCH FIRST ROW ONLY)
    WHERE ID = 1;
#+mvStore#>> UPDATE "PUBLIC"."TEST" /* PUBLIC.PRIMARY_KEY_2: ID = 1 */ SET ("F", "C", "A") = (SELECT 2, 3, 4 FROM "PUBLIC"."TEST" /* PUBLIC.TEST.tableScan */ FETCH FIRST ROW ONLY), ("B", "E") = (SELECT 5, 6 FROM "PUBLIC"."TEST" /* PUBLIC.TEST.tableScan */ FETCH FIRST ROW ONLY) WHERE "ID" = 1
#-mvStore#>> UPDATE "PUBLIC"."TEST" /* PUBLIC.PRIMARY_KEY_2: ID = 1 */ SET ("F", "C", "A") = (SELECT 2, 3, 4 FROM "PUBLIC"."TEST" /* PUBLIC.PRIMARY_KEY_2 */ FETCH FIRST ROW ONLY), ("B", "E") = (SELECT 5, 6 FROM "PUBLIC"."TEST" /* PUBLIC.PRIMARY_KEY_2 */ FETCH FIRST ROW ONLY) WHERE "ID" = 1

UPDATE TEST SET
    (F, C, A) = (SELECT 2, 3, 4 FROM TEST FETCH FIRST ROW ONLY),
    (B, E) = (SELECT 5, 6 FROM TEST FETCH FIRST ROW ONLY)
    WHERE ID = 1;
> update count: 1

TABLE TEST;
> ID A B C D E F
> -- - - - - - -
> 1  4 5 3 1 6 2
> rows: 1

UPDATE TEST SET (C, C) = (SELECT 1, 2 FROM TEST);
> exception DUPLICATE_COLUMN_NAME_1

UPDATE TEST SET (A, B) = (SELECT 1, 2, 3 FROM TEST);
> exception COLUMN_COUNT_DOES_NOT_MATCH

UPDATE TEST SET (D, E) = NULL;
> exception DATA_CONVERSION_ERROR_1

DROP TABLE TEST;
> ok

CREATE TABLE TEST(ID BIGINT GENERATED ALWAYS AS IDENTITY, ID2 BIGINT GENERATED ALWAYS AS (ID + 1),
    V INT, U INT ON UPDATE (5));
> ok

INSERT INTO TEST(V) VALUES 1;
> update count: 1

TABLE TEST;
> ID ID2 V U
> -- --- - ----
> 1  2   1 null
> rows: 1

UPDATE TEST SET V = V + 1;
> update count: 1

UPDATE TEST SET V = V + 1, ID = DEFAULT, ID2 = DEFAULT;
> update count: 1

TABLE TEST;
> ID ID2 V U
> -- --- - -
> 1  2   3 5
> rows: 1

MERGE INTO TEST USING (VALUES 1) T(X) ON TRUE WHEN MATCHED THEN UPDATE SET V = V + 1;
> update count: 1

MERGE INTO TEST USING (VALUES 1) T(X) ON TRUE WHEN MATCHED THEN UPDATE SET V = V + 1, ID = DEFAULT, ID2 = DEFAULT;
> update count: 1

TABLE TEST;
> ID ID2 V U
> -- --- - -
> 1  2   5 5
> rows: 1

MERGE INTO TEST KEY(V) VALUES (DEFAULT, DEFAULT, 5, 1);
> update count: 1

TABLE TEST;
> ID ID2 V U
> -- --- - -
> 1  2   5 1
> rows: 1

DROP TABLE TEST;
> ok

CREATE DOMAIN D AS BIGINT DEFAULT 100 ON UPDATE 200;
> ok

CREATE TABLE TEST(ID D GENERATED BY DEFAULT AS IDENTITY, V INT, G D GENERATED ALWAYS AS (V + 1));
> ok

INSERT INTO TEST(V) VALUES 1;
> update count: 1

TABLE TEST;
> ID V G
> -- - -
> 1  1 2
> rows: 1

UPDATE TEST SET V = 2;
> update count: 1

TABLE TEST;
> ID V G
> -- - -
> 1  2 3
> rows: 1

DROP TABLE TEST;
> ok

DROP DOMAIN D;
> ok

CREATE TABLE TEST(A INT, B INT, C INT) AS VALUES (0, 0, 1), (0, 0, 3);
> ok

CREATE TABLE S1(A INT, B INT) AS VALUES (1, 2);
> ok

CREATE TABLE S2(A INT, B INT) AS VALUES (3, 4);
> ok

UPDATE TEST SET (A, B) = (SELECT * FROM S1 WHERE C = A UNION SELECT * FROM S2 WHERE C = A);
> update count: 2

TABLE TEST;
> A B C
> - - -
> 1 2 1
> 3 4 3
> rows: 2

DROP TABLE TEST, S1, S2;
> ok

CREATE TABLE TEST(ID INT PRIMARY KEY, V INT) AS SELECT X, X FROM SYSTEM_RANGE(1, 13);
> ok

UPDATE TEST SET V = V + 1 WHERE ID <= 12 FETCH FIRST ROW ONLY;
> update count: 1

UPDATE TEST SET V = V + 1 WHERE ID <= 12 FETCH FIRST ROWS ONLY;
> update count: 1

UPDATE TEST SET V = V + 1 WHERE ID <= 12 FETCH NEXT ROW ONLY;
> update count: 1

UPDATE TEST SET V = V + 1 WHERE ID <= 12 FETCH NEXT ROWS ONLY;
> update count: 1

UPDATE TEST SET V = V + 1 WHERE ID <= 12 FETCH FIRST 2 ROW ONLY;
> update count: 2

UPDATE TEST SET V = V + 1 WHERE ID <= 12 FETCH FIRST 2 ROWS ONLY;
> update count: 2

UPDATE TEST SET V = V + 1 WHERE ID <= 12 FETCH NEXT 2 ROW ONLY;
> update count: 2

UPDATE TEST SET V = V + 1 WHERE ID <= 12 FETCH NEXT 2 ROWS ONLY;
> update count: 2

EXPLAIN UPDATE TEST SET V = V + 1 WHERE ID <= 12 FETCH FIRST 2 ROWS ONLY;
>> UPDATE "PUBLIC"."TEST" /* PUBLIC.PRIMARY_KEY_2: ID <= 12 */ SET "V" = "V" + 1 WHERE "ID" <= 12 FETCH FIRST 2 ROWS ONLY

EXPLAIN UPDATE TEST SET V = V + 1 FETCH FIRST 1 ROW ONLY;
#+mvStore#>> UPDATE "PUBLIC"."TEST" /* PUBLIC.TEST.tableScan */ SET "V" = "V" + 1 FETCH FIRST ROW ONLY
#-mvStore#>> UPDATE "PUBLIC"."TEST" /* PUBLIC.PRIMARY_KEY_2 */ SET "V" = "V" + 1 FETCH FIRST ROW ONLY

EXPLAIN UPDATE TEST SET V = V + 1;
#+mvStore#>> UPDATE "PUBLIC"."TEST" /* PUBLIC.TEST.tableScan */ SET "V" = "V" + 1
#-mvStore#>> UPDATE "PUBLIC"."TEST" /* PUBLIC.PRIMARY_KEY_2 */ SET "V" = "V" + 1

SELECT SUM(V) FROM TEST;
>> 103

DROP TABLE TEST;
> ok

CREATE TABLE FOO (ID INT, VAL VARCHAR) AS VALUES(1, 'foo1'), (2, 'foo2'), (3, 'foo3');
> ok

CREATE TABLE BAR (ID INT, VAL VARCHAR) AS VALUES(1, 'bar1'), (3, 'bar3'), (4, 'bar4');
> ok

SET MODE PostgreSQL;
> ok

UPDATE FOO SET VAL = BAR.VAL FROM BAR WHERE FOO.ID = BAR.ID;
> update count: 2

TABLE FOO;
> ID VAL
> -- ----
> 1  bar1
> 2  foo2
> 3  bar3
> rows: 3

UPDATE FOO SET BAR.VAL = FOO.VAL FROM BAR WHERE FOO.ID = BAR.ID;
> exception TABLE_OR_VIEW_NOT_FOUND_1

SET MODE Regular;
> ok
