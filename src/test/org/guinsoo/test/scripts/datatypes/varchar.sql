-- Copyright 2021 Gunsioo Group. Multiple-Licensed under the MPL 2.0,
-- and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
-- Initial Developer: Gunsioo Group
--

SELECT 'A' 'b'
    'c';
>> Abc

SELECT N'A' 'b'
    'c';
>> Abc

CREATE TABLE TEST(C1 VARCHAR, C2 CHARACTER VARYING, C3 VARCHAR2, C4 NVARCHAR, C5 NVARCHAR2, C6 VARCHAR_CASESENSITIVE,
    C7 LONGVARCHAR, C8 TID, C9 CHAR VARYING, C10 NCHAR VARYING, C11 NATIONAL CHARACTER VARYING, C12 NATIONAL CHAR VARYING);
> ok

SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'TEST' ORDER BY ORDINAL_POSITION;
> COLUMN_NAME DATA_TYPE
> ----------- -----------------
> C1          CHARACTER VARYING
> C2          CHARACTER VARYING
> C3          CHARACTER VARYING
> C4          CHARACTER VARYING
> C5          CHARACTER VARYING
> C6          CHARACTER VARYING
> C7          CHARACTER VARYING
> C8          CHARACTER VARYING
> C9          CHARACTER VARYING
> C10         CHARACTER VARYING
> C11         CHARACTER VARYING
> C12         CHARACTER VARYING
> rows (ordered): 12

DROP TABLE TEST;
> ok

CREATE TABLE T(C VARCHAR(0));
> exception INVALID_VALUE_2

CREATE TABLE T(C VARCHAR(1K));
> exception SYNTAX_ERROR_2

CREATE TABLE T(C1 VARCHAR(1 CHARACTERS), C2 VARCHAR(1 OCTETS));
> ok

DROP TABLE T;
> ok


CREATE TABLE T1(A CHARACTER VARYING(1048576));
> ok

CREATE TABLE T2(A CHARACTER VARYING(1048577));
> exception INVALID_VALUE_PRECISION

SET TRUNCATE_LARGE_LENGTH TRUE;
> ok

CREATE TABLE T2(A CHARACTER VARYING(1048577));
> ok

SELECT TABLE_NAME, CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'PUBLIC';
> TABLE_NAME CHARACTER_MAXIMUM_LENGTH
> ---------- ------------------------
> T1         1048576
> T2         1048576
> rows: 2

SET TRUNCATE_LARGE_LENGTH FALSE;
> ok

DROP TABLE T1, T2;
> ok

SELECT U&'a\0030a\+000025a';
>> a0a%a

SELECT U&'az0030az+000025a' UESCAPE 'z';
>> a0a%a

EXPLAIN SELECT U&'\fffd\+100000';
>> SELECT U&'\fffd\+100000'

SELECT U&'\';
> exception STRING_FORMAT_ERROR_1

SELECT U&'\0';
> exception STRING_FORMAT_ERROR_1

SELECT U&'\00';
> exception STRING_FORMAT_ERROR_1

SELECT U&'\003';
> exception STRING_FORMAT_ERROR_1

SELECT U&'\0030';
>> 0

SELECT U&'\zzzz';
> exception STRING_FORMAT_ERROR_1

SELECT U&'\+0';
> exception STRING_FORMAT_ERROR_1

SELECT U&'\+00';
> exception STRING_FORMAT_ERROR_1

SELECT U&'\+000';
> exception STRING_FORMAT_ERROR_1

SELECT U&'\+0000';
> exception STRING_FORMAT_ERROR_1

SELECT U&'\+00003';
> exception STRING_FORMAT_ERROR_1

SELECT U&'\+000030';
>> 0

SELECT U&'\+zzzzzz';
> exception STRING_FORMAT_ERROR_1

EXPLAIN SELECT U&'''\\', U&'''\\\fffd';
>> SELECT '''\', U&'''\\\fffd'
