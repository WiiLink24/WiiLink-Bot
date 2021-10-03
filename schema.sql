CREATE TABLE public.userinfo (
    userid character varying NOT NULL,
    strikes integer,
    afk boolean,
    afk_reason character varying
);
CREATE TABLE public.wads (
    file_id       serial,
    filename      varchar,
    readable_name varchar
);