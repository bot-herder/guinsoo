-- Copyright 2021 Gunsioo Group. Multiple-Licensed under the MPL 2.0,
-- and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
-- Initial Developer: Gunsioo Group
--

CREATE TABLE TEST(U UUID) AS (SELECT * FROM VALUES
    ('00000000-0000-0000-0000-000000000000'), ('00000000-0000-0000-9000-000000000000'),
    ('11111111-1111-1111-1111-111111111111'), ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'));
> ok

SELECT U FROM TEST ORDER BY U;
> U
> ------------------------------------
> 00000000-0000-0000-0000-000000000000
> 00000000-0000-0000-9000-000000000000
> 11111111-1111-1111-1111-111111111111
> aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa
> rows (ordered): 4

DROP TABLE TEST;
> ok

EXPLAIN VALUES UUID '11111111-1111-1111-1111-111111111111';
>> VALUES (UUID '11111111-1111-1111-1111-111111111111')

VALUES CAST('01234567-89AB-CDEF-0123-456789ABCDE' AS UUID);
> exception DATA_CONVERSION_ERROR_1

VALUES CAST(X'0123456789ABCDEF0123456789ABCD' AS UUID);
> exception DATA_CONVERSION_ERROR_1

VALUES CAST('01234567-89AB-CDEF-0123-456789ABCDEF' AS UUID);
>> 01234567-89ab-cdef-0123-456789abcdef

VALUES CAST(X'0123456789ABCDEF0123456789ABCDEF' AS UUID);
>> 01234567-89ab-cdef-0123-456789abcdef

VALUES CAST('01234567-89AB-CDEF-0123-456789ABCDEF-0' AS UUID);
> exception DATA_CONVERSION_ERROR_1

VALUES CAST(X'0123456789ABCDEF0123456789ABCDEF01' AS UUID);
> exception DATA_CONVERSION_ERROR_1
