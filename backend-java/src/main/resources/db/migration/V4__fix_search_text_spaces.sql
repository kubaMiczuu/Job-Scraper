UPDATE jobs
SET search_text = CONCAT_WS(' ',
    LOWER(title),
    LOWER(company),
    LOWER(ARRAY_TO_STRING(tech_keywords, ' ')));