-- Copyright 2021 Gunsioo Group. Multiple-Licensed under the MPL 2.0,
-- and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
-- Initial Developer: Gunsioo Group
--

CREATE TABLE TEST(D1 DATE);
> ok

SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'TEST' ORDER BY ORDINAL_POSITION;
> COLUMN_NAME DATA_TYPE
> ----------- ---------
> D1          DATE
> rows (ordered): 1

DROP TABLE TEST;
> ok

SELECT DATE '2000-01-02';
>> 2000-01-02

SELECT DATE '20000102';
>> 2000-01-02

SELECT DATE '-1000102';
>> -0100-01-02

SELECT DATE '3001231';
>> 0300-12-31

-- PostgreSQL returns 2020-12-31
SELECT DATE '201231';
> exception INVALID_DATETIME_CONSTANT_2

CALL DATE '-1000000000-01-01';
>> -1000000000-01-01

CALL DATE '1000000000-12-31';
>> 1000000000-12-31

CALL DATE '-1000000001-12-31';
> exception INVALID_DATETIME_CONSTANT_2

CALL DATE '1000000001-01-01';
> exception INVALID_DATETIME_CONSTANT_2

SELECT CAST (TIMESTAMP '1000000000-12-31 00:00:00' AS DATE);
>> 1000000000-12-31

SELECT CAST (DATE '1000000000-12-31' AS TIMESTAMP);
>> 1000000000-12-31 00:00:00

SELECT CAST (TIMESTAMP '-1000000000-01-01 00:00:00' AS DATE);
>> -1000000000-01-01

SELECT CAST (DATE '-1000000000-01-01' AS TIMESTAMP);
>> -1000000000-01-01 00:00:00

SELECT CAST (DATE '2000-01-01' AS TIME);
> exception DATA_CONVERSION_ERROR_1
