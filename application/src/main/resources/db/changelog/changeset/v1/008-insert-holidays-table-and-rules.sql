CREATE TABLE holidays
(
    id           BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    date         DATE         NOT NULL, -- Specific date of the holiday
    country_code CHAR(2),               -- ISO country code (optional)
    name         VARCHAR(100) NOT NULL, -- Name of the holiday
    is_recurring BOOLEAN DEFAULT true,  -- Repeats annually?
    UNIQUE (date, country_code)
);
CREATE TABLE holiday_rules
(
    id           BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    country_code CHAR(2),
    name         VARCHAR(100) NOT NULL,
    rule_type    VARCHAR(20)  NOT NULL, -- 'easter', 'nth_weekday', etc.
    month        INT,                   -- For rules of type "n-th Monday of the month"
    week_ordinal INT,                   -- 1 = first, 4 = last
    weekday      INT                    -- 1-7 (Monday-Sunday)
);
-- Fixed annual holidays (added to the holidays table)
INSERT INTO holidays (date, country_code, name, is_recurring)
VALUES ('2024-01-01', 'US', 'New Year''s Day', true),
       ('2024-07-04', 'US', 'Independence Day', true),
       ('2024-12-25', 'US', 'Christmas Day', true);

-- Dynamic holidays (rules for holiday_rules table)
INSERT INTO holiday_rules
    (country_code, name, rule_type, month, week_ordinal, weekday)
VALUES
    -- Easter
    ('US', 'Easter', 'easter', null, null, null),
    -- Martin Luther King Jr. Day: 3rd Monday in January
    ('US', 'Martin Luther King Jr. Day', 'nth_weekday', 1, 3, 1),

    -- Presidents Day: 3rd Monday in February
    ('US', 'Presidents Day', 'nth_weekday', 2, 3, 1),

    -- Memorial Day: Last Monday of May
    ('US', 'Memorial Day', 'nth_weekday', 5, -1, 1),

    -- Labor Day: 1st Monday of September
    ('US', 'Labor Day', 'nth_weekday', 9, 1, 1),

    -- Columbus Day: 2nd Monday in October
    ('US', 'Columbus Day', 'nth_weekday', 10, 2, 1),

    -- Thanksgiving: 4th Thursday of November
    ('US', 'Thanksgiving', 'nth_weekday', 11, 4, 4),

    -- Black Friday: The day after Thanksgiving (Friday)
    ('US', 'Black Friday', 'nth_weekday', 11, 4, 5);

CREATE OR REPLACE FUNCTION easter_date(year INTEGER)
RETURNS DATE AS $$
DECLARE
a INTEGER;
    b INTEGER;
    c INTEGER;
    d INTEGER;
    e INTEGER;
    f INTEGER;
    g INTEGER;
    h INTEGER;
    i INTEGER;
    k INTEGER;
    l INTEGER;
    m INTEGER;
month INTEGER;
day INTEGER;
BEGIN
    a := year % 19;
    b := year / 100;
    c := year % 100;
    d := b / 4;
    e := b % 4;
    f := (b + 8) / 25;
    g := (b - f + 1) / 3;
    h := (19 * a + b - d - g + 15) % 30;
    i := c / 4;
    k := c % 4;
    l := (32 + 2 * e + 2 * i - h - k) % 7;
    m := (a + 11 * h + 22 * l) / 451;

    -- Calculation of month and day
month := (h + l - 7 * m + 114) / 31;
day := ((h + l - 7 * m + 114) % 31) + 1;

    -- Formation of date
RETURN MAKE_DATE(year, month, day);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE
OR REPLACE FUNCTION get_holidays(year INT, country CHAR(2))
RETURNS TABLE (date DATE, name VARCHAR) AS $$
BEGIN
RETURN QUERY
-- Fixed holidays
SELECT (h.date + (year - EXTRACT (YEAR FROM h.date)) * INTERVAL '1 year')::DATE,
        h.name
    FROM holidays h
    WHERE h.is_recurring
      AND (h.country_code = country
       OR h.country_code IS NULL)

UNION ALL

-- Dynamic holidays (eg Easter)
SELECT CASE
       WHEN hr.rule_type = 'easter' THEN easter_date(year)
       WHEN hr.rule_type = 'nth_weekday' THEN
           CASE
               WHEN hr.week_ordinal > 0 THEN
                   ( -- First...fourth weekday
                       SELECT date_trunc('month', MAKE_DATE(year, hr.month, 1)) ::DATE
                          + (hr.week_ordinal - 1) *
                        INTERVAL '1 week' + (hr.weekday - EXTRACT(ISODOW FROM MAKE_DATE(year
                     , hr.month
                     , 1))) % 7 * INTERVAL '1 day'
                    )
                    ELSE
                     ( -- Last weekday
                        SELECT (MAKE_DATE(year, hr.month + 1, 1) - INTERVAL '1 day'):: DATE
                            - ( (EXTRACT (ISODOW FROM MAKE_DATE(year, hr.month + 1, 1) - INTERVAL '1 day')
                            - hr.weekday + 7) % 7 ) * INTERVAL '1 day'
                    )
                    END
                END::DATE, hr.name
  FROM holiday_rules hr
    WHERE hr.country_code = country;
END;
$$
LANGUAGE plpgsql;