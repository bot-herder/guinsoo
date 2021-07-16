-- Copyright 2021 Gunsioo Group. Multiple-Licensed under the MPL 2.0,
-- and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
-- Initial Developer: Gunsioo Group
--

CREATE TABLE TEST(T1 TIME WITH TIME ZONE, T2 TIME WITH TIME ZONE);
> ok

INSERT INTO TEST(T1, T2) VALUES (TIME WITH TIME ZONE '10:00:00+01', TIME WITH TIME ZONE '11:00:00+02');
> update count: 1

SELECT T1, T2, T1 = T2 FROM TEST;
> T1          T2          T1 = T2
> ----------- ----------- -------
> 10:00:00+01 11:00:00+02 TRUE
> rows: 1

SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'TEST' ORDER BY ORDINAL_POSITION;
> COLUMN_NAME DATA_TYPE
> ----------- -------------------
> T1          TIME WITH TIME ZONE
> T2          TIME WITH TIME ZONE
> rows (ordered): 2

ALTER TABLE TEST ADD (T3 TIME(0), T4 TIME(9) WITHOUT TIME ZONE);
> ok

SELECT COLUMN_NAME, DATA_TYPE, DATETIME_PRECISION FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'TEST' ORDER BY ORDINAL_POSITION;
> COLUMN_NAME DATA_TYPE           DATETIME_PRECISION
> ----------- ------------------- ------------------
> T1          TIME WITH TIME ZONE 0
> T2          TIME WITH TIME ZONE 0
> T3          TIME                0
> T4          TIME                9
> rows (ordered): 4

ALTER TABLE TEST ADD T5 TIME(10);
> exception INVALID_VALUE_SCALE

DROP TABLE TEST;
> ok

CREATE TABLE TEST(T TIME WITH TIME ZONE, T0 TIME(0) WITH TIME ZONE, T1 TIME(1) WITH TIME ZONE,
    T2 TIME(2) WITH TIME ZONE, T3 TIME(3) WITH TIME ZONE, T4 TIME(4) WITH TIME ZONE, T5 TIME(5) WITH TIME ZONE,
    T6 TIME(6) WITH TIME ZONE, T7 TIME(7) WITH TIME ZONE, T8 TIME(8) WITH TIME ZONE, T9 TIME(9) WITH TIME ZONE);
> ok

INSERT INTO TEST VALUES ('08:00:00.123456789-01', '08:00:00.123456789Z', '08:00:00.123456789+01:02:03',
    '08:00:00.123456789-3:00', '08:00:00.123456789+4:30', '08:00:00.123456789Z', '08:00:00.123456789Z',
    '08:00:00.123456789Z', '08:00:00.123456789Z', '08:00:00.123456789Z', '08:00:00.123456789Z');
> update count: 1

SELECT * FROM TEST;
> T           T0          T1                  T2             T3                 T4               T5                T6                 T7                  T8                   T9
> ----------- ----------- ------------------- -------------- ------------------ ---------------- ----------------- ------------------ ------------------- -------------------- ---------------------
> 08:00:00-01 08:00:00+00 08:00:00.1+01:02:03 08:00:00.12-03 08:00:00.123+04:30 08:00:00.1235+00 08:00:00.12346+00 08:00:00.123457+00 08:00:00.1234568+00 08:00:00.12345679+00 08:00:00.123456789+00
> rows: 1

DELETE FROM TEST;
> update count: 1

INSERT INTO TEST(T0, T8) VALUES ('23:59:59.999999999Z', '23:59:59.999999999Z');
> update count: 1

SELECT T0 FROM TEST;
>> 23:59:59+00

SELECT T8 FROM TEST;
>> 23:59:59.99999999+00

DROP TABLE TEST;
> ok

SELECT TIME WITH TIME ZONE '11:22:33';
> exception INVALID_DATETIME_CONSTANT_2

SELECT TIME WITH TIME ZONE '11:22:33 Europe/London';
> exception INVALID_DATETIME_CONSTANT_2

SELECT CAST (TIMESTAMP WITH TIME ZONE '1000000000-12-31 11:22:33.123456789+02' AS TIME WITH TIME ZONE);
>> 11:22:33+02

SELECT CAST (TIMESTAMP WITH TIME ZONE '1000000000-12-31 11:22:33.123456789+02' AS TIME(9) WITH TIME ZONE);
>> 11:22:33.123456789+02

SELECT CAST (TIMESTAMP WITH TIME ZONE '-1000000000-12-31 11:22:33.123456789+02' AS TIME(9) WITH TIME ZONE);
>> 11:22:33.123456789+02

SELECT CAST (TIME WITH TIME ZONE '10:00:00Z' AS DATE);
> exception DATA_CONVERSION_ERROR_1

SELECT TIME WITH TIME ZONE '23:00:00+01' - TIME WITH TIME ZONE '00:00:30-01';
>> INTERVAL '20:59:30' HOUR TO SECOND

SELECT TIME WITH TIME ZONE '10:00:00-10' + INTERVAL '30' MINUTE;
>> 10:30:00-10
