-- Copyright 2021 Gunsioo Group. Multiple-Licensed under the MPL 2.0,
-- and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
-- Initial Developer: Gunsioo Group
--

CREATE TABLE TEST(B BOOLEAN) AS (VALUES TRUE, FALSE, UNKNOWN);
> ok

SELECT * FROM TEST ORDER BY B;
> B
> -----
> null
> FALSE
> TRUE
> rows (ordered): 3

DROP TABLE TEST;
> ok

CREATE TABLE TEST AS (SELECT UNKNOWN B);
> ok

SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'TEST';
>> BOOLEAN

EXPLAIN SELECT CAST(NULL AS BOOLEAN);
>> SELECT UNKNOWN

SELECT NOT TRUE A, NOT FALSE B, NOT NULL C, NOT UNKNOWN D;
> A     B    C    D
> ----- ---- ---- ----
> FALSE TRUE null null
> rows: 1

DROP TABLE TEST;
> ok

EXPLAIN VALUES (TRUE, FALSE, UNKNOWN);
>> VALUES (TRUE, FALSE, UNKNOWN)

EXPLAIN SELECT A IS TRUE OR B IS FALSE FROM (VALUES (TRUE, TRUE)) T(A, B);
>> SELECT ("A" IS TRUE) OR ("B" IS FALSE) FROM (VALUES (TRUE, TRUE)) "T"("A", "B") /* table scan */
